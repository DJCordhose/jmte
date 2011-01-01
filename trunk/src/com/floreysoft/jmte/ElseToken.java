package com.floreysoft.jmte;

import java.util.Map;

public class ElseToken extends AbstractToken {
	public static final String ELSE = "else";

	protected IfToken ifToken = null;
	protected transient Object evaluated = null;

	public ElseToken() {
	}

	public ElseToken(ElseToken elseToken) {
		super(elseToken);
		this.ifToken = elseToken.ifToken;
	}

	@Override
	public String getText() {
		if (text == null) {
			text = ELSE + getIfToken() != null ? "(" + getIfToken().getText()
					+ ")" : "";
		}
		return text;
	}

	@Override
	public Token dup() {
		return new ElseToken(this);
	}

	@Override
	public Object evaluate(Map<String, Object> model, ErrorHandler errorHandler) {
		if (evaluated != null) {
			return evaluated;
		}
		evaluated = !(Boolean) getIfToken().evaluate(model, errorHandler);
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
