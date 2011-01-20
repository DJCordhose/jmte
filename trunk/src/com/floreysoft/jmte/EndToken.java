package com.floreysoft.jmte;


public class EndToken extends AbstractToken {
	public static final String END = "end";

	@Override
	public String getText() {
		if (text == null) {
			text = END;
		}
		return text;
	}

	@Override
	public Object evaluate(TemplateContext context) {
		return "";
	}
}
