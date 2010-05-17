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
	@SuppressWarnings("unchecked")
	public Token nextToken(String input, Map<String, Object> model,
			boolean skipMode, ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
		input = Util.trimFront(input);
		String[] split = input.split("( |\t|\r|\n)+");

		if (split.length == 0) {
			// empty expression like ${}
			return new StringToken("");
		} else if (split.length == 1) {
			String objectExpression = split[0];
			// ${
			// } which might be used for silent line breaks
			if (objectExpression.equals("")) {
				return new StringToken("");
			}
			try {
				Keyword cmd = Keyword.valueOf(objectExpression.toUpperCase());
				if (cmd == Keyword.ELSE) {
					return new ElseToken();
				} else if (cmd == Keyword.END) {
					return new EndToken();
				}
			} catch (IllegalArgumentException e) {
				// XXX will be thrown when this is not a keyword, in this case
				// simply proceed parsing it as a variable expression
			}
			Object value;
			if (!skipMode) {
				// single name expression like ${name}
				value = traverse(objectExpression, model);
				if (value == null) {
					errorHandler.error(String.format("Variable '%s' undefined",
							objectExpression));
					// gracefully ignore in production mode
					value = "";

				} else if (value instanceof Map || value instanceof Iterable) {
					errorHandler
							.error(String
									.format(
											"Illegal expansion of map or iterable variable '%s'",
											objectExpression));
					// gracefully ignore in production mode
					value = "";
				}
			} else {
				value = "";
			}

			return new StringToken(value.toString());
		} else {
			String cmdString = split[0];
			try {
				Keyword cmd = Keyword.valueOf(cmdString.toUpperCase());

				if (cmd == Keyword.IF) {
					boolean condition;
					boolean negate = false;
					if (!skipMode) {
						String objectExpression = split[1];
						if (objectExpression.startsWith("!")) {
							negate = true;
							objectExpression = objectExpression.substring(1);

						}
						Object value = traverse(objectExpression, model);
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
						Object value = traverse(objectExpression, model);
						Iterable<Object> iterable;
						if (value == null) {
							return new IfToken(false);
						} else if (value instanceof Map) {
							iterable = ((Map) value).entrySet();
						} else if (value instanceof Collection) {
							iterable = ((Iterable) value);
						} else {
							errorHandler
									.error(String
											.format(
													"Can only iterator over map or iterable on '%s'",
													objectExpression));
							return new IfToken(false);
						}
						String varName = split[2];
						ForEachToken forEachToken = new ForEachToken(varName,
								iterable);

						// if we have more parameters, we also have separator
						// data
						if (split.length > 3 || split.length == 3
								&& input.endsWith("  ")) {
							// but as the separator itself can contain spaces
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
					} else {
						return new IfToken(false);
					}
				}
				// XXX can never happen, but here to satisfy compiler
				return null;
			} catch (IllegalArgumentException iae) {
				throw new IllegalArgumentException(String.format(
						"Command '%s' is undefined", cmdString));
			}

		}
	}

	protected Object traverse(String objectExpression, Map<String, Object> model) {
		String[] split = objectExpression.split("\\.");

		String objectName = split[0];
		Object value = model.get(objectName);

		LinkedList<String> attributeNames = new LinkedList<String>(Arrays
				.asList(split));
		attributeNames.remove(0);
		value = traverse(value, attributeNames);
		return value;
	}

	protected Object traverse(Object o, LinkedList<String> attributeNames) {
		Object result;
		if (attributeNames.isEmpty()) {
			result = o;
		} else {
			if (o == null) {
				errorHandler
						.error(String
								.format("You can not make property calls on null values"));
				return null;
			}
			String attributeName = attributeNames.remove(0);
			Object nextStep = nextStep(o, attributeName);
			result = traverse(nextStep, attributeNames);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	protected Object nextStep(Object o, String attributeName) {
		Object result;
		if (o instanceof String) {
			errorHandler.error(String.format(
					"You can not make property calls on string '%s'", o
							.toString()));
			return o;
		} else if (o instanceof Map) {
			Map map = (Map) o;
			result = map.get(attributeName);
		} else {
			result = Util.getPropertyValue(o, attributeName);
		}
		return result;
	}

}
