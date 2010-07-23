package com.floreysoft.jmte;

import java.util.Map;
import java.util.logging.Logger;

public class DefaultErrorHandler implements ErrorHandler {
	private static final Logger LOG = Logger
			.getLogger(DefaultErrorHandler.class.getName());

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(String messageKey, Token token,
			Map<String, Object> parameters) throws ParseException {
		Message message = new ResourceBundleMessage(messageKey).withModel(
				parameters).onToken(token);
//		LOG.severe(message.format());
		throw new ParseException(message);
	}

	@Override
	public void error(String messageKey, Token token) throws ParseException {
		error(messageKey, token, null);
	}
}
