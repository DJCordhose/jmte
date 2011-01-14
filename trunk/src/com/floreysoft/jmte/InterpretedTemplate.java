package com.floreysoft.jmte;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class InterpretedTemplate extends AbstractTemplate implements Template {

	private transient LinkedList<Token> scopes = new LinkedList<Token>();
	protected final String template;
	protected final Engine engine;
	protected transient Lexer lexer = new Lexer();

	public InterpretedTemplate(String template, Engine engine) {
		this.template = template;
		this.engine = engine;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getUsedVariables() {
		final Set<String> usedVariables = new TreeSet<String>();

		final List<ProcessListener> oldListeners = engine.listeners;
		engine.listeners.clear();

		final List<StartEndPair> scan = engine.scan(template);
		final ScopedMap scopedMap = new ScopedMap(Collections.EMPTY_MAP);
		engine.addProcessListener(new ProcessListener() {

			@Override
			public void log(Token token, Action action) {
				if (token instanceof ExpressionToken) {
					String variable = ((ExpressionToken) token).getExpression();
					// do not include local variables defined by foreach
					if (!isLocal(variable)) {
						usedVariables.add(variable);
					}
				}
			}

			// a variable is local if any enclosing foreach has it as a step
			// variable
			private boolean isLocal(String variable) {
				for (Token token : scopes) {
					if (token instanceof EmptyForEachToken) {
						String foreachVarName = ((EmptyForEachToken) token)
								.getVarName();
						if (foreachVarName.equals(variable)) {
							return true;
						}
					}
				}
				return false;

			}

		});

		transformPure(engine.sourceName, scan, scopedMap);

		engine.listeners.addAll(oldListeners);

		return usedVariables;
	}

	@Override
	public String transform(Map<String, Object> model) {
		List<StartEndPair> scan = engine.scan(template);
		ScopedMap scopedMap = new ScopedMap(model);
		String transformed = transformPure(engine.sourceName, scan, scopedMap);
		String unescaped = Util.NO_QUOTE_MINI_PARSER.unescape(transformed);
		return unescaped;

	}

	@SuppressWarnings("unchecked")
	private String transformPure(String sourceName, List<StartEndPair> scan,
			ScopedMap model) {
		scopes.clear();

		final TokenStream tokenStream = new TokenStream(sourceName, template,
				scan, lexer, engine.getExprStartToken(), engine
						.getExprEndToken());
		final StringBuilder output = new StringBuilder(
				(int) (template.length() * engine.getExpansionSizeFactor()));
		Token token;
		while ((token = tokenStream.nextToken()) != null) {
			boolean skipMode = isSkipMode(model);
			if (token instanceof PlainTextToken) {
				if (!skipMode) {
					output.append(token.getText());
				}
			} else if (token instanceof StringToken) {
				if (!skipMode) {
					String expanded = (String) token.evaluate(engine, model,
							engine.getErrorHandler());
					output.append(expanded);
					engine.notifyListeners(token, ProcessListener.Action.EVAL);
				} else {
					engine.notifyListeners(token, ProcessListener.Action.SKIP);
				}
			} else if (token instanceof ForEachToken) {
				ForEachToken feToken = (ForEachToken) token;
				Iterable iterable = (Iterable) feToken.evaluate(engine, model,
						engine.getErrorHandler());
				feToken.setIterator(iterable.iterator());
				if (!feToken.iterator().hasNext()) {
					token = new EmptyForEachToken(feToken.getExpression(),
							feToken.getVarName(), feToken.getText());
					engine.notifyListeners(token,
							ProcessListener.Action.EMPTY_FOREACH);
				} else {
					model.enterScope();
					Object value = feToken.iterator().next();
					model.put(feToken.getVarName(), value);
					feToken.setFirst(true);
					feToken.setIndex(0);
					feToken.setLast(!feToken.iterator().hasNext());
					addSpecialVariables(feToken, model);
					engine.notifyListeners(token,
							ProcessListener.Action.ITERATE_FOREACH);
				}
				push(token);
			} else if (token instanceof IfToken) {
				push(token);
				engine.notifyListeners(token, ProcessListener.Action.IF);
			} else if (token instanceof ElseToken) {
				Token poppedToken = pop();
				if (!(poppedToken instanceof IfToken)) {
					engine.getErrorHandler().error("else-out-of-scope", token,
							Engine.toModel("surroundingToken", poppedToken));
				} else {
					ElseToken elseToken = (ElseToken) token;
					elseToken.setIfToken((IfToken) poppedToken);
					push(token);
				}
			} else if (token instanceof EndToken) {
				Token poppedToken = pop();
				if (poppedToken == null) {
					engine.getErrorHandler()
							.error("unmatched-end", token, null);
				} else if (poppedToken instanceof ForEachToken) {
					ForEachToken feToken = (ForEachToken) poppedToken;
					if (feToken.iterator().hasNext()) {
						// for each iteration we need to rewind to the beginning
						// of the for loop
						tokenStream.rewind(feToken);
						Object value = feToken.iterator().next();
						model.put(feToken.getVarName(), value);
						push(feToken);
						if (!skipMode) {
							output.append(feToken.getSeparator());
						}
						feToken.setFirst(false);
						feToken.setLast(!feToken.iterator().hasNext());
						feToken.setIndex(feToken.getIndex() + 1);
						addSpecialVariables(feToken, model);
						engine.notifyListeners(token,
								ProcessListener.Action.ITERATE_FOREACH);
					} else {
						model.exitScope();
					}
				}
			}
		}
		return output.toString();
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

	// if anywhere in the stack trace there is a negative condition, all the
	// inner parts must be skipped
	private boolean isSkipMode(Map<String, Object> model) {
		for (Token token : scopes) {
			if (token instanceof IfToken || token instanceof ElseToken
					|| token instanceof EmptyForEachToken) {
				boolean condition = (Boolean) token.evaluate(engine, model,
						engine.getErrorHandler());
				if (!condition) {
					engine.notifyListeners(token, ProcessListener.Action.SKIP);
					return true;
				}
			}
		}
		return false;
	}
}
