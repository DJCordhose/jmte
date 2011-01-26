package com.floreysoft.jmte.sample;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.template.AbstractCompiledTemplate;
import com.floreysoft.jmte.token.IfCmpToken;
import com.floreysoft.jmte.token.StringToken;

// ${if address='Fillbert'}${address}${else}NIX${end}
public class SampleIfCmpCompiledTemplate extends AbstractCompiledTemplate {

	public SampleIfCmpCompiledTemplate(Engine engine) {
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

		IfCmpToken token1 = new IfCmpToken(Arrays
				.asList(new String[] { "address" }), "address", "Filbert",
				false);

		context.push(token1);
		try {
			if ((Boolean) token1.evaluate(context)) {
				buffer.append(new StringToken("address", "address", null, null,
						null, null, null).evaluate(context));
			} else {
				buffer.append("NIX");
			}
		} finally {
			context.pop();
		}
		return buffer.toString();
	}

}
