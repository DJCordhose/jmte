package com.floreysoft.jmte;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.floreysoft.jmte.util.Util;

public abstract class AbstractCompiledTemplate extends Template {
	protected final Set<String> usedVariables = new TreeSet<String>();;
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

	@Override
	public String toString() {
		return template;
	}

}
