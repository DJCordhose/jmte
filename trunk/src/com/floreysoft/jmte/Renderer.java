package com.floreysoft.jmte;

/**
 * Renderer for a certain type.
 * 
 * @param <T>
 *            the type that can be rendered
 * 
 * @see NamedRenderer
 */
public interface Renderer<T> {
	/**
	 * Renders an object of the type supported by the renderer.
	 * 
	 * @param o
	 *            the object to render
	 */
	public String render(T o);
}