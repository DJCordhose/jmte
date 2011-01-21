package com.floreysoft.jmte;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;

import com.floreysoft.jmte.renderer.NamedRenderer;
import com.floreysoft.jmte.renderer.OptionRenderFormatInfo;
import com.floreysoft.jmte.renderer.RenderFormatInfo;
import com.floreysoft.jmte.renderer.Renderer;

import static org.objectweb.asm.Opcodes.*;

@SuppressWarnings("unchecked")
public abstract class AbstractEngineTest {

	protected abstract Engine newEngine();

	final Engine ENGINE_WITH_CUSTOM_RENDERERS = newEngine().registerRenderer(
			Object.class, new Renderer<Object>() {

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
	final Engine ENGINE_WITH_NAMED_RENDERERS = ENGINE_WITH_CUSTOM_RENDERERS
			.registerNamedRenderer(new NamedDateRenderer())
			.registerNamedRenderer(new NamedStringRenderer());

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

		String output = newEngine()
				.transform(
						"${if http://www\\.google\\.com/m8/feeds/groups/daniel\\.florey%40gmail\\.com/base/16e7715c8a9e5849}works${else}does not work${end}",
						simpleModel);
		assertEquals("works", output);
	}

	@Test
	public void simpleExpression() throws Exception {
		String output = newEngine().transform("${address}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void empty() throws Exception {
		String output = newEngine().transform("${\n}${ \n }${}", DEFAULT_MODEL);
		assertEquals("", output);
	}

	@Test
	public void suffixPrefix() throws Exception {
		String output = newEngine().transform("PREFIX${address}SUFFIX",
				DEFAULT_MODEL);
		assertEquals("PREFIX" + DEFAULT_MODEL.get("address") + "SUFFIX", output);
	}

	@Test
	public void noTransformation() throws Exception {
		String output = newEngine().transform("No transformation required",
				DEFAULT_MODEL);
		assertEquals("No transformation required", output);
	}

	@Test
	public void errorPosition() throws Exception {
		boolean foundPosition = false;
		try {
			newEngine().transform("\n${address}\n     ${else}NIX${end}",
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
		String output = newEngine()
				.transform("${map.mapEntry1}", DEFAULT_MODEL);
		assertEquals(MAP.get("mapEntry1"), output);

	}

	@Test
	public void propertyExpressionGetter() throws Exception {
		String output = newEngine().transform("${bean.property1}",
				DEFAULT_MODEL);
		assertEquals(BEAN.getProperty1().toString(), output);

	}

	@Test
	public void propertyExpressionField() throws Exception {
		String output = newEngine().transform("${bean.property2}",
				DEFAULT_MODEL);
		assertEquals(BEAN.property2.toString(), output);

	}

	@Test
	public void nullExpression() throws Exception {
		String output = newEngine().transform("${undefined}", DEFAULT_MODEL);
		assertEquals("", output);
	}

	@Test
	public void directMap() throws Exception {
		// if we try to directly output a map, we simply get the first value
		String output = newEngine().transform("${map}", DEFAULT_MODEL);
		assertEquals("{mapEntry1=mapValue1, mapEntry2=mapValue2}", output);
	}

	@Test
	public void directEmptyMap() throws Exception {
		// if we try to directly output an empty map, we simply get an empty
		// string
		String output = newEngine().transform("${emptyMap}", DEFAULT_MODEL);
		assertEquals("", output);
	}

	@Test
	public void directList() throws Exception {
		// if we try to directly output a list, we simply get the first value
		String output = newEngine().transform("${list}", DEFAULT_MODEL);
		assertEquals("[1.1, 1.2, 2.1, 2.2]", output);
	}

	@Test
	public void directArray() throws Exception {
		// if we try to directly output an array, we simply get the first value
		String output = newEngine().transform("${array}", DEFAULT_MODEL);
		assertEquals("1.1, 1.2", output);
	}

	@Test
	public void directEmptyList() throws Exception {
		// if we try to directly output an empty list, we simply get an empty
		// string
		String output = newEngine().transform("${emptyList}", DEFAULT_MODEL);
		assertEquals("", output);
	}

	@Test
	public void directEmptyArray() throws Exception {
		// if we try to directly output an empty array, we simply get an empty
		// string
		String output = newEngine().transform("${emptyArray}", DEFAULT_MODEL);
		assertEquals("", output);
	}

	@Test
	public void ifEmptyFalseExpression() throws Exception {
		String output = newEngine().transform(
				"${if empty}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void ifNullTrueExpression() throws Exception {
		String output = newEngine().transform(
				"${if address}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test(expected = ParseException.class)
	public void elseWithoutIfError() throws Exception {
		newEngine().transform("${address}${else}NIX${end}", DEFAULT_MODEL);
	}

	@Test(expected = ParseException.class)
	public void endWithoutBlockError() throws Exception {
		newEngine().transform("${address}${end}", DEFAULT_MODEL);
	}

	@Test
	public void defaultShortcut() throws Exception {
		String full = newEngine().transform(
				"${if address}${address}${else}NIX${end}", DEFAULT_MODEL);
		String shortCut = newEngine().transform("${address(NIX)}",
				DEFAULT_MODEL);
		assertEquals(full, shortCut);
	}

	@Test
	public void defaultShortcutActivated() throws Exception {
		String full = newEngine().transform(
				"${if noAddress}${address}${else}NIX${end}", DEFAULT_MODEL);
		String shortCut = newEngine().transform("${noAddress(NIX)}",
				DEFAULT_MODEL);
		assertEquals(full, shortCut);
	}

	@Test
	public void wrapShortcut() throws Exception {
		String full = newEngine().transform(
				"<h1>${if address}${address}${else}NIX${end}</h1>",
				DEFAULT_MODEL);
		String shortCut = newEngine().transform("${<h1>,address(NIX),</h1>}",
				DEFAULT_MODEL);
		assertEquals(full, shortCut);
	}

	@Test
	public void wrapShortcutActivated() throws Exception {
		String full = newEngine().transform(
				"<h1>${if noAddress}${address}${else}NIX${end}</h1>",
				DEFAULT_MODEL);
		String shortCut = newEngine().transform("${<h1>,noAddress(NIX),</h1>}",
				DEFAULT_MODEL);
		assertEquals(full, shortCut);
	}

	@Test
	public void wrapShortcutEmpty() throws Exception {
		String shortCut = newEngine().transform("${<h1>,noAddress,</h1>}",
				DEFAULT_MODEL);
		assertEquals("", shortCut);
	}

	@Test
	public void wrapNoPre() throws Exception {
		String shortCut = newEngine().transform("${,address,</h1>}",
				DEFAULT_MODEL);
		assertEquals("Fillbert</h1>", shortCut);
	}

	@Test
	public void wrapKeepWS() throws Exception {
		String shortCut = newEngine().transform("${   ,address,  }",
				DEFAULT_MODEL);
		assertEquals("   Fillbert  ", shortCut);
	}

	@Test
	public void wrapNoPost() throws Exception {
		String shortCut = newEngine().transform("${<h1>,address,}",
				DEFAULT_MODEL);
		assertEquals("<h1>Fillbert", shortCut);
	}

	@Test
	public void ifNotExpression() throws Exception {
		String output = newEngine().transform(
				"${if !hugo}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifNotElseExpression() throws Exception {
		String output = newEngine().transform(
				"${if !address}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void stringEq() throws Exception {
		String output = newEngine().transform(
				"${if address='Fillbert'}${address}${else}NIX${end}",
				DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void stringEqNotElse() throws Exception {
		String output = newEngine().transform(
				"${if !address='Fillbert'}${address}${else}NIX${end}",
				DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void stringEqInForeach() throws Exception {
		String output = newEngine()
				.transform(
						"${foreach strings string}${if string='String2'}${string}${end}${end}",
						DEFAULT_MODEL);
		assertEquals("String2", output);
	}

	@Test
	public void ifBooleanTrueExpression() throws Exception {
		String output = newEngine().transform(
				"${if bean.trueCond}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifBooleanObjTrueExpression() throws Exception {
		String output = newEngine().transform(
				"${if bean.trueCondObj}${address}${else}NIX${end}",
				DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifBooleanFalseExpression() throws Exception {
		String output = newEngine()
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
		String output = newEngine().transform("${if falseString}NO${end}",
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
		String output = newEngine().transform("${if !falseCallable}YES${end}",
				model);
		assertEquals("YES", output);
	}

	@Test
	public void ifBooleanObjFalseExpression() throws Exception {
		String output = newEngine().transform(
				"${if bean.falseCondObj}${address}${else}NIX${end}",
				DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void ifMapTrueExpression() throws Exception {
		String output = newEngine().transform(
				"${if map}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifMapFalseExpression() throws Exception {
		String output = newEngine().transform(
				"${if emptyMap}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void ifCollectionTrueExpression() throws Exception {
		String output = newEngine().transform(
				"${if list}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifCollectionFalseExpression() throws Exception {
		String output = newEngine().transform(
				"${if emptyList}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void ifIterableTrueExpression() throws Exception {
		String output = newEngine().transform(
				"${if iterable}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifIterableFalseExpression() throws Exception {
		String output = newEngine().transform(
				"${if emptyIterable}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void ifArrayTrueExpression() throws Exception {
		String output = newEngine().transform(
				"${if array}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifArrayFalseExpression() throws Exception {
		String output = newEngine().transform(
				"${if emptyArray }${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void ifPrimitiveArrayTrueExpression() throws Exception {
		String output = newEngine().transform(
				"${if intArray}${address}${else}NIX${end}", DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void ifPrimitiveArrayFalseExpression() throws Exception {
		String output = newEngine()
				.transform("${if emptyIntArray }${address}${else}NIX${end}",
						DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void nestedIfExpression() throws Exception {
		String output = newEngine()
				.transform(
						"${if hugo}${address}${else}${if address}${address}${else}NIX${end}${end}",
						DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

	@Test
	public void nestedIfSkip() throws Exception {
		String output = newEngine().transform(
				"${if nix}Something${if address}${address}${end}${end}",
				DEFAULT_MODEL);
		assertEquals("", output);
	}

	@Test
	public void nestedIfElseSkip() throws Exception {
		String output = newEngine()
				.transform(
						"${if something}${else}${if something}${else}Something${if address}${address}${end}${end}${end}",
						DEFAULT_MODEL);
		assertEquals("", output);
	}

	@Test
	public void simpleForeach() throws Exception {
		String output = newEngine().transform(
				"${foreach list item}${item}\n${end}", DEFAULT_MODEL);
		assertEquals("1.1, 1.2\n" + "2.1, 2.2\n", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void foreachSingletonAsList() throws Exception {
		// if the variable we want to iterate over is atomic, we simply wrap
		// it into a singleton list
		String output = newEngine().transform(
				"${foreach address item}${item}${end}", DEFAULT_MODEL);
		String expected = newEngine().transform("${address}", DEFAULT_MODEL);
		assertEquals(expected, output);
	}

	@Test
	public void foreachArray() throws Exception {
		String output = newEngine().transform(
				"${foreach array item}${item}\n${end}", DEFAULT_MODEL);
		assertEquals("1.1, 1.2\n" + "2.1, 2.2\n", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void foreachPrimitiveArray() throws Exception {
		String output = newEngine().transform(
				"${foreach intArray item}${item}\n${end}", DEFAULT_MODEL);
		assertEquals("1\n2\n", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void foreachMap() throws Exception {
		String output = newEngine().transform(
				"${foreach map entry}${entry.key}=${entry.value}\n${end}",
				DEFAULT_MODEL);
		assertEquals("mapEntry1=mapValue1\n" + "mapEntry2=mapValue2\n", output);
		assertNull(DEFAULT_MODEL.get("item"));

	}

	@Test
	public void specialForeachVariables() throws Exception {
		String output = newEngine()
				.transform(
						"${foreach list item}${item}\n${if last_item}last${end}${if first_item}first${end}${if even_item} even${end}${if odd_item} odd${end}${end}",
						DEFAULT_MODEL);
		assertEquals("1.1, 1.2\nfirst even" + "2.1, 2.2\nlast odd", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void foreachSeparator() throws Exception {
		String output = newEngine()
				.transform(
						"${ \n foreach\n\t  list  \r  item   ,}${item.property1}${end}",
						DEFAULT_MODEL);
		assertEquals("1.1  ,2.1", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void crazyForeachSeparator() throws Exception {
		String output = newEngine()
				.transform("${ foreach list item  }${item.property1}${end}",
						DEFAULT_MODEL);
		assertEquals("1.1 2.1", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void newlineForeachSeparator() throws Exception {
		String output = newEngine().transform(
				"${ foreach list item \n}${item.property1}${end}",
				DEFAULT_MODEL);
		assertEquals("1.1\n2.1", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void propertyForeach() throws Exception {
		String output = newEngine().transform(
				"${foreach list item}${item.property1}\n${end}", DEFAULT_MODEL);
		assertEquals("1.1\n" + "2.1\n", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void nestedForeach() throws Exception {
		String output = newEngine()
				.transform(
						"${foreach list item}${foreach item.list item2}${item2.property1}${end}\n${end}",
						DEFAULT_MODEL);
		assertEquals("1.12.1\n" + "1.12.1\n", output);
		assertNull(DEFAULT_MODEL.get("item"));
		assertNull(DEFAULT_MODEL.get("item2"));
	}

	@Test
	public void ifInNestedForeach() throws Exception {
		String output = newEngine()
				.transform(
						"${foreach list item}${foreach item.list item2}${if item}${item2.property1}${end}${end}\n${end}",
						DEFAULT_MODEL);
		assertEquals("1.12.1\n" + "1.12.1\n", output);
	}

	@Test
	public void emptyForeach() throws Exception {
		String output = newEngine().transform(
				"${foreach emptyList item}${item.property1}\n${end}",
				DEFAULT_MODEL);
		assertEquals("", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void ifInForeach() throws Exception {
		String output = newEngine()
				.transform(
						"${foreach list item}${if item}${item}${if hugo}${item}${end}${end}\n${end}",
						DEFAULT_MODEL);
		assertEquals("1.1, 1.2\n" + "2.1, 2.2\n", output);
		assertNull(DEFAULT_MODEL.get("item"));
	}

	@Test
	public void elseInForeach() throws Exception {
		String output = newEngine()
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
		String output = newEngine().transform(template, DEFAULT_MODEL);
		assertEquals("Nüscht", output);
	}

	@Test
	public void foreachDoubleVarName() throws Exception {
		String output = newEngine()
				.transform(
						"${foreach list item  }${item}:${foreach strings item ,}${item}${end}${end}",
						DEFAULT_MODEL);
		assertEquals(
				"1.1, 1.2:String1,String2,String3 2.1, 2.2:String1,String2,String3",
				output);
	}

	@Test
	public void simpleEscaping() throws Exception {
		String output = newEngine().transform("\\${\\}\n\\\\}", DEFAULT_MODEL);
		assertEquals("${}\n\\}", output);
	}

	@Test
	public void escapingKernel() throws Exception {
		String output = Util.MINI_PARSER.unescape("\\${\\}\n\\\\}");
		assertEquals("${}\n\\}", output);
	}

	@Test
	public void complexEscaping() throws Exception {
		String output = newEngine()
				.transform(
						"${foreach list item \\${\n \\},}${item.property1}${end}\n\\\\",
						DEFAULT_MODEL);
		assertEquals("1.1${\n },2.1\n\\", output);
	}

	@Test
	public void quotes() throws Exception {
		String output = newEngine().transform("\"${int}\" \\\"", DEFAULT_MODEL);
		assertEquals("\"0\" \"", output);
	}

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
				"<h1>${if address}${address;long(full)}${else}NIX${end}</h1>",
				DEFAULT_MODEL);
		String shortCut = ENGINE_WITH_CUSTOM_RENDERERS.transform(
				"${<h1>,address(NIX),</h1>;long(full)}", DEFAULT_MODEL);
		assertEquals(full, shortCut);
	}

	@Test
	public void defaultShortcutFormat() throws Exception {
		String full = ENGINE_WITH_CUSTOM_RENDERERS.transform(
				"${if address}${address;long(full)}${else}NIX${end}",
				DEFAULT_MODEL);
		String shortCut = ENGINE_WITH_CUSTOM_RENDERERS.transform(
				"${address(NIX);long(full)}", DEFAULT_MODEL);
		assertEquals(full, shortCut);
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

	@Test
	public void allVariables() throws Exception {
		Set<String> output = newEngine()
				.getUsedVariables(
						"${foreach strings string}${if string='String2'}${string}${adresse}${end}${end}${if !int}${date}${end}");
		// string is a local variable and should not be included here
		assertArrayEquals(new String[] { "adresse", "date", "int", "strings" },
				output.toArray());
	}

	@Test
	public void callable() throws Exception {
		Callable<Date> date = new Callable<Date>() {

			@Override
			public Date call() throws Exception {
				return new Date(0);
			}
		};
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("date", date);
		String output = ENGINE_WITH_NAMED_RENDERERS.transform(
				"${date;date(yyyy.MM.dd HH:mm:ss z)}", model);
		assertEquals("1970.01.01 01:00:00 MEZ", output);
	}

	@Test
	public void expression() throws Exception {
		TemplateExpression<Boolean> oddExpression = new TemplateExpression<Boolean>() {

			@Override
			public Boolean eval(TemplateContext context) {
				ForEachToken foreach = context.peek(ForEachToken.class);
				if (foreach != null) {
					return foreach.getIndex() % 2 == 1;
				}
				return false;
			}
		};
		Map<String, Object> model = new HashMap<String, Object>();
		model.putAll(DEFAULT_MODEL);
		model.put("oddExpression", oddExpression);

		String output = newEngine().transform(
				"${foreach list item}${item}\n" + "${if last_item}last${end}"
						+ "${if first_item}first${end}"
						+ "${if even_item} even${end}"
						+ "${if oddExpression} odd${end}${end}", model);
		assertEquals("1.1, 1.2\nfirst even" + "2.1, 2.2\nlast odd", output);
	}

	@Test
	public void callableForeach() throws Exception {
		Callable<List<String>> foreach = new Callable<List<String>>() {

			@Override
			public List<String> call() throws Exception {
				return Arrays.asList(new String[] { "i1", "i2", "i3" });
			}
		};
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("foreach", foreach);
		String output = newEngine().transform(
				"${foreach foreach item , }${item}${end}", model);
		assertEquals("i1, i2, i3", output);
	}

	@Test
	public void compiledSimpleSample() throws Exception {
		String input = "${address}";
		String interpretedOutput = newEngine().transform(input, DEFAULT_MODEL);
		String compiledOutput = new SampleSimpleExpressionCompiledTemplate(
				newEngine()).transform(DEFAULT_MODEL);
		assertEquals(interpretedOutput, compiledOutput);
	}

	@Test
	public void compiledComplexSample() throws Exception {
		String input = "${<h1>,address(NIX),</h1>;long(full)}";
		String interpretedOutput = ENGINE_WITH_CUSTOM_RENDERERS.transform(
				input, DEFAULT_MODEL);
		String compiledOutput = new SampleComplexExpressionCompiledTemplate(
				ENGINE_WITH_CUSTOM_RENDERERS).transform(DEFAULT_MODEL);
		assertEquals(interpretedOutput, compiledOutput);
	}

	@Test
	public void compiledIfSample() throws Exception {
		String input = "${if !bean.trueCond}${address}${else}NIX${end}";
		String interpretedOutput = newEngine().transform(input, DEFAULT_MODEL);
		String compiledOutput = new SampleIfEmptyFalseExpressionCompiledTemplate(
				newEngine()).transform(DEFAULT_MODEL);
		assertEquals(interpretedOutput, compiledOutput);
	}

	@Test
	public void compiledForeachSample() throws Exception {
		String input = "${ foreach list item \n}${item.property1}${end}";
		String interpretedOutput = newEngine().transform(input, DEFAULT_MODEL);
		String compiledOutput = new SampleNewlineForeachSeparatorCompiledTemplate(
				newEngine()).transform(DEFAULT_MODEL);
		assertEquals(interpretedOutput, compiledOutput);
	}

	@Test
	public void compiledSequenceSample() throws Exception {
		String input = "PREFIX${<h1>,address(NIX),</h1>;long(full)}SUFFIX";
		String interpretedOutput = newEngine().transform(input, DEFAULT_MODEL);
		String compiledOutput = new SampleCompiledSequenceTemplate(newEngine())
				.transform(DEFAULT_MODEL);
		assertEquals(interpretedOutput, compiledOutput);
	}

	@Test
	@Ignore
	public void compiledNestedSample() throws Exception {
		String input = "${foreach list item}${foreach item.list item2}OUTER_PRFIX${if item}${item2.property1}INNER_SUFFIX${end}${end}\n${end}";
		String interpretedOutput = newEngine().transform(input, DEFAULT_MODEL);
		String compiledOutput = new SampleNestedExpressionCompiledTemplate(newEngine())
				.transform(DEFAULT_MODEL);
		assertEquals(interpretedOutput, compiledOutput);
	}

}