package com.floreysoft.jmte.guts;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.floreysoft.jmte.ErrorHandler;
import com.floreysoft.jmte.Lexer;

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

	public static enum Keyword {
		IF, END, ELSE, FOREACH
	};

	protected ErrorHandler errorHandler;

	@Override
	public Token nextToken(char[] template, int start, int end,
			Map<String, Object> model, boolean skipMode,
			ErrorHandler errorHandler) {
		String input = new String(template, start, end - start);
		this.errorHandler = errorHandler;
		input = Util.trimFront(input);
		String[] split = input.split("( |\t|\r|\n)+");

		Token token = innerNextToken(template, start, end, model, skipMode,
				errorHandler, input, split);
		token.setBuffer(template);
		token.setStart(start);
		token.setEnd(end);
		return token;
	}

	@SuppressWarnings("unchecked")
	private Token innerNextToken(char[] template, int start, int end,
			Map<String, Object> model, boolean skipMode,
			ErrorHandler errorHandler, String input, String[] split) {
		if (split.length == 0) {
			// empty expression like ${}
			return new StringToken("");
		} else if (split.length == 1) {
			String objectExpression = split[0];
			// ${
			// } which might be used for silent line breaks
			if (objectExpression.equals("")) {
				return new StringToken("");
			} else {
				try {
					Keyword cmd = Keyword.valueOf(objectExpression
							.toUpperCase());
					if (cmd == Keyword.ELSE) {
						return new ElseToken();
					} else if (cmd == Keyword.END) {
						return new EndToken();
					}
				} catch (IllegalArgumentException e) {
					// XXX will be thrown when this is not a keyword, in this
					// case
					// simply proceed parsing it as a variable expression
				}
				Object value;
				if (!skipMode) {
					// single name expression like ${name}
					value = traverse(objectExpression, model, template, start,
							end);
					if (value == null) {
						errorHandler.error(String.format(
								"Variable '%s' undefined", objectExpression),
								template, start, end);
						// gracefully ignore in production mode
						value = "";

					} else if (value instanceof Map
							|| value instanceof Iterable) {
						errorHandler
								.error(
										String
												.format(
														"Illegal expansion of map or iterable variable '%s'",
														objectExpression),
										template, start, end);
						// gracefully ignore in production mode
						value = "";
					}
				} else {
					value = "";
				}

				return new StringToken(value.toString());
			}
		} else {
			String cmdString = split[0];
			Keyword cmd = null;
			try {
				cmd = Keyword.valueOf(cmdString.toUpperCase());
			} catch (IllegalArgumentException iae) {
				errorHandler.error(String.format("Command '%s' is undefined",
						cmdString), template, start, end);
				return new StringToken("");
			}

			if (cmd == Keyword.IF) {
				boolean condition;
				boolean negate = false;
				if (!skipMode) {
					String objectExpression = split[1];
					if (objectExpression.startsWith("!")) {
						negate = true;
						objectExpression = objectExpression.substring(1);
					}
					Object value = traverse(objectExpression, model, template,
							start, end);
					if (value == null) {
						condition = false;
					} else if (value instanceof Boolean) {
						condition = (Boolean) value;
					} else if (value instanceof Map) {
						condition = !((Map) value).isEmpty();
					} else if (value instanceof Collection) {
						condition = !((Collection) value).isEmpty();
					} else if (value instanceof Iterable) {
						Iterator iterator = ((Iterable) value).iterator();
						condition = iterator.hasNext();
						// TODO need to have checks for all kinds of
						// primitive
						// arrays
						// e.g. } else if (value instanceof int[]) {
					} else if (value.getClass().isArray()) {
						Object[] array = (Object[]) value;
						condition = array.length != 0;
					} else {
						condition = true;
					}
				} else {
					condition = false;
				}
				if (negate) {
					condition = !condition;
				}
				return new IfToken(condition);
			} else if (cmd == Keyword.FOREACH) {
				if (!skipMode) {
					String objectExpression = split[1];
					Object value = traverse(objectExpression, model, template,
							start, end);
					Iterable<Object> iterable;
					if (value == null) {
						return new IfToken(false);
					} else if (value instanceof Map) {
						iterable = ((Map) value).entrySet();
					} else if (value instanceof Iterable) {
						iterable = ((Iterable) value);
					} else if (value.getClass().isArray()) {
						Object[] array = (Object[]) value;
						iterable = Arrays.asList(array);
					} else {
						errorHandler
								.error(
										String
												.format(
														"Can only iterator over map or iterable on '%s'",
														objectExpression),
										template, start, end);
						return new IfToken(false);
					}
					String varName = split[2];
					ForEachToken forEachToken = new ForEachToken(varName,
							iterable);

					// if we have more parameters, we also have
					// separator
					// data
					if (split.length > 3 || split.length == 3
							&& input.endsWith("  ")) {
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
						forEachToken.setSeparator(separator);
					}
					return forEachToken;
				}
			} else {
				return new IfToken(false);
			}
		}
		// default in case anything went wrong
		return new StringToken("");
	}

	protected Object traverse(String objectExpression,
			Map<String, Object> model, char[] template, int start, int end) {
		String[] split = objectExpression.split("\\.");

		String objectName = split[0];
		Object value = model.get(objectName);

		LinkedList<String> attributeNames = new LinkedList<String>(Arrays
				.asList(split));
		attributeNames.remove(0);
		value = traverse(value, attributeNames, template, start, end);
		return value;
	}

	protected Object traverse(Object o, LinkedList<String> attributeNames,
			char[] template, int start, int end) {
		Object result;
		if (attributeNames.isEmpty()) {
			result = o;
		} else {
			if (o == null) {
				errorHandler
						.error(
								String
										.format("You can not make property calls on null values"),
								template, start, end);
				return null;
			}
			String attributeName = attributeNames.remove(0);
			Object nextStep = nextStep(o, attributeName, template, start, end);
			result = traverse(nextStep, attributeNames, template, start, end);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	protected Object nextStep(Object o, String attributeName, char[] template,
			int start, int end) {
		Object result;
		if (o instanceof String) {
			errorHandler.error(String.format(
					"You can not make property calls on string '%s'", o
							.toString()), template, start, end);
			return o;
		} else if (o instanceof Map) {
			Map map = (Map) o;
			result = map.get(attributeName);
		} else {
			try {
				result = Util.getPropertyValue(o, attributeName);
			} catch (Exception e) {
				errorHandler.error(String.format(
						"Property '%s' on object '%s' can not be accessed: %s",
						attributeName, o.toString(), e.getMessage()), template,
						start, end);
				result = "";
			}
		}
		return result;
	}

}
