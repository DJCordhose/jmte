package com.floreysoft.jmte.sample;

import java.util.HashSet;
import java.util.Set;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.template.AbstractCompiledTemplate;
import com.floreysoft.jmte.token.StringToken;

public class SampleCompiledSequenceTemplate extends AbstractCompiledTemplate {

	public SampleCompiledSequenceTemplate(Engine engine) {
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

		buffer.append("PREFIX");

		buffer.append(new StringToken("address", "address", "NIX", "<h1>",
				"</h1>", "long", "full").evaluate(context));

		buffer.append("SUFFIX");

		return buffer.toString();
	}

}
