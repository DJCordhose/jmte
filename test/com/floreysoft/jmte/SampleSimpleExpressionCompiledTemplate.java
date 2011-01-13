package com.floreysoft.jmte;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// ${address}
public class SampleSimpleExpressionCompiledTemplate extends AbstractCompiledTemplate {

	public SampleSimpleExpressionCompiledTemplate(String template, Engine engine) {
		super(template, engine);
	}

	@Override
	public Set<String> getUsedVariables() {
		Set<String> usedVariables = new HashSet<String>();
		usedVariables.add("address");
		return usedVariables;
	}

	@Override
	public String transform(Map<String, Object> model) {
		StringToken stringToken = new StringToken("address", "address", null, null, null, null,
				null);
		return stringToken.evaluate(engine, model, engine.getErrorHandler())
				.toString();

	}

}
