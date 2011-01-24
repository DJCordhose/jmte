package com.floreysoft.jmte.message;

import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import com.floreysoft.jmte.token.Token;

public class ProductionErrorHandler implements ErrorHandler {

	private static final Logger LOG = Logger
			.getLogger(ProductionErrorHandler.class.getName());

	private Locale locale = new Locale("en");

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(String messageKey, Token token,
			Map<String, Object> parameters) throws ParseException {
		Message message = new ResourceBundleMessage(messageKey).withModel(
				parameters).onToken(token);
		LOG.severe(message.format(locale));
	}
	
	@Override
	public void error(String messageKey, Token token) throws ParseException {
		error(messageKey, token, null);
	}
	
	@Override
	public ErrorHandler setLocale(Locale locale) {
		this.locale = locale;
		return this;
	}


}
