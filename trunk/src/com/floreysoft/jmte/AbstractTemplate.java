package com.floreysoft.jmte;

import java.util.Map;



public abstract class AbstractTemplate implements Template {

	protected void addSpecialVariables(ForEachToken feToken,
			Map<String, Object> model) {
		String suffix = feToken.getVarName();
		model.put(FIRST_PREFIX + suffix, feToken.isFirst());
		model.put(LAST_PREFIX + suffix, feToken.isLast());
		model.put(EVEN_PREFIX + suffix, feToken.getIndex() % 2 == 0);
		model.put(ODD_PREFIX + suffix, feToken.getIndex() % 2 == 1);
	}
}
