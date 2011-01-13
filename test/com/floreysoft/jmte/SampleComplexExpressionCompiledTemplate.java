package com.floreysoft.jmte;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// ${<h1>,address(NIX),</h1>;long(full)}
public class SampleComplexExpressionCompiledTemplate extends
		AbstractCompiledTemplate {

	public SampleComplexExpressionCompiledTemplate(String template,
			Engine engine) {
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

		StringToken stringToken = new StringToken("address", "address", "NIX",
				"<h1>", "</h1>", "long", "full");
		return stringToken.evaluate(engine, model, engine.getErrorHandler())
				.toString();

	}

}
