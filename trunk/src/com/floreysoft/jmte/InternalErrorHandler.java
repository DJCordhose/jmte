package com.floreysoft.jmte;

import java.util.Map;
import java.util.logging.Logger;

public class InternalErrorHandler implements ErrorHandler {

	private static final Logger LOG = Logger.getLogger(InternalErrorHandler.class
			.getName());

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(String messageKey, Token token,
			Map<String, Object> parameters) throws ParseException {
		LOG.warning(String.format(
				"Internal error '%s' on '%s'(%d:%d) with parameters %s",
				messageKey, token.getText(), token.getLine(),
				token.getColumn(), parameters != null ? parameters.toString()
						: ""));
	}
}
