package com.floreysoft.jmte.guts;

import java.util.logging.Logger;

import com.floreysoft.jmte.ErrorHandler;

public class DefaultErrorHandler implements ErrorHandler {

	private static final Logger LOG = Logger
			.getLogger(DefaultErrorHandler.class.getName());

	private Mode mode = Mode.DEVELOPMENT;

	private char[] input;
	private int currentBlockStart;
	private int currentBlockEnd;

	private int getLine() {
		int line = 1;
		for (int i = 0; i < currentBlockStart; i++) {
			if (input[i] == '\n') {
				line++;
			}
		}
		return line;
	}

	int getColumn() {
		int column = 0;
		for (int i = currentBlockStart; i >= 0; i--) {
			if (input[i] == '\n') {
				break;
			} else {
				column++;
			}
		}
		return column;
	}

	public void warning(String message) {
		String completeMessage = completeMessage("Warning", message);
		LOG.warning(completeMessage);
	}

	public void error(String message) throws IllegalArgumentException {
		String completeMessage = completeMessage("Error", message);
		if (mode == Mode.DEVELOPMENT) {
			throw new IllegalArgumentException(completeMessage);
		} else {
			LOG.severe(completeMessage);
		}
	}

	private String completeMessage(String type, String message) {
		String context = String.valueOf(input, currentBlockStart,
				currentBlockEnd - currentBlockStart);
		int line = getLine();
		int column = getColumn();
		String completeMessage = String.format(
				"%s while parsing '%s' at (%d:%d): %s", type, context, line,
				column, message);
		return completeMessage;
	}

	public void setCurrentBlockBounds(int start, int end) {
		this.currentBlockStart = start;
		this.currentBlockEnd = end;
	}

	public void setInput(char[] input) {
		this.input = input;
		setCurrentBlockBounds(0, 0);
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

}
