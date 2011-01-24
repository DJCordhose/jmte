package com.floreysoft.jmte;

import com.google.caliper.SimpleBenchmark;

public class CompiledCaliperTest {

	/**
	 * Tests a selection of scripts supposed to be the most frequently used
	 * 
	 * @author olli
	 * 
	 */
	public static class PortfolioBenchmark extends SimpleBenchmark {
		InterpretedEngineTest engineTest = new InterpretedEngineTest();
		CompiledEngineTest compiledEngineTest = new CompiledEngineTest();

		public void timeSimpleExpressionReference(int reps) throws Exception {
			Engine engine = engineTest.newEngine();
			for (int i = 0; i < reps; i++) {
				engine.transform("${address}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeSimpleExpressionCompiled(int reps) throws Exception {
			Engine engine = compiledEngineTest.newEngine();
			for (int i = 0; i < reps; i++) {
				engine.transform("${address}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timePrototypeCompiledSimpleExpression(int reps)
				throws Exception {
			for (int i = 0; i < reps; i++) {
				new SampleSimpleExpressionCompiledTemplate(new Engine())
						.transform(InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeComplexExpressionReference(int reps) throws Exception {
			Engine engine = engineTest.newEngine();
			for (int i = 0; i < reps; i++) {
				engine.transform("${<h1>,address(NIX),</h1>;long(full)}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeComplexExpressionCompiled(int reps) throws Exception {
			Engine engine = compiledEngineTest.newEngine();
			for (int i = 0; i < reps; i++) {
				engine.transform("${<h1>,address(NIX),</h1>;long(full)}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timePrototypeCompiledComplexExpression(int reps)
				throws Exception {
			for (int i = 0; i < reps; i++) {
				new SampleSimpleExpressionCompiledTemplate(new Engine())
						.transform(InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeIf(int reps) throws Exception {
			Engine engine = engineTest.newEngine();
			for (int i = 0; i < reps; i++) {
				engine.transform("${if empty}${address}${else}NIX${end}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeIfCompiled(int reps) throws Exception {
			Engine engine = compiledEngineTest.newEngine();
			for (int i = 0; i < reps; i++) {
				engine.transform("${if empty}${address}${else}NIX${end}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timePrototypeCompiledIfExpression(int reps)
				throws Exception {
			for (int i = 0; i < reps; i++) {
				new SampleIfEmptyFalseExpressionCompiledTemplate(new Engine())
						.transform(InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeForeach(int reps) throws Exception {
			Engine engine = engineTest.newEngine();
			for (int i = 0; i < reps; i++) {
				engine.transform(
						"${ foreach list item \n}${item.property1}${end}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeForeachCompiled(int reps) throws Exception {
			Engine engine = compiledEngineTest.newEngine();
			for (int i = 0; i < reps; i++) {
				engine.transform(
						"${ foreach list item \n}${item.property1}${end}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timePrototypeCompiledForeachExpression(int reps)
				throws Exception {
			for (int i = 0; i < reps; i++) {
				new SampleNewlineForeachSeparatorCompiledTemplate(new Engine())
						.transform(InterpretedEngineTest.DEFAULT_MODEL);
			}
		}
	}

}
