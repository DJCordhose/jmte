package com.floreysoft.jmte.guts;

public class StringToken implements Token {
	protected String value;

	public StringToken(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
