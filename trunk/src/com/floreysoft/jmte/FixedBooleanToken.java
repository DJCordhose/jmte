package com.floreysoft.jmte;

import java.util.Map;

public class FixedBooleanToken extends AbstractToken {
	private final boolean fixedValue;

	public FixedBooleanToken(boolean fixedValue) {
		this.fixedValue = fixedValue;
	}

	public FixedBooleanToken(FixedBooleanToken fixedValueToken) {
		super(fixedValueToken);
		this.fixedValue = fixedValueToken.fixedValue;
	}

	@Override
	public Token dup() {
		return new FixedBooleanToken(this);
	}

	@Override
	public Object evaluate(Map<String, Object> model, ErrorHandler errorHandler) {
		return fixedValue;
	}

}
