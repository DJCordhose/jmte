package com.floreysoft.jmte;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IfToken extends ExpressionToken {
	public static final String IF = "if";

	protected final boolean negated;

	public IfToken(String expression, boolean negated) {
		super(expression);
		this.negated = negated;
	}

	public IfToken(IfToken ifToken) {
		super(ifToken);
		this.negated = ifToken.negated;
	}

	@Override
	public String getText() {
		if (text == null) {
			text = IF + " " + getExpression();
		}
		return text;
	}

	@Override
	public Token dup() {
		return new IfToken(this);
	}

	public boolean isNegated() {
		return negated;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object evaluate(Engine engine, Map<String, Object> model, ErrorHandler errorHandler) {

		final boolean condition;
		final Object value = traverse(getSegments(), model, errorHandler);
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

		Object evaluated = negated ? !condition : condition;

		return evaluated;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
