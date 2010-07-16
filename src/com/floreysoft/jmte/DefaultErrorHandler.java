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
	public void error(String message, Token token)
			throws IllegalArgumentException {
		String completeMessage = completeMessage("Error", message, token);
		if (mode == Mode.DEVELOPMENT) {
			throw new IllegalArgumentException(completeMessage);
		} else {
			LOG.severe(completeMessage);
		}
	}

	private String completeMessage(String type, String message, Token token) {
		String context = token.getText();
		int line = token.getLine();
		int column = token.getColumn();
		String sourceName = token.getSourceName() != null ? token.getSourceName() : "";
		String completeMessage = String.format(
				"%s while parsing '%s' at %s(%d:%d): %s", type, context,
				sourceName, line, column, message);
		return completeMessage;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}
}
