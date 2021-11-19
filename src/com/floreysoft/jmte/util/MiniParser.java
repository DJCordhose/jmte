package com.floreysoft.jmte.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for embedded mini languages.
 *
 * <p>
 * <ul>
 * <li>Solves Demarcation: Where does an embedded language begin and where does
 * it end
 * <ul>
 * <li>Escaping
 * <li>Quotation
 * <li>Graceful reaction to and recovery from invalid input
 * </ul>
 * </li>
 * <li>Lays ground for common patterns of mini langauge processing
 * <ul>
 * <li>all kinds of nested brackets
 * <li>segmentation of data
 * <li>not loosing context
 * <li>context sensitive parsing aka lexer modes/states
 * </ul>
 * </li>
 * </ul>
 * </p>
 *
 * Thread safe.
 *
 * @author olli
 *
 */
public final class MiniParser {

	public final static char DEFAULT_ESCAPE_CHAR = '\\';
	public final static char DEFAULT_QUOTE_CHAR = '"';

	public synchronized static MiniParser defaultInstance() {
		return new MiniParser(DEFAULT_ESCAPE_CHAR, DEFAULT_QUOTE_CHAR, false,
				false, false);
	}

	public synchronized static MiniParser trimmedInstance() {
		return new MiniParser(DEFAULT_ESCAPE_CHAR, DEFAULT_QUOTE_CHAR, false,
				true, false);
	}

	public synchronized static MiniParser ignoreCaseInstance() {
		return new MiniParser(DEFAULT_ESCAPE_CHAR, DEFAULT_QUOTE_CHAR, true,
				false, false);
	}

	public synchronized static MiniParser fullRawInstance() {
		return new MiniParser((char) -1, (char) -1, false, false, true);
	}

	public synchronized static MiniParser rawOutputInstance() {
		return new MiniParser(DEFAULT_ESCAPE_CHAR, DEFAULT_QUOTE_CHAR, false,
				false, true);
	}

	private static class ParsingContext {
		boolean escaped = false;
		boolean quoted = false;

		boolean isEscaped() {
			return escaped || quoted;
		}
	}

	private final char escapeChar;
	private final char quoteChar;
	private final boolean ignoreCase;
	private final boolean trim;
	private final boolean rawOutput;

	public MiniParser(final char escapeChar, final char quoteChar,
			final boolean ignoreCase, final boolean trim,
			final boolean rawOutput) {
		this.escapeChar = escapeChar;
		this.quoteChar = quoteChar;
		this.ignoreCase = ignoreCase;
		this.trim = trim;
		this.rawOutput = rawOutput;
	}

	public String replace(final String input, final String oldString,
			final String newString) {
		ParsingContext context = new ParsingContext();

		try {
			if (oldString == null || oldString.equals("")) {
				return input;
			}
			StringBuilder buffer = new StringBuilder();
			for (int index = 0; index < input.length(); index++) {
				if (input.regionMatches(ignoreCase, index, oldString, 0,
						oldString.length())) {
					buffer.append(newString);
					index += oldString.length() - 1;
				} else {
					char c = input.charAt(index);
					append(buffer, c, context);
				}
			}

			return buffer.toString();
		} finally {
			context.escaped = false;
			context.quoted = false;
		}
	}

	public List<String> split(final String input, final char separator) {
		return split(input, separator, Integer.MAX_VALUE);
	}

	public List<String> split(final String input, final char separator,
			final int maxSegments) {
		return splitInternal(input, false, separator, null, maxSegments);
	}

	public List<String> split(final String input, final String separatorSet) {
		return split(input, separatorSet, Integer.MAX_VALUE);
	}

	public List<String> split(final String input, final String separatorSet,
			final int maxSegments) {
		return splitInternal(input, false, (char) -1, separatorSet, maxSegments);
	}

	public List<String> splitOnWhitespace(final String input,
			final int maxSegments) {
		return splitInternal(input, true, (char) -1, null, maxSegments);
	}

	public List<String> splitOnWhitespace(final String input) {
		return splitOnWhitespace(input, Integer.MAX_VALUE);
	}

	// Common implementation for single char separator and string set separator.
	// Has the benefit of shared code and caliper mini benchmarks showed no
	// measurable performance penalty for additional check which separator to
	// use
	private List<String> splitInternal(final String input,
			final boolean splitOnWhitespace, final char separator,
			final String separatorSet, final int maxSegments) {
		if (input == null) {
			return null;
		}
		ParsingContext context = new ParsingContext();
		try {
			final List<String> segments = new ArrayList<String>();
			StringBuilder buffer = new StringBuilder();

			for (int index = 0; index < input.length(); index++) {
				final char c = input.charAt(index);
				boolean separatedByWhitespace = false;
				if (splitOnWhitespace) {
					for (; index < input.length()
							&& Character.isWhitespace(input.charAt(index)); index++) {
						separatedByWhitespace = true;
					}
					if (separatedByWhitespace) {
						index--;
					}
				}

				final boolean separates = separatedByWhitespace
						|| (separatorSet != null ? separatorSet.indexOf(c) != -1
								: c == separator);
				// in case we are not already in the last segment and there is
				// an
				// unsecaped, unquoted separator, this segment is now done
				if (segments.size() != maxSegments - 1 && separates
						&& !context.isEscaped()) {
					finish(segments, buffer);
					buffer = new StringBuilder();
				} else {
					append(buffer, c, context);
				}
			}
			if (!splitOnWhitespace || buffer.length() != 0) {
				finish(segments, buffer);
			}
			return segments;
		} finally {
			context.escaped = false;
			context.quoted = false;
		}
	}

	private void finish(final List<String> segments, StringBuilder buffer) {
		String string = buffer.toString();
		segments.add(trim ? string.trim() : string);
	}

	public int lastIndexOf(final String input, final String substring) {
		return indexOfInternal(input, substring, true);
	}

	public int indexOf(final String input, final String substring) {
		return indexOfInternal(input, substring, false);
	}

	private int indexOfInternal(final String input, final String substring,
			boolean last) {
		int resultIndex = -1;
		ParsingContext context = new ParsingContext();
		for (int index = 0; index < input.length(); index++) {
			if (input.regionMatches(ignoreCase, index, substring, 0, substring
					.length())
					&& !context.isEscaped()) {
				resultIndex = index;
				if (!last) {
					break;
				}
			}
		}
		return resultIndex;

	}

	public List<String> scan(final String input, final String splitStart,
			final String splitEnd) {
		return scan(input, splitStart, splitEnd, false);
	}

	public List<String> greedyScan(final String input, final String splitStart,
			final String splitEnd) {
		return scan(input, splitStart, splitEnd, true);
	}

	public List<String> scan(final String input, final String splitStart,
			final String splitEnd, boolean greedy) {
		if (input == null) {
			return null;
		}
		ParsingContext context = new ParsingContext();

		try {
			final List<String> segments = new ArrayList<String>();
			StringBuilder buffer = new StringBuilder();
			boolean started = false;
			int lastIndexOfEnd = greedy ? lastIndexOfEnd = lastIndexOf(input,
					splitEnd) : -1;
			char c;
			int index = 0;
			while (index < input.length()) {
				c = input.charAt(index);
				final boolean greedyCond = !started || !greedy
						|| index == lastIndexOfEnd;
				final String separator = started ? splitEnd : splitStart;
				if (input.regionMatches(ignoreCase, index, separator, 0,
						separator.length())
						&& !context.isEscaped() && greedyCond) {
					finish(segments, buffer);
					buffer = new StringBuilder();
					started = !started;
					index += separator.length();
				} else {
					append(buffer, c, context);
					index++;
				}
			}
			// add trailing element to result
			if (buffer.length() != 0) {
				finish(segments, buffer);
			}
			return segments;
		} finally {
			context.escaped = false;
			context.quoted = false;
		}
	}

	public String unescape(final String input) {
		ParsingContext context = new ParsingContext();
		final StringBuilder unescaped = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			append(unescaped, c, context);
		}
		return unescaped.toString();
	}

	// the heart of it all
	private void append(StringBuilder buffer, char c, ParsingContext context) {

		// version manually simplified
		// final boolean shouldAppend = rawOutput || escaped
		// || (c != quoteChar && c != escapeChar);
		// final boolean newEscaped = c == escapeChar && !escaped;
		// final boolean newQuoted = (c == quoteChar && !escaped) ? !quoted
		// : quoted;

		// side-effect free version directly extracted from if

		// final boolean shouldAppend = (c == escapeChar && (escaped ||
		// rawOutput))
		// || (c == quoteChar && (escaped || rawOutput))
		// || !(c == quoteChar || c == escapeChar);
		// final boolean newEscaped = c == escapeChar ? !escaped
		// : (c == quoteChar ? false : false);
		// final boolean newQuoted = c == escapeChar ? quoted
		// : (c == quoteChar ? (!escaped ? !quoted : quoted) : quoted);

		// if (shouldAppend) {
		// buffer.append(c);
		// }
		//
		// escaped = newEscaped;
		// quoted = newQuoted;

		// original version
		// XXX needed to revert to this original version as micro benchmark
		// tests
		// showed a slow down of more than 100%
		if (c == escapeChar) {
			if (context.escaped || rawOutput) {
				buffer.append(c);
			}
			context.escaped = !context.escaped;
		} else if (c == quoteChar) {
			if (context.escaped) {
				buffer.append(c);
				context.escaped = false;
			} else {
				context.quoted = !context.quoted;
				if (rawOutput) {
					buffer.append(c);
				}
			}
		} else {
			buffer.append(c);
			context.escaped = false;
		}
	}
}
