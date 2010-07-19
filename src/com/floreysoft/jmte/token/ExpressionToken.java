package com.floreysoft.jmte.token;

public class ExpressionToken extends DefaultToken {
	protected String value;

	public ExpressionToken(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
