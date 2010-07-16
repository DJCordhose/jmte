package com.floreysoft.jmte;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.floreysoft.jmte.token.DefaultToken;
import com.floreysoft.jmte.token.ElseToken;
import com.floreysoft.jmte.token.EndToken;
import com.floreysoft.jmte.token.ForEachToken;
import com.floreysoft.jmte.token.IfToken;
import com.floreysoft.jmte.token.StringToken;

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
	
	static enum Keyword {
		IF, END, ELSE, FOREACH
	};

	protected ErrorHandler errorHandler;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Token nextToken(final char[] template, final int start, final int end,
			final Map<String, Object> model, final boolean skipMode,
			final ErrorHandler errorHandler) {
		String input = new String(template, start, end - start);
		this.errorHandler = errorHandler;
		input = Util.trimFront(input);
		String[] split = input.split("( |\t|\r|\n)+");

		DefaultToken token = innerNextToken(template, start, end, model, skipMode,
				errorHandler, input, split);
		token.setBuffer(template);
		token.setStart(start);
		token.setEnd(end);
		return token;
	}

	@SuppressWarnings("unchecked")
	private DefaultToken innerNextToken(final char[] template, final int start, final int end,
			final Map<String, Object> model, final boolean skipMode,
			final ErrorHandler errorHandler, final String input, final String[] split) {
		Token errorToken = new DefaultToken(template, start, end);
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
					value = traverse(objectExpression, model, errorToken);
					if (value == null) {
						value = "";

					} else if (!(value instanceof String)) {
						final List<Object> arrayAsList = Util
								.arrayAsList(value);
						if (arrayAsList != null) {
							value = arrayAsList.size() > 0 ? arrayAsList.get(0)
									: "";
						} else if (value instanceof Map) {
							final Map map = (Map) value;
							final Collection values = map.values();
							value = values.size() > 0 ? values.iterator()
									.next() : "";
						} else if (value instanceof Iterable) {
							final Iterable iterable = (Iterable) value;
							final Iterator iterator = iterable.iterator();
							value = iterator.hasNext() ? iterator.next() : "";
						}
					}
				} else {
					// in skip mode keep old expression
					value = objectExpression;
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
						cmdString), errorToken);
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
					Object value = traverse(objectExpression, model, errorToken);
					if (value == null || value.toString().equals("")) {
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
					} else {
						List list = Util.arrayAsList(value);
						// XXX looks strange, but is ok: list will be null if is
						// is not an array which results to true
						condition = list == null || !list.isEmpty();
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
					Object value = traverse(objectExpression, model, errorToken);
					Iterable<Object> iterable;
					if (value == null) {
						return new IfToken(false);
					} else if (value instanceof Map) {
						iterable = ((Map) value).entrySet();
					} else if (value instanceof Iterable) {
						iterable = ((Iterable) value);
					} else {
						iterable = Util.arrayAsList(value);
						if (iterable == null) {
							// we have a single value here and simply wrap it in a List
							iterable = Collections.singletonList(value);
						}
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
				} else {
					return new IfToken(false);
				}
			}
		}
		// default in case anything went wrong
		return new StringToken("");
	}

	protected Object traverse(String objectExpression,
			Map<String, Object> model, Token errorToken) {
		String[] split = objectExpression.split("\\.");

		String objectName = split[0];
		Object value = model.get(objectName);

		LinkedList<String> attributeNames = new LinkedList<String>(Arrays
				.asList(split));
		attributeNames.remove(0);
		value = traverse(value, attributeNames, errorToken);
		return value;
	}

	protected Object traverse(Object o, LinkedList<String> attributeNames, Token errorToken) {
		Object result;
		if (attributeNames.isEmpty()) {
			result = o;
		} else {
			if (o == null) {
				return null;
			}
			String attributeName = attributeNames.remove(0);
			Object nextStep = nextStep(o, attributeName, errorToken);
			result = traverse(nextStep, attributeNames, errorToken);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	protected Object nextStep(Object o, String attributeName, Token errorToken) {
		Object result;
		if (o instanceof String) {
			errorHandler.error(String.format(
					"You can not make property calls on string '%s'", o
							.toString()), errorToken);
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
						attributeName, o.toString(), e.getMessage() != null ? e
								.getMessage() : e.getCause() != null ? e
								.getCause().getMessage() : ""), errorToken);
				result = "";
			}
		}
		return result;
	}

}
