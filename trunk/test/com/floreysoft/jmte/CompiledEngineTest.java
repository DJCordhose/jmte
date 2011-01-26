package com.floreysoft.jmte;

public class CompiledEngineTest extends AbstractEngineTest {

	protected Engine newEngine() {
		Engine engine = new Engine();
		engine.setUseCompilation(true);
		return engine;
	}

}
