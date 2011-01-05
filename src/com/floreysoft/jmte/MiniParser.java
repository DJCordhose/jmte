package com.floreysoft.jmte;

import java.util.ArrayList;
import java.util.List;

final class MiniParser {

	public final static char DEFAULT_SEPARATOR_CHAR = ',';
	public final static char DEFAULT_ESCAPE_CHAR = '\\';
	public final static char DEFAULT_QUOTE_CHAR = '"';

	private final char escapeChar;
	private final char quoteChar;
	private transient boolean escaped = false;
	private transient boolean quoted = false;

	public MiniParser() {
		this(DEFAULT_ESCAPE_CHAR, DEFAULT_QUOTE_CHAR);
	}

	public MiniParser(final char escapeChar, final char quoteChar) {
		this.escapeChar = escapeChar;
		this.quoteChar = quoteChar;
	}

	public List<String> split(final String input) {
		return split(input, DEFAULT_SEPARATOR_CHAR, Integer.MAX_VALUE);
	}

	public List<String> split(final String input, final char separator) {
		return split(input, separator, Integer.MAX_VALUE);
	}

	public synchronized List<String> split(final String input, final char separator,
			final int maxSegments) {
		try {
			List<String> segments = new ArrayList<String>();
			StringBuilder buffer = new StringBuilder();

			for (int i = 0; i < input.length(); i++) {
				char c = input.charAt(i);
				// in case we are not already in the last segment and there is
				// an
				// unsecaped, unquoted separator, this segment is now done
				if (segments.size() != maxSegments - 1 && c == separator
						&& !escaped && !quoted) {
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

	private void append(StringBuilder buffer, char c) {
		if (quoted) {
			if (c == quoteChar) {
				quoted = false;
			} else {
				buffer.append(c);
			}
		} else if (c == escapeChar) {
			if (escaped) {
				buffer.append(c);
			}
			escaped = !escaped;
		} else {
			buffer.append(c);
			escaped = false;
		}
	}
}
