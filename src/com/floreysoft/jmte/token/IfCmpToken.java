package com.floreysoft.jmte.token;

import java.util.List;

import com.floreysoft.jmte.TemplateContext;


public class IfCmpToken extends IfToken {
	private final AbstractToken operand;

	public IfCmpToken(String expression, AbstractToken operand, boolean negated) {
		super(expression, negated);
		this.operand = operand;
	}

	public IfCmpToken(List<String> segments, String expression, AbstractToken operand, boolean negated) {
		super(segments, expression, negated);
		this.operand = operand;
	}

	public String getOperand(TemplateContext context) {
		return operand == null ? "true" : operand.evaluate(context).toString();
	}

	@Override
	public String getText() {
		if (text == null) {
			text = String
					.format(IF + " %s='%s'", getExpression(), operand == null ? "" : operand.getText());
		}
		return text;
	}

	@Override
	public Object evaluate(TemplateContext context) {
		final Object value = evaluatePlain(context);
		final boolean condition = value != null && getOperand(context).equals(value.toString());
		final Object evaluated = negated ? !condition : condition;
		return evaluated;
	}

}
