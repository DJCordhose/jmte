package com.floreysoft.jmte;

import java.util.Map;

public abstract class ExpressionToken extends AbstractToken {

	public final static String segmentsToString(String[] segments, int start,
			int end) {
		if (start >= segments.length || end > segments.length) {
			throw new IllegalArgumentException("Range is not inside segments");
		}
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < end; i++) {
			String segment = segments[i];
			builder.append(segment);
			if (i < end - 1) {
				builder.append(".");
			}
		}
		return builder.toString();
	}

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

	public abstract Object evaluate(Engine engine, Map<String, Object> model,
			ErrorHandler errorHandler);

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
			throw new IllegalStateException("There is no last segment");
		}

		return getSegments()[getSegments().length - 1];
	}

	public String getAllButLastSegment() {
		if (isEmpty()) {
			throw new IllegalStateException("There are no segments");
		}

		return segmentsToString(segments, 0, getSegments().length - 1);
	}

	public String getAllButFirstSegment() {
		if (isEmpty()) {
			throw new IllegalStateException("There are no segments");
		}

		return segmentsToString(segments, 1, getSegments().length);
	}

	public void setExpression(String expression) {
		this.text = null;
		this.segments = Util.splitEscaped(expression, '.', '\\');
		this.expression = Util.unescape(expression);
	}

	public String getExpression() {
		return expression;
	}

	public void setSegments(String[] segments) {
		this.segments = segments;
		this.expression = segmentsToString(segments, 0, segments.length);
		this.text = null;
	}

}
