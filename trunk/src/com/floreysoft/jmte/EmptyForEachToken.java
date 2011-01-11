package com.floreysoft.jmte;

import java.util.Map;

public class EmptyForEachToken extends ExpressionToken {

	private final String varName;

	public EmptyForEachToken(String expression, String varName, String text) {
		super(expression);
		this.text = text;
		this.varName = varName;
	}

	@Override
	public Object evaluate(Engine engine, Map<String, Object> model,
			ErrorHandler errorHandler) {
		return false;
	}

	public String getVarName() {
		return varName;
	}

}
