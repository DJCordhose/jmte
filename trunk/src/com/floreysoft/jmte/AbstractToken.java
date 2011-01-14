package com.floreysoft.jmte;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public abstract class AbstractToken implements Token {

	protected String text;
	protected int line;
	protected int column;
	protected String sourceName;
	private int tokenIndex;

	public AbstractToken() {
	}

	public AbstractToken(AbstractToken token) {
		this.text = token.text;
		this.line = token.line;
		this.column = token.column;
		this.sourceName = token.sourceName;
		this.setTokenIndex(token.getTokenIndex());
	}

	public AbstractToken(char[] buffer, int start, int end, int tokenIndex) {
		this(null, buffer, start, end, tokenIndex);
	}

	public AbstractToken(String sourceName, char[] buffer, int start, int end,
			int tokenIndex) {
		this.setSourceName(sourceName);
		setText(buffer, start, end);
		setLine(buffer, start, end);
		setColumn(buffer, start, end);
		this.setTokenIndex(tokenIndex);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public void setText(char[] buffer, int start, int end) {
		setText(new String(buffer, start, end - start));
	}

	public void setLine(char[] buffer, int start, int end) {
		line = 1;
		for (int i = 0; i < start; i++) {
			if (buffer[i] == '\n') {
				line++;
			}
		}
	}

	public void setColumn(char[] buffer, int start, int end) {
		column = 0;
		if (buffer.length != 0) {
			for (int i = start; i >= 0; i--) {
				if (buffer[i] == '\n') {
					break;
				} else {
					column++;
				}
			}
		}
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

	public abstract Object evaluate(Engine engine, Map<String, Object> model,
			ErrorHandler errorHandler);

	protected Object traverse(List<String> segments, Map<String, Object> model,
			ErrorHandler errorHandler) {
		if (segments.size() == 0) {
			return null;
		}
		String objectName = segments.get(0);
		Object value = model.get(objectName);

		LinkedList<String> attributeNames = new LinkedList<String>(segments);
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

	@Override
	public int getTokenIndex() {
		return tokenIndex;
	}

	public void setTokenIndex(int tokenIndex) {
		this.tokenIndex = tokenIndex;
	}

}
