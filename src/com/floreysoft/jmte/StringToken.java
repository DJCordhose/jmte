package com.floreysoft.jmte;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class StringToken extends ExpressionToken {
	public StringToken(String[] segments) {
		super(segments);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object evaluate(Map<String, Object> model, ErrorHandler errorHandler) {

		if (evaluated != null) {
			return evaluated;
		}

		final String string;
		final Object value = traverse(segments, model, errorHandler);
		if (value == null) {
			string = "";
		} else if (value instanceof String) {
			string = (String) value;
		} else {
			final List<Object> arrayAsList = Util.arrayAsList(value);
			if (arrayAsList != null) {
				string = arrayAsList.size() > 0 ? arrayAsList.get(0).toString()
						: "";
			} else if (value instanceof Map) {
				final Map map = (Map) value;
				final Collection values = map.values();
				string = values.size() > 0 ? values.iterator().next()
						.toString() : "";
			} else if (value instanceof Iterable) {
				final Iterable iterable = (Iterable) value;
				final Iterator iterator = iterable.iterator();
				string = iterator.hasNext() ? iterator.next().toString() : "";
			} else {
				string = value.toString();
			}
		}

		evaluated = string;

		return evaluated;
	}

}
