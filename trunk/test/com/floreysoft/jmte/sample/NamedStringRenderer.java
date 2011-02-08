package com.floreysoft.jmte.sample;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;
import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.renderer.OptionRenderFormatInfo;

public final class NamedStringRenderer implements NamedRenderer {

	private String convert(Object o) {
		if (o instanceof String) {
			return (String) o;
		}

		if (o instanceof Integer) {
			Integer i = (Integer) o;
			return String.valueOf(i);
		}
		return null;
	}

	@Override
	public String render(Object value, String parameters) {
		String o = convert(value);
		if (o == null) {
			return null;
		}
		if (parameters.equalsIgnoreCase("uppercase")) {
			return o.toUpperCase();
		}

		return "String=" + o + "(" + parameters + ")";
	}

	@Override
	public RenderFormatInfo getFormatInfo() {
		return new OptionRenderFormatInfo(new String[] { "uppercase", "" });
	}

	@Override
	public String getName() {
		return "string";
	}

	@Override
	public Class<?>[] getSupportedClasses() {
		Class<?>[] clazzes = { String.class, Integer.class };
		return clazzes;
	}
}
