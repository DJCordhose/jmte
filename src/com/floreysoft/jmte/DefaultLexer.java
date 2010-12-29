package com.floreysoft.jmte;

/**
 * <p>
 * Default implementation for lexer. You are invited to subclass it if you want
 * extended behavior of script sections. One possible way would be to first
 * check for your extended input and if it does not match simply delegate to the
 * super implementation.
 * </p>
 * <p>
 * You are also free to implement it from scratch.
 * </p>
 */
public class DefaultLexer implements Lexer {

	/**
	 * {@inheritDoc}
	 */
	@Override
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
			return new StringToken("");
		}

		// LENGTH 1

		if (split.length == 1) {
			final String objectExpression = split[0];
			// ${
			// } which might be used for silent line breaks
			if (objectExpression.equals("")) {
				return new StringToken("");
			}
			final String cmd = objectExpression;
			if (cmd.equalsIgnoreCase(ElseToken.ELSE)) {
				return new ElseToken();
			}
			if (cmd.equalsIgnoreCase(EndToken.END)) {
				return new EndToken();
			}
			// be sure to use the raw input as we might have to preserve whitespace for prefix and postfix
			AbstractToken defaultStringToken = WrappedDefaultStringToken.parse(untrimmedInput);
			if (defaultStringToken != null) {
				return defaultStringToken;
			}
			defaultStringToken = DefaultStringToken.parse(objectExpression);
			if (defaultStringToken != null) {
				return defaultStringToken;
			}
			
			// this is not a keyword, in this
			// case
			// simply proceed parsing it as a variable expression
			return new StringToken(objectExpression);
		}

		// LENGTH 2..n

		final String cmd = split[0];
		final String objectExpression = split[1];

		if (cmd.equalsIgnoreCase(IfToken.IF)) {
			final boolean negated;
			final String ifExpression;
			// TODO: Both '!' and '=' work only if there are no white space separators
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
