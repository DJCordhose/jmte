package com.floreysoft.jmte;

public interface NamedRenderer<T> {
	/**
	 * Converts any object to the input this renderer requires.
	 * 
	 * @param o
	 *            the object to convert
	 * @return the converted result of type required by the renderer
	 */
	public T convert(Object o);

	/**
	 * Renders an object of the type supported by the renderer.
	 * 
	 * @param o
	 *            the object to render
	 * @param parameters
	 *            any parameter string
	 * @return the renderer object
	 */
	public String render(T o, String parameters);
}