package com.floreysoft.jmte;

import java.util.ArrayList;
import java.util.List;

/**
 * Not thread safe.
 * 
 * @author olli
 * 
 */
final class MiniParser {

	public final static char DEFAULT_SEPARATOR_CHAR = ',';
	public final static char DEFAULT_ESCAPE_CHAR = '\\';
	public final static char DEFAULT_QUOTE_CHAR = '"';

	public static MiniParser defaultInstance() {
		return new MiniParser();
	}

	public static MiniParser ignoreCaseInstance() {
		return new MiniParser(DEFAULT_ESCAPE_CHAR, DEFAULT_QUOTE_CHAR, true);
	}

	private final char escapeChar;
	private final char quoteChar;
	private final boolean ignoreCase;

	private transient boolean escaped = false;
	private transient boolean quoted = false;

	public MiniParser() {
		this(DEFAULT_ESCAPE_CHAR, DEFAULT_QUOTE_CHAR, false);
	}

	public MiniParser(final char escapeChar, final char quoteChar,
			final boolean ignoreCase) {
		this.escapeChar = escapeChar;
		this.quoteChar = quoteChar;
		this.ignoreCase = ignoreCase;
	}

	public String replace(final String input, final String oldString,
			final String newString) {
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
					append(buffer, c);
				}
			}

			return buffer.toString();
		} finally {
			escaped = false;
			quoted = false;
		}
	}

	public List<String> split(final String input) {
		return split(input, DEFAULT_SEPARATOR_CHAR, Integer.MAX_VALUE);
	}

	public List<String> split(final String input, final char separator) {
		return split(input, separator, Integer.MAX_VALUE);
	}

	public List<String> split(final String input, final char separator,
			final int maxSegments) {
		try {
			final List<String> segments = new ArrayList<String>();
			StringBuilder buffer = new StringBuilder();

			for (int index = 0; index < input.length(); index++) {
				final char c = input.charAt(index);
				// in case we are not already in the last segment and there is
				// an
				// unsecaped, unquoted separator, this segment is now done
				if (segments.size() != maxSegments - 1 && c == separator
						&& !isEscaped()) {
					segments.add(buffer.toString());
					buffer = new StringBuilder();
				} else {
					append(buffer, c);
				}
			}
			// add trailing element to result
			segments.add(buffer.toString());
			return segments;
		} finally {
			escaped = false;
			quoted = false;
		}
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
		for (int index = 0; index < input.length(); index++) {
			if (input.regionMatches(ignoreCase, index, substring, 0, substring
					.length())
					&& !isEscaped()) {
				resultIndex = index;
				if (!last) {
					break;
				}
			}
		}
		return resultIndex;

	}

	public List<String> carveOut(final String input, final String splitStart,
			final String splitEnd) {
		return carveOut(input, splitStart, splitEnd, false);
	}

	public List<String> carveOut(final String input, final String splitStart,
			final String splitEnd, boolean greedy) {
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
						&& !isEscaped() && greedyCond) {
					segments.add(buffer.toString());
					buffer = new StringBuilder();
					started = !started;
					index += separator.length();
				} else {
					append(buffer, c);
					index++;
				}
			}
			// add trailing element to result
			if (buffer.length() != 0) {
				segments.add(buffer.toString());
			}
			return segments;
		} finally {
			escaped = false;
			quoted = false;
		}
	}

	// the heart of it all
	private void append(StringBuilder buffer, char c) {
		if (c == escapeChar) {
			if (escaped) {
				buffer.append(c);
			}
			escaped = !escaped;
		} else if (c == quoteChar) {
			quoted = !quoted;
		} else {
			buffer.append(c);
			escaped = false;
		}
	}

	private boolean isEscaped() {
		return escaped || quoted;
	}
}
