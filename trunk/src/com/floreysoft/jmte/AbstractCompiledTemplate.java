package com.floreysoft.jmte;

import java.util.Map;
import java.util.Set;

public abstract class AbstractCompiledTemplate extends AbstractTemplate implements
		Template {
	@SuppressWarnings("unchecked")
	public static <T> Class<T> loadClass(String name, byte[] b, Class<T> type) {
		return MY_CLASS_LOADER.defineClass(name, b);
	}

	public static Class<?> loadClass(String name, byte[] b) {
		return MY_CLASS_LOADER.defineClass(name, b);
	}

	@SuppressWarnings("unchecked")
	private static class MyClassLoader extends ClassLoader {
		public Class defineClass(String name, byte[] b) {
			return defineClass(name, b, 0, b.length);
		}
	};

	private static MyClassLoader MY_CLASS_LOADER = new MyClassLoader();

	public AbstractCompiledTemplate(String template, Engine engine) {
		super(template, engine);
	}
}
