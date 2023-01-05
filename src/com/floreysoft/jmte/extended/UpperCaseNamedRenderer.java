package com.floreysoft.jmte.extended;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;

import java.util.Locale;
import java.util.Map;

/**
 * Upper case {@link NamedRenderer}.
 * <p>
 * This renderer renders the given {@link String} into UPPER CASE
 * </p>
 */
public class UpperCaseNamedRenderer implements NamedRenderer {

    @Override
    public String render(Object o, String s, Locale locale, Map<String, Object> map) {
        if (o == null) {
            return "";
        }

        return o.toString().toUpperCase(Locale.ROOT);
    }

    @Override
    public String getName() {
        return "uppercase";
    }

    @Override
    public RenderFormatInfo getFormatInfo() {
        return null;
    }

    @Override
    public Class<?>[] getSupportedClasses() {
        return new Class[]{String.class};
    }
}
