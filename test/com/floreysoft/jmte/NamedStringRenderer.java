package com.floreysoft.jmte;


public class NamedStringRenderer implements NamedRenderer {

	String convert(Object o) {
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

		return o;
	}

	@Override
	public RenderFormatInfo getFormatInfo() {
		return new OptionRenderFormatInfo(new String[] {"uppercase", ""});
	}

	@Override
	public String getName() {
		return "string";
	}

	@Override
	public Class[] getSupportedClasses() {
		Class[] clazzes = { String.class, Integer.class };
		return clazzes;
	}
}
