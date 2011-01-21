package com.floreysoft.jmte;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class InterpretedTemplate extends AbstractTemplate implements Template {

	protected final String template;
	protected final Engine engine;
	protected final String sourceName;

	public InterpretedTemplate(String template, String sourceName, Engine engine) {
		this.template = template;
		this.engine = engine;
		this.sourceName = sourceName;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getUsedVariables() {
		final Set<String> usedVariables = new TreeSet<String>();

		final List<ProcessListener> oldListeners = engine.listeners;
		engine.listeners.clear();

		final List<StartEndPair> scan = engine.scan(template);
		final ScopedMap scopedMap = new ScopedMap(Collections.EMPTY_MAP);

		final TemplateContext context = new TemplateContext(template,
				sourceName, scopedMap, engine);

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
				for (Token token : context.scopes) {
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

		transformPure(context, scan);

		engine.listeners.addAll(oldListeners);

		return usedVariables;
	}

	@Override
	public String transform(Map<String, Object> model) {
		List<StartEndPair> scan = engine.scan(template);
		TemplateContext context = new TemplateContext(template, sourceName,
				new ScopedMap(model), engine);
		String transformed = transformPure(context, scan);
		String unescaped = Util.NO_QUOTE_MINI_PARSER.unescape(transformed);
		return unescaped;

	}

	@SuppressWarnings("unchecked")
	protected String transformPure(TemplateContext context,
			List<StartEndPair> scan) {

		final TokenStream tokenStream = new TokenStream(context.sourceName,
				context.template, scan, context.lexer, context.engine
						.getExprStartToken(), context.engine.getExprEndToken());
		final StringBuilder output = new StringBuilder((int) (context.template
				.length() * context.engine.getExpansionSizeFactor()));
		Token token;
		while ((token = tokenStream.nextToken()) != null) {
			boolean skipMode = context.isSkipMode();
			if (token instanceof PlainTextToken) {
				if (!skipMode) {
					output.append(token.getText());
				}
			} else if (token instanceof StringToken) {
				if (!skipMode) {
					String expanded = (String) token.evaluate(context);
					output.append(expanded);
					context.engine.notifyListeners(token,
							ProcessListener.Action.EVAL);
				} else {
					context.engine.notifyListeners(token,
							ProcessListener.Action.SKIP);
				}
			} else if (token instanceof ForEachToken) {
				ForEachToken feToken = (ForEachToken) token;
				Iterable iterable = (Iterable) feToken.evaluate(context);
				feToken.setIterator(iterable.iterator());
				if (!feToken.iterator().hasNext()) {
					token = new EmptyForEachToken(feToken.getExpression(),
							feToken.getVarName(), feToken.getText());
					context.engine.notifyListeners(token,
							ProcessListener.Action.EMPTY_FOREACH);
				} else {
					context.model.enterScope();
					context.model.put(feToken.getVarName(), feToken.advance());
					addSpecialVariables(feToken, context.model);
					context.engine.notifyListeners(token,
							ProcessListener.Action.ITERATE_FOREACH);
				}
				context.push(token);
			} else if (token instanceof IfToken) {
				context.push(token);
				context.engine
						.notifyListeners(token, ProcessListener.Action.IF);
			} else if (token instanceof ElseToken) {
				Token poppedToken = context.pop();
				if (!(poppedToken instanceof IfToken)) {
					context.engine.getErrorHandler().error("else-out-of-scope",
							token,
							Engine.toModel("surroundingToken", poppedToken));
				} else {
					ElseToken elseToken = (ElseToken) token;
					elseToken.setIfToken((IfToken) poppedToken);
					context.push(token);
				}
			} else if (token instanceof EndToken) {
				Token poppedToken = context.pop();
				if (poppedToken == null) {
					context.engine.getErrorHandler().error("unmatched-end",
							token, null);
				} else if (poppedToken instanceof ForEachToken) {
					ForEachToken feToken = (ForEachToken) poppedToken;
					if (feToken.iterator().hasNext()) {
						// for each iteration we need to rewind to the beginning
						// of the for loop
						tokenStream.rewind(feToken);
						context.model.put(feToken.getVarName(), feToken.advance());
						context.push(feToken);
						if (!skipMode) {
							output.append(feToken.getSeparator());
						}
						addSpecialVariables(feToken, context.model);
						context.engine.notifyListeners(token,
								ProcessListener.Action.ITERATE_FOREACH);
					} else {
						context.model.exitScope();
					}
				}
			}
		}
		return output.toString();
	}
}
