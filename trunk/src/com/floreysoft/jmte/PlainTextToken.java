package com.floreysoft.jmte;


public class PlainTextToken extends AbstractToken {
	
	public PlainTextToken(String text) {
		setText(text);
	}

	@Override
	public Object evaluate(TemplateContext context) {
		return getText();
	}
}
