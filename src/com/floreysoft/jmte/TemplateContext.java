package com.floreysoft.jmte;

import java.util.LinkedList;
import java.util.List;

public class TemplateContext {

	// it is stateless, so we only need one
	protected static final Lexer lexer = new Lexer();
	protected final ScopedMap model;
	protected final List<Token> scopes;
	protected final String template;
	protected final Engine engine;
	protected final String sourceName;

	public TemplateContext(String template, String sourceName, ScopedMap model,
			Engine engine) {
		this.model = model;
		this.template = template;
		this.engine = engine;
		this.scopes = new LinkedList<Token>();
		this.sourceName = sourceName;
	}

	public ScopedMap getModel() {
		return model;
	}

	public List<Token> getScopes() {
		return scopes;
	}

	public String getTemplate() {
		return template;
	}

	public Engine getEngine() {
		return engine;
	}

	public Lexer getLexer() {
		return lexer;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void push(Token token) {
		scopes.add(token);
	}

	public Token pop() {
		if (scopes.isEmpty()) {
			return null;
		} else {
			Token token = scopes.remove(scopes.size() - 1);
			return token;
		}
	}

	public Token peek() {
		if (scopes.isEmpty()) {
			return null;
		} else {
			Token token = scopes.get(scopes.size() - 1);
			return token;
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Token> T peek(Class<T> type) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			Token token = scopes.get(i);
			if (token.getClass().equals(type)) {
				return (T) token;
			}
		}
		return null;
	}

	/**
	 * If anywhere in the stack trace there is a negative condition, all the
	 * inner parts must be skipped.
	 */
	public boolean isSkipMode() {
		for (Token token : scopes) {
			if (token instanceof IfToken || token instanceof ElseToken
					|| token instanceof EmptyForEachToken) {
				boolean condition = (Boolean) token.evaluate(this);
				if (!condition) {
					engine.notifyListeners(token, ProcessListener.Action.SKIP);
					return true;
				}
			}
		}
		return false;
	}

}
