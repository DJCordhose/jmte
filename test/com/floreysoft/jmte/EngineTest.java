package com.floreysoft.jmte;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;

import com.floreysoft.jmte.message.*;
import com.floreysoft.jmte.renderer.NullRenderer;
import com.floreysoft.jmte.renderer.OptionRenderFormatInfo;
import com.floreysoft.jmte.renderer.SimpleNamedRenderer;
import com.floreysoft.jmte.template.ErrorReportingOutputAppender;
import com.floreysoft.jmte.util.StartEndPair;
import org.junit.Ignore;
import org.junit.Test;

import com.floreysoft.jmte.encoder.XMLEncoder;
import com.floreysoft.jmte.renderer.RawRenderer;
import com.floreysoft.jmte.sample.NamedDateRenderer;
import com.floreysoft.jmte.sample.NamedStringRenderer;
import com.floreysoft.jmte.token.AnnotationToken;
import com.floreysoft.jmte.token.ForEachToken;
import com.floreysoft.jmte.token.Token;
import com.floreysoft.jmte.util.Util;

@SuppressWarnings("rawtypes")
public class EngineTest {

	static final DefaultModelAdaptor MODEL_ADAPTOR = new DefaultModelAdaptor();
	public static final String LONG_TEMPLATE_MANY_ITERATIONS = "${foreach longList item}"
			+ "SOME TEXT"
			+ "${if address='Filbert'}${address}${else}NIX${end}"
			+ "${foreach strings string}${if string='String2'}${string}${end}${end}"
			+ "${if bean.trueCond}${address}${else}NIX${end}"
			+ "${if bean.trueCondObj}${address}${else}NIX${end}"
			+ "${if map}${address}${else}NIX${end}"
			+ "MORE TEXT"
			+ "${if hugo}${address}${else}${if address}${address}${else}NIX${end}${end}"
			+ "${if nix}Something${if address}${address}${end}${end}"
			+ "${if something}${else}${if something}${else}Something${if address}${address}${end}${end}${end}"
			+ "\n${end}"
			+ "${foreach list item}${foreach item.list item2}${if item}${item2.property1}${end}${end}\n${end}";
	public static final String LONG_TEMPLATE = "SOME TEXT"
			+ "${if address='Filbert'}${address}${else}NIX${end}"
			+ "${foreach strings string}${if string='String2'}${string}${end}${end}"
			+ "${if bean.trueCond}${address}${else}NIX${end}"
			+ "${if bean.trueCondObj}${address}${else}NIX${end}"
			+ "${if map}${address}${else}NIX${end}"
			+ "MORE TEXT"
			+ "${if hugo}${address}${else}${if address}${address}${else}NIX${end}${end}"
			+ "${if nix}Something${if address}${address}${end}${end}"
			+ "${if something}${else}${if something}${else}Something${if address}${address}${end}${end}${end}"
			+ "${foreach list item}${foreach item.list item2}${if item}${item2.property1}${end}${end}\n${end}";
	private static final int SIZE_LONG_LIST = 1000;

	protected Engine newEngine() {
		Engine engine = Engine.createEngine();
		return engine;
	}

	// used to suppress error messages on stderr
	protected ErrorHandler getTestErrorHandler() {
		return new AbstractErrorHandler() {

			@Override
			public void error(ErrorMessage errorMessage, Token token,
					Map<String, Object> parameters) throws ParseException {
				Message message = new ResourceBundleMessage(errorMessage.key)
						.withModel(parameters).onToken(token);
				throw new ParseException(message);
			}
		};
	}

	final Engine ENGINE_WITH_CUSTOM_RENDERERS = newEngine().registerRenderer(
			Object.class, new Renderer<Object>() {

				@Override
				public String render(Object o, Locale locale, Map<String,Object> model) {
					return "Object=" + o.toString();
				}
			}).registerRenderer(MyBean.class, new Renderer<MyBean>() {

		@Override
		public String render(MyBean o, Locale locale, Map<String,Object> model) {
			return "Render=" + o.property1.toString();
		}

	});
	final Engine ENGINE_WITH_NAMED_RENDERERS = ENGINE_WITH_CUSTOM_RENDERERS
			.registerNamedRenderer(new NamedDateRenderer())
			.registerNamedRenderer(new NamedStringRenderer());

	private static final MyBean MyBean1 = new MyBean("1.1", "1.2");
	private static final MyBean MyBean2 = new MyBean("2.1", "2.2");

	private static final class RawNoopRenderer implements Renderer<String>, RawRenderer {
		@Override
		public String render(String o, Locale locale, Map<String,Object> model) {
			return o;
		}
	}

	private static final class RawNamedNoopRenderer implements NamedRenderer, RawRenderer {

		@Override
		public String render(Object o, String format, Locale locale, Map<String,Object> model) {
			return o.toString();
		}

		@Override
		public String getName() {
			return "raw";
		}

		@Override
		public RenderFormatInfo getFormatInfo() {
			return null;
		}

		@Override
		public Class<?>[] getSupportedClasses() {
			Class<?>[] clazzes = { String.class };
			return clazzes;
		}
	}

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

	public static enum MyType {
		TYPE_A, TYPE_B;
	}

	public static class MyBean {
		public Map<MyType, String> mapEnumAsKey = new HashMap<>();

		private Object property1 = "propertyValue1";
		public Object property2 = "propertyValue2";
		public boolean falseCond = false;
		public Boolean falseCondObj = new Boolean(false);
		public MyBean[] array = ARRAY;

		public MyBean(Object property1, Object property2) {
			this.property1 = property1;
			this.property2 = property2;
		}

		public MyBean() {
			mapEnumAsKey.put(MyType.TYPE_A, "A");
			mapEnumAsKey.put(MyType.TYPE_B, "B");
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

	private final static List<String> LONG_LIST = new ArrayList<String>();
	static {
		for (int i = 0; i < SIZE_LONG_LIST; i++) {
			LONG_LIST.add("list_entry_" + i);
		}
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

	final static Locale DEFAULT_LOCALE = Locale.getDefault();

	final static Map<String, Object> DEFAULT_MODEL = new HashMap<String, Object>();
	static {
		DEFAULT_MODEL.put("something", "something");
		DEFAULT_MODEL.put("address", "Filbert");
		DEFAULT_MODEL.put("addressWithSpace", "Filbert Street");
		DEFAULT_MODEL.put("map", MAP);
		DEFAULT_MODEL.put("list", LIST);
		DEFAULT_MODEL.put("longList", LONG_LIST);
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
		DEFAULT_MODEL.put("bigDecimal0", new BigDecimal("0"));
		DEFAULT_MODEL.put("bigDecimal1", new BigDecimal("1.0"));
	}

	@Test
	public void transformRobustToNull() {
		String output = newEngine()
				.transform(null, new HashMap<>());
		assertNull(output);
	}

	@Test
	public void variableName() throws Exception {
		Map<String, Object> simpleModel = new HashMap<String, Object>();
		simpleModel
				.put("http://www.google.com/m8/feeds/groups/daniel.florey%40gmail.com/base/16e7715c8a9e5849",
                        "true");
		simpleModel
				.put("http://www.google.com/m8/feeds/groups/daniel.florey%40gmail.com/base/6",
						"true");

		String output = newEngine()
				.transform(
						"${if http://www\\.google\\.com/m8/feeds/groups/daniel\\.florey%40gmail\\.com/base/16e7715c8a9e5849}works${else}does not work${end}",
						simpleModel);
		assertEquals("works", output);
	}

	@Test
	public void format() throws Exception {
		String output = newEngine().format("${if 1}${1}${else}${2}${end}",
                "arg1", "arg2");
		assertEquals("arg1", output);
		// just to prove that shortcut for false-branch works
		newEngine().format("${if 1}${1}${else}${2}${end}", "arg1");

		// check for null values
		output = newEngine().format("${if 1}${1}${else}${2}${end}", null,
				"arg2");
		assertEquals("arg2", output);

		// check for boolean values
		output = newEngine().format("${if 1}${1}${else}${2}${end}", false,
				"arg2");
		assertEquals("arg2", output);
	}

	@Test
	public void formatNamed() throws Exception {
		String output = newEngine().transform("${if 1}${2}${else}broken${end}",
                new ModelBuilder("1", "arg1", "2", "arg2").build());
		assertEquals("arg2", output);
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
			Engine newEngine = newEngine();
			newEngine.setErrorHandler(getTestErrorHandler());
			newEngine.transform("\n${address}\n     ${else}NIX${end}",
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
	public void indexedArrayAccess() throws Exception {
		final Map<String, Object> model = createIndexArrayMock();

		String output = newEngine().transform("${array[1].name}", model);
		assertEquals("Olli", output);
	}

	@Test
	public void indexedArrayRangedAccess() throws Exception {
		final Map<String, Object> model = createIndexArrayMock();

		String output = newEngine().transform("${array2[0,1]}", model);
		assertEquals("[{name=first}, {name=second}]", output);
	}

	@Test
	public void indexedArrayRangedEndAccess() throws Exception {
		final Map<String, Object> model = createIndexArrayMock();

		String output = newEngine().transform("${array2[0,]}", model);
		assertEquals("[{name=first}, {name=second}, {name=third}]", output);
	}

	@Test
	public void indexedArrayAccessOfIndexedArray() throws Exception {
		final Map<String, Object> model = createIndexArrayMock();

		String output = newEngine().transform("${array1[0].array2[0].name}", model);
		assertEquals("Olli", output);
	}

	@Test
	public void indexOutOfBoundsArrayAccess() throws Exception {
		final Map<String, Object> model = createIndexArrayMock();

		final Engine engine = newEngine();
		final JournalingErrorHandler errorHandler = new JournalingErrorHandler();
		engine.setErrorHandler(errorHandler);
		String output = engine.transform("${array[3].name}", model);
		assertEquals("", output);
		assertEquals(1, errorHandler.entries.size());
		assertEquals("Index '3' on array '[{}, {name=Olli}]' does not exist", errorHandler.entries.get(0).formattedMessage.formatPlain());
	}

	@Test
	public void indexedLastArrayAccess() throws Exception {
		final Map<String, Object> model = createIndexArrayMock();

		String output = newEngine().transform("${array[last].name}", model);
		assertEquals("Olli", output);
	}

	@Test
	public void arrayLengthAccess() throws Exception {
		final Map<String, Object> model = createIndexArrayMock();

		String output = newEngine().transform("${array.length}", model);
		assertEquals("2", output);
	}

	@Test
	public void notArrayLengthAccess() throws Exception {
		final Map<String, Object> model = new HashMap<String, Object>();
		final Map<String, Object> el1 = new HashMap<String, Object>();
		el1.put("name", "Olli");
		el1.put("length", 25);
		model.put("notArray", el1);

		String output = newEngine().transform("${notArray.length}", model);
		assertEquals("25", output);
	}

	@Test
	public void notArrayIndexAccess() throws Exception {
		final Map<String, Object> model = new HashMap<String, Object>();
		final Map<String, Object> el1 = new HashMap<String, Object>();
		el1.put("name", "Olli");
		model.put("notArray", el1);

		final Engine engine = newEngine();
		final JournalingErrorHandler errorHandler = new JournalingErrorHandler();
		engine.setErrorHandler(errorHandler);
		String output = engine.transform("${notArray[1].name}", model);
		assertEquals(1, errorHandler.entries.size());
		assertEquals("You can not access non-array '{name=Olli}' as an array", errorHandler.entries.get(0).formattedMessage.formatPlain());
	}

	private Map<String, Object> createIndexArrayMock() {
		final Map<String, Object> model = new HashMap<String, Object>();
		final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		final Map<String, Object> el0 = new HashMap<String, Object>();
		list.add(el0);
		final Map<String, Object> el1 = new HashMap<String, Object>();
		el1.put("name", "Olli");
		list.add(el1);
		model.put("array", list);

		final List<Map<String, Object>> array1 = new ArrayList<Map<String, Object>>();
		final List<Map<String, Object>> array2 = new ArrayList<Map<String, Object>>();
		final Map<String, Object> el1_0 = new HashMap<String, Object>();
		el1_0.put("array2", array2);
		array1.add(el1_0);
		final Map<String, Object> el2_0 = new HashMap<String, Object>();
		el2_0.put("name", "Olli");
		array2.add(el2_0);
		model.put("array1", array1);

		final List<Map<String, Object>> list2 = new ArrayList<Map<String, Object>>();
		final Map<String, Object> el1_1 = new HashMap<String, Object>();
		final Map<String, Object> el2_1 = new HashMap<String, Object>();
		final Map<String, Object> el3_1 = new HashMap<String, Object>();
		el1_1.put("name", "first");
		list2.add(el1_1);
		el2_1.put("name", "second");
		list2.add(el2_1);
		el3_1.put("name", "third");
		list2.add(el3_1);
		model.put("array2", list2);

		return model;
	}

	@Test
	public void stringIndexAccess(){
		String output = newEngine().transform("${address[0]}", DEFAULT_MODEL);
		assertEquals("F", output);
	}

	@Test
	public void stringIndexRangeAccess(){
		String output = newEngine().transform("${address[0,3]}", DEFAULT_MODEL);
		assertEquals("Fil", output);
	}

	@Test
	public void stringIndexRangeEndAccess(){
		String output = newEngine().transform("${address[2,]}", DEFAULT_MODEL);
		assertEquals("lbert", output);
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
		Engine newEngine = newEngine();
		newEngine.setErrorHandler(getTestErrorHandler());
		newEngine.transform("${address}${else}NIX${end}", DEFAULT_MODEL);
	}

	@Test(expected = ParseException.class)
	public void endWithoutBlockError() throws Exception {
		Engine newEngine = newEngine();
		newEngine.setErrorHandler(getTestErrorHandler());
		newEngine.transform("${address}${end}", DEFAULT_MODEL);
	}

	@Test(expected = ParseException.class)
	public void invalidExpressionError() throws Exception {
		Engine newEngine = newEngine();
		newEngine.setErrorHandler(getTestErrorHandler());
		newEngine.transform("${loop does not exist}", DEFAULT_MODEL);
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
		assertEquals("Filbert</h1>", shortCut);
	}

	@Test
	public void wrapKeepWS() throws Exception {
		String shortCut = newEngine().transform("${   ,address,  }",
				DEFAULT_MODEL);
		assertEquals("   Filbert  ", shortCut);
	}

	@Test
	public void wrapNoPost() throws Exception {
		String shortCut = newEngine().transform("${<h1>,address,}",
				DEFAULT_MODEL);
		assertEquals("<h1>Filbert", shortCut);
	}

	@Test
	public void wrapEscaping() throws Exception {
		String template = "${ \\,,address,, }";
		String shortCut = newEngine().transform(template, DEFAULT_MODEL);
		assertEquals(" ,Filbert, ", shortCut);
	}

	@Test
	public void wrapSkipWS() throws Exception {
		String template = "${, address,}";
		String shortCut = newEngine().transform(template, DEFAULT_MODEL);
		assertEquals("Filbert", shortCut);
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
	public void stringEqQuoteWithSpace() throws Exception {
		String output = newEngine().transform(
				"${if addressWithSpace='Filbert Street'}${addressWithSpace}${else}NIX${end}",
				DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("addressWithSpace"), output);
	}

    @Test
    public void stringEqWithSpace() throws Exception {
        String output = newEngine().transform(
                "${if addressWithSpace=Filbert Street}${addressWithSpace}${else}NIX${end}",
                DEFAULT_MODEL);
        assertEquals(DEFAULT_MODEL.get("addressWithSpace"), output);
    }

    @Test
	public void stringEqQuote() throws Exception {
		String output = newEngine().transform(
				"${if address='Filbert'}${address}${else}NIX${end}",
				DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("address"), output);
	}

    @Test
    public void stringEqDobuleQuote() throws Exception {
        String output = newEngine().transform(
                "${if address=\"Filbert\"}${address}${else}NIX${end}",
                DEFAULT_MODEL);
        assertEquals(DEFAULT_MODEL.get("address"), output);
    }

	@Test
	public void objectNeq() throws Exception {
		String output = newEngine().transform(
				"${if !bigDecimal0=0}${bigDecimal0}${else}NIX${end}",
				DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void objectEq() throws Exception {
		String output = newEngine().transform(
				"${if bigDecimal1='1.0'}${bigDecimal1}${else}NIX${end}",
				DEFAULT_MODEL);
		assertEquals(DEFAULT_MODEL.get("bigDecimal1").toString(), output);
	}

	@Test
	public void stringEqNotElse() throws Exception {
		String output = newEngine().transform(
                "${if !address='Filbert'}${address}${else}NIX${end}",
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
	public void simpleForeachIgnoredNewlinesInsideTags() throws Exception {
		String output = newEngine().transform(
				"${\nforeach list item}${item}${\nend\n}", DEFAULT_MODEL);
		assertEquals("1.1, 1.2" + "2.1, 2.2", output);
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
    public void loopModeArrayforeachMap() throws Exception {
        Engine engine = newEngine();
        engine.setModelAdaptor(new DefaultModelAdaptor(DefaultModelAdaptor.LoopMode.LIST));
        String output = engine.transform(
                "${foreach map entry}${entry.mapEntry1}, ${entry.mapEntry2}${end}",
                DEFAULT_MODEL);
        assertEquals("mapValue1, mapValue2", output);
    }

	@Test
	public void loopModeArrayforeachEntriesMap() throws Exception {
		Engine engine = newEngine();
		engine.setModelAdaptor(new DefaultModelAdaptor(DefaultModelAdaptor.LoopMode.LIST));
		String output = engine.transform(
				"${foreach map._entries entry}${entry.key}=${entry.value}\n${end}",
				DEFAULT_MODEL);
		assertEquals("mapEntry1=mapValue1\n" + "mapEntry2=mapValue2\n", output);
	}

	@Test
	public void loopModeArrayforeachKeysMap() throws Exception {
		Engine engine = newEngine();
		engine.setModelAdaptor(new DefaultModelAdaptor(DefaultModelAdaptor.LoopMode.LIST));
		String output = engine.transform(
				"${foreach map._keys entry}${entry}\n${end}",
				DEFAULT_MODEL);
		assertEquals("mapEntry1\n" + "mapEntry2\n", output);
	}

	@Test
	public void loopModeArrayforeachValuesMap() throws Exception {
		Engine engine = newEngine();
		engine.setModelAdaptor(new DefaultModelAdaptor(DefaultModelAdaptor.LoopMode.LIST));
		String output = engine.transform(
				"${foreach map._values entry}${entry}\n${end}",
				DEFAULT_MODEL);
		assertEquals("mapValue1\n" + "mapValue2\n", output);
	}

	@Test
    public void loopModeArrayForeachArray() throws Exception {
        Engine engine = newEngine();
        engine.setModelAdaptor(new DefaultModelAdaptor(DefaultModelAdaptor.LoopMode.LIST));
        String output = engine.transform(
                "${foreach array item}${item}\n${end}", DEFAULT_MODEL);
        assertEquals("1.1, 1.2\n" + "2.1, 2.2\n", output);
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
				+ "${foreach emptyList item}" + "${end}" + "${else}"
				+ "Nüscht" + "${end}";
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
				"Render=propertyValue1 and Render=propertyValue1 and Object=Filbert",
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
				clearTimezone("\"1970.01.01 01:00:00 MEZ\" and \"01.01.1970 01:00:00 +0100\" and  and String=Filbert(this is the format(no matter what I type; - this is part of the format))"),
				clearTimezone(output));
	}

	@Test
	public void namedRendererHasPrecedence() throws Exception {
		final Engine engine = newEngine();
		engine.registerRenderer(String.class, new Renderer<String>() {
			@Override
			public String render(String o, Locale locale, Map<String, Object> model) {
				return "typed="+o;
			}
		});
		engine.registerNamedRenderer(new SimpleNamedRenderer("string") {
			@Override
			public String render(Object o, String format, Locale locale, Map<String, Object> model) {
				if (o instanceof String) {
					return "named="+o;
				}
				return null;
			}
		});
		final Map<String, Object> model = new HashMap<>();
		model.put("string","string");
		String output = engine.transform(
						"${string;string}",
						model);
		assertEquals(
				"named=string",
				output);
	}

	@Test
	public void nullNotPassedIntoRenderer() throws Exception {
		final Engine engine = newEngine();
		engine.registerNamedRenderer(new SimpleNamedRenderer("string") {
			@Override
			public String render(Object o, String format, Locale locale, Map<String, Object> model) {
				return "named="+o;
			}
		});
		final Map<String, Object> model = new HashMap<>();
		// no value
//		model.put("string","string");
		String output = engine.transform(
				"${string;string}",
				model);
		assertEquals(
				"",
				output);
	}

	private static class NullStringRenderer extends SimpleNamedRenderer implements NullRenderer {
		public NullStringRenderer() {
			super("string");
		}

		@Override
		public String render(Object o, String format, Locale locale, Map<String, Object> model) {
			return "named="+o;
		}
	}

	@Test
	public void nullPassedIntoNullRenderer() throws Exception {
		final Engine engine = newEngine();
		engine.registerNamedRenderer(new NullStringRenderer());
		final Map<String, Object> model = new HashMap<>();
		// no value
//		model.put("string","string");
		String output = engine.transform(
				"${string;string}",
				model);
		assertEquals(
				"named=null",
				output);
	}

	private String clearTimezone(String timeString) {
		return timeString.replace("MEZ", "").replace("CET", "");
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
		assertEquals(clearTimezone("1970.01.01 01:00:00 MEZ"), clearTimezone(output));
	}

	@Test
	public void processor() throws Exception {
		Processor<Boolean> oddExpression = new Processor<Boolean>() {

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
	public void longList() throws Exception {
		String output = newEngine()
				.transform(
                        "${foreach longList item}STUFF${item}MORE_STUFF${item}\n${end}",
                        DEFAULT_MODEL);
		StringBuilder expected = new StringBuilder();
		for (int i = 0; i < SIZE_LONG_LIST; i++) {
			String item = "list_entry_" + i;
			expected.append("STUFF").append(item).append("MORE_STUFF")
					.append(item).append("\n");
		}
		assertEquals(expected.toString(), output);
	}

	@Test
	public void largeTemplateManyIterations() throws Exception {
		String template = LONG_TEMPLATE_MANY_ITERATIONS;
		final String expectedLine = "SOME TEXT" + "Filbert" + "String2"
				+ "Filbert" + "Filbert" + "Filbert" + "MORE TEXT" + "Filbert"
				+ "\n";

		String output = newEngine().transform(template, DEFAULT_MODEL);

		StringBuilder expected = new StringBuilder();
		for (int i = 0; i < SIZE_LONG_LIST; i++) {
			expected.append(expectedLine);
		}
		expected.append("1.12.1\n" + "1.12.1\n");

		assertEquals(expected.toString(), output);

	}

	@Test
	public void largeTemplate() throws Exception {
		String template = LONG_TEMPLATE;

		final String expected = "SOME TEXT" + "Filbert" + "String2" + "Filbert"
				+ "Filbert" + "Filbert" + "MORE TEXT" + "Filbert" + "1.12.1\n"
				+ "1.12.1\n";

		String output = newEngine().transform(template, DEFAULT_MODEL);
		assertEquals(expected.toString(), output);

	}

	@Test
	public void comment() throws Exception {
		String input = "${-- comment}${address}";
		String output = newEngine().transform(input, DEFAULT_MODEL);
		assertEquals("Filbert", output);
	}

	@Test
	public void annotation() throws Exception {
		String input = "${@type MyBean bean}${foreach bean.list item , }${item.property1}${end}";
		String output = newEngine().transform(input, DEFAULT_MODEL);
		assertEquals("1.1, 2.1", output);
	}

	@Test
	public void annotationProcessor() throws Exception {
		String input = "${@sample argument}${foreach bean.list item , }${item.property1}${end}";
		Engine engine = newEngine();
		engine.registerAnnotationProcessor(new AnnotationProcessor<String>() {

            @Override
            public String eval(AnnotationToken token, TemplateContext context) {
                return token.getArguments();
            }

            @Override
            public String getType() {
                return "sample";
            }
        });
		String output = engine.transform(input, DEFAULT_MODEL);
		assertEquals("argument1.1, 2.1", output);
	}

	@Test
	public void backslashInData() throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("str", "Hello \\ world!");
		String output = newEngine().transform("back\\\\slash for all: ${str}",
				model);
		assertEquals("back\\slash for all: Hello \\ world!", output);
	}
	
	@Test
	public void forachIterator() throws Exception {
		String actual = newEngine().transform("${foreach list i}${_it.property1}${end}", DEFAULT_MODEL);
		String output = newEngine().transform("${foreach list i}${i.property1}${end}", DEFAULT_MODEL);
		assertEquals(output, actual);
	}
	
	@Test		
	public void forachIteratorNested() throws Exception {
		String actual = newEngine()
				.transform(
                        "${foreach list item}${foreach _it.list item2}${_it.property1}${end}\n${end}",
                        DEFAULT_MODEL);
		String output = newEngine()
				.transform(
						"${foreach list item}${foreach item.list item2}${item2.property1}${end}\n${end}",
						DEFAULT_MODEL);
		assertEquals(output, actual);
	}
	
	@Test
	public void forachIndex() throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("array", new String[] { "item A", "item B", "item C" });

		Engine engine = newEngine();
		String actual = engine.transform("${foreach array item \n}${index_item}. ${item}${end}", model);
		String expected = "1. item A\n" + "2. item B\n" + "3. item C";
		assertEquals(expected, actual);
	}
	
	@Test
	public void nestedForachIndex() throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("array", new String[] { "outer#1", "outer#2"});
		model.put("array2", new String[] { "inner#1", "inner#2"});

		Engine engine = newEngine();
		String actual = engine.transform("${foreach array item \n}${index_item;index} ${item}\n${foreach array2 inner \n}${index_inner;index(a)} ${inner}${end}${end}", model);
		String expected = "1 outer#1\n" + "1 inner#1\n" +"2 inner#2\n" + "2 outer#2\n"+ "1 inner#1\n" +"2 inner#2";
		assertEquals(expected, actual);
	}


	private static NamedRenderer indexRenderer = new NamedRenderer() {
		
		@Override
		public String render(Object o, String format, Locale locale, Map<String,Object> model) {
			if (format != null && format.length() > 0 && Character.isLetter(format.charAt(0))) {
				// format is character, use small letter alpha formatter
				String string = o.toString();
				try {
					int parsedInt = Integer.parseInt(string);
					if (parsedInt > 0 && parsedInt <= 26) {
						return "abcdefghijklmnopqrstuvwxyz".charAt(parsedInt - 1) + ".";
					}
				} catch (NumberFormatException nfe) {
					// do nothing, simply fall back to default formatting
				}
			}
			// suppose we have a number a index, use it with a bit of formatting 
			return "(" + o.toString() + ")";
		}
		
		@Override
		public Class<?>[] getSupportedClasses() {
			return new Class[]{String.class};
		}
		
		@Override
		public String getName() {
			return "index";
		}
		
		@Override
		public RenderFormatInfo getFormatInfo() {
			return null;
		}
	};

	@Test
	public void forachIndexWithRenderer() throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("array", new String[] { "item A", "item B", "item C" });

		Engine engine = newEngine();
		// sample renderer that can do number and letter bullet points
		// can easily be extended to any other format
		engine.registerNamedRenderer(indexRenderer);
		
		String actual = engine.transform("${foreach array item \n}${index_item;index} ${item}${end}", model);
		String expected = "(1) item A\n" + "(2) item B\n" + "(3) item C";
		assertEquals(expected, actual);
	}

	@Test
	public void nestedForachIndexWithRenderer() throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("array", new String[] { "outer#1", "outer#2"});
		model.put("array2", new String[] { "inner#1", "inner#2"});

		Engine engine = newEngine();
		// sample renderer that can do number and letter bullet points
		// can easily be extended to any other format
		engine.registerNamedRenderer(indexRenderer);
		
		String actual = engine.transform("${foreach array item \n}${index_item;index} ${item}\n${foreach array2 inner \n}${index_inner;index(a)} ${inner}${end}${end}", model);
		String expected = "(1) outer#1\n" + "a. inner#1\n" +"b. inner#2\n" + "(2) outer#2\n"+ "a. inner#1\n" +"b. inner#2";
		assertEquals(expected, actual);
	}

	// Bug: https://code.google.com/p/jmte/issues/detail?id=23
	@Test
	public void slashWrappedInside() throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		Object book = new Object() {
			@SuppressWarnings("unused")
			public String getTitle() {
				return "whatever";
			}
		};
		model.put("book", book);
		Engine engine = newEngine();
		String actual = engine.transform("<input type='text' name='title' value=${book.title}/>", model);
		String expected = "<input type='text' name='title' value=whatever/>";
		assertEquals(expected, actual);
	}
	
	@Test
	public void xmlEncoder() throws Exception {
		Engine engine = newEngine();
		engine.setEncoder(new XMLEncoder());

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("toEncode", "&<>'\"");

		String actual = engine.transform("${toEncode}", model);
		assertEquals("&amp;&lt;&gt;&apos;&quot;", actual);
	}

	@Test
	public void xmlEncoderRawRenderer() throws Exception {
		Renderer<String> rawRenderer = new RawNoopRenderer();
		Engine engine = newEngine();
		engine.registerRenderer(String.class, rawRenderer);
		engine.setEncoder(new XMLEncoder());

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("toEncode", "&<>'\"");

		String actual = engine.transform("${toEncode}", model);
		assertEquals("&<>'\"", actual);
	}

	@Test
	public void xmlEncoderRawNamedRenderer() throws Exception {
		NamedRenderer rawRenderer = new RawNamedNoopRenderer();
		Engine engine = newEngine();
		engine.registerNamedRenderer(rawRenderer);
		engine.setEncoder(new XMLEncoder());

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("toEncode", "&<>'\"");

		String actual = engine.transform("${toEncode;raw}", model);
		assertEquals("&<>'\"", actual);
	}

	@Test
	public void enumAsKey() throws Exception {
		String output = newEngine().transform(
				"${bean.mapEnumAsKey.TYPE_A}", DEFAULT_MODEL);
		assertEquals("A", output);
	}

	@Test
	public void i18nVarNames() throws Exception {
		final Map<String, Object> model = new HashMap<String, Object>();
		model.put("åäüøöß爱", "Olli");

		String output = newEngine().transform("${åäüøöß爱}", model);
		assertEquals("Olli", output);
	}

	@Test
	public void i18nIfCondition() throws Exception {
		final Map<String, Object> model = new HashMap<String, Object>();
		model.put("åäüøöß爱", "åäüøöß爱");

		String output = newEngine().transform("${if åäüøöß爱='åäüøöß爱'}${åäüøöß爱}${else}NIX${end}", model);
		assertEquals("åäüøöß爱", output);
	}

	@Test
	public void gracefulIfErrorSpaces() throws Exception {
		final Map<String, Object> model = new HashMap<String, Object>();
		model.put("n a m e", "Olli");

		final Engine engine = newEngine();
		engine.setErrorHandler(new JournalingErrorHandler());
		String output = engine.transform("${if n a m e}NIX${end}", model);
	}

	@Test
	public void ifEqSample() throws Exception {
		String input = "${if address='Filbert'}${address}${else}NIX${end}";
		String output = newEngine().transform(input, DEFAULT_MODEL);
		assertEquals("Filbert", output);
	}

	@Test
	public void ifEqRendererSample() throws Exception {
		String input = "${if address;string='Filbert'}${address}${else}NIX${end}";
		String output = newEngine().transform(input, DEFAULT_MODEL);
		assertEquals("Filbert", output);
	}

	@Test
	public void ifEqRendererBracketsSample() throws Exception {
		String input = "${if address;string()='Filbert'}${address}${else}NIX${end}";
		String output = newEngine().transform(input, DEFAULT_MODEL);
		assertEquals("Filbert", output);
	}

	@Test
	public void ifEqSpacesSample() throws Exception {
		String input = "${if address = 'Filbert'}${address}${else}NIX${end}";
		String output = newEngine().transform(input, DEFAULT_MODEL);
		assertEquals("Filbert", output);
	}

	@Test
	public void ifEqSpacesElseSample() throws Exception {
		String input = "${if address = 'Filbert2'}${address}${else}NIX${end}";
		String output = newEngine().transform(input, DEFAULT_MODEL);
		assertEquals("NIX", output);
	}

	@Test
	public void ifEqSpacesRendererSample() throws Exception {
		String input = "${if address;string = 'Filbert'}${address}${else}NIX${end}";
		String output = newEngine().transform(input, DEFAULT_MODEL);
		assertEquals("Filbert", output);
	}

	@Test
	public void ifEqSpacesBracketsRendererSample() throws Exception {
		String input = "${if address;string() = 'Filbert'}${address}${else}NIX${end}";
		String output = newEngine().transform(input, DEFAULT_MODEL);
		assertEquals("Filbert", output);
	}

	@Test
	public void ifEqSpaceBeforeSample() throws Exception {
		String input = "${if address ='Filbert'}${address}${else}NIX${end}";
		String output = newEngine().transform(input, DEFAULT_MODEL);
		assertEquals("Filbert", output);
	}

	@Test
	public void ifEqSpaceAfterSample() throws Exception {
		String input = "${if address= 'Filbert'}${address}${else}NIX${end}";
		String output = newEngine().transform(input, DEFAULT_MODEL);
		assertEquals("Filbert", output);
	}

	@Test
	public void ifEqSegment() throws Exception {
		Map<String, String> headerEntry1 = new HashMap<>();
		Map<String, String> headerEntry2 = new HashMap<>();
		headerEntry1.put("name", "Subject");
		headerEntry1.put("value", "Hiho");
		headerEntry2.put("name", "From");
		headerEntry2.put("value", "Olli");

		ArrayList headers = new ArrayList();
		headers.add(headerEntry1);
		headers.add(headerEntry2);

		Map payload = new HashMap();
		payload.put("headers", headers);

		Map model = new HashMap();
		model.put("payload", payload);

		String input = "${foreach payload.headers header}${if header.name='Subject'}${header.value}${end}${end}";
		String output = newEngine().transform(input, model);
		assertEquals("Hiho", output);
	}

	@Test
	public void gracefulErrorSpaces() throws Exception {
		final Map<String, Object> model = new HashMap<String, Object>();
		model.put("n a m e", "Olli");

		final Engine engine = newEngine();
		engine.setErrorHandler(new JournalingErrorHandler());
		String output = engine.transform("${n a m e}", model);
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
