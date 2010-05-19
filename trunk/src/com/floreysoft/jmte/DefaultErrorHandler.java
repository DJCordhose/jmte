package com.floreysoft.jmte;

import java.util.logging.Logger;

/**
 * Default error handler having {@link Mode two modes} of handling.
 */
public class DefaultErrorHandler implements ErrorHandler {

	/**
	 * Mode to decide what to do with error messages.
	 */
	public static enum Mode {
		/**
		 * This mode tries to handle errors as graceful as possible while
		 * logging them.
		 */
		PRODUCTION,
		/**
		 * In this mode each error leads to an exception.
		 */
		DEVELOPMENT
	};

	private static final Logger LOG = Logger
			.getLogger(DefaultErrorHandler.class.getName());
	private Mode mode;

	private char[] input;
	private int currentBlockStart;
	private int currentBlockEnd;

	/**
	 * Creates an error handler of the desired mode
	 * 
	 * @param mode
	 *            the desired mode
	 */
	public DefaultErrorHandler(Mode mode) {
		super();
		this.mode = mode;
	}

	/**
	 * Creates an error handler in development mode.
	 */
	public DefaultErrorHandler() {
		this(Mode.DEVELOPMENT);
	}

	/**
	 * {@inheritDoc}
	 */
	public void error(String message, char[] template, int start, int end)
			throws IllegalArgumentException {
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
