package com.floreysoft.jmte.token;

import java.util.Map;

import com.floreysoft.jmte.ErrorHandler;

public class ElseToken extends AbstractToken {

	@Override
	public Object evaluate(Map<String, Object> model, ErrorHandler errorHandler) {
		return "";
	}
}
