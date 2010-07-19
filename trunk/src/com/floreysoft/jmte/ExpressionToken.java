package com.floreysoft.jmte;

import java.util.Map;

public abstract class ExpressionToken extends AbstractToken {
	private String[] segments;

	protected transient Object evaluated = null;

	public ExpressionToken(String[] segments) {
		if (segments == null) {
			throw new IllegalArgumentException(
					"Parameter segements must not be null");
		}
		this.segments = segments;
	}

	public ExpressionToken(ExpressionToken expressionToken) {
		super(expressionToken);
		this.segments = expressionToken.segments;
	}

	public boolean isComposed() {
		return segments.length > 1;
	}

	public boolean isEmpty() {
		return segments.length == 0;
	}

	public abstract Token dup();

	public abstract Object evaluate(Map<String, Object> model,
			ErrorHandler errorHandler);

	public String[] getSegments() {
		return segments;
	}

	public String getFirstSegment() {
		if (isEmpty()) {
			throw new IllegalStateException("There is no first segment");
		}

		return segments[0];
	}

	public String getLastSegment() {
		if (isEmpty()) {
			throw new IllegalStateException("There is no first segment");
		}

		return segments[segments.length - 1];
	}

}
