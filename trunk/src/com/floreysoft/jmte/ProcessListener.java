package com.floreysoft.jmte;

public interface ProcessListener {
	public static enum Action {
		/**
		 * Expression being executed. Not reported in compiled mode.
		 */
		EVAL,
		/**
		 * Expression being skipped. Not reported in compiled mode.
		 */
		SKIP,
		/**
		 * End of control structure. Not reported in compiled mode.
		 */
		END
	}

	void log(Token token, Action action);
}
