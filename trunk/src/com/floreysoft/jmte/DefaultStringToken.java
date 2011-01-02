package com.floreysoft.jmte;

import java.util.Map;

public class DefaultStringToken extends StringToken {
	
	public static AbstractToken parse(String expression) {
		if (expression.contains("(")) {
			final int defaultStart = expression.indexOf('(');
			final int defaultEnd = expression.indexOf(')');
			if (defaultEnd == -1 || defaultStart > defaultEnd) {
				return new InvalidToken();
			}
			final String defaultValue = expression.substring(defaultStart + 1, defaultEnd);
			final String variable = expression.substring(0, defaultStart);
			StringToken stringToken = (StringToken) StringToken.parse(variable);
			return new DefaultStringToken(stringToken, defaultValue);
		}
		return null;
	}
	
	private final String defaultValue;
	
	public DefaultStringToken(StringToken stringToken, String defaultValue) {
		super(stringToken);
		this.defaultValue = defaultValue;
	}

	public DefaultStringToken(DefaultStringToken defaultToken) {
		super(defaultToken);
		this.defaultValue = defaultToken.defaultValue;
	}

	@Override
	public String getText() {
		if (text == null) {
			text = String.format("%s(%s)", getExpression(), getDefaultValue());
		}
		return text;
	}

	@Override
	public Token dup() {
		return new DefaultStringToken(this);
	}

	@Override
	public Object evaluate(Engine engine, Map<String, Object> model, ErrorHandler errorHandler) {

		if (evaluated != null) {
			return evaluated;
		}
		evaluated = super.evaluate(engine, model, errorHandler);
		if (evaluated == null || evaluated.equals("")) {
			evaluated = getDefaultValue();
		}
		return evaluated;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getVariable() {
		return getExpression();
	}

}
