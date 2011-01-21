package com.floreysoft.jmte;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// ${if !bean.trueCond}${address}${else}NIX${end}
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
	protected String transformCompiled(TemplateContext context) {
		StringBuilder buffer = new StringBuilder();

		IfToken ifToken = new IfToken(Arrays.asList(new String[] { "bean",
				"trueCond" }), "bean.trueCond", true);

		context.push(ifToken);
		try {
			if ((Boolean) ifToken.evaluate(context)) {
				StringToken stringToken = new StringToken("address", "address",
						null, null, null, null, null);
				buffer.append(stringToken.evaluate(context).toString());
			} else {
				buffer.append("NIX");
			}
		} finally {
			context.pop();
		}
		return buffer.toString();
	}

}
