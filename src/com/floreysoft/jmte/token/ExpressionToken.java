package com.floreysoft.jmte.token;

import java.util.Map;

import com.floreysoft.jmte.ErrorHandler;

public abstract class ExpressionToken extends AbstractToken {
	protected final String[] segments;

	protected transient Object evaluated = null;
	
	public ExpressionToken(String[] segments) {
		this.segments = segments;
	}

	public abstract Object evaluate(Map<String, Object> model,
			ErrorHandler errorHandler);
}
