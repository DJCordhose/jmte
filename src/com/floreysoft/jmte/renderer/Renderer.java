package com.floreysoft.jmte.renderer;

/**
 * Renderer for a certain type.
 * 
 * @param <T> the type that can be rendered
 * 
 * @see NamedRenderer
 */
public interface Renderer<T> {
	/**
	 * Renders the given value.
	 */
	public String render(T value);
}