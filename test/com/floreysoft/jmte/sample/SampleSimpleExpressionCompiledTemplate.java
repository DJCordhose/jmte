package com.floreysoft.jmte.sample;

import java.util.TreeSet;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.template.AbstractCompiledTemplate;
import com.floreysoft.jmte.token.StringToken;

// ${address}
public class SampleSimpleExpressionCompiledTemplate extends
		AbstractCompiledTemplate {

	public SampleSimpleExpressionCompiledTemplate() {
	}

	public SampleSimpleExpressionCompiledTemplate(Engine engine) {
		super(engine);
		usedVariables = new TreeSet<String>();
		usedVariables.add("address");
	}

	@Override
	protected String transformCompiled(TemplateContext context) {
		StringBuilder buffer = new StringBuilder();

		buffer.append(new StringToken("address", "address", null, null, null,
				null, null).evaluate(context));

		return buffer.toString();
	}

}
