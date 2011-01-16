package com.floreysoft.jmte;


// ${address}
public class SampleSimpleExpressionCompiledTemplate extends AbstractCompiledTemplate {

	public SampleSimpleExpressionCompiledTemplate() {
	}

	public SampleSimpleExpressionCompiledTemplate(Engine engine) {
		super(engine);
		usedVariables.add("address");
	}

	
	@Override
	protected String transformCompiled(ScopedMap model) {
		StringToken stringToken = new StringToken("address", "address", null, null, null, null,
				null);
		return stringToken.evaluate(getEngine(), model, getEngine().getErrorHandler())
				.toString();

	}

}
