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
		SKIP,
		/**
		 * Foreach loop over empty iterable
		 */
		EMPTY_FOREACH,
		/**
		 * Iteration over a loop
		 */
		ITERATE_FOREACH,
		/**
		 * Start of an if expression
		 */
		IF;
	}

	void log(Token token, Action action);
}
