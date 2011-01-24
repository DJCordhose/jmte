package com.floreysoft.jmte;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Default implementation of the model adapter.
 * <p>
 * Does the object traversal using the "." operator. Resolved value will be
 * checked if it is either a {@link Processor} or a {@link Callable} in which
 * case the final resolved value is computed by calling those executable
 * objects.
 * </p>
 * 
 * <p>
 * Inherit from this adapter if you want a slight change of this behavior and
 * set you new adator on the engine {@link Engine#setModelAdaptor(ModelAdaptor)}
 * .
 * </p>
 */
public class DefaultModelAdaptor implements ModelAdaptor {

	@Override
	@SuppressWarnings("unchecked")
	public Object getValue(TemplateContext context, Token token,
			List<String> segments, String expression) {
		Object value = traverse(segments, context.model, context.engine
				.getErrorHandler(), token);
		// if value implements both, we use the more specialized implementation
		if (value instanceof Processor) {
			value = ((Processor) value).eval(context);
		} else if (value instanceof Callable) {
			try {
				value = ((Callable) value).call();
			} catch (Exception e) {
			}
		}
		return value;
	}

	protected Object traverse(List<String> segments, Map<String, Object> model,
			ErrorHandler errorHandler, Token token) {
		if (segments.size() == 0) {
			return null;
		}
		String objectName = segments.get(0);
		Object value = model.get(objectName);

		LinkedList<String> attributeNames = new LinkedList<String>(segments);
		attributeNames.remove(0);
		value = traverse(value, attributeNames, errorHandler, token);
		return value;
	}

	protected Object traverse(Object o, LinkedList<String> attributeNames,
			ErrorHandler errorHandler, Token token) {
		Object result;
		if (attributeNames.isEmpty()) {
			result = o;
		} else {
			if (o == null) {
				return null;
			}
			String attributeName = attributeNames.remove(0);
			Object nextStep = nextStep(o, attributeName, errorHandler, token);
			result = traverse(nextStep, attributeNames, errorHandler, token);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	protected Object nextStep(Object o, String attributeName,
			ErrorHandler errorHandler, Token token) {
		Object result;
		if (o instanceof String) {
			errorHandler.error("no-call-on-string", token, Engine.toModel(
					"receiver", o.toString()));
			return o;
		} else if (o instanceof Map) {
			Map map = (Map) o;
			result = map.get(attributeName);
		} else {
			try {
				result = Util.getPropertyValue(o, attributeName);
			} catch (Exception e) {
				errorHandler.error("property-access-error", token, Engine
						.toModel("property", attributeName, "object", o,
								"exception", e));
				result = "";
			}
		}
		return result;
	}

}
