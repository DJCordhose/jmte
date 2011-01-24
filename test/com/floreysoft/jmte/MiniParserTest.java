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

import org.junit.Ignore;
import org.junit.Test;

import com.floreysoft.jmte.util.MiniParser;
import com.floreysoft.jmte.util.NestedParser;


public final class MiniParserTest {
	static final String WS_SPLIT_STRING = " a  \t\n  \r b c";
	static final String SPLIT_STRING = "1, \n adsdsdsdsd, \t5454545,\"67676,\\\"3434\"";
	MiniParser miniParser = MiniParser.defaultInstance();
	MiniParser trimmedMiniParser = MiniParser.trimmedInstance();
	MiniParser miniParserIgnoreCase = MiniParser.ignoreCaseInstance();
	MiniParser rawMiniParser = MiniParser.rawOutputInstance();

	NestedParser nestedParser = new NestedParser();

	@Test
	public void replaceSimple() throws Exception {
		String output = miniParser.replace("Input String", "Str", "R");
		assertEquals("Input Ring", output);
	}

	@Test
	public void replaceIgnorecase() throws Exception {
		String output = miniParserIgnoreCase
				.replace("Input String", "str", "R");
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
	public void scanSimple() throws Exception {
		String input = "function(param1, param2)";
		List<String> segments = miniParser.scan(input, "(", ")");
		assertEquals(2, segments.size());
		assertEquals("function", segments.get(0));
		assertEquals("param1, param2", segments.get(1));
	}

	@Test
	public void scanAndSplit() throws Exception {
		String input = "function(param1, param2)";
		
		List<String> segments = MiniParser.defaultInstance().scan(input, "(", ")");
		assertEquals(2, segments.size());
	
		String functionName = segments.get(0);
		String parameterString = segments.get(1);
		
		assertEquals("function", functionName);
		assertEquals("param1, param2", parameterString);
		
		List<String> parameters = MiniParser.trimmedInstance().split(parameterString, ',');
		assertEquals(2, parameters.size());
		assertEquals("param1", parameters.get(0));
		assertEquals("param2", parameters.get(1));
	}
	
	@Test
	public void scanStringSeparator() throws Exception {
		String input = "function${param1, param2}$";
		List<String> segments = miniParser.scan(input, "${", "}$");
		assertEquals(2, segments.size());
		assertEquals("function", segments.get(0));
		assertEquals("param1, param2", segments.get(1));
	}

	@Test
	public void scanSimpleRest() throws Exception {
		String input = "function(param1, param2)rest";
		List<String> segments = miniParser.scan(input, "(", ")");
		assertEquals(3, segments.size());
		assertEquals("function", segments.get(0));
		assertEquals("param1, param2", segments.get(1));
		assertEquals("rest", segments.get(2));
	}

	@Test
	public void scanEndBeforeStartMissingEnd() throws Exception {
		String input = "function)(param1, param2";
		List<String> segments = miniParser.scan(input, "(", ")");
		assertEquals(2, segments.size());
		assertEquals("function)", segments.get(0));
		assertEquals("param1, param2", segments.get(1));
	}

	@Test
	public void scanEscape() throws Exception {
		String input = "fun\\(ction\\)(param1, param2)";
		List<String> segments = miniParser.scan(input, "(", ")");
		assertEquals(2, segments.size());
		assertEquals("fun(ction)", segments.get(0));
		assertEquals("param1, param2", segments.get(1));
	}

	@Test
	public void scanQuote() throws Exception {
		String input = "\"fun(ction)\"(param1, param2)";
		List<String> segments = miniParser.scan(input, "(", ")");
		assertEquals(2, segments.size());
		assertEquals("fun(ction)", segments.get(0));
		assertEquals("param1, param2", segments.get(1));
	}

	@Test
	public void scanNonGreedy() throws Exception {
		String input = "function(param1, param2(innerParam))";
		List<String> segments = miniParser.scan(input, "(", ")");
		assertEquals(3, segments.size());
		assertEquals("function", segments.get(0));
		assertEquals("param1, param2(innerParam", segments.get(1));
		assertEquals(")", segments.get(2));
	}

	@Test
	public void scanGreedy() throws Exception {
		String input = "function(param1, param2(innerParam))";
		List<String> segments = miniParser.scan(input, "(", ")", true);
		assertEquals(2, segments.size());
		assertEquals("function", segments.get(0));
		assertEquals("param1, param2(innerParam)", segments.get(1));
	}

	@Test
	public void scanLong() throws Exception {
		String input = "prefix ${inner1} interlude ${inner2} postfix";
		List<String> segments = miniParser.scan(input, "${", "}");
		assertEquals(5, segments.size());
		assertEquals("prefix ", segments.get(0));
		assertEquals("inner1", segments.get(1));
		assertEquals(" interlude ", segments.get(2));
		assertEquals("inner2", segments.get(3));
		assertEquals(" postfix", segments.get(4));
	}

	@Test
	public void scanNoPrefix() throws Exception {
		String input = "${inner1} interlude ${inner2} postfix";
		List<String> segments = miniParser.scan(input, "${", "}");
		assertEquals(5, segments.size());
		assertEquals("", segments.get(0));
		assertEquals("inner1", segments.get(1));
		assertEquals(" interlude ", segments.get(2));
		assertEquals("inner2", segments.get(3));
		assertEquals(" postfix", segments.get(4));
	}

	@Test
	public void split() throws Exception {
		// "1, \n adsdsdsdsd, \t5454545,\"67676,\\\"3434\"";
		String input = SPLIT_STRING;
		List<String> segments = trimmedMiniParser.split(input, ',');
		assertEquals(4, segments.size());
		assertEquals("1", segments.get(0));
		assertEquals("adsdsdsdsd", segments.get(1));
		assertEquals("5454545", segments.get(2));
		assertEquals("67676,\"3434", segments.get(3));
	}

	@Test
	public void rawSplit() throws Exception {
		// "1, \n adsdsdsdsd, \t5454545,\"67676,\\\"3434\"";
		String input = SPLIT_STRING;
		List<String> segments = rawMiniParser.split(input, ',');
		assertEquals("\"67676,\\\"3434\"", segments.get(3));
	}

	@Test
	public void splitOnWhitespace() throws Exception {
		String input = WS_SPLIT_STRING;
		List<String> segments = miniParser.splitOnWhitespace(input);
		assertEquals(4, segments.size());
		assertEquals("", segments.get(0));
		assertEquals("a", segments.get(1));
		assertEquals("b", segments.get(2));
		assertEquals("c", segments.get(3));
	}

	@Test
	public void nestedParser() throws Exception {
		String input = "string=unparsed,unprocessed(maxLength=10, trim, uppercase)";
		String[] operators = { "()", ",", "=" };

		List<Object> parsed = nestedParser.parse(input, Arrays
				.asList(operators));
		assertEquals(2, parsed.size());

		String functionName = (String) parsed.get(0);
		assertEquals("string=unparsed,unprocessed", functionName);

		List<Object> params = (List<Object>) parsed.get(1);
		assertEquals(3, params.size());

		List<Object> nameValueParam = (List<Object>) params.get(0);
		assertEquals(2, nameValueParam.size());
		String maxLengthParam = (String) nameValueParam.get(0);
		assertEquals("maxLength", maxLengthParam);
		String maxLengthValue = (String) nameValueParam.get(1);
		assertEquals("10", maxLengthValue);

		String trimParam = (String) params.get(1);
		assertEquals("trim", trimParam);
		String uppercaseParam = (String) params.get(2);
		assertEquals("uppercase", uppercaseParam);

	}

	public static void main(String[] args) {
		String input = "string=unparsed,unprocessed(maxLength=10, trim, uppercase)";
		String[] operators = { "()", ",", "=" };
		List<Object> parse = new NestedParser().parse(input, operators);
		System.out.println(parse);

	}

}
