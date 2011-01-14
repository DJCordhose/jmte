package com.floreysoft.jmte;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// ${if empty}${address}${else}NIX${end}
public class SampleIfEmptyFalseExpressionCompiledTemplate extends
		AbstractCompiledTemplate {

	public SampleIfEmptyFalseExpressionCompiledTemplate(String template,
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
	protected String transformCompiled(ScopedMap model) {
		IfToken ifToken = new IfToken("empty", false);

		if ((Boolean) ifToken.evaluate(engine, model, engine.getErrorHandler())) {
			StringToken stringToken = new StringToken("address", "address",
					null, null, null, null, null);
			return stringToken.toString();
		} else {
			return "NIX";
		}
	}

}
