package com.floreysoft.jmte.sample;

import java.util.Arrays;
import java.util.TreeSet;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.template.AbstractCompiledTemplate;
import com.floreysoft.jmte.token.ForEachToken;
import com.floreysoft.jmte.token.IfToken;
import com.floreysoft.jmte.token.StringToken;

// ${foreach list item}${foreach item.list item2}OUTER_PRFIX${if item}${item2.property1}INNER_SUFFIX${end}${end}\n${end}
public class SampleNestedExpressionCompiledTemplate extends
		AbstractCompiledTemplate {

	public SampleNestedExpressionCompiledTemplate() {
	}

	public SampleNestedExpressionCompiledTemplate(Engine engine) {
		super(engine);
		usedVariables = new TreeSet<String>();
		usedVariables.add("address");
	}

	@Override
	@SuppressWarnings("unchecked")
	// ${foreach list item}${foreach item.list item2}OUTER_PRFIX
	// ${if item}${item2.property1}INNER_SUFFIX${end}${end}\n${end}
	protected String transformCompiled(TemplateContext context) {
		StringBuilder buffer = new StringBuilder();

		// ${foreach list item}
		ForEachToken token1 = new ForEachToken("list", "item", "");
		token1.setIterable((Iterable) token1.evaluate(context));
		context.model.enterScope();
		context.push(token1);
		try {
			while (token1.iterator().hasNext()) {
				context.model.put(token1.getVarName(), token1.advance());
				addSpecialVariables(token1, context.model);

				// ${foreach item.list item2}
				ForEachToken token2 = new ForEachToken(Arrays
						.asList(new String[] { "item", "list" }), "item.list",
						"item2", "");
				token2.setIterable((Iterable) token2.evaluate(context));
				context.model.enterScope();
				context.push(token2);
				try {
					while (token2.iterator().hasNext()) {
						context.model
								.put(token2.getVarName(), token2.advance());
						addSpecialVariables(token2, context.model);

						// OUTER_PRFIX
						buffer.append("OUTER_PRFIX");

						// ${if item}
						IfToken token3 = new IfToken("item", false);
						context.push(token3);
						try {
							if ((Boolean) token3.evaluate(context)) {

								// ${item2.property1}
								buffer.append(new StringToken(Arrays
										.asList(new String[] { "item2",
												"property1" }),
										"item.property1").evaluate(context));

								// INNER_SUFFIX
								buffer.append("INNER_SUFFIX");

							}
						} finally {
							context.pop();
						}

						if (!token2.isLast()) {
							buffer.append(token2.getSeparator());
						}
					}
				} finally {
					context.model.exitScope();
					context.pop();
				}

				// \n
				buffer.append("\n");
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
