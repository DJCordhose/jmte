package com.floreysoft.jmte;

import com.floreysoft.jmte.sample.SampleComplexExpressionCompiledTemplate;
import com.floreysoft.jmte.sample.SampleIfEmptyFalseExpressionCompiledTemplate;
import com.floreysoft.jmte.sample.SampleNewlineForeachSeparatorCompiledTemplate;
import com.floreysoft.jmte.sample.SampleSimpleExpressionCompiledTemplate;
import com.google.caliper.SimpleBenchmark;

public class CompiledCaliperTest {
	static final Engine cachingEngine = new Engine();
	static {
		cachingEngine.setEnabledInterpretedTemplateCache(true);
	}
	static final Engine referenceEngine = new Engine();
	static {
		referenceEngine.setEnabledInterpretedTemplateCache(false);
	}
	static final Engine compilingEngine = new Engine();
	static {
		compilingEngine.setUseCompilation(true);
	}

	static final SampleSimpleExpressionCompiledTemplate simpleExpressiontemplate = new SampleSimpleExpressionCompiledTemplate(
			new Engine());
	static final SampleNewlineForeachSeparatorCompiledTemplate foreachTemplate = new SampleNewlineForeachSeparatorCompiledTemplate(
			new Engine());
	static final SampleIfEmptyFalseExpressionCompiledTemplate ifTemplate = new SampleIfEmptyFalseExpressionCompiledTemplate(
			new Engine());
	static final SampleComplexExpressionCompiledTemplate complexTemplate = new SampleComplexExpressionCompiledTemplate(
			new Engine());

	/**
	 * Tests a selection of scripts supposed to be the most frequently used
	 * 
	 * @author olli
	 * 
	 */
	public static class PortfolioBenchmark extends SimpleBenchmark {
		public void timeSimpleExpressionReference(int reps) throws Exception {
			Engine engine = referenceEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform("${address}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeSimpleExpressionCached(int reps) throws Exception {
			Engine engine = cachingEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform("${address}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeSimpleExpressionCompiled(int reps) throws Exception {
			Engine engine = compilingEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform("${address}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timePrototypeCompiledSimpleExpression(int reps)
				throws Exception {
			for (int i = 0; i < reps; i++) {
				simpleExpressiontemplate.transform(
						InterpretedEngineTest.DEFAULT_MODEL,
						AbstractEngineTest.MODEL_ADAPTOR, null);
			}
		}

		public void timeJMTEReflect(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				referenceEngine.transform("${bean.property1}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeJMTEReflectCached(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				cachingEngine.transform("${bean.property1}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeJMTEReflectCompiled(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				compilingEngine.transform("${bean.property1}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeComplexExpressionReference(int reps) throws Exception {
			Engine engine = referenceEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform("${<h1>,address(NIX),</h1>;long(full)}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeComplexExpressionCached(int reps) throws Exception {
			Engine engine = cachingEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform("${<h1>,address(NIX),</h1>;long(full)}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeComplexExpressionCompiled(int reps) throws Exception {
			Engine engine = compilingEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform("${<h1>,address(NIX),</h1>;long(full)}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timePrototypeCompiledComplexExpression(int reps)
				throws Exception {
			for (int i = 0; i < reps; i++) {
				complexTemplate.transform(InterpretedEngineTest.DEFAULT_MODEL,
						AbstractEngineTest.MODEL_ADAPTOR, null);
			}
		}

		public void timeIf(int reps) throws Exception {
			Engine engine = referenceEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform("${if empty}${address}${else}NIX${end}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeIfCached(int reps) throws Exception {
			Engine engine = cachingEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform("${if empty}${address}${else}NIX${end}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeIfCompiled(int reps) throws Exception {
			Engine engine = compilingEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform("${if empty}${address}${else}NIX${end}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timePrototypeCompiledIfExpression(int reps)
				throws Exception {
			for (int i = 0; i < reps; i++) {
				ifTemplate.transform(InterpretedEngineTest.DEFAULT_MODEL,
						AbstractEngineTest.MODEL_ADAPTOR, null);
			}
		}

		public void timeForeach(int reps) throws Exception {
			Engine engine = referenceEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform(
						"${ foreach list item \n}${item.property1}${end}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeForeachCached(int reps) throws Exception {
			Engine engine = cachingEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform(
						"${ foreach list item \n}${item.property1}${end}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeForeachCompiled(int reps) throws Exception {
			Engine engine = compilingEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform(
						"${ foreach list item \n}${item.property1}${end}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timePrototypeCompiledForeachExpression(int reps)
				throws Exception {
			for (int i = 0; i < reps; i++) {
				foreachTemplate.transform(InterpretedEngineTest.DEFAULT_MODEL,
						AbstractEngineTest.MODEL_ADAPTOR, null);
			}
		}
	}

}
