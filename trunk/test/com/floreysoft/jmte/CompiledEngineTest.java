package com.floreysoft.jmte;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class CompiledEngineTest extends AbstractEngineTest {

	protected Engine newEngine() {
		return new Engine().setUseCompilation(true);
	}

}
