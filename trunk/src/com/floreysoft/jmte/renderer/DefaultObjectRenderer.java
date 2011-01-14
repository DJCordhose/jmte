package com.floreysoft.jmte.renderer;

import java.util.List;

import com.floreysoft.jmte.Util;

public class DefaultObjectRenderer implements Renderer<Object> {

	@Override
	public String render(Object value) {
		final String renderedResult;

		final List<Object> arrayAsList = Util.arrayAsList(value);
		if (arrayAsList != null) {
			renderedResult = arrayAsList.size() > 0 ? arrayAsList.get(0)
					.toString() : "";
		} else {
			renderedResult = value.toString();
		}
		return renderedResult;
	}
}
