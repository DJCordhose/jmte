package com.floreysoft.jmte;

import java.util.Iterator;

@SuppressWarnings("unchecked")
public class DefaultIterableRenderer implements Renderer<Iterable> {

	@Override
	public String render(Iterable iterable) {
		final String renderedResult;

		final Iterator<?> iterator = iterable.iterator();
		renderedResult = iterator.hasNext() ? iterator.next().toString() : "";
		return renderedResult;

	}

}
