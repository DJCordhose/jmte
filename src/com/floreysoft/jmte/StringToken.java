package com.floreysoft.jmte;

import java.util.Map;

public class StringToken extends ExpressionToken {
	private final String namedRenderer;
	private final String namedRendererParameters;

	public StringToken(String expression, String namedRenderer,
			String namedRendererParameters) {
		super(expression);
		this.namedRenderer = namedRenderer;
		this.namedRendererParameters = namedRendererParameters;
	}

	public StringToken(StringToken stringToken) {
		super(stringToken);
		this.namedRenderer = stringToken.namedRenderer;
		this.namedRendererParameters = stringToken.namedRendererParameters;
	}

	@Override
	public String getText() {
		if (text == null) {
			text = getExpression();
		}
		return text;
	}

	@Override
	public Object evaluate(Engine engine, Map<String, Object> model,
			ErrorHandler errorHandler) {

		final String renderedResult;
		final Object value = traverse(getSegments(), model, errorHandler);
		if (value == null) {
			renderedResult = "";
		} else {
			String namedRendererResult = null;
			if (namedRenderer != null && !namedRenderer.equals("")) {
				NamedRenderer<Object> rendererForName = engine
						.resolveNamedRenderer(namedRenderer);
				if (rendererForName != null) {
					Object converted = rendererForName.convert(value);
					namedRendererResult = rendererForName.render(converted,
							namedRendererParameters);
				}
			}
			if (namedRendererResult != null) {
				renderedResult = namedRendererResult;
			} else {
				Renderer<Object> rendererForClass = engine
						.resolveRendererForClass(value.getClass());
				if (rendererForClass != null) {
					renderedResult = rendererForClass.render(value);
				} else {
					renderedResult = value.toString();
				}
			}
		}

		return renderedResult;
	}

}
