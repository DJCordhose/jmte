package com.floreysoft.jmte;

import java.util.Map;

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
	public Object evaluate(Engine engine, Map<String, Object> model,
			ErrorHandler errorHandler) {
		return "";
	}
}
