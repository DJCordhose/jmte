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
	 * @param context
	 *            current context during template evaluation
	 * @param o
	 *            the object to render
	 */
	public String render(TemplateContext context, T o);
}