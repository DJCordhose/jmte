package com.floreysoft.jmte;

import java.util.List;

public interface ModelAdaptor {
	public Object getValue(TemplateContext context, Token token, List<String> segments, String expression);
}
