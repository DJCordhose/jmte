package com.floreysoft.jmte.renderer;

import java.util.Map;

import com.floreysoft.jmte.Renderer;
import com.floreysoft.jmte.TemplateContext;


@SuppressWarnings("unchecked")
public class DefaultMapRenderer implements Renderer<Map> {

	@Override
	public String render(Map map) {
		final String renderedResult;

		if (map.size() == 0) {
			renderedResult = "";
		} else if (map.size() == 1) {
			renderedResult = map.values().iterator().next().toString();
		} else {
			renderedResult = map.toString();
		}
		return renderedResult;

	}

}
