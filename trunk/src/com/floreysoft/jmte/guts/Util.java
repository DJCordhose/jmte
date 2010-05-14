package com.floreysoft.jmte.guts;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Util {
	public static String trimFront(String input) {
		int i = 0;
		while (i < input.length() && Character.isWhitespace(input.charAt(i)))
			i++;
		return input.substring(i);
	}

	public static Object getPropertyValue(Object o, String attributeName) {
		Object result = null;
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(o.getClass());
			PropertyDescriptor[] propertyDescriptors = beanInfo
					.getPropertyDescriptors();
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				if (propertyDescriptor.getName().equals(attributeName)) {
					Method readMethod = propertyDescriptor.getReadMethod();
					if (readMethod != null) {
						result = readMethod.invoke(o);
						break;
					}
				}
			}
			if (result == null) {
				Field field = o.getClass().getField(attributeName);
				if (Modifier.isPublic(field.getModifiers())) {
					result = field.get(o);
				}
			}

		} catch (Exception e) {
		}
		return result;
	}

}
