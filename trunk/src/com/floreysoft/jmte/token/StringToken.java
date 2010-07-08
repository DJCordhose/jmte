package com.floreysoft.jmte.token;

public class StringToken extends AbstractToken {
	protected String value;

	public StringToken(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
