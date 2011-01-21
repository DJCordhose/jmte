package com.floreysoft.jmte;

import java.util.Arrays;
import java.util.Iterator;

// ${foreach list item}${foreach item.list item2}OUTER_PRFIX${if item}${item2.property1}INNER_SUFFIX${end}${end}\n${end}
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
	// ${foreach list item}${foreach item.list item2}OUTER_PRFIX${if
	// item}${item2.property1}INNER_SUFFIX${end}${end}\n${end}
	protected String transformCompiled(TemplateContext context) {
		StringBuilder buffer = new StringBuilder();

		// ${foreach list item}
		ForEachToken token1 = new ForEachToken("list", "item", "\n");
		token1.setIterable((Iterable) token1.evaluate(context));
		context.model.enterScope();
		context.push(token1);
		try {
			while (token1.iterator().hasNext()) {
				context.model.put(token1.getVarName(), token1.advance());
				addSpecialVariables(token1, context.model);
				getEngine().notifyListeners(token1,
						ProcessListener.Action.ITERATE_FOREACH);

				// ${foreach item.list item2}
				ForEachToken token2 = new ForEachToken(Arrays
						.asList(new String[] { "item", "list" }), "item2", "\n");
				token1.setIterable((Iterable) token2.evaluate(context));
				context.model.enterScope();
				context.push(token2);
				try {
					while (token2.iterator().hasNext()) {
						context.model
								.put(token1.getVarName(), token2.advance());
						addSpecialVariables(token1, context.model);
						addSpecialVariables(token1, context.model);
						getEngine().notifyListeners(token2,
								ProcessListener.Action.ITERATE_FOREACH);

						StringToken token3 = new StringToken(Arrays
								.asList(new String[] { "item2", "property1" }),
								"item.property1");
						buffer.append(token3.evaluate(context));

						if (!token2.isLast()) {
							buffer.append(token2.getSeparator());
						}
					}
				} finally {
					context.model.exitScope();
					context.pop();
				}

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
