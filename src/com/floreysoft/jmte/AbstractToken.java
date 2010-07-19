package com.floreysoft.jmte;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

public abstract class AbstractToken implements Token {

	private char[] buffer;
	private int start;
	private int end;
	private String sourceName;

	protected boolean duped = false;

	public AbstractToken() {

	}

	public AbstractToken(AbstractToken token) {
		this.duped = true;
		this.setSourceName(token.sourceName);
		this.setBuffer(token.buffer);
		this.setStart(token.start);
		this.setEnd(token.end);

	}

	public AbstractToken(char[] buffer, int start, int end) {
		this(null, buffer, start, end);
	}

	public AbstractToken(String sourceName, char[] buffer, int start, int end) {
		this.setSourceName(sourceName);
		this.setBuffer(buffer);
		this.setStart(start);
		this.setEnd(end);
	}

	public String getText() {
		return new String(getBuffer(), getStart(), getEnd() - getStart());
	}

	public void setBuffer(char[] buffer) {
		this.buffer = buffer;
	}

	public char[] getBuffer() {
		return buffer;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getStart() {
		return start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getEnd() {
		return end;
	}

	public int getLine() {
		int line = 1;
		for (int i = 0; i < start; i++) {
			if (buffer[i] == '\n') {
				line++;
			}
		}
		return line;
	}

	public int getColumn() {
		int column = 0;
		for (int i = start; i >= 0; i--) {
			if (buffer[i] == '\n') {
				break;
			} else {
				column++;
			}
		}
		return column;
	}

	@Override
	public String toString() {
		return getText();
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	@Override
	public String getSourceName() {
		return sourceName;
	}

	public abstract Token dup();
	
	public abstract Object evaluate(Map<String, Object> model,
			ErrorHandler errorHandler);

	protected Object traverse(String[] segments, Map<String, Object> model,
			ErrorHandler errorHandler) {
		if (segments.length == 0) {
			return null;
		}
		String objectName = segments[0];
		Object value = model.get(objectName);

		LinkedList<String> attributeNames = new LinkedList<String>(Arrays
				.asList(segments));
		attributeNames.remove(0);
		value = traverse(value, attributeNames, errorHandler);
		return value;
	}

	protected Object traverse(Object o, LinkedList<String> attributeNames,
			ErrorHandler errorHandler) {
		Object result;
		if (attributeNames.isEmpty()) {
			result = o;
		} else {
			if (o == null) {
				return null;
			}
			String attributeName = attributeNames.remove(0);
			Object nextStep = nextStep(o, attributeName, errorHandler);
			result = traverse(nextStep, attributeNames, errorHandler);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	protected Object nextStep(Object o, String attributeName,
			ErrorHandler errorHandler) {
		Object result;
		if (o instanceof String) {
			errorHandler.error("no-call-on-string", this, Engine.toModel(
					"receiver", o.toString()));
			return o;
		} else if (o instanceof Map) {
			Map map = (Map) o;
			result = map.get(attributeName);
		} else {
			try {
				result = Util.getPropertyValue(o, attributeName);
			} catch (Exception e) {
				errorHandler.error("property-access-error", this, Engine
						.toModel("property", attributeName, "object", o,
								"exception", e));
				result = "";
			}
		}
		return result;
	}

}
