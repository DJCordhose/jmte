package com.floreysoft.jmte;


public interface NamedRenderer {
	/**
	 * Renders an object of the type supported by the renderer.
	 * 
	 * @param o
	 *            the object to render
	 * @param format
	 *            anything that tells the renderer how to do its work
	 * @return the renderer object
	 */
	public String render(Object o, String format);

	public String getName();

	public RenderFormatInfo getFormatInfo();

	public Class<?>[] getSupportedClasses();

}