package com.floreysoft.jmte;

import java.util.Map;

import com.floreysoft.jmte.token.AbstractToken;

/**
 * Interface for parsing script sections which are included inside the special
 * characters (${ and } by default).
 * 
 * You can implement it in any way you like and replace it in the {@link Engine}
 * using {@link Engine#setLexer(Lexer)}.
 * 
 * It might be a good idea to inherit from {@link DefaultLexer} and add new
 * functionality. Possible extensions might be complex expressions in if
 * conditions or includes from other files.
 * 
 */
public interface Lexer {

	/**
	 * Scans and expands the input to find out what kind of token a certain
	 * script section represents.
	 * 
	 * @param template
	 *            the complete string template
	 * @param start
	 *            the position in the complete template where the script section
	 *            to be parsed <em>starts</em>
	 * @param end
	 *            the position in the complete template where the script section
	 *            to be parsed <em>ends</em>
	 * @param model
	 *            the complete model possibly containing temporary data
	 * @param skipMode
	 *            <code>true</code> if no expressions shall be evaluated while
	 *            parsing - this is both for performance and even safety as in
	 *            this mode some variables might remain unset which otherwise 
	 *            would have to be set in non-skip mode
	 * @param errorHandler
	 *            handler to issue error messages and warnings
	 * @return implementation of {@link AbstractToken} to indicate the type of section
	 *         that was parsed
	 */
	Token nextToken(char[] template, int start, int end,
			Map<String, Object> model, boolean skipMode,
			ErrorHandler errorHandler);
}
