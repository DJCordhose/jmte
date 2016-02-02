package com.floreysoft.jmte.token;

import java.util.List;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.Renderer;
import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.encoder.Encoder;
import com.floreysoft.jmte.renderer.NullRenderer;
import com.floreysoft.jmte.renderer.RawRenderer;

public class StringToken extends ExpressionToken {
	// ${<h1>,address(NIX),</h1>;long(full)}
	private final String defaultValue; // NIX
	private final String prefix; // <h1>
	private final String suffix; // </h1>
	private final String rendererName; // long
	private final String parameters; // full

	public StringToken() {
		this("", "", null, null, null, null, null);
	}

	public StringToken(String text, String variableName, String defaultValue,
			String prefix, String suffix, String rendererName, String parameters) {
		super(variableName);
		this.defaultValue = defaultValue;
		this.prefix = prefix;
		this.suffix = suffix;
		this.rendererName = rendererName;
		this.parameters = parameters;
		setText(text);
	}

	public StringToken(String variableName) {
		this(variableName, variableName, null, null, null, null, null);
	}

	public StringToken(String text, List<String> segments, String variableName,
			String defaultValue, String prefix, String suffix,
			String rendererName, String parameters) {
		super(segments, variableName);
		this.defaultValue = defaultValue;
		this.prefix = prefix;
		this.suffix = suffix;
		this.rendererName = rendererName;
		this.parameters = parameters;
		setText(text);
	}

	public StringToken(List<String> segments, String variableName) {
		super(segments, variableName);
		this.defaultValue = null;
		this.prefix = null;
		this.suffix = null;
		this.rendererName = null;
		this.parameters = null;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object evaluate(TemplateContext context) {

		// step 1: get value or default
		final Object value = resolveDefault(evaluatePlain(context));

		// step 2: using named renderer (if present), type renderer (if present), just toString (if not null) or null (in that order)
		// if there is a renderer, determine if it outputs a raw value
		final String renderedResult;
		boolean rawRendering = false;
		final NamedRenderer rendererForName = this.resolveNamedRenderer(context);
		if (rendererForName != null && (value != null || rendererForName instanceof NullRenderer)) {
			if (rendererForName instanceof RawRenderer) {
				rawRendering = true;
			}
			renderedResult = rendererForName.render(value, parameters, context.locale, context.model);
		} else if (value != null) {
			final Renderer<Object> rendererForClass = (Renderer<Object>) context
					.resolveRendererForClass(value.getClass());
			if (rendererForClass != null) {
				if (rendererForClass instanceof RawRenderer) {
					rawRendering = true;
				}
				renderedResult = rendererForClass.render(value, context.locale, context.model);
			} else {
				renderedResult = value.toString();
			}
		} else {
			renderedResult = null;
		}

		// shortcut: if rendered result is empty, do not perform subsequent steps
		if (renderedResult == null || renderedResult.equals("")) {
			return "";
		}

		// step 3: apply prefix / suffix
		final String prefixedRenderedResult = (prefix != null ? prefix : "") + renderedResult + (suffix != null ? suffix : "");

		// step 4: encode if there is an encoder and it is not rendered as raw
		final Encoder encoder = context.getEncoder();
		if (!rawRendering && encoder != null) {
			final String encodedPrefixedRenderedResult = encoder.encode(prefixedRenderedResult);
			return encodedPrefixedRenderedResult;
		} else {
			return prefixedRenderedResult;
		}
	}

	private NamedRenderer resolveNamedRenderer(TemplateContext context) {
		if (this.rendererName != null && !this.rendererName.equals("")) {
			return context.resolveNamedRenderer(rendererName);
		} else {
			return null;
		}
	}

	private Object resolveDefault(Object value) {
		if (value == null || value.equals("")) {
			if (this.defaultValue != null) {
				value = defaultValue;
			}
		}
		return value;
	}

	public String getRendererName() {
		return rendererName;
	}

	public String getParameters() {
		return parameters;
	}

  @Override
  public String emit() {
    StringBuilder sb = new StringBuilder();
    if ( prefix != null ) {
      sb.append(prefix).append(',');
    }
    sb.append(getExpression());
    if ( defaultValue != null ) {
      sb.append('(').append(defaultValue).append(')');
    }
    if ( suffix != null ) {
      sb.append(',').append(suffix);
    }
    if ( rendererName != null ) {
      sb.append(';').append(rendererName);
    }
    if ( parameters != null ) {
      sb.append('(').append(parameters).append(')');
    }
    return sb.toString();
  }
}
