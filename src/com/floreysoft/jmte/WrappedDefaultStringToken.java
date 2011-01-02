package com.floreysoft.jmte;

import java.util.Map;

public class WrappedDefaultStringToken extends StringToken {

	public static AbstractToken parse(String expression) {
		if (expression.contains(",")) {
			final int firstComma = expression.indexOf(',');
			final int lastComma = expression.lastIndexOf(',');

			final String innerString;
			final String prefix;
			final String postfix;

			if (firstComma != -1 && lastComma != -1 && firstComma != lastComma) {
				innerString = expression.substring(firstComma + 1, lastComma);
				prefix = expression.substring(0, firstComma);
				postfix = expression.substring(lastComma + 1);
			} else {
				return new InvalidToken();
			}

			final StringToken innerToken;
			AbstractToken defaultToken = DefaultStringToken.parse(innerString);
			if (defaultToken instanceof InvalidToken) {
				return defaultToken;
			}
			if (defaultToken != null) {
				innerToken = (StringToken) defaultToken;
			} else {
				innerToken = (StringToken) StringToken.parse(innerString);
			}

			return new WrappedDefaultStringToken(prefix, postfix, innerToken);

		}
		return null;
	}

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
	public Token dup() {
		return new WrappedDefaultStringToken(this);
	}

	@Override
	public Object evaluate(Engine engine, Map<String, Object> model, ErrorHandler errorHandler) {

		if (evaluated != null) {
			return evaluated;
		}
		evaluated = getPrefix() + inner.evaluate(engine, model, errorHandler)
				+ getPostfix();
		return evaluated;
	}

}
