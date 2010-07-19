package com.floreysoft.jmte;

import java.util.Map;


public class InvalidToken extends AbstractToken {

	public String evaluate(Map<String, Object> model, ErrorHandler errorHandler) {
		errorHandler.error("invalid-expression", this);
		return "";
	}

	public Token dup() {
		return this;
	}

}
