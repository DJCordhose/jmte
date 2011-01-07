package com.floreysoft.jmte;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

	@Override
	public RenderFormatInfo formatInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "string";
	}

	@Override
	public Set<Class> getSupportedClasses() {
		Class[] clazzes = { String.class, Integer.class };
		return new HashSet(Arrays.asList(clazzes));
	}
}
