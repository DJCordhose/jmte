package com.floreysoft.jmte;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import com.floreysoft.jmte.template.VariableDescription;
import org.junit.Ignore;
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
	@Ignore
	public void allVariables() throws Exception {
		Set<String> output = newEngine()
				.getUsedVariables(
						"${foreach strings string}${if string='String2'}${string}${adresse}${end}${end}${if !int}${date}${end}");
		// string is a local variable and should not be included here
		assertArrayEquals(new String[] { "adresse", "date", "int", "strings" }, output.toArray());
	}

	@Test
	public void allVariableDescriptions() throws Exception {
		List<VariableDescription> output = newEngine()
				.getUsedVariableDescriptions(
						"${foreach strings string}${if string='String2'}${string}${adresse;text}${end}${end}${if !int}${date;number(whatever=Huhn)}${end}");
		// string is a local variable and should not be included here
		assertArrayEquals(new VariableDescription[]{new VariableDescription("strings", VariableDescription.Context.FOR_EACH),
				new VariableDescription("adresse", "text", null, VariableDescription.Context.TEXT),
				new VariableDescription("int", VariableDescription.Context.IF),
				new VariableDescription("date", "number", "whatever=Huhn", VariableDescription.Context.TEXT)
		}, output.toArray());
	}

}
