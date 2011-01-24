package com.floreysoft.jmte;

import java.util.Locale;
import java.util.Map;

import com.floreysoft.jmte.token.Token;

/**
 * Interface used to handle errors while expanding a template. This interface is
 * called by the {@link Engine} and by the {@link Lexer}.
 * 
 * @see Engine
 * @see Lexer
 */
public interface ErrorHandler {
	
	/**
	 * Handles an error while interpreting a template in an appropriate way.
	 * This might contain logging the error or even throwing an exception.
	 * 
	 * @param messageKey
	 *            key for the error message
	 * @param token
	 *            the token this error occurred on
	 * @param parameters
	 *            additional parameters to be filled into message or <code>null</<code> if you do not have additional parameters
	 */
	public void error(String messageKey, Token token,
			Map<String, Object> parameters) throws ParseException;
	public void error(String messageKey, Token token) throws ParseException;
	public ErrorHandler setLocale(Locale locale);

}
