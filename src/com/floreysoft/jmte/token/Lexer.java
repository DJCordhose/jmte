package com.floreysoft.jmte.token;

import static com.floreysoft.jmte.util.NestedParser.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.floreysoft.jmte.util.Util;

public class Lexer {
	private static final Pattern EQ_PATTERN = Pattern.compile("=(?![^(]*\\))");

	public AbstractToken nextToken(final char[] template, final int start,
			final int end) {
		String input = new String(template, start, end - start);
		if (input.startsWith("--")) {
			// comment
			return null;
		}
		AbstractToken token = innerNextToken(input);
		token.setText(template, start, end);
		token.setLine(template, start, end);
		token.setColumn(template, start, end);
		return token;
	}
	
	private String unescapeAccess(List<? extends Object> arr,int index){
		String val = access(arr,index);
		if (val!=null && val.trim().length()>0){
			val = Util.NO_QUOTE_MINI_PARSER.unescape(val);
		}
		return val;
	}

	private AbstractToken innerNextToken(final String untrimmedInput) {
		final String input = Util.trimFront(untrimmedInput);
		// annotation
		if (input.length() > 0 && input.charAt(0) == '@') {
			final List<String> split = Util.RAW_MINI_PARSER.splitOnWhitespace(
					input.substring(1), 2);
			String receiver = access(split, 0);
			String arguments = access(split, 1);
			AnnotationToken annotationToken = new AnnotationToken(receiver,
					arguments);
			return annotationToken;
		}

		final List<String> split = Util.RAW_MINI_PARSER
				.splitOnWhitespace(input);

		// LENGTH 0
		if (split.size() == 0) {
			// empty expression like ${}
			return new StringToken();
		}

		if (split.size() >= 2) {
			// LENGTH 2..n

			final String cmd = split.get(0);
			final String objectExpression = split.get(1);

			if (cmd.equalsIgnoreCase(IfToken.IF)) {
				final boolean negated;
				final String ifExpression;
				// separators
				if (objectExpression.startsWith("!")) {
					negated = true;
					ifExpression = objectExpression.substring(1);
				} else {
					negated = false;
					ifExpression = objectExpression;
				}
				if (!input.contains("=") && !input.contains(";")) {
					return new IfToken(ifExpression, negated);
				} else {
										final AbstractToken innerToken;
                    // HACK: if the value we compare to contains a space, it is cut off
                    // add the part that is cut off here
                    final String completeIfExpression =
                            ifExpression + input.substring(input.indexOf(ifExpression) + ifExpression.length());
                    final int posFirstSemi = completeIfExpression.indexOf(';');

                    // check if there is a = which is not between two brackets
										final boolean hasCmp;
										final int posEq;

										Matcher matcher = EQ_PATTERN.matcher(completeIfExpression);

										if(matcher.find()){
											hasCmp = true;
											posEq = matcher.start(0);
										} else {
											hasCmp = false;
											posEq = -1;
										}
                    final String complexVariable;

                    if (hasCmp) {
                        String operand = completeIfExpression.substring(posEq + 1);
                        // heuristic: when there is leading or trailing space and after that a quote begins,
						// it must be ignorable white space
						if (isQuoted(operand.trim())) {
							operand = operand.trim();
						}
                        // remove optional quotations
                        if (isQuoted(operand)) {
                        	innerToken = new PlainTextToken(operand.substring(1, operand.length() - 1));
                        } else {
													// no string operand since there are no quotes -> resolve this to be a resolved expression
													innerToken = innerNextToken(operand);
												}
                        complexVariable = completeIfExpression.substring(0, posEq).trim();
                    } else {
                        complexVariable = completeIfExpression;
												innerToken = null;
                    }
                    // if there is a semicolon before an eq, this must be a renderer applied to the variable
                    // like:
                    // name;string(fromAfterFirst= ;toBeforeLast=$mychar)
                    //
                    // or there is no eq, but a semicolon
                    // like
                    // var;gtFive()
                    // or
                    // var;gtFive
                    if ((posFirstSemi != -1 && posEq != -1 && posFirstSemi < posEq) ||
                        (posFirstSemi != -1 && posEq == -1)) {
                        // name
                        final String variable = complexVariable.substring(0, posFirstSemi);
                        // string(fromAfterFirst= ;toBeforeLast=$mychar)
                        final String renderer = completeIfExpression.substring(posFirstSemi + 1);
                        final List<String> scannedFormat = Util.MINI_PARSER.greedyScan(renderer ,
                                "(", ")");
                        // string
                        String rendererName = access(scannedFormat, 0);
                        // fromAfterFirst= ;toBeforeLast=$mychar
                        String parameters = access(scannedFormat, 1);
                        return new IfCmpRendererToken(variable, innerToken, negated, rendererName, parameters);
                    } else {
                        return new IfCmpToken(complexVariable, innerToken, negated);
                    }
				}
			}
			if (cmd.equalsIgnoreCase(ForEachToken.FOREACH)) {
                final String varName;
                if (split.size() < 3) {
                    varName = ForEachToken.UNDEFINED_VARNAME;
                } else {
                    varName = split.get(2);
                }
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
				if (separator !=null){
					separator = Util.NO_QUOTE_MINI_PARSER.unescape(separator);
				}
                final ForEachToken forEachToken = new ForEachToken(objectExpression, varName, separator
                        .length() != 0 ? separator : null);
                return forEachToken;
			}
		}

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

		// be sure to use the raw input as we might have to preserve
		// whitespace for prefix and postfix
		// only innermost parsers are allowed to unescape
		final List<String> strings = Util.RAW_OUTPUT_MINI_PARSER.split(
				untrimmedInput, ';', 2);
		// <h1>,address(NIX),</h1>
		final String complexVariable = strings.get(0);
		// only innermost parsers are allowed to unescape
		final List<String> wrappedStrings = Util.RAW_OUTPUT_MINI_PARSER.split(
				complexVariable, ',', 3);
		// <h1>
		prefix = wrappedStrings.size() == 3 ? unescapeAccess(wrappedStrings, 0) : null;
		// </h1>
		suffix = wrappedStrings.size() == 3 ? unescapeAccess(wrappedStrings, 2) : null;

		// address(NIX)
		final String completeDefaultString = (wrappedStrings.size() == 3 ? unescapeAccess(
				wrappedStrings, 1)
				: complexVariable).trim();
		final List<String> defaultStrings = Util.MINI_PARSER.greedyScan(
				completeDefaultString, "(", ")");
		// address
		variableName = unescapeAccess(defaultStrings, 0);
		// NIX
		defaultValue = unescapeAccess(defaultStrings, 1);

		// long(full)
		final String format = access(strings, 1);
		final List<String> scannedFormat = Util.MINI_PARSER.greedyScan(format,
				"(", ")");
		// long
		rendererName = access(scannedFormat, 0);
		// full
		parameters = access(scannedFormat, 1);

		// this is not a well formed variable name
		if (variableName.contains(" ")) {
			return new InvalidToken();
		}

		final StringToken stringToken = new StringToken(untrimmedInput,
				variableName, defaultValue, prefix, suffix, rendererName,
				parameters);
		return stringToken;

	}

	private static boolean isQuoted(String operand) {
		return operand.startsWith("'") || operand.startsWith("\"");
	}

}
