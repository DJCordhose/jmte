package com.floreysoft.jmte;

import java.util.HashSet;
import java.util.Set;

// ${<h1>,address(NIX),</h1>;long(full)}
public class SampleComplexExpressionCompiledTemplate extends
		AbstractCompiledTemplate {

	public SampleComplexExpressionCompiledTemplate(Engine engine) {
		super(engine);
	}

	@Override
	public Set<String> getUsedVariables() {
		Set<String> usedVariables = new HashSet<String>();
		usedVariables.add("address");
		return usedVariables;
	}

	@Override
	protected String transformCompiled(ScopedMap model) {
		StringBuilder buffer = new StringBuilder();

		buffer.append(new StringToken("address", "address", "NIX", "<h1>",
				"</h1>", "long", "full").evaluate(getEngine(), model,
				getEngine().getErrorHandler()).toString());

		return buffer.toString();
	}

}
