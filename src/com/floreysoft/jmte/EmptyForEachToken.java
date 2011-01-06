package com.floreysoft.jmte;

import java.util.Map;

public class EmptyForEachToken extends AbstractToken {

	public EmptyForEachToken(String text) {
		this.text = text;
	}

	@Override
	public Object evaluate(Engine engine, Map<String, Object> model, ErrorHandler errorHandler) {
		return false;
	}

}
