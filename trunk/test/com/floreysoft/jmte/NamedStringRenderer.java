package com.floreysoft.jmte;

@CompatibleWith( { String.class, Integer.class })
public class NamedStringRenderer implements NamedRenderer<String> {

	@Override
	public String convert(Object o) {
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
	public String render(String o, String parameters) {
		if (parameters.equalsIgnoreCase("uppercase")) {
			return o.toUpperCase();
		}

		return o;
	}
}
