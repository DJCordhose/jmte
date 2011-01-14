package com.floreysoft.jmte;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public abstract class AbstractCompiledTemplate extends AbstractTemplate implements
		Template {
	protected final Set<String> usedVariables = new HashSet<String>();
	
	public AbstractCompiledTemplate(String template, Engine engine) {
		super(template, engine);
	}
	
	@Override
	public Set<String> getUsedVariables() {
		return usedVariables;
	}

	@Override
	public String transform(Map<String, Object> model) {
		ScopedMap scopedMap = new ScopedMap(model);
		String transformed = transformCompiled(scopedMap);
		String unescaped = Util.NO_QUOTE_MINI_PARSER.unescape(transformed);
		return unescaped;

	}

	protected abstract String transformCompiled(ScopedMap scopedMap);

}
