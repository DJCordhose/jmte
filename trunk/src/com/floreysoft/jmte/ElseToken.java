package com.floreysoft.jmte;

import java.util.Map;


public class ElseToken extends AbstractToken {
	public static final String ELSE = "else";

	public ElseToken() {
	}
	
	public ElseToken(ElseToken elseToken) {
		super(elseToken);
	}

	@Override
	public Token dup() {
		return new ElseToken(this);
	}
	@Override
	public Object evaluate(Map<String, Object> model, ErrorHandler errorHandler) {
		return "";
	}
	
}
