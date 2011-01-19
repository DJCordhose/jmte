package com.floreysoft.jmte;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// ${if empty}${address}${else}NIX${end}
public class SampleIfEmptyFalseExpressionCompiledTemplate extends
		AbstractCompiledTemplate {

	public SampleIfEmptyFalseExpressionCompiledTemplate(Engine engine) {
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

		IfToken ifToken = new IfToken("empty", false);

		Boolean condition = (Boolean) ifToken.evaluate(getEngine(), model, getEngine().getErrorHandler());
		
		if (condition) {
			StringToken stringToken = new StringToken("address", "address",
					null, null, null, null, null);
			buffer.append(stringToken.toString());
		} else {
			buffer.append("NIX");
		}
		return buffer.toString();
	}

}
