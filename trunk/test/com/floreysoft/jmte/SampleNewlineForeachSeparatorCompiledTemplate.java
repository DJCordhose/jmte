package com.floreysoft.jmte;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// ${ foreach list item \n}${item.property1}${end}
public class SampleNewlineForeachSeparatorCompiledTemplate extends
		AbstractCompiledTemplate {

	public SampleNewlineForeachSeparatorCompiledTemplate(Engine engine) {
		super(engine);
	}

	@Override
	public Set<String> getUsedVariables() {
		Set<String> usedVariables = new HashSet<String>();
		usedVariables.add("address");
		return usedVariables;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected String transformCompiled(TemplateContext context) {
		StringBuilder buffer = new StringBuilder();
		ForEachToken feToken = new ForEachToken("list", "item", "\n");
		Iterable iterable = (Iterable) feToken.evaluate(context);
		feToken.setIterator(iterable.iterator());

		context.model.enterScope();
		context.push(feToken);
		try {
			Iterator<Object> iterator = feToken.iterator();
			boolean first = true;
			while (iterator.hasNext()) {
				Object value = iterator.next();
				context.model.put(feToken.getVarName(), value);
				feToken.setIndex(feToken.getIndex() + 1);
				addSpecialVariables(feToken, context.model);
				getEngine().notifyListeners(feToken,
						ProcessListener.Action.ITERATE_FOREACH);

				StringToken stringToken = new StringToken(Arrays
						.asList(new String[] { "item", "property1" }),
						"item.property1");
				Object evaluated = stringToken.evaluate(context);
				buffer.append(evaluated.toString());

				first = false;
				if (!feToken.isLast()) {
					buffer.append(feToken.getSeparator());
				}
			}
		} finally {
			context.model.exitScope();
			context.pop();
		}
		return buffer.toString();
	}

}
