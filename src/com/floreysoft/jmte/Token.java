package com.floreysoft.jmte;

/**
 * Internal structure returned by the {@link Lexer} passing parsed information
 * into the {@link Engine}.
 */
public interface Token {

	/**
	 * Returns the text of the token.
	 * 
	 * @return the text
	 */
	public String getText();
}
