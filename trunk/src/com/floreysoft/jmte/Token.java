package com.floreysoft.jmte;

import java.util.Map;

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

	public int getLine();

	public int getColumn();

	public String getSourceName();
	
	public Object evaluate(Engine engine, Map<String, Object> model, ErrorHandler errorHandler);
	
	public int getTokenIndex();
}
