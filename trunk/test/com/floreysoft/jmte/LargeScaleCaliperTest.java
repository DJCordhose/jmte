package com.floreysoft.jmte;

import com.floreysoft.jmte.realLife.RealLiveTest;
import com.google.caliper.SimpleBenchmark;

public class LargeScaleCaliperTest {
	private static final String LONG_LIST = "${foreach longList item}STUFF${item}MORE_STUFF${item}\n${end}";
	private static final String LONG_TEXT = "${foreach longList item}STUFF${end}";

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

	/**
	 * Tests a selection of scripts supposed to be the most frequently used
	 * 
	 * @author olli
	 * 
	 */
	public static class PortfolioBenchmark extends SimpleBenchmark {

		private static final RealLiveTest REAL_LIVE_TEST = new RealLiveTest();

		public void timeSimpleReference(int reps) throws Exception {
			Engine engine = referenceEngine;
			engine.setEnabledInterpretedTemplateCache(false);
			for (int i = 0; i < reps; i++) {
				engine.transform("${address}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeSimpleCachedReference(int reps) throws Exception {
			Engine engine = cachingEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform("${address}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeSimpleCompiledReference(int reps) throws Exception {
			Engine engine = compilingEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform("${address}",
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeLong(int reps) throws Exception {
			final Engine engine = referenceEngine;
			for (int i = 0; i < reps; i++) {
				engine
						.transform(LONG_LIST,
								InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeLongCached(int reps) throws Exception {
			final Engine engine = cachingEngine;
			for (int i = 0; i < reps; i++) {
				engine
						.transform(LONG_LIST,
								InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeLongCompiled(int reps) throws Exception {
			final Engine engine = compilingEngine;
			for (int i = 0; i < reps; i++) {
				engine
						.transform(LONG_LIST,
								InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeLongText(int reps) throws Exception {
			final Engine engine = referenceEngine;
			for (int i = 0; i < reps; i++) {
				engine
						.transform(LONG_TEXT,
								InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeLongTextCached(int reps) throws Exception {
			final Engine engine = cachingEngine;
			for (int i = 0; i < reps; i++) {
				engine
						.transform(LONG_TEXT,
								InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeLongTextCompiled(int reps) throws Exception {
			final Engine engine = compilingEngine;
			for (int i = 0; i < reps; i++) {
				engine
						.transform(LONG_TEXT,
								InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeLongTemplate(int reps) throws Exception {
			final Engine engine = referenceEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform(AbstractEngineTest.LONG_TEMPLATE,
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeLongTemplateCached(int reps) throws Exception {
			final Engine engine = cachingEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform(AbstractEngineTest.LONG_TEMPLATE,
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeLongTemplateCompiled(int reps) throws Exception {
			final Engine engine = compilingEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform(AbstractEngineTest.LONG_TEMPLATE,
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeLongTemplateManyIterations(int reps) throws Exception {
			final Engine engine = referenceEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform(AbstractEngineTest.LONG_TEMPLATE_MANY_ITERATIONS,
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeLongTemplateManyIterationsCached(int reps)
				throws Exception {
			final Engine engine = cachingEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform(AbstractEngineTest.LONG_TEMPLATE_MANY_ITERATIONS,
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeLongTemplateManyIterationsCompiled(int reps)
				throws Exception {
			final Engine engine = compilingEngine;
			for (int i = 0; i < reps; i++) {
				engine.transform(AbstractEngineTest.LONG_TEMPLATE_MANY_ITERATIONS,
						InterpretedEngineTest.DEFAULT_MODEL);
			}
		}

		public void timeRealLife(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				REAL_LIVE_TEST.shop(referenceEngine);
			}
		}

		public void timeRealLifeCached(int reps)
				throws Exception {
			for (int i = 0; i < reps; i++) {
				REAL_LIVE_TEST.shop(cachingEngine);
			}
		}

		public void timeRealLifeCompiled(int reps)
				throws Exception {
			for (int i = 0; i < reps; i++) {
				REAL_LIVE_TEST.shop(compilingEngine);
			}
		}
	}

}
