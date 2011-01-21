package com.floreysoft.jmte;

import java.util.Arrays;
import java.util.Iterator;

// ${foreach list item}${foreach item.list item2}${if item}${item2.property1}${end}${end}\n${end}
public class SampleNestedExpressionCompiledTemplate extends
		AbstractCompiledTemplate {

	public SampleNestedExpressionCompiledTemplate() {
	}

	public SampleNestedExpressionCompiledTemplate(Engine engine) {
		super(engine);
		usedVariables.add("address");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected String transformCompiled(TemplateContext context) {
		StringBuilder buffer = new StringBuilder();
		ForEachToken feToken = new ForEachToken("list", "item", "\n");
		Iterable iterable = (Iterable) feToken.evaluate(context);
		feToken.setIterator(iterable.iterator());

		context.model.enterScope();
		try {
			Iterator<Object> iterator = feToken.iterator();
			boolean first = true;
			while (iterator.hasNext()) {
				Object value = iterator.next();
				context.model.put(feToken.getVarName(), value);
				feToken.setFirst(first);
				feToken.setLast(!iterator.hasNext());
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
		}
		return buffer.toString();
	}
}
