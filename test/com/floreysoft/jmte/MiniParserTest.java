package com.floreysoft.jmte;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.floreysoft.jmte.Engine.StartEndPair;

public final class MiniParserTest {
	MiniParser miniParser = MiniParser.defaultInstance();
	MiniParser miniParserIgnoreCase = MiniParser.ignoreCaseInstance();

	@Test
	public void replaceSimple() throws Exception {
		String output = miniParser.replace("Input String", "Str", "R");
		assertEquals("Input Ring", output);
	}

	@Test
	public void replaceIgnorecase() throws Exception {
		String output = miniParserIgnoreCase.replace("Input String", "str", "R");
		assertEquals("Input Ring", output);
	}

	@Test
	public void replaceWithNothing() throws Exception {
		String output = miniParser.replace("Input String", "put", "");
		assertEquals("In String", output);
	}

	@Test
	public void replaceStart() throws Exception {
		String output = miniParser.replace("Input String", "In", "Out");
		assertEquals("Output String", output);
	}

	@Test
	public void replaceEnd() throws Exception {
		String output = miniParser.replace("Input String", "ing", "ong");
		assertEquals("Input Strong", output);
	}

	@Test
	public void replaceNothing() throws Exception {
		String input = "Input String";
		String output = miniParser.replace(input, "", "ong");
		assertTrue(input == output);
	}

	@Test
	public void noReplacePrefix() throws Exception {
		String output = miniParser.replace("Input String", "pute", "pate");
		assertEquals("Input String", output);
	}

	@Test
	public void carveOutSimple() throws Exception {
		String input = "function(param1, param2)";
		List<String> segments = miniParser.carveOut(input, "(", ")");
		assertEquals(2, segments.size());
		assertEquals("function", segments.get(0));
		assertEquals("param1, param2", segments.get(1));
	}

	@Test
	public void carveOutStringSeparator() throws Exception {
		String input = "function${param1, param2}$";
		List<String> segments = miniParser.carveOut(input, "${", "}$");
		assertEquals(2, segments.size());
		assertEquals("function", segments.get(0));
		assertEquals("param1, param2", segments.get(1));
	}

	@Test
	public void carveOutSimpleRest() throws Exception {
		String input = "function(param1, param2)rest";
		List<String> segments = miniParser.carveOut(input, "(", ")");
		assertEquals(3, segments.size());
		assertEquals("function", segments.get(0));
		assertEquals("param1, param2", segments.get(1));
		assertEquals("rest", segments.get(2));
	}

	@Test
	public void carveOutEndBeforeStartMissingEnd() throws Exception {
		String input = "function)(param1, param2";
		List<String> segments = miniParser.carveOut(input, "(", ")");
		assertEquals(2, segments.size());
		assertEquals("function)", segments.get(0));
		assertEquals("param1, param2", segments.get(1));
	}

	@Test
	public void carveOutEscape() throws Exception {
		String input = "fun\\(ction\\)(param1, param2)";
		List<String> segments = miniParser.carveOut(input, "(", ")");
		assertEquals(2, segments.size());
		assertEquals("fun(ction)", segments.get(0));
		assertEquals("param1, param2", segments.get(1));
	}

	@Test
	public void carveOutQuote() throws Exception {
		String input = "\"fun(ction)\"(param1, param2)";
		List<String> segments = miniParser.carveOut(input, "(", ")");
		assertEquals(2, segments.size());
		assertEquals("fun(ction)", segments.get(0));
		assertEquals("param1, param2", segments.get(1));
	}

	@Test
	public void carveOutNonGreedy() throws Exception {
		String input = "function(param1, param2(innerParam))";
		List<String> segments = miniParser.carveOut(input, "(", ")");
		assertEquals(3, segments.size());
		assertEquals("function", segments.get(0));
		assertEquals("param1, param2(innerParam", segments.get(1));
		assertEquals(")", segments.get(2));
	}

	@Test
	public void carveOutGreedy() throws Exception {
		String input = "function(param1, param2(innerParam))";
		List<String> segments = miniParser.carveOut(input, "(", ")", true);
		assertEquals(2, segments.size());
		assertEquals("function", segments.get(0));
		assertEquals("param1, param2(innerParam)", segments.get(1));
	}
}
