package com.floreysoft.jmte;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class CompiledEngineTest extends AbstractEngineTest {

	protected Engine newEngine() {
		return new Engine().setUseCompilation(true);
	}

	@Test
	public void compiledSimpleSample() throws Exception {
		String input = "${address}";
		String interpretedOutput = newEngine().transform(
				input, DEFAULT_MODEL);
		String compiledOutput = new SampleSimpleExpressionCompiledTemplate(newEngine())
				.transform(DEFAULT_MODEL);
		assertEquals(interpretedOutput, compiledOutput);
	}

	@Test
	public void compiledComplexSample() throws Exception {
		String input = "${<h1>,address(NIX),</h1>;long(full)}";
		String interpretedOutput = ENGINE_WITH_CUSTOM_RENDERERS.transform(
				input, DEFAULT_MODEL);
		String compiledOutput = new SampleComplexExpressionCompiledTemplate(
				ENGINE_WITH_CUSTOM_RENDERERS).transform(DEFAULT_MODEL);
		assertEquals(interpretedOutput, compiledOutput);
	}

	@Test
	public void compiledIfSample() throws Exception {
		String input = "${if empty}${address}${else}NIX${end}";
		String interpretedOutput = newEngine().transform(input, DEFAULT_MODEL);
		String compiledOutput = new SampleIfEmptyFalseExpressionCompiledTemplate(
				newEngine()).transform(DEFAULT_MODEL);
		assertEquals(interpretedOutput, compiledOutput);
	}

	@Test
	public void compiledForeachSample() throws Exception {
		String input = "${ foreach list item \n}${item.property1}${end}";
		String interpretedOutput = newEngine().transform(input, DEFAULT_MODEL);
		String compiledOutput = new SampleNewlineForeachSeparatorCompiledTemplate(
				newEngine()).transform(DEFAULT_MODEL);
		assertEquals(interpretedOutput, compiledOutput);
	}

	public static void main(String[] args) {
		Template template = new Compiler("", null, new Engine()).compile();
		String compiledOutput = template.transform(DEFAULT_MODEL);
		System.out.println(compiledOutput);
	}
}
