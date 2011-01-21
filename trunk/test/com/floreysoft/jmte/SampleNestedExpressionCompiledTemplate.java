package com.floreysoft.jmte;

import java.util.Arrays;
import java.util.Iterator;

// ${foreach list item}${foreach item.list item2}${if item2.trueCond}${item2.property1}${end}${end}\n${end}
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
		ForEachToken token1 = new ForEachToken("list", "item", "\n");
		token1.setIterable((Iterable) token1.evaluate(context));

		context.model.enterScope();
		context.push(token1);
		try {
			
			while (token1.iterator().hasNext()) {
				Object value = token1.iterator().next();
				context.model.put(token1.getVarName(), value);
				token1.setIndex(token1.getIndex() + 1);
				addSpecialVariables(token1, context.model);
				getEngine().notifyListeners(token1,
						ProcessListener.Action.ITERATE_FOREACH);

				
				
				StringToken token2 = new StringToken(Arrays
						.asList(new String[] { "item", "property1" }),
						"item.property1");
				Object evaluated = token2.evaluate(context);
				buffer.append(evaluated.toString());

				if (!token1.isLast()) {
					buffer.append(token1.getSeparator());
				}
			}
		} finally {
			context.model.exitScope();
			context.pop();
		}
		return buffer.toString();
	}
}
