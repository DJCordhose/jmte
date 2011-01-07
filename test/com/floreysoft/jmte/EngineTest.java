package com.floreysoft.jmte;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Test;

import com.floreysoft.jmte.Engine.StartEndPair;

@SuppressWarnings("unchecked")
public final class EngineTest {

	private static final MyBean MyBean1 = new MyBean("1.1", "1.2");
	private static final MyBean MyBean2 = new MyBean("2.1", "2.2");

	private static class MyIterable implements Iterable {
		List<Object> list = new ArrayList<Object>();

		public MyIterable() {

		}

		public MyIterable(List<Object> list) {
			this.list = list;
		}

		@Override
		public Iterator<Object> iterator() {
			return list.iterator();
		}
	}

	public static class MyBean {

		private Object property1 = "propertyValue1";
		public Object property2 = "propertyValue2";
		public boolean falseCond = false;
		public Boolean falseCondObj = new Boolean(false);

		public MyBean(Object property1, Object property2) {
			this.property1 = property1;
			this.property2 = property2;
		}

		public MyBean() {
		}

		public List getList() {
			return LIST;
		}

		public Map getMap() {
			return MAP;
		}

		public Object getProperty1() {
			return property1;
		}

		public boolean getTrueCond() {
			return true;
		}

		public Boolean getTrueCondObj() {
			return new Boolean(true);
		}

		@Override
		public String toString() {
			return property1.toString() + ", " + property2.toString();
		}

	}

	private final static Map<String, Object> MAP = new HashMap<String, Object>();
	static {
		MAP.put("mapEntry1", "mapValue1");
		MAP.put("mapEntry2", "mapValue2");
	}

	private final static List<MyBean> LIST = new ArrayList<MyBean>();
	static {
		LIST.add(MyBean1);
		LIST.add(MyBean2);
	}

	private final static MyBean[] ARRAY = new MyBean[2];
	static {
		ARRAY[0] = MyBean1;
		ARRAY[1] = MyBean2;
	}

	private final static int[] INT_ARRAY = { 1, 2 };

	private final static Iterable ITERABLE;
	static {
		List<Object> list = new ArrayList<Object>();
		list.add("iterableEntry1");
		list.add("iterableEntry2");
		ITERABLE = new MyIterable(list);
	}

	private final static MyBean BEAN = new MyBean();

	private final static String[] STRINGS = { "String1", "String2", "String3" };

	final static Map<String, Object> DEFAULT_MODEL = new HashMap<String, Object>();
	static {
		DEFAULT_MODEL.put("something", "something");
		DEFAULT_MODEL.put("address", "Fillbert");
		DEFAULT_MODEL.put("map", MAP);
		DEFAULT_MODEL.put("list", LIST);
		DEFAULT_MODEL.put("iterable", ITERABLE);
		DEFAULT_MODEL.put("array", ARRAY);
		DEFAULT_MODEL.put("intArray", INT_ARRAY);
		DEFAULT_MODEL.put("bean", BEAN);
		DEFAULT_MODEL.put("emptyMap", new HashMap());
		DEFAULT_MODEL.put("emptyList", new ArrayList());
		DEFAULT_MODEL.put("emptyArray", new Object[0]);
		DEFAULT_MODEL.put("emptyIntArray", new int[0]);
		DEFAULT_MODEL.put("emptyIterable", new MyIterable());
		DEFAULT_MODEL.put("empty", "");
		DEFAULT_MODEL.put("strings", STRINGS);
		DEFAULT_MODEL.put("date", new Date(0));
		DEFAULT_MODEL.put("int", 0);
	}

	@Test
	public void unterminatedScan() throws Exception {
		String line = "${no end";
		List<StartEndPair> scan = new Engine().scan(line);
		assertEquals(0, scan.size());
	}

	@Test
	public void extract() throws Exception {
		String line = "${if adresse}Sie wohnen an ${adresse}";
		List<StartEndPair> scan = new Engine().scan(line);
		assertEquals(2, scan.size());

		assertEquals(2, scan.get(0).start);
		assertEquals(12, scan.get(0).end);

		assertEquals(29, scan.get(1).start);
		assertEquals(36, scan.get(1).end);

	}

	@Test
	public void variableName() throws Exception {
		Map<String, Object> simpleModel = new HashMap<String, Object>();
		simpleModel
				.put(
						"http://www.google.com/m8/feeds/groups/daniel.florey%40gmail.com/base/16e7715c8a9e5849",
						"true");
		simpleModel
				.put(
						"http://www.google.com/m8/feeds/groups/daniel.florey%40gmail.com/base/6",
						"true");

		String output = new Engine()
				.transform(
						"${if http://www\\.google\\.com/m8/feeds/groups/daniel\\.florey%40gmail\\.com/base/16e7715c8a9e5849}works${else}does not work${end}",
						simpleModel);
		assertEquals("works", output);
	}

	@Test
	public void simpleExpression() throws Exception {
		String output = new Engine().transform("${address}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void empty() throws Exception {
		String output = new Engine()
				.transform("${\n}${ \n }${}", DEFAULT_MODEL);
		assertEquals("", output);
	}

	@Test
	public void suffixPrefix() throws Exception {
		String output = new Engine().transform("PREFIX${address}SUFFIX",
				DEFAULT_MODEL);
		assertEquals("PREFIX" + DEFAULT_MODEL.get("address") + "SUFFIX", output);
	}

	@Test
	public void noTransformation() throws Exception {
		String output = new Engine().transform("No transformation required",
				DEFAULT_MODEL);
		assertEquals("No transformation required", output);
	}

	@Test
	public void errorPosition() throws Exception {
		boolean foundPosition = false;
		try {
			new Engine().transform("\n${address}\n     ${else}NIX${end}",
					DEFAULT_MODEL);
		} catch (ParseException e) {
			String message = e.getMessage();
			foundPosition = message
					.equals("Error while parsing 'else' at location (3:8): Can't use else outside of if block!");

		}
		assertTrue(
				"Position not found in exception message or exception not thrown",
				foundPosition);
	}

	@Test
	public void mapExpression() throws Exception {
		String output = new Engine().transform("${map.mapEntry1}",
				DEFAULT_MODEL);
		assertEquals(MAP.get("mapEntry1"), output);

	}

	@Test
	public void propertyExpressionGetter() throws Exception {
		String output = new Engine().transform("${bean.property1}",
				DEFAULT_MODEL);
		assertEquals(BEAN.getProperty1().toString(), output);

	}

	@Test
	public void propertyExpressionField() throws Exception {
		String output = new Engine().transform("${bean.property2}",
				DEFAULT_MODEL);
		assertEquals(BEAN.property2.toString(), output);

	}

	@Test
	public void nullExpression() throws Exception {
		String output = new Engine().transform("${undefined}", DEFAULT_MODEL);
		assertEquals("", output);
	}

	@Test
	public void directMap() throws Exception {
		// if we try to directly output a map, we simply get the first value
		String output = new Engine().transform("${map}", DEFAULT_MODEL);
		assertEquals("{mapEntry1=mapValue1, mapEntry2=mapValue2}", output);
	}

	@Test
	public void directEmptyMap() throws Exception {
		// if we try to directly output an empty map, we simply get an empty
		// string
		String output = new Engine().transform("${emptyMap}", DEFAULT_MODEL);
		assertEquals("", output);
	}

	@Test
	public void directList() throws Exception {
		// if we try to directly output a list, we simply get the first value
		String output = new Engine().transform("${list}", DEFAULT_MODEL);
		assertEquals("[1.1, 1.2, 2.1, 2.2]", output);
	}

	@Test
	public void directArray() throws Exception {
		// if we try to directly output an array, we simply get the first value
		String output = new Engine().transform("${array}", DEFAULT_MODEL);
		assertEquals("1.1, 1.2", output);
	}

	@Test
	public void directEmptyList() throws Exception {
		// if we try to directly output an empty list, we simply get an empty
		// string
		String output = new Engine().transform("${emptyList}", DEFAULT_MODEL);
		assertEquals("", output);
	}

	@Test
	public void directEmptyArray() throws Exception {
		// if we try to directly output an empty array, we simply get an empty
		// string
		String output = new Engine().transform("${emptyArray}", DEFAULT_MODEL);
		assertEquals("", output);
	}

	@Test
	public void ifEmptyFalseExpression() throws Exception {
		String output = new Engine().transform(
				"${if empty}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void ifNullTrueExpression() throws Exception {
		String output = new Engine().transform(
				"${if address}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test(expected = ParseException.class)
	public void elseWithoutIfError() throws Exception {
		new Engine().transform("${address}${else}NIX${end}", DEFAULT_MODEL);
	}

	@Test(expected = ParseException.class)
	public void endWithoutBlockError() throws Exception {
		new Engine().transform("${address}${end}", DEFAULT_MODEL);
	}

	@Test
	public void defaultShortcut() throws Exception {
		String full = new Engine().transform(
				"${if address}${address}${else}NIX${end}", DEFAULT_MODEL);
		String shortCut = new Engine().transform("${address(NIX)}",
				DEFAULT_MODEL);
		assertEquals(full, shortCut);
	}

	@Test
	public void defaultShortcutActivated() throws Exception {
		String full = new Engine().transform(
				"${if noAddress}${address}${else}NIX${end}", DEFAULT_MODEL);
		String shortCut = new Engine().transform("${noAddress(NIX)}",
				DEFAULT_MODEL);
		assertEquals(full, shortCut);
	}

	@Test
	public void wrapShortcut() throws Exception {
		String full = new Engine().transform(
				"<h1>${if address}${address}${else}NIX${end}</h1>",
				DEFAULT_MODEL);
		String shortCut = new Engine().transform("${<h1>,address(NIX),</h1>}",
				DEFAULT_MODEL);
		assertEquals(full, shortCut);
	}

	@Test
	public void wrapShortcutActivated() throws Exception {
		String full = new Engine().transform(
				"<h1>${if noAddress}${address}${else}NIX${end}</h1>",
				DEFAULT_MODEL);
		String shortCut = new Engine().transform(
				"${<h1>,noAddress(NIX),</h1>}", DEFAULT_MODEL);
		assertEquals(full, shortCut);
	}

	@Test
	public void wrapNoPre() throws Exception {
		String shortCut = new Engine().transform("${,address,</h1>}",
				DEFAULT_MODEL);
		assertEquals("Fillbert</h1>", shortCut);
	}

	@Test
	public void wrapKeepWS() throws Exception {
		String shortCut = new Engine().transform("${   ,address,  }",
				DEFAULT_MODEL);
		assertEquals("   Fillbert  ", shortCut);
	}

	@Test
	public void wrapNoPost() throws Exception {
		String shortCut = new Engine().transform("${<h1>,address,}",
				DEFAULT_MODEL);
		assertEquals("<h1>Fillbert", shortCut);
	}

	@Test
	public void ifNotExpression() throws Exception {
		String output = new Engine().transform(
				"${if !hugo}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifNotElseExpression() throws Exception {
		String output = new Engine().transform(
				"${if !address}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void stringEq() throws Exception {
		String output = new Engine().transform(
				"${if address='Fillbert'}${address}${else}NIX${end}",
				DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void stringEqNotElse() throws Exception {
		String output = new Engine().transform(
				"${if !address='Fillbert'}${address}${else}NIX${end}",
				DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void stringEqInForeach() throws Exception {
		String output = new Engine()
				.transform(
						"${foreach strings string}${if string='String2'}${string}${end}${end}",
						DEFAULT_MODEL);
		assertEquals("String2", output);
	}

	@Test
	public void ifBooleanTrueExpression() throws Exception {
		String output = new Engine().transform(
				"${if bean.trueCond}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifBooleanObjTrueExpression() throws Exception {
		String output = new Engine().transform(
				"${if bean.trueCondObj}${address}${else}NIX${end}",
				DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifBooleanFalseExpression() throws Exception {
		String output = new Engine()
				.transform("${if bean.falseCond}${address}${else}NIX${end}",
						DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void ifBooleanStringExpression() throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		String falseString = new Boolean(false).toString();
		assertEquals("false", falseString);
		model.put("falseString", falseString);
		String output = new Engine().transform("${if falseString}NO${end}",
				model);
		assertEquals("", output);
	}

	@Test
	public void ifBooleanCallableExpression() throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("falseCallable", new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return false;
			}
			
		});
		String output = new Engine().transform("${if !falseCallable}YES${end}",
				model);
		assertEquals("YES", output);
	}

	@Test
	public void ifBooleanObjFalseExpression() throws Exception {
		String output = new Engine().transform(
				"${if bean.falseCondObj}${address}${else}NIX${end}",
				DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void ifMapTrueExpression() throws Exception {
		String output = new Engine().transform(
				"${if map}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifMapFalseExpression() throws Exception {
		String output = new Engine().transform(
				"${if emptyMap}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void ifCollectionTrueExpression() throws Exception {
		String output = new Engine().transform(
				"${if list}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifCollectionFalseExpression() throws Exception {
		String output = new Engine().transform(
				"${if emptyList}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void ifIterableTrueExpression() throws Exception {
		String output = new Engine().transform(
				"${if iterable}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifIterableFalseExpression() throws Exception {
		String output = new Engine().transform(
				"${if emptyIterable}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void ifArrayTrueExpression() throws Exception {
		String output = new Engine().transform(
				"${if array}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifArrayFalseExpression() throws Exception {
		String output = new Engine().transform(
				"${if emptyArray }${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void ifPrimitiveArrayTrueExpression() throws Exception {
		String output = new Engine().transform(
				"${if intArray}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifPrimitiveArrayFalseExpression() throws Exception {
		String output = new Engine()
				.transform("${if emptyIntArray }${address}${else}NIX${end}",
						DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void nestedIfExpression() throws Exception {
		String output = new Engine()
				.transform(
						"${if hugo}${address}${else}${if address}${address}${else}NIX${end}${end}",
						DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void nestedIfSkip() throws Exception {
		String output = new Engine().transform(
				"${if nix}Something${if address}${address}${end}${end}",
				DEFAULT_MODEL);
		assertEquals("", output);
	}

	@Test
	public void nestedIfElseSkip() throws Exception {
		String output = new Engine()
				.transform(
						"${if something}${else}${if something}${else}Something${if address}${address}${end}${end}${end}",
						DEFAULT_MODEL);
		assertEquals("", output);
	}

	@Test
	public void simpleForeach() throws Exception {
		String output = new Engine().transform(
				"${foreach list item}${item}\n${end}", DEFAULT_MODEL);
		assertEquals("1.1, 1.2\n" + "2.1, 2.2\n", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void foreachSingletonAsList() throws Exception {
		// if the variable we want to iterate over is atomic, we simply wrap
		// it into a singleton list
		String output = new Engine().transform(
				"${foreach address item}${item}${end}", DEFAULT_MODEL);
		String expected = new Engine().transform("${address}", DEFAULT_MODEL);
		assertEquals(expected, output);
	}

	@Test
	public void mergedForeach() throws Exception {
		List amount = Arrays.asList(1, 2, 3);
		List price = Arrays.asList(3.6, 2, 3.0);
		List total = Arrays.asList("3.6", "4", "9");

		List<Map<String, Object>> mergedLists = Engine.mergeLists(new String[] {
				"amount", "price", "total" }, amount, price, total);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("mergedLists", mergedLists);
		String output = new Engine()
				.transform(
						"${foreach mergedLists item}${item.amount} x ${item.price} = ${item.total}\n${end}",
						model);
		assertEquals("1 x 3.6 = 3.6\n" + "2 x 2 = 4\n" + "3 x 3.0 = 9\n",
				output);
	}

	@Test
	public void foreachArray() throws Exception {
		String output = new Engine().transform(
				"${foreach array item}${item}\n${end}", DEFAULT_MODEL);
		assertEquals("1.1, 1.2\n" + "2.1, 2.2\n", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void foreachPrimitiveArray() throws Exception {
		String output = new Engine().transform(
				"${foreach intArray item}${item}\n${end}", DEFAULT_MODEL);
		assertEquals("1\n2\n", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void foreachMap() throws Exception {
		String output = new Engine().transform(
				"${foreach map entry}${entry.key}=${entry.value}\n${end}",
				DEFAULT_MODEL);
		assertEquals("mapEntry1=mapValue1\n" + "mapEntry2=mapValue2\n", output);
		assertNull(DEFAULT_MODEL.get("item"));

	}

	@Test
	public void specialForeachVariables() throws Exception {
		String output = new Engine()
				.transform(
						"${foreach list item}${item}\n${if last_item}last${end}${if first_item}first${end}${if even_item} even${end}${if odd_item} odd${end}${end}",
						DEFAULT_MODEL);
		assertEquals("1.1, 1.2\nfirst even" + "2.1, 2.2\nlast odd", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void foreachSeparator() throws Exception {
		String output = new Engine()
				.transform(
						"${ \n foreach\n\t  list  \r  item   ,}${item.property1}${end}",
						DEFAULT_MODEL);
		assertEquals("1.1  ,2.1", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void crazyForeachSeparator() throws Exception {
		String output = new Engine()
				.transform("${ foreach list item  }${item.property1}${end}",
						DEFAULT_MODEL);
		assertEquals("1.1 2.1", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void newlineForeachSeparator() throws Exception {
		String output = new Engine().transform(
				"${ foreach list item \n}${item.property1}${end}",
				DEFAULT_MODEL);
		assertEquals("1.1\n2.1", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void propertyForeach() throws Exception {
		String output = new Engine().transform(
				"${foreach list item}${item.property1}\n${end}", DEFAULT_MODEL);
		assertEquals("1.1\n" + "2.1\n", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void nestedForeach() throws Exception {
		String output = new Engine()
				.transform(
						"${foreach list item}${foreach item.list item2}${item2.property1}${end}\n${end}",
						DEFAULT_MODEL);
		assertEquals("1.12.1\n" + "1.12.1\n", output);
		assertNull(DEFAULT_MODEL.get("item"));
		assertNull(DEFAULT_MODEL.get("item2"));
	}

	@Test
	public void emptyForeach() throws Exception {
		String output = new Engine().transform(
				"${foreach emptyList item}${item.property1}\n${end}",
				DEFAULT_MODEL);
		assertEquals("", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void ifInForeach() throws Exception {
		String output = new Engine()
				.transform(
						"${foreach list item}${if item}${item}${if hugo}${item}${end}${end}\n${end}",
						DEFAULT_MODEL);
		assertEquals("1.1, 1.2\n" + "2.1, 2.2\n", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void elseInForeach() throws Exception {
		String output = new Engine()
				.transform(
						"${foreach strings string , }${if string=String1}nada${else}nüscht${end}${end}",
						DEFAULT_MODEL);
		assertEquals("nada, nüscht, nüscht", output);
	}

	@Test
	public void scopingInFalseIf() throws Exception {
		String template = "${if emptyList}" + "Was da"
				+ "${foreach emptyList item}" + "${end}" + "${else}" + "Nüscht"
				+ "${end}";
		String output = new Engine().transform(template, DEFAULT_MODEL);
		assertEquals("Nüscht", output);
	}

	@Test
	public void foreachDoubleVarName() throws Exception {
		String output = new Engine()
				.transform(
						"${foreach list item  }${item}:${foreach strings item ,}${item}${end}${end}",
						DEFAULT_MODEL);
		assertEquals(
				"1.1, 1.2:String1,String2,String3 2.1, 2.2:String1,String2,String3",
				output);
	}

	@Test
	public void simpleEscaping() throws Exception {
		String output = new Engine().transform("\\${\\}\n\\\\}", DEFAULT_MODEL);
		assertEquals("${}\n\\}", output);
	}

	@Test
	public void escapingKernel() throws Exception {
		String output = Util.MINI_PARSER.unescape("\\${\\}\n\\\\}");
		assertEquals("${}\n\\}", output);
	}

	@Test
	public void complexEscaping() throws Exception {
		String output = new Engine()
				.transform(
						"${foreach list item \\${\n \\},}${item.property1}${end}\n\\\\",
						DEFAULT_MODEL);
		assertEquals("1.1${\n },2.1\n\\", output);
	}

	@Test
	public void quotes() throws Exception {
		String output = new Engine()
				.transform("\"${int}\" \\\"", DEFAULT_MODEL);
		assertEquals("\"0\" \"", output);
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
	public void format() throws Exception {
		String output = Engine.format("${if 1}${1}${else}${2}${end}", "arg1",
				"arg2");
		assertEquals("arg1", output);
		// just to prove that shortcut for false-branch works
		Engine.format("${if 1}${1}${else}${2}${end}", "arg1");

		// check for null values
		output = Engine.format("${if 1}${1}${else}${2}${end}", null, "arg2");
		assertEquals("arg2", output);

		// check for boolean values
		output = Engine.format("${if 1}${1}${else}${2}${end}", false, "arg2");
		assertEquals("arg2", output);
	}

	@Test
	public void formatNamed() throws Exception {
		String output = Engine.formatNamed("${if 1}${2}${else}broken${end}",
				"1", "arg1", "2", "arg2");
		assertEquals("arg2", output);
	}

	private final static Engine ENGINE_WITH_CUSTOM_RENDERERS = new Engine()
			.registerRenderer(Object.class, new Renderer<Object>() {

				@Override
				public String render(Object o) {
					return "Object=" + o.toString();
				}
			}).registerRenderer(MyBean.class, new Renderer<MyBean>() {

				@Override
				public String render(MyBean o) {
					return "Render=" + o.property1.toString();
				}

			});

	@Test
	public void renderer() throws Exception {
		String output = ENGINE_WITH_CUSTOM_RENDERERS
				.transform(
						"${bean} and ${bean;long} and ${address;this is the format(no matter what I type; - this is part of the format)}",
						DEFAULT_MODEL);
		assertEquals(
				"Render=propertyValue1 and Render=propertyValue1 and Object=Fillbert",
				output);
	}

	@Test
	public void wrapShortcutFormat() throws Exception {
		String full = ENGINE_WITH_CUSTOM_RENDERERS.transform(
				"<h1>${if address}${address;long}${else}NIX${end}</h1>",
				DEFAULT_MODEL);
		String shortCut = ENGINE_WITH_CUSTOM_RENDERERS.transform(
				"${<h1>,address(NIX),</h1>;long}", DEFAULT_MODEL);
		assertEquals(full, shortCut);
	}

	@Test
	public void defaultShortcutFormat() throws Exception {
		String full = ENGINE_WITH_CUSTOM_RENDERERS.transform(
				"${if address}${address;long}${else}NIX${end}", DEFAULT_MODEL);
		String shortCut = ENGINE_WITH_CUSTOM_RENDERERS.transform(
				"${address(NIX);long}", DEFAULT_MODEL);
		assertEquals(full, shortCut);
	}

	private final static Engine ENGINE_WITH_NAMED_RENDERERS = ENGINE_WITH_CUSTOM_RENDERERS
			.registerNamedRenderer(new NamedDateRenderer())
			.registerNamedRenderer(new NamedStringRenderer());

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
	public void namedRenderer() throws Exception {
		String output = ENGINE_WITH_NAMED_RENDERERS
				.transform(
						"\"${date;date(yyyy.MM.dd HH:mm:ss z)}\" and \"${int;date}\" and ${bean;date(long)} and ${address;string(this is the format(no matter what I type; - this is part of the format))}",
						DEFAULT_MODEL);
		assertEquals(
				"\"1970.01.01 01:00:00 MEZ\" and \"01.01.1970 01:00:00 +0100\" and Render=propertyValue1 and String=Fillbert(this is the format(no matter what I type; - this is part of the format))",
				output);
	}

	// sandbox just for quick testing
	public static void main(String[] args) {
	}
}
