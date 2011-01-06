package com.floreysoft.jmte;

import java.util.Map;

public class DefaultStringToken extends StringToken {
	private final String defaultValue;
	
	public DefaultStringToken(StringToken stringToken, String defaultValue) {
		super(stringToken);
		this.defaultValue = defaultValue;
	}

	@Override
	public String getText() {
		if (text == null) {
			text = String.format("%s(%s)", getExpression(), getDefaultValue());
		}
		return text;
	}

	@Override
	public Object evaluate(Engine engine, Map<String, Object> model, ErrorHandler errorHandler) {

		Object evaluated = super.evaluate(engine, model, errorHandler);
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
