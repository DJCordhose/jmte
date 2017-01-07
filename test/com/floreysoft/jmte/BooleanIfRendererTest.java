package com.floreysoft.jmte;

import com.floreysoft.jmte.renderer.AbstractParameterRenderer;
import com.floreysoft.jmte.renderer.RawRenderer;
import org.junit.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BooleanIfRendererTest {

    private static class DummyRenderer extends AbstractParameterRenderer<String> implements RawRenderer {

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

    private Engine newEngine() {
        final Engine engine = new Engine();
        engine.registerNamedRenderer(new DummyRenderer());
        return engine;
    }

    @Test
    public void booleanIfRenderer() {
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("name", "Daniel Georg Florey");
        model.put("mychar", " ");

        final Engine engine = newEngine();
        String output = engine.transform("${if name;dummy(fromAfterFirst= ;toBeforeLast=$mychar)=\"Georg\"}${name;dummy(fromAfterFirst= )}${end}", model);
        assertEquals("Georg Florey", output);
    }


}


