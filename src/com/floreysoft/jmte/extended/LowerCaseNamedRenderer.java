package com.floreysoft.jmte.extended;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;

import java.util.Locale;
import java.util.Map;

/**
 * Lower case {@link NamedRenderer}.
 * <p>
 * This renderer renders the given {@link String} into lower case
 * </p>
 */
public class LowerCaseNamedRenderer implements NamedRenderer {

    @Override
    public String render(Object o, String s, Locale locale, Map<String, Object> map) {
        if (o == null) {
            return "";
        }

        return o.toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getName() {
        return "lowercase";
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
