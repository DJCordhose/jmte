package com.floreysoft.jmte;

// ${address}
public class SampleSimpleExpressionCompiledTemplate extends
		AbstractCompiledTemplate {

	public SampleSimpleExpressionCompiledTemplate() {
	}

	public SampleSimpleExpressionCompiledTemplate(Engine engine) {
		super(engine);
		usedVariables.add("address");
	}

	@Override
	protected String transformCompiled(TemplateContext context) {
		StringToken stringToken = new StringToken("address", "address", null,
				null, null, null, null);
		return stringToken.evaluate(context).toString();

	}

}
