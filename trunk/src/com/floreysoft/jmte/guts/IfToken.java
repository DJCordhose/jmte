package com.floreysoft.jmte.guts;

public class IfToken extends Token {

	private boolean condition;
	
	public IfToken(boolean condition) {
		this.condition = condition;
	}

	public void setCondition(boolean condition) {
		this.condition = condition;
	}

	public boolean getCondition() {
		return condition;
	}
}
