package com.floreysoft.jmte;

import java.util.List;

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
