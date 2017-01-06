package com.floreysoft.jmte;

import static com.floreysoft.jmte.message.ErrorMessage.*;

import com.floreysoft.jmte.message.ErrorEntry;
import com.floreysoft.jmte.message.ErrorMessage;
import com.floreysoft.jmte.message.JournalingErrorHandler;
import com.floreysoft.jmte.renderer.OptionRenderFormatInfo;
import com.floreysoft.jmte.template.ErrorReportingOutputAppender;
import com.floreysoft.jmte.token.Token;
import com.floreysoft.jmte.util.StartEndPair;
import com.floreysoft.jmte.util.Util;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.util.*;

import static org.junit.Assert.*;

public class ErrorReportingTest {

	private Engine newInlineErrorEngine() {
		final Engine engine = new Engine();
		engine.setErrorHandler(new JournalingErrorHandler());
		engine.setOutputAppender(new ErrorReportingOutputAppender());
		return engine;
	}

	@Test
	public void nonArray() throws Exception {
		final Map<String, Object> model = new HashMap<String, Object>();
		final Map<String, Object> el1 = new HashMap<String, Object>();
		el1.put("name", "Olli");
		model.put("notArray", el1);

		final Engine engine = newInlineErrorEngine();
		String output = engine.transform("${notArray[1].name}", model);
		assertEquals("[!!not-array-error|You can not access non-array '{name=Olli}' as an array|${notArray[1].name}!!]", output);
	}

    @Test
    public void noStaticErrorNonArray() {
        List<ErrorEntry>  staticErrors = new Engine().getStaticErrors("${notArray[1].name}");
        assertEquals(0, staticErrors.size());
    }

    @Test
	public void invalidExpression() {
		final Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", "Olli");

		final Engine engine = newInlineErrorEngine();
		String output = engine.transform("${sjhdjsdh ${name}", model);
		assertEquals("[!!invalid-expression|Invalid expression!|${sjhdjsdh ${name}!!]", output);
	}

    @Test
    public void staticInvalidExpression() {
        List<ErrorEntry>  staticErrors = new Engine().getStaticErrors("${sjhdjsdh ${name}");
        assertEquals(1, staticErrors.size());
        assertTrue(staticErrors.get(0).errorMessage == INVALID_EXPRESSION);
    }

    @Test
	public void unmatchedEnd() {
		final Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", "Olli");

		final Engine engine = newInlineErrorEngine();
		String output = engine.transform("${name}${end}no end?", model);
		assertEquals("Olli[!!unmatched-end|Unmatched end|${end}!!]no end?", output);
	}

    @Test
	public void staticUnmatchedEnd() {
		List<ErrorEntry>  staticErrors = new Engine().getStaticErrors("${name}${end}no end?");
		assertEquals(1, staticErrors.size());
        assertTrue(staticErrors.get(0).errorMessage == UNMATCHED_END);
	}

	@Test
	public void noErrorMatchedEnd() {
		final Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", "Olli");

		final Engine engine = newInlineErrorEngine();
		String output = engine.transform("${if name}${name}${end}no end?", model);
		assertEquals("Ollino end?", output);
	}

	@Test
	public void elseOutOfScope() {
		final Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", "Olli");

		final Engine engine = newInlineErrorEngine();
		String output = engine.transform("${name}${else}no if?", model);
		assertEquals("Olli[!!else-out-of-scope|Can't use else outside of if block!|${else}!!]no if?", output);
	}

    @Test
    public void staticElseOutOfScope() {
        List<ErrorEntry>  staticErrors = new Engine().getStaticErrors("${name}${else}no if?");
        assertEquals(1, staticErrors.size());
        assertTrue(staticErrors.get(0).errorMessage == ELSE_OUT_OF_SCOPE);
    }

    @Test
	public void noErrorElseInScope() {
		final Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", "Olli");

		final Engine engine = newInlineErrorEngine();
		String output = engine.transform("_start_${if name}${name}${else}no name${end}_end_", model);
		assertEquals("_start_Olli_end_", output);
	}

    @Test
	public void missingEnd() {
		final Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", "Olli");

		final Engine engine = newInlineErrorEngine();
		String output = engine.transform("${if name}${name}no end?", model);
		assertEquals("Ollino end?[!!missing-end|Missing end|!!]", output);
	}

    @Test
    public void staticMissingEnd() {
        List<ErrorEntry>  staticErrors = new Engine().getStaticErrors("${if name}${name}no end?");
        assertEquals(1, staticErrors.size());
        assertTrue(staticErrors.get(0).errorMessage == MISSING_END);
    }

    @Test
	public void callOnString() {
		final Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", "Olli");

		final Engine engine = newInlineErrorEngine();
		String output = engine.transform("_start_${name.lastName}_end_", model);
		assertEquals("_start_[!!no-call-on-string|You can not make property calls on string 'Olli'!|${name.lastName}!!]_end_", output);
	}

    @Test
	public void noSuchProperty() {
		final Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", new Object() {
            @Override
            public String toString() {
                return "myObject";
            }
        });

		final Engine engine = newInlineErrorEngine();
		String output = engine.transform("_start_${name.doesNotExist}_end_", model);
		assertEquals("_start_[!!property-access-error|Property 'doesNotExist' on object 'myObject' can not be accessed: \"java.lang.NoSuchFieldException: doesNotExist\"!|${name.doesNotExist}!!]_end_", output);
	}

    @Test
	public void arrayOutOfBounds() {
		final Map<String, Object> model = new HashMap<String, Object>();
		final List<String> el1 = new ArrayList<>();
		el1.add("Olli");
		model.put("array", el1);

		final Engine engine = newInlineErrorEngine();
		String output = engine.transform("_start_${array[2]}_end_", model);
		assertEquals("_start_[!!index-out-of-bounds-error|Index '2' on array '[Olli]' does not exist|${array[2]}!!]_end_", output);
	}

    @Test
    public void noStaticErrorArrayOutOfBounds() {
        List<ErrorEntry>  staticErrors = new Engine().getStaticErrors("_start_${array[2]}_end_");
        assertEquals(0, staticErrors.size());
    }

    @Test
	public void invalidIndex() {
		final Map<String, Object> model = new HashMap<String, Object>();
		final List<String> el1 = new ArrayList<>();
		el1.add("Olli");
		model.put("array", el1);

		final Engine engine = newInlineErrorEngine();
		String output = engine.transform("_start_${array[NIX]}_end_", model);
		assertEquals("_start_[!!invalid-index-error|'NIX' on array '[Olli]' is not a valid index|${array[NIX]}!!]_end_", output);
	}

	@Test
	public void invalidArraySyntaxInverse() {
		final Map<String, Object> model = new HashMap<String, Object>();
		final List<String> el1 = new ArrayList<>();
		el1.add("Olli");
		model.put("array", el1);

		final Engine engine = newInlineErrorEngine();
		String output = engine.transform("_start_${array]NIX[}_end_", model);
		assertEquals("_start_[!!invalid-array-syntax|'array]NIX[' is not a valid array syntax|${array]NIX[}!!]_end_", output);
	}

	@Test
	public void invalidArraySyntaxNoClose() {
		final Map<String, Object> model = new HashMap<String, Object>();
		final List<String> el1 = new ArrayList<>();
		el1.add("Olli");
		model.put("array", el1);

		final Engine engine = newInlineErrorEngine();
		String output = engine.transform("_start_${array[NIX}_end_", model);
		assertEquals("_start_[!!invalid-array-syntax|'array[NIX' is not a valid array syntax|${array[NIX}!!]_end_", output);
	}

	@Test
	public void invalidArraySyntaxNoOpen() {
		final Map<String, Object> model = new HashMap<String, Object>();
		final List<String> el1 = new ArrayList<>();
		el1.add("Olli");
		model.put("array", el1);

		final Engine engine = newInlineErrorEngine();
		String output = engine.transform("_start_${arrayNIX]}_end_", model);
		assertEquals("_start_[!!invalid-array-syntax|'arrayNIX]' is not a valid array syntax|${arrayNIX]}!!]_end_", output);
	}

	@Test
    public void noStaticErrorInvalidIndex() {
        List<ErrorEntry>  staticErrors = new Engine().getStaticErrors("_start_${array[NIX]}_end_");
        assertEquals(0, staticErrors.size());
    }

    @Test
    public void missingForEachVariable() {
        final Map<String, Object> model = new HashMap<String, Object>();
        final List<String> el1 = new ArrayList<>();
        el1.add("Olli");
        model.put("array", el1);

        final Engine engine = newInlineErrorEngine();
        String output = engine.transform("${foreach array}${element}${end}", model);
        assertEquals("[!!foreach-undefined-varname|Missing variable name in foreach|${foreach array}!!]", output);
    }

    @Test
    public void staticMissingForEachVariable() {
        List<ErrorEntry>  staticErrors = new Engine().getStaticErrors("${foreach array}${element}${end}");
        assertEquals(1, staticErrors.size());
        assertTrue(staticErrors.get(0).errorMessage == FOR_EACH_UNDEFINED_VARNAME);
    }

}


