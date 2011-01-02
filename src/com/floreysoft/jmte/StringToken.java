package com.floreysoft.jmte;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StringToken extends ExpressionToken {

	public static AbstractToken parse(String compoundExpression) {
		if (compoundExpression.contains(";")) {
			final int defaultStart = compoundExpression.indexOf(';');
			final String formatName = compoundExpression
					.substring(defaultStart + 1);
			final String expression = compoundExpression.substring(0,
					defaultStart);
			return new StringToken(expression, formatName);
		}
		return new StringToken(compoundExpression, null);
	}

	private final String formatName;

	public StringToken(String expression, String formatName) {
		super(expression);
		this.formatName = formatName;
	}

	public StringToken(StringToken stringToken) {
		super(stringToken);
		this.formatName = stringToken.formatName;
	}

	@Override
	public Token dup() {
		return new StringToken(this);
	}

	@Override
	public String getText() {
		if (text == null) {
			text = getExpression();
		}
		return text;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object evaluate(Engine engine, Map<String, Object> model,
			ErrorHandler errorHandler) {

		if (evaluated != null) {
			return evaluated;
		}

		final String string;
		final Object value = traverse(getSegments(), model, errorHandler);
		if (value == null) {
			string = "";
		} else {
			Renderer rendererForClass = engine.rendererForClass(value
					.getClass());
			if (rendererForClass != null) {
				string = rendererForClass.render(value, formatName);
			} else if (value instanceof String) {
				string = (String) value;
			} else {
				final List<Object> arrayAsList = Util.arrayAsList(value);
				if (arrayAsList != null) {
					string = arrayAsList.size() > 0 ? arrayAsList.get(0)
							.toString() : "";
				} else if (value instanceof Map) {
					final Map map = (Map) value;
					if (map.size() == 0) {
						string = "";
					} else if (map.size() == 1) {
						string = map.values().iterator().next().toString();
					} else {
						string = map.toString();
					}
				} else if (value instanceof Collection) {
					final Collection collection = (Collection) value;
					if (collection.size() == 0) {
						string = "";
					} else if (collection.size() == 1) {
						string = collection.iterator().next().toString();
					} else {
						string = collection.toString();
					}
				} else if (value instanceof Iterable) {
					final Iterable iterable = (Iterable) value;
					final Iterator iterator = iterable.iterator();
					string = iterator.hasNext() ? iterator.next().toString()
							: "";
				} else {
					string = value.toString();
				}
			}
		}

		evaluated = string;

		return evaluated;
	}

}
