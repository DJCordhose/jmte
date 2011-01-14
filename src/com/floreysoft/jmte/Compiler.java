package com.floreysoft.jmte;


public class Compiler {

	@SuppressWarnings("unchecked")
	protected static <T> Class<T> loadClass(String name, byte[] b, Class<T> type) {
		return MY_CLASS_LOADER.defineClass(name, b);
	}

	protected static Class<?> loadClass(String name, byte[] b) {
		return MY_CLASS_LOADER.defineClass(name, b);
	}

	@SuppressWarnings("unchecked")
	private static class MyClassLoader extends ClassLoader {
		public Class defineClass(String name, byte[] b) {
			return defineClass(name, b, 0, b.length);
		}
	};
	
	private static MyClassLoader MY_CLASS_LOADER = new MyClassLoader();


	public static Template compile(String template) {
		// TODO
		return null;
	}

}
