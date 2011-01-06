package com.floreysoft.jmte;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ForEachToken extends ExpressionToken {
	public static final String FOREACH = "foreach";
	
	private String varName;
	private String separator;
	
	private transient Iterator<Object> iterator;
	private transient boolean last;
	private transient boolean first;
	private transient int index;

	public ForEachToken(String expression, String varName, String separator) {
		super(expression);
		this.varName = varName;
		this.separator = separator;
	}

	@Override
	public String getText() {
		if (text == null) {
			text = FOREACH + " " + getExpression() + " " + varName + (separator == null ? "" : " " + separator);
		}
		return text;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Object evaluate(Engine engine, Map<String, Object> model, ErrorHandler errorHandler) {
		
		final Iterable<Object> iterable;
		final Object value = traverse(getSegments(), model, errorHandler);
		if (value == null) {
			iterable = Collections.emptyList();
		} else if (value instanceof Map) {
			iterable = ((Map) value).entrySet();
		} else if (value instanceof Iterable) {
			iterable = ((Iterable) value);
		} else {
			List<Object> arrayAsList = Util.arrayAsList(value);
			if (arrayAsList != null) {
				iterable = arrayAsList;
			} else {
				// we have a single value here and simply wrap it in a List
				iterable = Collections.singletonList(value);
			}
		}
		
		return iterable;
	}

	
	public Iterator<Object> iterator() {
		return getIterator();
	}

	public String getVarName() {
		return varName;
	}

	public String getSeparator() {
		return separator;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	public boolean isLast() {
		return last;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}

	public boolean isFirst() {
		return first;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public void setIterator(Iterator<Object> iterator) {
		this.iterator = iterator;
	}

	public Iterator<Object> getIterator() {
		return iterator;
	}
}
