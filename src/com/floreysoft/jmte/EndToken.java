package com.floreysoft.jmte;

import java.util.Map;


public class EndToken extends AbstractToken {

	@Override
	public Object evaluate(Map<String, Object> model, ErrorHandler errorHandler) {
		return "";
	}
}
