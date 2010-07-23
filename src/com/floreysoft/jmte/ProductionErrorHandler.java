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
		Message message = new ResourceBundleMessage(messageKey).withModel(
				parameters).onToken(token);
		LOG.severe(message.format());
	}
	
	@Override
	public void error(String messageKey, Token token) throws ParseException {
		error(messageKey, token, null);
	}

}
