package com.floreysoft.jmte;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * <p>
 * The template engine.
 * </p>
 * 
 * <p>
 * Usually this is the only class you need calling
 * {@link #transform(String, Map)}. Like this
 * </p>
 * 
 * <pre>
 * Engine engine = new Engine();
 * String transformed = engine.transform(input, model);
 * </pre>
 * 
 * <p>
 * You have to provide the template input written in the template language and a
 * model from String to Object. Maybe like this
 * </p>
 * 
 * <pre>
 * String input = &quot;${name}&quot;;
 * Map&lt;String, Object&gt; model = new HashMap&lt;String, Object&gt;();
 * model.put(&quot;name&quot;, &quot;Minimal Template Engine&quot;);
 * Engine engine = new Engine();
 * String transformed = engine.transform(input, model);
 * assert (transformed.equals(&quot;Minimal Template Engine&quot;));
 * </pre>
 * 
 * where <code>input</code> contains the template and <code>model</code> the
 * model. <br>
 * 
 * @see Lexer
 * @see ErrorHandler
 * @see Tool
 */
public final class Engine {
	public static final String ODD_PREFIX = "odd_";
	public static final String EVEN_PREFIX = "even_";
	public static final String LAST_PREFIX = "last_";
	public static final String FIRST_PREFIX = "first_";

	/**
	 * Pairs of begin/end.
	 * 
	 */
	public static class StartEndPair {
		public final int start;
		public final int end;

		public StartEndPair(int start, int end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString() {
			return "" + start + "-" + end;
		}
	}

	/**
	 * Replacement for {@link java.lang.String.format}. All arguments will be
	 * put into the model having their index starting from 1 as their name.
	 * 
	 * @param pattern
	 *            the template
	 * @param args
	 *            any number of arguments
	 * @return the expanded template
	 */
	public static String format(String pattern, Object... args) {
		Map<String, Object> model = arrayToModel(null, args);
		Engine engine = new Engine();
		String output = engine.transform(pattern, model);
		return output;
	}

	public static String formatNamed(String pattern, String name1, Object value1) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(name1, value1);
		Engine engine = new Engine();
		String output = engine.transform(pattern, model);
		return output;
	}

	public static String formatNamed(String pattern, String name1,
			Object value1, String name2, Object value2) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(name1, value1);
		model.put(name2, value2);
		Engine engine = new Engine();
		String output = engine.transform(pattern, model);
		return output;
	}

	public static String formatNamed(String pattern, String name1,
			Object value1, String name2, Object value2, String name3,
			Object value3) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(name1, value1);
		model.put(name2, value2);
		model.put(name3, value3);
		Engine engine = new Engine();
		String output = engine.transform(pattern, model);
		return output;
	}

	public static String formatNamed(String pattern, String name1,
			Object value1, String name2, Object value2, String name3,
			Object value3, String name4, Object value4) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(name1, value1);
		model.put(name2, value2);
		model.put(name3, value3);
		model.put(name4, value4);
		Engine engine = new Engine();
		String output = engine.transform(pattern, model);
		return output;
	}

	public static String formatNamed(String pattern, String name1,
			Object value1, String name2, Object value2, String name3,
			Object value3, String name4, Object value4, String name5,
			Object value5) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(name1, value1);
		model.put(name2, value2);
		model.put(name3, value3);
		model.put(name4, value4);
		model.put(name5, value4);
		Engine engine = new Engine();
		String output = engine.transform(pattern, model);
		return output;
	}

	/**
	 * Transforms an array to a model using the index of the elements (starting
	 * from 1) in the array and a prefix to form their names.
	 * 
	 * @param prefix
	 *            the prefix to add to the index or <code>null</code> if none
	 *            shall be applied
	 * @param args
	 *            the array to be transformed into the model
	 * @return the model containing the arguments
	 */
	public static Map<String, Object> arrayToModel(String prefix,
			Object... args) {
		Map<String, Object> model = new HashMap<String, Object>();
		if (prefix == null) {
			prefix = "";
		}
		for (int i = 0; i < args.length; i++) {
			Object value = args[i];
			String name = prefix + (i + 1);
			model.put(name, value);
		}
		return model;
	}

	public static Map<String, Object> toModel(String name1, Object value1) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(name1, value1);
		return model;
	}

	public static Map<String, Object> toModel(String name1, Object value1,
			String name2, Object value2) {
		Map<String, Object> model = toModel(name1, value1);
		model.put(name2, value2);
		return model;
	}

	public static Map<String, Object> toModel(String name1, Object value1,
			String name2, Object value2, String name3, Object value3) {
		Map<String, Object> model = toModel(name1, value1, name2, value2);
		model.put(name3, value3);
		return model;
	}

	/**
	 * Merges any number of named lists into a single one contained their
	 * combined values. Can be very handy in case of a servlet request which
	 * might contain several lists of parameters that you want to iterate over
	 * in a combined way.
	 * 
	 * @param names
	 *            the names of the variables in the following lists
	 * @param lists
	 *            the lists containing the values for the named variables
	 * @return a merge list containing the combined values of the lists
	 */
	public static List<Map<String, Object>> mergeLists(String[] names,
			List<Object>... lists) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		if (lists.length != 0) {

			// first check if all looks good
			int expectedSize = lists[0].size();
			for (int i = 1; i < lists.length; i++) {
				List<Object> list = lists[i];
				if (list.size() != expectedSize) {
					throw new IllegalArgumentException(
							"All lists and array of names must have the same size!");
				}
			}

			// yes, things are ok
			List<Object> masterList = lists[0];
			for (int i = 0; i < masterList.size(); i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (int j = 0; j < lists.length; j++) {
					String name = names[j];
					List<Object> list = lists[j];
					Object value = list.get(i);
					map.put(name, value);
				}
				resultList.add(map);
			}
		}
		return resultList;
	}

	private String exprStartToken = "${";
	private String exprEndToken = "}";
	private double expansionSizeFactor = 1.2;
	private Lexer lexer = new DefaultLexer();
	private ErrorHandler errorHandler = new DefaultErrorHandler();
	private Locale locale = new Locale("en");
	private String sourceName = null;
	private boolean useEscaping = true;

	private transient LinkedList<Token> scopes = new LinkedList<Token>();

	/**
	 * Creates a new engine having <code>${</code> and <code>}</code> as start
	 * and end strings for expressions.
	 */
	public Engine() {
	}

	public Engine withSourceName(String sourceName) {
		this.sourceName = sourceName;
		return this;
	}

	/**
	 * * @param useEscaping tells the method whether to use (<code>true</code>)
	 * or ignore escape character \\
	 * 
	 * @return
	 */
	public Engine useEscaping(boolean useEscaping) {
		this.useEscaping = useEscaping;
		return this;
	}

	/**
	 * Transforms a template into an expanded output using the given model.
	 * 
	 * @param template
	 *            the template to expand
	 * @param model
	 *            the model used to evaluate expressions inside the template
	 * @return the expanded output
	 */
	public String transform(String template, Map<String, Object> model) {
		List<StartEndPair> scan = scan(template);
		ScopedMap scopedMap = new ScopedMap(model);
		String transformed = transformPure(sourceName, template, scan,
				scopedMap);
		if (!useEscaping) {
			return transformed;
		} else {
			String unescaped = Util.unescape(transformed);
			return unescaped;
		}
	}

	@SuppressWarnings("unchecked")
	private String transformPure(String sourceName, String input,
			List<StartEndPair> scan, ScopedMap model) {
		char[] inputChars = input.toCharArray();
		StringBuilder output = new StringBuilder(
				(int) (input.length() * getExpansionSizeFactor()));
		int offset = 0;
		int i = 0;
		while (i < scan.size()) {
			StartEndPair startEndPair = scan.get(i);
			int length = startEndPair.start - getExprStartToken().length()
					- offset;
			boolean skipMode = isSkipMode(model);
			if (!skipMode) {
				output.append(inputChars, offset, length);
			}
			offset = startEndPair.end + getExprEndToken().length();
			i++;

			Token token = lexer.nextToken(sourceName, inputChars,
					startEndPair.start, startEndPair.end);
			if (token instanceof StringToken) {
				if (!skipMode) {
					String expanded = (String) token.evaluate(model,
							errorHandler);
					output.append(expanded);
				}
			} else if (token instanceof ForEachToken) {
				ForEachToken feToken = (ForEachToken) token;
				Iterable iterable = (Iterable) feToken.evaluate(model,
						errorHandler);
				feToken.setIterator(iterable.iterator());
				if (!feToken.iterator().hasNext()) {
					// XXX Hack to make an empty iteration a false if
					token = new FixedBooleanToken(false);
				} else {
					model.enterScope();
					Object value = feToken.iterator().next();
					model.put(feToken.getVarName(), value);
					feToken.setScanIndex(i);
					feToken.setOffset(offset);
					feToken.setFirst(true);
					feToken.setIndex(0);
					feToken.setLast(!feToken.iterator().hasNext());
					addSpecialVariables(feToken, model);
				}
				push(token);
			} else if (token instanceof IfToken) {
				push(token);
			} else if (token instanceof ElseToken) {
				Token poppedToken = pop();
				if (!(poppedToken instanceof IfToken)) {
					getErrorHandler().error("else-out-of-scope", token,
							Engine.toModel("surroundingToken", poppedToken));
				}
				// if we see an if we simply negate the value of the
				// enclosing if and replace the if token with this fixed
				// boolean value
				boolean negatedFixedValue = !(Boolean) poppedToken.evaluate(
						model, errorHandler);
				FixedBooleanToken fixedBooleanToken = new FixedBooleanToken(
						negatedFixedValue);
				push(fixedBooleanToken);
			} else if (token instanceof EndToken) {
				Token poppedToken = pop();
				if (poppedToken == null) {
					getErrorHandler().error("unmatched-end", token, null);
				} else if (poppedToken instanceof ForEachToken) {
					ForEachToken feToken = (ForEachToken) poppedToken;
					if (feToken.iterator().hasNext()) {
						Object value = feToken.iterator().next();
						model.put(feToken.getVarName(), value);
						i = feToken.getScanIndex();
						offset = feToken.getOffset();
						startEndPair = scan.get(i);
						push(feToken);
						if (!skipMode && feToken.getSeparator() != null) {
							output.append(feToken.getSeparator());
						}
						feToken.setFirst(false);
						feToken.setLast(!feToken.iterator().hasNext());
						feToken.setIndex(feToken.getIndex() + 1);
						addSpecialVariables(feToken, model);
					} else {
						model.exitScope();
					}
				}
			}
		}

		// do not forget to add the final chunk of pure text (might be the
		// only
		// chunk indeed)
		int remainingChars = input.length() - offset;
		output.append(inputChars, offset, remainingChars);
		return output.toString();
	}

	private void addSpecialVariables(ForEachToken feToken,
			Map<String, Object> model) {
		String suffix = feToken.getVarName();
		model.put(FIRST_PREFIX + suffix, feToken.isFirst());
		model.put(LAST_PREFIX + suffix, feToken.isLast());
		model.put(EVEN_PREFIX + suffix, feToken.getIndex() % 2 == 0);
		model.put(ODD_PREFIX + suffix, feToken.getIndex() % 2 == 1);
	}

	private void push(Token token) {
		scopes.add(token);
	}

	private Token pop() {
		if (scopes.isEmpty()) {
			return null;
		} else {
			Token token = scopes.removeLast();
			return token;
		}
	}

	private Token peek() {
		if (scopes.isEmpty()) {
			return null;
		} else {
			return scopes.getLast();
		}
	}

	// if anywhere in the stack trace there is a negated if, we surely are in
	// skip mode
	private boolean isSkipMode(Map<String, Object> model) {
		boolean skip = false;

		for (Token token : scopes) {
			if (token instanceof IfToken || token instanceof FixedBooleanToken) {
				boolean ifCondition = (Boolean) token.evaluate(model,
						errorHandler);
				if (!ifCondition) {
					skip = true;
					break;
				}
			}
		}
		return skip;
	}

	/**
	 * Scans the input and spits out begin/end pairs telling you where
	 * expressions can be found.
	 * 
	 * @param input
	 *            the input
	 * @return the begin/end pairs telling you where expressions can be found
	 */
	public List<StartEndPair> scan(String input) {
		List<StartEndPair> result = new ArrayList<StartEndPair>();
		int fromIndex = 0;
		while (true) {
			int exprStart = input.indexOf(getExprStartToken(), fromIndex);
			if (exprStart == -1) {
				break;
			}
			if (useEscaping && Util.isEscaped(input, exprStart)) {
				fromIndex = exprStart + getExprStartToken().length();
				continue;
			}

			exprStart += getExprStartToken().length();
			int exprEnd = input.indexOf(getExprEndToken(), exprStart);
			if (exprEnd == -1) {
				break;
			}
			while (useEscaping && Util.isEscaped(input, exprEnd)) {
				exprEnd = input.indexOf(getExprEndToken(), exprEnd
						+ getExprEndToken().length());
			}

			fromIndex = exprEnd + getExprEndToken().length();

			StartEndPair startEndPair = new StartEndPair(exprStart, exprEnd);
			result.add(startEndPair);
		}
		return result;
	}

	public String emitToken(Token token) {
		return getExprStartToken() + token.getText() + getExprEndToken();
	}

	/**
	 * Sets a new lexer - which might be different from standard one.
	 * 
	 * @param lexer
	 *            the new lexer
	 */
	public Engine withLexer(Lexer lexer) {
		this.lexer = lexer;
		return this;
	}

	/**
	 * Gets the currently used lexer.
	 * 
	 * @return the currently used lexer
	 */
	public Lexer getLexer() {
		return lexer;
	}

	/**
	 * Sets the error handler to be used in this engine
	 * 
	 * @param errorHandler
	 *            the new error handler
	 */
	public Engine withErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
		this.errorHandler.withLocale(locale);
		return this;
	}

	/**
	 * Gets the currently used error handler
	 * 
	 * @return the error handler
	 */
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public String getExprStartToken() {
		return exprStartToken;
	}

	public String getExprEndToken() {
		return exprEndToken;
	}

	public Engine withExprStartToken(String exprStartToken) {
		this.exprStartToken = exprStartToken;
		return this;
	}

	public Engine withExprEndToken(String exprEndToken) {
		this.exprEndToken = exprEndToken;
		return this;
	}

	public Engine withExpansionSizeFactor(double expansionSizeFactor) {
		this.expansionSizeFactor = expansionSizeFactor;
		return this;
	}

	public double getExpansionSizeFactor() {
		return expansionSizeFactor;
	}

	public Engine withLocale(Locale locale) {
		this.locale = locale;
		if (this.errorHandler != null) {
			this.errorHandler.withLocale(locale);
		}
		return this;
	}

	public Locale getLocale() {
		return locale;
	}
}
