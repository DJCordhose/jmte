package com.floreysoft.jmte;



public class InvalidToken extends AbstractToken {
	public Object evaluate(TemplateContext context) {
		context.engine.getErrorHandler().error("invalid-expression", this);
		return "";
	}

	public Token dup() {
		return this;
	}

}
