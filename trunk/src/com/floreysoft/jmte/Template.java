package com.floreysoft.jmte;

import java.util.Map;
import java.util.Set;

public interface Template {
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
