package com.floreysoft.jmte.template;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.ProcessListener;
import com.floreysoft.jmte.ScopedMap;
import com.floreysoft.jmte.StartEndPair;
import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.ProcessListener.Action;
import com.floreysoft.jmte.token.ElseToken;
import com.floreysoft.jmte.token.EndToken;
import com.floreysoft.jmte.token.ExpressionToken;
import com.floreysoft.jmte.token.ForEachToken;
import com.floreysoft.jmte.token.IfToken;
import com.floreysoft.jmte.token.PlainTextToken;
import com.floreysoft.jmte.token.StringToken;
import com.floreysoft.jmte.token.Token;
import com.floreysoft.jmte.token.TokenStream;
import com.floreysoft.jmte.util.Util;

public class InterpretedTemplate extends Template {

	protected final String template;
	protected final Engine engine;
	protected final String sourceName;
	protected transient TokenStream tokenStream;
	protected transient StringBuilder output;
	protected transient TemplateContext context;

	public InterpretedTemplate(String template, String sourceName, Engine engine) {
		this.template = template;
		this.engine = engine;
		this.sourceName = sourceName;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getUsedVariables() {
		final Set<String> usedVariables = new TreeSet<String>();

		final Engine engine = new Engine();

		final List<StartEndPair> scan = Util.scan(template, engine
				.getExprStartToken(), engine.getExprEndToken(), true);
		final ScopedMap scopedMap = new ScopedMap(Collections.EMPTY_MAP);

		context = new TemplateContext(template, sourceName, scopedMap, engine);

		engine.addProcessListener(new ProcessListener() {

			@Override
			public void log(Token token, Action action) {
				if (token instanceof ExpressionToken) {
					String variable = ((ExpressionToken) token).getExpression();
					if (!isLocal(variable)) {
						usedVariables.add(variable);
					}
				}
			}

			// a variable is local if any enclosing foreach has it as a step
			// variable
			private boolean isLocal(String variable) {
				for (Token token : context.scopes) {
					if (token instanceof ForEachToken) {
						String foreachVarName = ((ForEachToken) token)
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

		return usedVariables;
	}

	@Override
	public String transform(Map<String, Object> model) {
		final List<StartEndPair> scan = Util.scan(template, engine
				.getExprStartToken(), engine.getExprEndToken(), true);
		context = new TemplateContext(template, sourceName,
				new ScopedMap(model), engine);
		String transformed = transformPure(context, scan);
		String unescaped = Util.NO_QUOTE_MINI_PARSER.unescape(transformed);
		return unescaped;

	}

	protected String transformPure(TemplateContext context,
			List<StartEndPair> scan) {

		tokenStream = new TokenStream(context.sourceName, context.template,
				scan, context.lexer, context.engine.getExprStartToken(),
				context.engine.getExprEndToken());
		output = new StringBuilder(
				(int) (context.template.length() * context.engine
						.getExpansionSizeFactor()));
		tokenStream.nextToken();
		while (tokenStream.currentToken() != null) {
			content(false);
		}
		return output.toString();
	}

	private void foreach(boolean inheritedSkip) {
		ForEachToken feToken = (ForEachToken) tokenStream.currentToken();
		Iterable iterable = (Iterable) feToken.evaluate(context);
		feToken.setIterator(iterable.iterator());
		tokenStream.consume();

		context.model.enterScope();
		context.push(feToken);
		try {

			// in case we do not want to evaluate the body, we just do a quick
			// scan until the matching end
			if (inheritedSkip || !feToken.iterator().hasNext()) {
				Token contentToken;
				while ((contentToken = tokenStream.currentToken()) != null
						&& !(contentToken instanceof EndToken)) {
					content(true);
				}
				if (contentToken == null) {
					engine.getErrorHandler().error("missing-end", feToken);
				} else {
					tokenStream.consume();
					context.notifyProcessListeners(contentToken, Action.END);
				}
			} else {

				while (feToken.iterator().hasNext()) {

					context.model.put(feToken.getVarName(), feToken.advance());
					addSpecialVariables(feToken, context.model);

					// for each iteration we need to rewind to the beginning
					// of the for loop
					tokenStream.rewind(feToken);
					Token contentToken;
					while ((contentToken = tokenStream.currentToken()) != null
							&& !(contentToken instanceof EndToken)) {
						content(false);
					}
					if (contentToken == null) {
						engine.getErrorHandler().error("missing-end", feToken);
					} else {
						tokenStream.consume();
						context.notifyProcessListeners(contentToken, Action.END);
					}
					if (!feToken.isLast()) {
						output.append(feToken.getSeparator());
					}
				}
			}

		} finally {
			context.model.exitScope();
			context.pop();
		}
	}

	private void condition(boolean inheritedSkip) {
		IfToken ifToken = (IfToken) tokenStream.currentToken();
		tokenStream.consume();

		context.push(ifToken);
		try {
			boolean localSkip;
			if (inheritedSkip) {
				localSkip = true;
			} else {
				localSkip = !(Boolean) ifToken.evaluate(context);
			}

			Token contentToken;
			while ((contentToken = tokenStream.currentToken()) != null
					&& !(contentToken instanceof EndToken)
					&& !(contentToken instanceof ElseToken)) {
				content(localSkip);
			}

			if (contentToken instanceof ElseToken) {
				tokenStream.consume();
				// toggle for else branch
				if (!inheritedSkip) {
					localSkip = !localSkip;
				}
				context.notifyProcessListeners(contentToken,
						inheritedSkip ? Action.SKIP : Action.EVAL);

				while ((contentToken = tokenStream.currentToken()) != null
						&& !(contentToken instanceof EndToken)) {
					content(localSkip);
				}

			}

			if (contentToken == null) {
				engine.getErrorHandler().error("missing-end", ifToken);
			} else {
				tokenStream.consume();
				context.notifyProcessListeners(contentToken, Action.END);
			}
		} finally {
			context.pop();
		}
	}

	private void content(boolean skip) {
		Token token = tokenStream.currentToken();
		context.notifyProcessListeners(token, skip ? Action.SKIP : Action.EVAL);
		if (token instanceof PlainTextToken) {
			tokenStream.consume();
			if (!skip) {
				output.append(token.getText());
			}
		} else if (token instanceof StringToken) {
			tokenStream.consume();
			if (!skip) {
				String expanded = (String) token.evaluate(context);
				output.append(expanded);
			}
		} else if (token instanceof ForEachToken) {
			foreach(skip);
		} else if (token instanceof IfToken) {
			condition(skip);
		} else if (token instanceof ElseToken) {
			tokenStream.consume();
			engine.getErrorHandler().error("else-out-of-scope", token);
		} else if (token instanceof EndToken) {
			tokenStream.consume();
			engine.getErrorHandler().error("unmatched-end", token, null);
		}

	}

	@Override
	public String toString() {
		return template;
	}
}
