package com.floreysoft.jmte;

import java.util.LinkedList;
import java.util.List;

import com.floreysoft.jmte.ProcessListener.Action;
import com.floreysoft.jmte.token.Lexer;
import com.floreysoft.jmte.token.Token;

public class TemplateContext {

	// it is stateless, so we only need one
	public static final Lexer lexer = new Lexer();
	public final ScopedMap model;
	public final List<Token> scopes;
	public final String template;
	public final Engine engine;
	public final String sourceName;

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

	public void notifyProcessListeners(Token token, Action action) {
		engine.notifyProcessListeners(token, action);
	}
}
