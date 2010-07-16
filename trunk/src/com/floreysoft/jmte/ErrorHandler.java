package com.floreysoft.jmte;

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
	 * @param message
	 *            the detailed error message
	 * @param token
	 *            the token this error occured on
	 */
	public void error(String message, Token token);
}
