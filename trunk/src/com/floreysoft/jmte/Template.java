package com.floreysoft.jmte;

import java.util.Map;
import java.util.Set;

public interface Template {
	public static final String ODD_PREFIX = "odd_";
	public static final String EVEN_PREFIX = "even_";
	public static final String LAST_PREFIX = "last_";
	public static final String FIRST_PREFIX = "first_";

	/**
	 * Transforms a template into an expanded output using the given model.
	 * 
	 * @param model
	 *            the model used to evaluate expressions inside the template
	 * @return the expanded output
	 */
	public String transform(Map<String, Object> model);

	public Set<String> getUsedVariables();

}
