package com.floreysoft.jmte;

import java.util.List;
import java.util.Map;


public abstract class AbstractTemplate implements Template {

	protected final String template;
	protected final Engine engine;
	protected transient Lexer lexer = new Lexer();

	public AbstractTemplate(String template, Engine engine) {
		this.template = template;
		this.engine = engine;
	}

	protected void addSpecialVariables(ForEachToken feToken,
			Map<String, Object> model) {
		String suffix = feToken.getVarName();
		model.put(FIRST_PREFIX + suffix, feToken.isFirst());
		model.put(LAST_PREFIX + suffix, feToken.isLast());
		model.put(EVEN_PREFIX + suffix, feToken.getIndex() % 2 == 0);
		model.put(ODD_PREFIX + suffix, feToken.getIndex() % 2 == 1);
	}
	/**
	 * Scans the input and spits out begin/end pairs telling you where
	 * expressions can be found.
	 * 
	 * @param input
	 *            the input
	 * @return the begin/end pairs telling you where expressions can be found
	 */
	protected List<StartEndPair> scan() {
		return Util.scan(template, engine.getExprStartToken(), engine
				.getExprEndToken(), true);
	}

}
