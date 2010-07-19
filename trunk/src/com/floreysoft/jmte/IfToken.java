package com.floreysoft.jmte;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class IfToken extends ExpressionToken {

	private final boolean negated;

	public IfToken(String[] segments, boolean negated) {
		super(segments);
		this.negated = negated;
	}

	public IfToken(boolean value) {
		this(null, false);
		evaluated = value;
		
	}

	public boolean isNegated() {
		return negated;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object evaluate(Map<String, Object> model, ErrorHandler errorHandler) {

		if (evaluated != null) {
			return evaluated;
		}
		
		final boolean condition;
		final Object value = traverse(segments, model, errorHandler);
		if (value == null || value.toString().equals("")) {
			condition = false;
		} else if (value instanceof Boolean) {
			condition = (Boolean) value;
		} else if (value instanceof Map) {
			condition = !((Map) value).isEmpty();
		} else if (value instanceof Collection) {
			condition = !((Collection) value).isEmpty();
		} else if (value instanceof Iterable) {
			Iterator iterator = ((Iterable) value).iterator();
			condition = iterator.hasNext();
		} else {
			List list = Util.arrayAsList(value);
			// XXX looks strange, but is ok: list will be null if is
			// is not an array which results to true
			condition = list == null || !list.isEmpty();
		}

		evaluated = negated ? !condition : condition;
		
		return evaluated;
	}

}
