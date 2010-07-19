package com.floreysoft.jmte;

import java.util.Map;

public abstract class ExpressionToken extends AbstractToken {
	private String[] segments;
	private String expression;

	protected transient Object evaluated = null;

	public ExpressionToken(String expression) {
		if (expression == null) {
			throw new IllegalArgumentException(
					"Parameter expression must not be null");
		}
		this.setExpression(expression);
	}

	public ExpressionToken(ExpressionToken expressionToken) {
		super(expressionToken);
		this.setExpression(expressionToken.expression);
	}

	public boolean isComposed() {
		return getSegments().length > 1;
	}

	public boolean isEmpty() {
		return getSegments().length == 0;
	}

	public abstract Token dup();

	public abstract Object evaluate(Map<String, Object> model,
			ErrorHandler errorHandler);

	private void setSegments(String expression) {
		segments = expression.split("\\.");
	}
	
	public String[] getSegments() {
		return segments;
	}

	public String getFirstSegment() {
		if (isEmpty()) {
			throw new IllegalStateException("There is no first segment");
		}

		return getSegments()[0];
	}

	public String getLastSegment() {
		if (isEmpty()) {
			throw new IllegalStateException("There is no first segment");
		}

		return getSegments()[getSegments().length - 1];
	}

	public void setExpression(String expression) {
		this.expression = expression;
		setSegments(expression);
	}

	public String getExpression() {
		return expression;
	}

	public void setSegments(String[] segments) {
		this.segments = segments;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < segments.length; i++) {
			String segment = segments[i];
			builder.append(segment);
			if (i < segments.length - 1) {
				builder.append(".");
			}
		}
		this.expression = builder.toString();
	}

}
