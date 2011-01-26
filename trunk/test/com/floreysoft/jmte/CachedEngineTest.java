package com.floreysoft.jmte;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.floreysoft.jmte.renderer.OptionRenderFormatInfo;
import com.floreysoft.jmte.template.InterpretedTemplate;
import com.floreysoft.jmte.token.Token;
import com.floreysoft.jmte.util.StartEndPair;
import com.floreysoft.jmte.util.Util;

public class CachedEngineTest extends InterpretedEngineTest {

	protected Engine newEngine() {
		Engine engine = new Engine();
		engine.setEnabledInterpretedTemplateCache(true);
		return engine;
	}

	@Test
	public void reentrantCache() throws Exception {
		Engine engine = newEngine();
		engine.setEnabledInterpretedTemplateCache(true);
		String template = "${foreach list item}${foreach item.list item2}${if item}${item2.property1}${end}${end}\n${end}";
		String output1 = engine.transform(template, DEFAULT_MODEL);
		assertEquals("1.12.1\n" + "1.12.1\n", output1);
		String output2 = engine.transform(template, DEFAULT_MODEL);
		assertEquals("1.12.1\n" + "1.12.1\n", output2);
	}

}
