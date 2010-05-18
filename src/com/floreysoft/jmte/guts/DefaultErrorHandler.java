package com.floreysoft.jmte.guts;

import java.util.logging.Logger;

import com.floreysoft.jmte.ErrorHandler;

public class DefaultErrorHandler implements ErrorHandler {
	public static enum Mode {
		PRODUCTION, DEVELOPMENT
	};

	private static final Logger LOG = Logger
			.getLogger(DefaultErrorHandler.class.getName());
	private Mode mode;

	private char[] input;
	private int currentBlockStart;
	private int currentBlockEnd;


	public DefaultErrorHandler(Mode mode) {
		super();
		this.mode = mode;
	}

	public DefaultErrorHandler() {
		this(Mode.DEVELOPMENT);
	}

	public void error(String message, char[] template, int start, int end) throws IllegalArgumentException {
		this.input = template;
		this.currentBlockStart = start;
		this.currentBlockEnd = end;
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
	

	protected int getLine() {
		int line = 1;
		for (int i = 0; i < currentBlockStart; i++) {
			if (input[i] == '\n') {
				line++;
			}
		}
		return line;
	}

	protected int getColumn() {
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

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

}
