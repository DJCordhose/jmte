package com.floreysoft.jmte;

public interface ProcessListener {
	public static enum Action {
		EVAL, SKIP, EMPTY_FOREACH, ITERATE_FOREACH;
	}
	
	void log(Token token, Action action);
}
