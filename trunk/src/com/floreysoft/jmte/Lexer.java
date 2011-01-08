package com.floreysoft.jmte;

import static com.floreysoft.jmte.NestedParser.*;
import java.util.List;

public class Lexer {

	public AbstractToken nextToken(final String sourceName,
			final char[] template, final int start, final int end) {
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
		final List<String> split = Util.RAW_MINI_PARSER
				.splitOnWhitespace(input);

		// LENGTH 0

		if (split.size() == 0) {
			// empty expression like ${}
			return new StringToken();
		}

		// be sure to use the raw input as we might have to preserve
		// whitespace for prefix and postfix
		final List<String> strings = Util.MINI_PARSER.split(untrimmedInput,
				';', 2);
		// LENGTH 1 OR special formatting input is present

		if (split.size() == 1 || strings.size() == 2) {
			final String objectExpression = split.get(0);
			// ${
			// } which might be used for silent line breaks
			if (objectExpression.equals("")) {
				return new StringToken();
			}
			final String cmd = objectExpression;
			if (cmd.equalsIgnoreCase(ElseToken.ELSE)) {
				return new ElseToken();
			}
			if (cmd.equalsIgnoreCase(EndToken.END)) {
				return new EndToken();
			}

			// ${<h1>,address(NIX),</h1>;long(full)}
			String variableName = null; // address
			String defaultValue = null; // NIX
			String prefix = null; // <h1>
			String suffix = null; // </h1>
			String rendererName = null; // long
			String parameters = null; // full

			// <h1>,address(NIX),</h1>
			final String complexVariable = strings.get(0);
			final List<String> wrappedStrings = split(complexVariable, ',', 3);
			// <h1>
			prefix = wrappedStrings.size() == 3 ? access(wrappedStrings, 0)
					: null;
			// </h1>
			suffix = wrappedStrings.size() == 3 ? access(wrappedStrings, 2)
					: null;

			// address(NIX)
			final String completeDefaultString = (wrappedStrings.size() == 3 ? access(
					wrappedStrings, 1)
					: complexVariable).trim();
			final List<String> defaultStrings = greedyScan(
					completeDefaultString, "(", ")");
			// address
			variableName = access(defaultStrings, 0);
			// NIX
			defaultValue = access(defaultStrings, 1);

			// long(full)
			final String format = access(strings, 1);
			final List<String> scannedFormat = greedyScan(format, "(", ")");
			// long
			rendererName = access(scannedFormat, 0);
			// full
			parameters = access(scannedFormat, 1);

			final StringToken stringToken = new StringToken(untrimmedInput,
					variableName, defaultValue, prefix, suffix, rendererName,
					parameters);
			return stringToken;

		}

		// LENGTH 2..n

		final String cmd = split.get(0);
		final String objectExpression = split.get(1);

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
			final String varName = split.get(2);
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
