package com.floreysoft.jmte;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// ${ foreach list item \n}${item.property1}${end}
public class SampleNewlineForeachSeparatorCompiledTemplate extends
		AbstractCompiledTemplate {

	public SampleNewlineForeachSeparatorCompiledTemplate(String template,
			Engine engine) {
		super(template, engine);
	}

	@Override
	public Set<String> getUsedVariables() {
		Set<String> usedVariables = new HashSet<String>();
		usedVariables.add("address");
		return usedVariables;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected String transformCompiled(ScopedMap model) {
		StringBuilder buffer = new StringBuilder();
		ForEachToken feToken = new ForEachToken("list", "item", "\n");
		Iterable iterable = (Iterable) feToken.evaluate(engine, model, engine
				.getErrorHandler());
		feToken.setIterator(iterable.iterator());

		Iterator<Object> iterator = feToken.iterator();
		boolean first = true;
		while (iterator.hasNext()) {
			Object value = iterator.next();
			model.put(feToken.getVarName(), value);
			feToken.setFirst(first);
			feToken.setLast(!iterator.hasNext());
			feToken.setIndex(feToken.getIndex() + 1);
			addSpecialVariables(feToken, model);
			engine.notifyListeners(feToken,
					ProcessListener.Action.ITERATE_FOREACH);

			StringToken stringToken = new StringToken(Arrays
					.asList(new String[] { "item", "property1" }),
					"item.property1");
			Object evaluated = stringToken.evaluate(engine, model, engine
					.getErrorHandler());
			buffer.append(evaluated.toString());

			first = false;
			if (!feToken.isLast()) {
				buffer.append(feToken.getSeparator());
			}
		}
		return buffer.toString();

	}

}
