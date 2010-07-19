package com.floreysoft.jmte;

import java.util.Map;
import java.util.logging.Logger;

public class ProductionErrorHandler implements ErrorHandler {

	private static final Logger LOG = Logger
			.getLogger(ProductionErrorHandler.class.getName());

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(String messageKey, Token token,
			Map<String, Object> parameters) throws ParseException {
		ResourceBundleMessage message = new ResourceBundleMessage(messageKey)
				.onToken(token);
		if (parameters != null) {
			message.withModel(parameters);
		}
		LOG.severe(message.format());
	}
	
	@Override
	public void error(String messageKey, Token token) throws ParseException {
		error(messageKey, token, null);
	}

}
