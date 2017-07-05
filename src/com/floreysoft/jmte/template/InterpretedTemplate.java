package com.floreysoft.jmte.template;

import java.util.*;
import com.floreysoft.jmte.DefaultModelAdaptor;
import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.ModelAdaptor;
import com.floreysoft.jmte.ProcessListener;
import com.floreysoft.jmte.ProcessListener.Action;
import com.floreysoft.jmte.ScopedMap;
import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.message.ErrorEntry;
import com.floreysoft.jmte.message.JournalingErrorHandler;
import com.floreysoft.jmte.token.ElseToken;
import com.floreysoft.jmte.token.EndToken;
import com.floreysoft.jmte.token.ExpressionToken;
import com.floreysoft.jmte.token.ForEachToken;
import com.floreysoft.jmte.token.IfToken;
import com.floreysoft.jmte.token.InvalidToken;
import com.floreysoft.jmte.token.PlainTextToken;
import com.floreysoft.jmte.token.StringToken;
import com.floreysoft.jmte.token.Token;
import com.floreysoft.jmte.token.TokenStream;

import static com.floreysoft.jmte.message.ErrorMessage.*;

public class InterpretedTemplate extends AbstractTemplate {

	protected final TokenStream tokenStream;
	protected transient TemplateContext context;

	// a variable is local if any enclosing foreach has it as a step
	// variable
	private static boolean isLocal(TemplateContext context, String variable) {
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

	public InterpretedTemplate(String template, String sourceName, Engine engine) {
		this.template = template;
		this.engine = engine;
		this.sourceName = sourceName;
		tokenStream = new TokenStream(sourceName, template, engine
				.getExprStartToken(), engine.getExprEndToken());
		tokenStream.prefill();
	}

	private StringBuilder getOutput() {
		return this.context.output;
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized Set<String> getUsedVariables() {
		if (this.usedVariables != null) {
			return this.usedVariables;
		}

		this.usedVariables = new TreeSet<String>();
		final List<VariableDescription> variableDescriptions = this.getUsedVariableDescriptions();
		for (VariableDescription variableDescription : variableDescriptions) {
			this.usedVariables.add(variableDescription.name);
		}
		return this.usedVariables;
	}

    @Override
    public List<ErrorEntry> getStaticErrors() {
        final Engine engine = new Engine();
        final JournalingErrorHandler errorHandler = new JournalingErrorHandler();
        engine.setErrorHandler(errorHandler);
        final ScopedMap scopedMap = new ScopedMap(Collections.EMPTY_MAP);
        final Locale locale = Locale.getDefault();
        this.context = new TemplateContext(this.template, locale, this.sourceName, scopedMap,
                new DefaultModelAdaptor(), engine, errorHandler, null);
        transformPure(this.context);

        final List<ErrorEntry> staticErrors = new ArrayList<>();
        for (ErrorEntry entry: errorHandler.entries) {
            if (entry.errorMessage.isStatic) {
                staticErrors.add(entry);
            }
        }
        return staticErrors;
    }

    @Override
	public List<VariableDescription> getUsedVariableDescriptions() {
		final List<VariableDescription> variableDescriptions = new ArrayList<>();
		final Engine engine = new Engine();
		engine.setErrorHandler(new JournalingErrorHandler());
		final ScopedMap scopedMap = new ScopedMap(Collections.EMPTY_MAP);

		final ProcessListener processListener = new ProcessListener() {

			@Override
			public void log(TemplateContext context, Token token, Action action) {
				if (token instanceof ExpressionToken) {
					final String name = ((ExpressionToken) token).getExpression();
					String renderer = null;
					String pattern = null;
					VariableDescription.Context variableContext = null;
					if (token instanceof StringToken) {
						renderer = ((StringToken) token).getRendererName();
						pattern = ((StringToken) token).getParameters();
						variableContext = VariableDescription.Context.TEXT;
					} else if (token instanceof IfToken) {
						variableContext = VariableDescription.Context.IF;
					} else if (token instanceof ForEachToken) {
						variableContext = VariableDescription.Context.FOR_EACH;
					}
					if (!InterpretedTemplate.isLocal(context, name)) {
						variableDescriptions.add(new VariableDescription(name, renderer, pattern, variableContext));
					}
				}
			}

		};
		final Locale locale = Locale.getDefault();
		this.context = new TemplateContext(this.template, locale, this.sourceName, scopedMap,
				new DefaultModelAdaptor(), engine, engine.getErrorHandler(), processListener);
		transformPure(this.context);

		return variableDescriptions;
	}

	@Override
	public synchronized String transform(Map<String, Object> model, Locale locale,
			ModelAdaptor modelAdaptor, ProcessListener processListener) {
		try {
			context = new TemplateContext(template, locale, sourceName, new ScopedMap(
					model), modelAdaptor, engine, engine.getErrorHandler(), processListener);
			String transformed = transformPure(context);
			return transformed;
		} finally {
			context = null;
		}
	}

	protected String transformPure(TemplateContext context) {
		tokenStream.reset();
		tokenStream.nextToken();
		while (tokenStream.currentToken() != null) {
			content(false);
		}
		return getOutput().toString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void foreach(boolean inheritedSkip) {
		ForEachToken feToken = (ForEachToken) tokenStream.currentToken();
		if (feToken.getVarName() == ForEachToken.UNDEFINED_VARNAME) {
            this.context.engine.getErrorHandler().error(FOR_EACH_UNDEFINED_VARNAME, feToken);
            this.context.engine.getOutputAppender().append(this.getOutput(), "", feToken);
        }
		Iterable iterable = (Iterable) feToken.evaluate(context);
		// begin a fresh iteration with a reset index
		feToken.setIterator(iterable.iterator());
		feToken.resetIndex();
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
					this.context.engine.getErrorHandler().error(MISSING_END, feToken);
					this.context.engine.getOutputAppender().append(this.getOutput(), "", feToken);
				} else {
					tokenStream.consume();
					context.notifyProcessListener(contentToken, Action.END);
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
						this.context.engine.getErrorHandler().error(MISSING_END, feToken);
                        this.context.engine.getOutputAppender().append(this.getOutput(), "", feToken);
                    } else {
						tokenStream.consume();
						context.notifyProcessListener(contentToken, Action.END);
					}
					if (!feToken.isLast()) {
						this.context.engine.getOutputAppender().append(this.getOutput(), feToken.getSeparator(), feToken);
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
				context.notifyProcessListener(contentToken,
						inheritedSkip ? Action.SKIP : Action.EVAL);

				while ((contentToken = tokenStream.currentToken()) != null
						&& !(contentToken instanceof EndToken)) {
					content(localSkip);
				}

			}

			if (contentToken == null) {
				this.context.engine.getErrorHandler().error(MISSING_END, ifToken);
                this.context.engine.getOutputAppender().append(this.getOutput(), null, ifToken);
            } else {
				tokenStream.consume();
				context.notifyProcessListener(contentToken, Action.END);
			}
		} finally {
			context.pop();
		}
	}

	private void content(boolean skip) {
		Token token = tokenStream.currentToken();
		context.notifyProcessListener(token, skip ? Action.SKIP : Action.EVAL);
		if (token instanceof PlainTextToken) {
			tokenStream.consume();
			if (!skip) {
				this.context.engine.getOutputAppender().append(this.getOutput(), token.getText(), token);
			}
		} else if (token instanceof StringToken) {
			tokenStream.consume();
			if (!skip) {
				String expanded = (String) token.evaluate(context);
				this.context.engine.getOutputAppender().append(this.getOutput(), expanded, token);
			}
		} else if (token instanceof ForEachToken) {
			foreach(skip);
		} else if (token instanceof IfToken) {
			condition(skip);
		} else if (token instanceof ElseToken) {
			tokenStream.consume();
			this.context.engine.getErrorHandler().error(ELSE_OUT_OF_SCOPE, token);
			this.context.engine.getOutputAppender().append(this.getOutput(), "", token);
		} else if (token instanceof EndToken) {
			tokenStream.consume();
			this.context.engine.getErrorHandler().error(UNMATCHED_END, token);
			this.context.engine.getOutputAppender().append(this.getOutput(), "", token);
		} else if (token instanceof InvalidToken) {
			tokenStream.consume();
			this.context.engine.getErrorHandler().error(INVALID_EXPRESSION, token);
			this.context.engine.getOutputAppender().append(this.getOutput(), "", token);
		} else {
			tokenStream.consume();
			// what ever else there may be, we just evaluate it
			String evaluated = (String) token.evaluate(context);
			this.context.engine.getOutputAppender().append(this.getOutput(), evaluated, token);
		}

	}

	@Override
	public String toString() {
		return template;
	}
}
