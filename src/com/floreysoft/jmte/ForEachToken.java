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
	private transient int scanIndex;
	private transient int offset;
	private transient boolean last;
	private transient boolean first;
	private transient int index;

	public ForEachToken(String expression, String varName, String separator) {
		super(expression);
		this.varName = varName;
		this.separator = separator;
	}

	public ForEachToken(ForEachToken forEachToken) {
		super(forEachToken);
		this.varName = forEachToken.varName;
		this.separator = forEachToken.separator;
	}

	@Override
	public String getText() {
		if (text == null) {
			text = FOREACH + " " + getExpression() + " " + varName + (separator == null ? "" : " " + separator);
		}
		return text;
	}

	@Override
	public Token dup() {
		return new ForEachToken(this);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Object evaluate(Map<String, Object> model, ErrorHandler errorHandler) {
		
		if (evaluated != null) {
			return evaluated;
		}

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
		
		evaluated = iterable;
		return evaluated;
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

	public void setScanIndex(int scanIndex) {
		this.scanIndex = scanIndex;
	}

	public int getScanIndex() {
		return scanIndex;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getOffset() {
		return offset;
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
