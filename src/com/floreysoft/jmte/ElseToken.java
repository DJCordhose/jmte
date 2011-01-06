package com.floreysoft.jmte;

import java.util.Map;

public class ElseToken extends AbstractToken {
	public static final String ELSE = "else";

	protected IfToken ifToken = null;

	@Override
	public String getText() {
		if (text == null) {
			text = ELSE + getIfToken() != null ? "(" + getIfToken().getText()
					+ ")" : "";
		}
		return text;
	}

	@Override
	public Object evaluate(Engine engine, Map<String, Object> model, ErrorHandler errorHandler) {
		Boolean evaluated = !(Boolean) getIfToken().evaluate(engine, model, errorHandler);
		return evaluated;
	}

	public void setIfToken(IfToken ifToken) {
		this.ifToken = ifToken;
	}

	public IfToken getIfToken() {
		if (ifToken == null) {
			throw new IllegalStateException(
					"An else token can only be evaluated using an associated if token - which is missing");
		}
		return ifToken;
	}

}
