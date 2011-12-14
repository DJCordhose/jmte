package com.floreysoft.jmte;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;


public final class HelperTest {
	protected Engine newEngine() {
		final Engine engine = new Engine();
		return engine;
	}
	
	@Test
	public void variablesAvailable() throws Exception {
		boolean variablesAvailable = newEngine().variablesAvailable(AbstractEngineTest.DEFAULT_MODEL, "something", "bean.property1");
		assertTrue(variablesAvailable);
	}
	
	@Test
	public void noVariablesAvailable() throws Exception {
		boolean variablesAvailable = newEngine().variablesAvailable(AbstractEngineTest.DEFAULT_MODEL);
		assertTrue(variablesAvailable);
	}
	
	@Test
	public void variablesNotAvailable() throws Exception {
		boolean variablesAvailable = newEngine().variablesAvailable(AbstractEngineTest.DEFAULT_MODEL, "something", "bean.property1", "bean.propertyNotThere");
		assertFalse(variablesAvailable);
	}
	
	@Test
	public void variablesAvailableBooleanTrue() throws Exception {
		boolean variablesAvailable = newEngine().variablesAvailable(AbstractEngineTest.DEFAULT_MODEL, "bean.trueCond");
		assertTrue(variablesAvailable);
	}
	
	@Test
	public void variablesAvailableBooleanFalse() throws Exception {
		boolean variablesAvailable = newEngine().variablesAvailable(AbstractEngineTest.DEFAULT_MODEL, "bean.falseCond");
		assertFalse(variablesAvailable);
	}
	
	@Test
	public void allVariables() throws Exception {
		Set<String> output = newEngine()
				.getUsedVariables(
						"${foreach strings string}${if string='String2'}${string}${adresse}${end}${end}${if !int}${date}${end}");
		// string is a local variable and should not be included here
		assertArrayEquals(new String[] { "adresse", "date", "int", "strings" }, output.toArray());
	}

}
