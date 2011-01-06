package com.floreysoft.jmte;

import java.util.Map;

public class IfCmpToken extends IfToken {
	private final String operand;
	
	public IfCmpToken(String expression, String operand, boolean negated) {
		super(expression, negated);
		this.operand = operand;
	}
	public String getOperand() {
		return operand;
	}

	@Override
	public String getText() {
		if (text == null) {
			text = String.format(IF+" %s='%s'", getExpression(), getOperand());
		}
		return text;
	}

	@Override
	public Object evaluate(Engine engine, Map<String, Object> model, ErrorHandler errorHandler) {

		final Object value = traverse(getSegments(), model, errorHandler);
		final boolean condition = getOperand().equals(value);

		final Object evaluated = negated ? !condition : condition;
		return evaluated;
	}

}
