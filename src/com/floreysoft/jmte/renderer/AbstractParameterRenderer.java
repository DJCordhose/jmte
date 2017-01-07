package com.floreysoft.jmte.renderer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.floreysoft.jmte.DefaultModelAdaptor;
import com.floreysoft.jmte.NamedRenderer;

public abstract class AbstractParameterRenderer<T> implements NamedRenderer {
	private static final Logger logger = Logger.getLogger(AbstractParameterRenderer.class.getName());

	@Override
	public String render(Object o, String pattern, Locale locale, Map<String, Object> model) {
		Map<String,String> parameters = parsePattern(pattern, model);
		T value = parse(o);
		String formattedValue = null;
		if (value != null) {
			formattedValue = format(value, locale, parameters, model);
		}
		return formattedValue;
	}

	protected abstract T parse(Object o);

	protected abstract String format(T value, Locale locale, Map<String, String> parameters, Map<String, Object> model);
	
	protected Map<String, String> parsePattern(String pattern, Map<String, Object> model) {
		logger.log(Level.FINE, "Extracting parameters from pattern="+pattern);
		Map<String, String> parameters = new HashMap<String, String>();
		if (pattern != null) {
			String[] tokens = pattern.split(";");
			for (String token : tokens) {
				int index = token.indexOf('=');
				String key, value = null;
				if (index > 0) {
					key = token.substring(0, index);
					value = token.substring(index + 1);
				} else {
					key = token;
				}
				logger.log(Level.FINE, "Extracted parameter with key=" + key + ", value=" + value);
				if (value != null && value.startsWith("$")) {
					String expression = value.substring(1);
					logger.log(Level.FINE, "Resolving pattern value with path=" + expression);
					DefaultModelAdaptor modelAdaptor = new DefaultModelAdaptor();
					value = (String) modelAdaptor.getValue(model, expression);
				}
				parameters.put(key, value);
			}
		}
		return parameters;
	}
}