package com.floreysoft.jmte;

import com.floreysoft.jmte.message.JournalingErrorHandler;
import com.floreysoft.jmte.template.ErrorReportingOutputAppender;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BooleanIfRendererTest {

	private Engine newEngine() {
		final Engine engine = new Engine();
		return engine;
	}

	@Test
	@Ignore
	public void booleanIfRenderer() {
		final Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", "Daniel Georg Florey");
		model.put("mychar", " ");

		final Engine engine = newEngine();
		String output = engine.transform("${if name;string(fromAfterFirst= ;toBeforeLast=$mychar)=\"Georg\"}${name;string(fromAfterFirst=)}${end}", model);
		assertEquals("Georg Florey", output);
	}


}


