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
import com.floreysoft.jmte.token.Token;
import com.floreysoft.jmte.util.StartEndPair;
import com.floreysoft.jmte.util.Util;

public class InterpretedEngineTest extends AbstractEngineTest {

	protected Engine newEngine() {
		Engine engine = new Engine();
		engine.setEnabledInterpretedTemplateCache(false);
		return engine;
	}

	@Test
	public void unterminatedScan() throws Exception {
		String line = "${no end";
		List<StartEndPair> scan = Util.scan(line, newEngine()
				.getExprStartToken(), newEngine().getExprEndToken(), true);
		assertEquals(0, scan.size());
	}

	@Test
	public void extract() throws Exception {
		String line = "${if adresse}Sie wohnen an ${adresse}";
		List<StartEndPair> scan = Util.scan(line, newEngine()
				.getExprStartToken(), newEngine().getExprEndToken(), true);
		assertEquals(2, scan.size());

		assertEquals(2, scan.get(0).start);
		assertEquals(12, scan.get(0).end);

		assertEquals(29, scan.get(1).start);
		assertEquals(36, scan.get(1).end);

	}

	@Test
	@SuppressWarnings("unchecked")
	public void mergedForeach() throws Exception {
		List amount = Arrays.asList(1, 2, 3);
		List price = Arrays.asList(3.6, 2, 3.0);
		List total = Arrays.asList("3.6", "4", "9");

		List<Map<String, Object>> mergedLists = ModelBuilder.mergeLists(
				new String[] { "amount", "price", "total" }, amount, price,
				total);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("mergedLists", mergedLists);
		String output = newEngine()
				.transform(
						"${foreach mergedLists item}${item.amount} x ${item.price} = ${item.total}\n${end}",
						model);
		assertEquals("1 x 3.6 = 3.6\n" + "2 x 2 = 4\n" + "3 x 3.0 = 9\n",
				output);
	}

	@Test
	public void stream2String() throws Exception {
		String charsetName = "ISO-8859-15";
		String input = "stream content";
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				input.getBytes(charsetName));
		String streamToString = Util.streamToString(byteArrayInputStream,
				charsetName);
		assertEquals(input, streamToString);
	}

	@Test
	public void reader2String() throws Exception {
		String input = "reader content";
		StringReader stringReader = new StringReader(input);
		String readerToString = Util.readerToString(stringReader);
		assertEquals(input, readerToString);
	}

	@Test
	public void file2String() throws Exception {
		String charsetName = "ISO-8859-15";
		File file = new File("example/basic.mte");
		String fileToString = Util.fileToString(file, charsetName);
		assertEquals("${if address}${address}${else}NIX${end}", fileToString);
	}

	@Test
	public void namedRendererRegistry() throws Exception {
		NamedRenderer stringRenderer = ENGINE_WITH_NAMED_RENDERERS
				.resolveNamedRenderer("string");
		assertNotNull(stringRenderer);
		RenderFormatInfo formatInfo = stringRenderer.getFormatInfo();
		assertTrue(formatInfo instanceof OptionRenderFormatInfo);
		OptionRenderFormatInfo optionRenderInfo = (OptionRenderFormatInfo) formatInfo;
		assertArrayEquals(new String[] { "uppercase", "" }, optionRenderInfo
				.getOptions());

		NamedRenderer dateRenderer = ENGINE_WITH_NAMED_RENDERERS
				.resolveNamedRenderer("date");
		assertNotNull(dateRenderer);

		Collection<NamedRenderer> allNamedRenderers = ENGINE_WITH_NAMED_RENDERERS
				.getAllNamedRenderers();
		assertEquals(2, allNamedRenderers.size());

		Collection<NamedRenderer> compatibleRenderers2 = ENGINE_WITH_NAMED_RENDERERS
				.getCompatibleRenderers(Long.class);
		assertEquals(1, compatibleRenderers2.size());

		Collection<NamedRenderer> compatibleRenderers1 = ENGINE_WITH_NAMED_RENDERERS
				.getCompatibleRenderers(Number.class);
		assertEquals(2, compatibleRenderers1.size());

		Collection<NamedRenderer> compatibleRenderers3 = ENGINE_WITH_NAMED_RENDERERS
				.getCompatibleRenderers(Boolean.class);
		assertEquals(0, compatibleRenderers3.size());

	}

	@Test
	public void processListener() throws Exception {
		String input = "${if empty}EMPTY${else}NOT_EMPTY${end}${foreach not_there var}${var}${end}";
		Engine engine = newEngine();
		final List<ProcessListener.Action> actions = new ArrayList<ProcessListener.Action>();
		final ProcessListener processListener = new ProcessListener() {

			@Override
			public void log(TemplateContext context, Token token, Action action) {
				actions.add(action);
			}

		};
		engine.transform(input, DEFAULT_MODEL, processListener);
		assertArrayEquals(new ProcessListener.Action[] {
				ProcessListener.Action.EVAL, ProcessListener.Action.SKIP,
				ProcessListener.Action.EVAL, ProcessListener.Action.EVAL,
				ProcessListener.Action.END, ProcessListener.Action.EVAL,
				ProcessListener.Action.SKIP, ProcessListener.Action.END },
				actions.toArray());

	}
}
