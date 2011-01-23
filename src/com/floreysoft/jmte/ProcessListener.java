package com.floreysoft.jmte;

public interface ProcessListener {
	public static enum Action {
		/**
		 * Expression being executed
		 */
		EVAL,
		/**
		 * Expression being skipped or condition given for skipping an
		 * expression
		 */
		SKIP
	}

	void log(Token token, Action action);
}
