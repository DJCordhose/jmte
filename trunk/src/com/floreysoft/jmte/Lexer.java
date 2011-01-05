package com.floreysoft.jmte;

import java.util.List;

public class Lexer {

	public Token nextToken(final String sourceName, final char[] template,
			final int start, final int end) {
		String input = new String(template, start, end - start);

		AbstractToken token = innerNextToken(input);
		token.setSourceName(sourceName);
		token.setText(template, start, end);
		token.setLine(template, start, end);
		token.setColumn(template, start, end);
		return token;
	}

	private AbstractToken innerNextToken(final String untrimmedInput) {
		final String input = Util.trimFront(untrimmedInput);
		final String[] split = input.split("( |\t|\r|\n)+");

		// LENGTH 0

		if (split.length == 0) {
			// empty expression like ${}
			return new StringToken("", null);
		}

		// be sure to use the raw input as we might have to preserve
		// whitespace for prefix and postfix
		final List<String> strings = Util.MINI_PARSER.split(untrimmedInput,
				';', 2);
		// LENGTH 1 OR special formatting input is present

		if (split.length == 1 || strings.size() == 2) {
			final String objectExpression = split[0];
			// ${
			// } which might be used for silent line breaks
			if (objectExpression.equals("")) {
				return new StringToken("", null);
			}
			final String cmd = objectExpression;
			if (cmd.equalsIgnoreCase(ElseToken.ELSE)) {
				return new ElseToken();
			}
			if (cmd.equalsIgnoreCase(EndToken.END)) {
				return new EndToken();
			}

			final String complexVariable = strings.get(0);
			final String format = strings.size() == 2 ? strings.get(1) : null;

			final List<String> wrappedStrings = Util.MINI_PARSER.split(
					complexVariable, ',', 3);
			final String completeDefaultString = (wrappedStrings.size() == 3 ? wrappedStrings
					.get(1)
					: complexVariable).trim();
			final List<String> defaultStrings = Util.carveOut(
					completeDefaultString, "(", ")", '\\');

			final String variable = defaultStrings.get(0);
			final StringToken stringToken = new StringToken(variable, format);
			final DefaultStringToken defaultStringToken = defaultStrings.size() == 2 ? new DefaultStringToken(
					stringToken, defaultStrings.get(1))
					: null;
			final WrappedDefaultStringToken wrappedDefaultStringToken = wrappedStrings
					.size() == 3 ? new WrappedDefaultStringToken(wrappedStrings
					.get(0), wrappedStrings.get(2),
					defaultStringToken != null ? defaultStringToken
							: stringToken) : null;
			if (wrappedDefaultStringToken != null) {
				return wrappedDefaultStringToken;
			} else if (defaultStringToken != null) {
				return defaultStringToken;
			} else {
				return stringToken;
			}
		}

		// LENGTH 2..n

		final String cmd = split[0];
		final String objectExpression = split[1];

		if (cmd.equalsIgnoreCase(IfToken.IF)) {
			final boolean negated;
			final String ifExpression;
			// TODO: Both '!' and '=' work only if there are no white space
			// separators
			if (objectExpression.startsWith("!")) {
				negated = true;
				ifExpression = objectExpression.substring(1);
			} else {
				negated = false;
				ifExpression = objectExpression;
			}
			if (!ifExpression.contains("=")) {
				return new IfToken(ifExpression, negated);
			} else {
				final String[] ifSplit = ifExpression.split("=");
				final String variable = ifSplit[0];
				String operand = ifSplit[1];
				// remove optional quotations
				if (operand.startsWith("'") || operand.startsWith("\"")) {
					operand = operand.substring(1, operand.length() - 1);
				}
				return new IfCmpToken(variable, operand, negated);
			}
		}
		if (cmd.equalsIgnoreCase(ForEachToken.FOREACH)) {
			final String varName = split[2];
			// we might also have
			// separator
			// data
			// but as the separator itself can contain
			// spaces
			// and the number of spaces between the previous
			// parts is unknown, we need to do this smarter
			int gapCount = 0;
			int separatorBegin = 0;
			while (separatorBegin < input.length()) {
				char c = input.charAt(separatorBegin);
				separatorBegin++;
				if (Character.isWhitespace(c)) {
					gapCount++;
					if (gapCount == 3) {
						break;
					} else {
						while (Character.isWhitespace(c = input
								.charAt(separatorBegin)))
							separatorBegin++;
					}
				}
			}

			String separator = input.substring(separatorBegin);
			return new ForEachToken(objectExpression, varName, separator
					.length() != 0 ? separator : null);
		}

		// if all this fails
		return new InvalidToken();
	}
}
