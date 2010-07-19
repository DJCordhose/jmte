package com.floreysoft.jmte.token;

import java.util.Map;

import com.floreysoft.jmte.ErrorHandler;

public class InvalidToken extends AbstractToken {

	public String evaluate(Map<String, Object> model, ErrorHandler errorHandler) {
		errorHandler.error("invalid-expression", this);
		return "";
	}

}
