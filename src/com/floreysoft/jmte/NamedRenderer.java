package com.floreysoft.jmte;

import java.util.Set;

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
	 * @param format
	 *            anything that tells the renderer how to do its work
	 * @return the renderer object
	 */
	public String render(T o, String format);

	public String getName();

	public RenderFormatInfo formatInfo();

	public Set<Class> getSupportedClasses();

}