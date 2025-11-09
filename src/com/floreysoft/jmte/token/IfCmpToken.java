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

    @Override public void setColumn(char[] buffer, int start, int end) {
        super.setColumn(buffer, start, end);
        if (operand != null) {
            operand.setColumn(buffer, start + operand.getColumn() - 1, end);
        }
    }

    @Override public void setLine(char[] buffer, int start, int end) {
        super.setLine(buffer, start, end);
        if (operand != null) {
            operand.setLine(buffer, start + operand.getLine() - 1, end);
        }
    }

}
