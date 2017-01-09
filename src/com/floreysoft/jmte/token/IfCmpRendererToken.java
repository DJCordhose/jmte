package com.floreysoft.jmte.token;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.TemplateContext;

public class IfCmpRendererToken extends IfCmpToken {
	private final String rendererName;
	private final String parameters;

	public IfCmpRendererToken(String expression, String operand, boolean negated, String rendererName, String parameters) {
		super(expression, operand, negated);
		this.rendererName = rendererName;
		this.parameters = parameters;
	}

    private NamedRenderer resolveNamedRenderer(TemplateContext context) {
        if (this.rendererName != null && !this.rendererName.equals("")) {
            return context.resolveNamedRenderer(rendererName);
        } else {
            return null;
        }
    }

    @Override
    public Object evaluate(TemplateContext context) {
        final Object value = evaluatePlain(context);
        final String string;
        final NamedRenderer rendererForName = this.resolveNamedRenderer(context);
        if (rendererForName != null) {
            string = rendererForName.render(value, this.parameters, context.locale, context.model);
        } else {
            string = value == null ? null : value.toString();
        }
        final boolean condition = getOperand().equals(string);
        final Object evaluated = negated ? !condition : condition;
        return evaluated;
    }


}
