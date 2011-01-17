package com.floreysoft.jmte;

import java.util.Map;

public class PlainTextToken extends AbstractToken {
	
	public PlainTextToken(String text) {
		setText(text);
	}

	@Override
	public Object evaluate(Engine engine, Map<String, Object> model,
			ErrorHandler errorHandler) {
		return getText();
	}
}
