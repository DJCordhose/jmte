package com.floreysoft.jmte;

import java.util.Map;

public class EmptyForEachToken extends AbstractToken {

	public EmptyForEachToken(String text) {
		this.text = text;
	}

	public EmptyForEachToken(EmptyForEachToken fixedValueToken) {
		super(fixedValueToken);
	}

	@Override
	public Token dup() {
		return new EmptyForEachToken(this);
	}

	@Override
	public Object evaluate(Map<String, Object> model, ErrorHandler errorHandler) {
		return false;
	}

}
