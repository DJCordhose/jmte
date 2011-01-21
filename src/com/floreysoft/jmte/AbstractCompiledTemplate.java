package com.floreysoft.jmte;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractCompiledTemplate extends AbstractTemplate
		implements Template {
	protected final Set<String> usedVariables = new HashSet<String>();
	private Engine engine;
	private String template;
	private String sourceName;

	public AbstractCompiledTemplate() {
	}

	public AbstractCompiledTemplate(Engine engine) {
		this.engine = engine;
	}

	@Override
	public Set<String> getUsedVariables() {
		return usedVariables;
	}

	@Override
	public String transform(Map<String, Object> model) {
		TemplateContext context = new TemplateContext(template, sourceName,
				new ScopedMap(model), engine);

		String transformed = transformCompiled(context);
		String unescaped = Util.NO_QUOTE_MINI_PARSER.unescape(transformed);
		return unescaped;

	}

	protected abstract String transformCompiled(TemplateContext context);

	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	public Engine getEngine() {
		return engine;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getTemplate() {
		return template;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getSourceName() {
		return sourceName;
	}

}
