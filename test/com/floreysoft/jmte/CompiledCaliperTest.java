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

		public void timeSimpleExpressionReference(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				new Engine().transform("${address}", InterpretedEngineTest.DEFAULT_MODEL);
				;
			}
		}

		public void timeComplexExpressionReference(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				engineTest.ENGINE_WITH_CUSTOM_RENDERERS.transform(
						"${<h1>,address(NIX),</h1>;long(full)}",
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

		public void timePrototypeCompiledComplexExpression(int reps)
				throws Exception {
			for (int i = 0; i < reps; i++) {
				new SampleSimpleExpressionCompiledTemplate(new Engine())
						.transform(InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeIf(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				engineTest.ifEmptyFalseExpression();
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
			for (int i = 0; i < reps; i++) {
				engineTest.newlineForeachSeparator();
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
