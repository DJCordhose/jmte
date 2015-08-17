package com.floreysoft.jmte;

import java.util.List;
import com.floreysoft.jmte.token.Token;

/**
 * Adaptor between engine and model.
 * 
 */
public interface ModelAdaptor {
	/**
	 * Gets a value from the model.
	 * 
	 * @param context
	 *            the current context including the scoped model
	 * @param token
	 *            the token that asks for this value (e.g. used for error
	 *            reporting)
	 * @param segments
	 *            an already split version of the expression for faster
	 *            processing
	 * @param expression
	 *            the expression describing the desired value
	 * @return the value
	 */
	public Object getValue(TemplateContext context, Token token,
			List<String> segments, String expression);

    /**
     * Gets an iterable for an arbitrary value.
     *
     * @param value the value to get the iterable for
     * @return a matching iterable
     */
    public Iterable<Object> getIterable(Object value);

    /**
     * Gets the name of the special iterator variable which will be used as an alternative
     * variable name of the iterator in a foreach loop. E.g. something like <em>it</em>.
     *
     * @return name of the special iterator variable
     */
    public String getSpecialIteratorVariable();

}
