package com.floreysoft.jmte;

import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import com.floreysoft.jmte.token.Token;

public class InternalErrorHandler implements ErrorHandler {

	private static final Logger LOG = Logger.getLogger(InternalErrorHandler.class
			.getName());

	private Locale locale = new Locale("en");

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
