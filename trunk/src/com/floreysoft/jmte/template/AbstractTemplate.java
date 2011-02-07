package com.floreysoft.jmte.template;

import java.util.Map;
import java.util.Set;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.ModelAdaptor;
import com.floreysoft.jmte.ProcessListener;
import com.floreysoft.jmte.token.ForEachToken;

public abstract class AbstractTemplate implements Template {

	public static final String ODD_PREFIX = "odd_";
	public static final String EVEN_PREFIX = "even_";
	public static final String LAST_PREFIX = "last_";
	public static final String FIRST_PREFIX = "first_";

	protected Engine engine;
	protected String template;
	protected String sourceName;
	protected Set<String> usedVariables;
	
	protected void addSpecialVariables(ForEachToken feToken,
			Map<String, Object> model) {
		String suffix = feToken.getVarName();
		model.put(FIRST_PREFIX + suffix, feToken.isFirst());
		model.put(LAST_PREFIX + suffix, feToken.isLast());
		model.put(EVEN_PREFIX + suffix, feToken.getIndex() % 2 == 0);
		model.put(ODD_PREFIX + suffix, feToken.getIndex() % 2 == 1);
	}

	/**
	 * Transforms a template into an expanded output using the given model.
	 * 
	 * @param model
	 *            the model used to evaluate expressions inside the template
	 * @param modelAdaptor
	 *            adaptor used for this transformation to look up values from
	 *            model
	 * @return the expanded output
	 */
	public abstract String transform(Map<String, Object> model,
			ModelAdaptor modelAdaptor, ProcessListener processListener);

	/**
	 * Transforms a template into an expanded output using the given model.
	 * 
	 * @param model
	 *            the model used to evaluate expressions inside the template
	 * @return the expanded output
	 */
	public String transform(Map<String, Object> model,
			ProcessListener processListener) {
		return transform(model, engine.getModelAdaptor(), processListener);
	}
	
	/**
	 * Transforms a template into an expanded output using the given model.
	 * 
	 * @param model
	 *            the model used to evaluate expressions inside the template
	 * @return the expanded output
	 */
	public String transform(Map<String, Object> model) {
		return transform(model, engine.getModelAdaptor(), null);
	}
	
	public abstract Set<String> getUsedVariables();

}
