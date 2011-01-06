package com.floreysoft.jmte;

import java.util.Map;

public class WrappedDefaultStringToken extends StringToken {

	private final String prefix;
	private final String postfix;
	private final StringToken inner;

	public WrappedDefaultStringToken(String prefix, String postfix,
			StringToken inner) {
		super(inner);
		this.prefix = prefix;
		this.postfix = postfix;
		this.inner = inner;
	}

	public WrappedDefaultStringToken(WrappedDefaultStringToken defaultToken) {
		super(defaultToken);
		this.inner = defaultToken.inner;
		this.prefix = defaultToken.prefix;
		this.postfix = defaultToken.postfix;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getPostfix() {
		return postfix;
	}

	@Override
	public String getText() {
		if (text == null) {
			text = String.format("%s,%s,%s", getPrefix(), inner.getText(),
					getPostfix());
		}
		return text;
	}

	@Override
	public Object evaluate(Engine engine, Map<String, Object> model, ErrorHandler errorHandler) {

		Object evaluated = getPrefix() + inner.evaluate(engine, model, errorHandler)
				+ getPostfix();
		return evaluated;
	}

}
