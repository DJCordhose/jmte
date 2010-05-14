package com.floreysoft.jmte;

public interface ErrorHandler {
	public static enum Mode {
		PRODUCTION, DEVELOPMENT
	};

	public void error(String message);
	public void warning(String message);

	public void setInput(char[] input);
	public void setCurrentBlockBounds(int start, int end);
	public void setMode(Mode development);
}
