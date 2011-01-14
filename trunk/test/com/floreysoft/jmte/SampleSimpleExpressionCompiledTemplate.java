package com.floreysoft.jmte;


// ${address}
public class SampleSimpleExpressionCompiledTemplate extends AbstractCompiledTemplate {

	public SampleSimpleExpressionCompiledTemplate(String template, Engine engine) {
		super(template, engine);
		usedVariables.add("address");
	}

	@Override
	protected String transformCompiled(ScopedMap model) {
		StringToken stringToken = new StringToken("address", "address", null, null, null, null,
				null);
		return stringToken.evaluate(engine, model, engine.getErrorHandler())
				.toString();

	}

}
