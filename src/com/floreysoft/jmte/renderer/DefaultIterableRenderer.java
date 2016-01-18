package com.floreysoft.jmte.renderer;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import com.floreysoft.jmte.Renderer;
import com.floreysoft.jmte.TemplateContext;


@SuppressWarnings("unchecked")
public class DefaultIterableRenderer implements Renderer<Iterable> {

	@Override
	public String render(Iterable iterable, Locale locale, Map<String, Object> model) {
		final String renderedResult;

		final Iterator<?> iterator = iterable.iterator();
		renderedResult = iterator.hasNext() ? iterator.next().toString() : "";
		return renderedResult;

	}

}
