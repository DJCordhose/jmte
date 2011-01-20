package com.floreysoft.jmte;


public class EmptyForEachToken extends ExpressionToken {

	private final String varName;

	public EmptyForEachToken(String expression, String varName, String text) {
		super(expression);
		this.text = text;
		this.varName = varName;
	}

	@Override
	public Object evaluate(TemplateContext context) {
		return false;
	}

	public String getVarName() {
		return varName;
	}

}
