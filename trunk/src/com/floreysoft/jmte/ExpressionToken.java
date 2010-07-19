package com.floreysoft.jmte;

import java.util.Map;


public abstract class ExpressionToken extends AbstractToken {
	private final String[] segments;

	protected transient Object evaluated = null;
	
	public ExpressionToken(String[] segments) {
		if (segments == null) {
			throw new IllegalArgumentException("Parameter segements must not be null");
		}
		this.segments = segments;
	}

	public boolean isComposed() {
		return getSegments().length > 1;
	}
	
	public boolean isEmpty() {
		return getSegments().length == 0;
	}
	
	public abstract Object evaluate(Map<String, Object> model,
			ErrorHandler errorHandler);

	public String[] getSegments() {
		return segments;
	}
}
