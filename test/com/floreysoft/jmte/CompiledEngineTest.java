package com.floreysoft.jmte;

public final class CompiledEngineTest extends AbstractEngineTest {

	protected Engine newEngine() {
		Engine engine = new Engine();
		engine.setUseCompilation(true);
		return engine;
	}

}
