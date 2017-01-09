package com.floreysoft.jmte;

import com.floreysoft.jmte.renderer.AbstractParameterRenderer;
import com.floreysoft.jmte.renderer.RawRenderer;
import org.junit.Test;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;

public class BooleanIfRendererTest {

    private static class DummyStringRenderer extends AbstractParameterRenderer<String> implements RawRenderer {

        @Override
        public String getName() {
            return "dummy";
        }

        @Override
        public RenderFormatInfo getFormatInfo() {
            return null;
        }

        @Override
        public Class<?>[] getSupportedClasses() {
            return new Class<?>[0];
        }

        @Override
        protected String parse(Object o) {
            return o.toString();
        }

        @Override
        protected String format(String value, Locale locale, Map<String, String> parameters, Map<String, Object> model) {
            if (value == null) {
                return "";
            }
            int fromIndex = -1, toIndex = -1;
            final String fromAfterFirst = parameters.get("fromAfterFirst");
            if (fromAfterFirst != null) {
                fromIndex = value.indexOf(fromAfterFirst) + 1;
            }

            final String toBeforeLast = parameters.get("toBeforeLast");
            if (toBeforeLast != null) {
                toIndex = value.lastIndexOf(toBeforeLast);
            }
            if (fromIndex > -1 && toIndex > -1) {
                value = value.substring(fromIndex, toIndex);
            } else if (fromIndex > -1) {
                value = value.substring(fromIndex);
            }
            return value;
        }
    }

    private static class DummyGtFiveRenderer extends AbstractParameterRenderer<Boolean> {

        @Override
        public String getName() {
            return "gtFive";
        }

        @Override
        public RenderFormatInfo getFormatInfo() {
            return null;
        }

        @Override
        public Class<?>[] getSupportedClasses() {
            return new Class<?>[0];
        }

        @Override
        protected Boolean parse(Object o) {
            Number value = null;
            if (o instanceof Number) {
                value = (Number) o;
            } else if (o instanceof String) {
                String string = (String) o;
                NumberFormat format = NumberFormat.getIntegerInstance();
                try {
                    value = format.parse(string);
                } catch (ParseException e) {
                }
            }
            return value != null && value.intValue() > 5;
        }

        @Override
        protected String format(Boolean value, Locale locale, Map<String, String> parameters, Map<String, Object> model) {
            return value.toString();
        }

    }

    private Engine newEngine() {
        final Engine engine = new Engine();
        engine.registerNamedRenderer(new DummyStringRenderer());
        engine.registerNamedRenderer(new DummyGtFiveRenderer());
        return engine;
    }

    @Test
    public void booleanIfRendererCompare() {
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("name", "Daniel Georg Florey");
        model.put("mychar", " ");

        final Engine engine = newEngine();
        final String template = "${if name;dummy(fromAfterFirst= ;toBeforeLast=$mychar)=\"Georg\"}${name;dummy(fromAfterFirst= )}${end}";
        String output = engine.transform(template, model);
        assertEquals("Georg Florey", output);
    }

    @Test
    public void booleanIfRenderer() {
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("var", "10");

        final Engine engine = newEngine();
        final String template = "${if var;gtFive(this=does;not= ;make=sense)}greater than 5${end}";
        String output = engine.transform(template, model);
        assertEquals("greater than 5", output);
    }

    @Test
    public void booleanIfRendererNoParameters() {
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("var", "10");

        final Engine engine = newEngine();
        final String template = "${if var;gtFive}greater than 5${end}";
        String output = engine.transform(template, model);
        assertEquals("greater than 5", output);
    }

    @Test
    public void booleanIfRendererEmptyParameters() {
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("var", "10");

        final Engine engine = newEngine();
        final String template = "${if var;gtFive()}greater than 5${end}";
        String output = engine.transform(template, model);
        assertEquals("greater than 5", output);
    }

    @Test
    public void booleanIfRendererNullValue() {
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("var", null);

        final Engine engine = newEngine();
        final String template = "${if var;gtFive()}greater than 5${else}not greater than 5${end}";
        String output = engine.transform(template, model);
        assertEquals("not greater than 5", output);
    }

    @Test
    public void booleanIfRendererNullValueRendererNotFound() {
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("var", null);

        final Engine engine = newEngine();
        final String template = "${if var;doesNotExist()}greater than 5${else}not greater than 5${end}";
        String output = engine.transform(template, model);
        assertEquals("not greater than 5", output);
    }

}


