package com.floreysoft.jmte;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.floreysoft.jmte.ProcessListener.Action;

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
	static class StartEndPair {
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
	private Lexer lexer = new Lexer();
	private ErrorHandler errorHandler = new DefaultErrorHandler();
	private Locale locale = new Locale("en");
	private String sourceName = null;
	private final Map<Class<?>, Renderer<?>> renderers = new HashMap<Class<?>, Renderer<?>>();
	private final List<ProcessListener> listeners = new ArrayList<ProcessListener>();

	private transient LinkedList<Token> scopes = new LinkedList<Token>();

	/**
	 * Creates a new engine having <code>${</code> and <code>}</code> as start
	 * and end strings for expressions.
	 */
	public Engine() {
	}

	public Engine setSourceName(String sourceName) {
		this.sourceName = sourceName;
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
		String unescaped = Util.MINI_PARSER.unescape(transformed);
		return unescaped;
	}

	@SuppressWarnings("unchecked")
	private String transformPure(String sourceName, String input,
			List<StartEndPair> scan, ScopedMap model) {
		final TokenStream tokenStream = new TokenStream(sourceName, input,
				scan, lexer, getExprStartToken(), getExprEndToken());
		final StringBuilder output = new StringBuilder(
				(int) (input.length() * getExpansionSizeFactor()));
		Token token;
		while ((token = tokenStream.nextToken()) != null) {
			boolean skipMode = isSkipMode(model);
			if (token instanceof PlainTextToken) {
				if (!skipMode) {
					output.append(token.getText());
				}
			} else if (token instanceof StringToken) {
				if (!skipMode) {
					String expanded = (String) token.evaluate(this, model,
							errorHandler);
					output.append(expanded);
					notifyListeners(token, ProcessListener.Action.EVAL);
				} else {
					notifyListeners(token, ProcessListener.Action.SKIP);
				}
			} else if (token instanceof ForEachToken) {
				ForEachToken feToken = (ForEachToken) token;
				Iterable iterable = (Iterable) feToken.evaluate(this, model,
						errorHandler);
				feToken.setIterator(iterable.iterator());
				if (!feToken.iterator().hasNext()) {
					token = new EmptyForEachToken(feToken.getText());
					notifyListeners(token, ProcessListener.Action.EMPTY_FOREACH);
				} else {
					model.enterScope();
					Object value = feToken.iterator().next();
					model.put(feToken.getVarName(), value);
					feToken.setFirst(true);
					feToken.setIndex(0);
					feToken.setLast(!feToken.iterator().hasNext());
					addSpecialVariables(feToken, model);
					notifyListeners(token,
							ProcessListener.Action.ITERATE_FOREACH);
				}
				push(token);
			} else if (token instanceof IfToken) {
				push(token);
			} else if (token instanceof ElseToken) {
				Token poppedToken = pop();
				if (!(poppedToken instanceof IfToken)) {
					getErrorHandler().error("else-out-of-scope", token,
							Engine.toModel("surroundingToken", poppedToken));
				} else {
					ElseToken elseToken = (ElseToken) token;
					elseToken.setIfToken((IfToken) poppedToken);
					push(token);
				}
			} else if (token instanceof EndToken) {
				Token poppedToken = pop();
				if (poppedToken == null) {
					getErrorHandler().error("unmatched-end", token, null);
				} else if (poppedToken instanceof ForEachToken) {
					ForEachToken feToken = (ForEachToken) poppedToken;
					if (feToken.iterator().hasNext()) {
						// for each iteration we need to rewind to the beginning
						// of the for loop
						tokenStream.rewind(feToken);
						Object value = feToken.iterator().next();
						model.put(feToken.getVarName(), value);
						push(feToken);
						if (!skipMode && feToken.getSeparator() != null) {
							output.append(feToken.getSeparator());
						}
						feToken.setFirst(false);
						feToken.setLast(!feToken.iterator().hasNext());
						feToken.setIndex(feToken.getIndex() + 1);
						addSpecialVariables(feToken, model);
						notifyListeners(token,
								ProcessListener.Action.ITERATE_FOREACH);
					} else {
						model.exitScope();
					}
				}
			}
		}
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

	// if anywhere in the stack trace there is a negative condition, all the
	// inner parts must be skipped
	private boolean isSkipMode(Map<String, Object> model) {
		for (Token token : scopes) {
			if (token instanceof IfToken || token instanceof ElseToken
					|| token instanceof EmptyForEachToken) {
				boolean condition = (Boolean) token.evaluate(this, model,
						errorHandler);
				if (!condition) {
					notifyListeners(token, ProcessListener.Action.SKIP);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Scans the input and spits out begin/end pairs telling you where
	 * expressions can be found.
	 * 
	 * @param input
	 *            the input
	 * @return the begin/end pairs telling you where expressions can be found
	 */
	List<StartEndPair> scan(String input) {
		return Util.scan(input, getExprStartToken(), getExprEndToken(), true);
	}

	/**
	 * Sets the error handler to be used in this engine
	 * 
	 * @param errorHandler
	 *            the new error handler
	 */
	public Engine setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
		this.errorHandler.setLocale(locale);
		return this;
	}

	public <C> Engine addRenderer(Class<C> clazz, Renderer<C> renderer) {
		renderers.put(clazz, renderer);
		return this;
	}

	public Engine removeRenderer(Class<?> clazz) {
		renderers.remove(clazz);
		return this;
	}

	private Renderer rendererForClass(Class<?> clazz) {
		Renderer resolvedRenderer = renderers.get(clazz);
		if (resolvedRenderer != null) {
			return resolvedRenderer;
		} else {
			return new Renderer() {

				@Override
				public String render(Object o, String format) {
					return null;
				}

			};
		}
	}

	@SuppressWarnings("unchecked")
	protected String render(Class<?> clazz, Object value, String format) {
		String result = rendererForClass(clazz).render(value, format);
		if (result == null) {
			Class<?>[] interfaces = clazz.getInterfaces();
			for (Class<?> interfaze : interfaces) {
				result = rendererForClass(interfaze).render(value, format);
				if (result != null) {
					break;
				}
			}
		}
		if (result == null) {
			Class<?> superclass = clazz.getSuperclass();
			if (superclass != null) {
				result = render(superclass, value, format);
			}
		}
		return result;
	}

	public Engine addProcessListener(ProcessListener listener) {
		listeners.add(listener);
		return this;
	}

	public Engine removeProcessListener(ProcessListener listener) {
		listeners.remove(listener);
		return this;
	}

	private void notifyListeners(Token token, Action action) {
		for (ProcessListener processListener : listeners) {
			processListener.log(token, action);
		}
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

	public Engine setExprStartToken(String exprStartToken) {
		this.exprStartToken = exprStartToken;
		return this;
	}

	public Engine setExprEndToken(String exprEndToken) {
		this.exprEndToken = exprEndToken;
		return this;
	}

	public Engine setExpansionSizeFactor(double expansionSizeFactor) {
		this.expansionSizeFactor = expansionSizeFactor;
		return this;
	}

	public double getExpansionSizeFactor() {
		return expansionSizeFactor;
	}

	public Engine setLocale(Locale locale) {
		this.locale = locale;
		if (this.errorHandler != null) {
			this.errorHandler.setLocale(locale);
		}
		return this;
	}

	public Locale getLocale() {
		return locale;
	}
}
