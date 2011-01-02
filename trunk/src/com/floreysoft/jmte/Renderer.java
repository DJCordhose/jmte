package com.floreysoft.jmte;

public interface Renderer<T> {
	public String render(T o, String format);
}