package com.floreysoft.jmte;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.floreysoft.jmte.ProcessListener.Action;
import com.floreysoft.jmte.message.DefaultErrorHandler;
import com.floreysoft.jmte.renderer.DefaultCollectionRenderer;
import com.floreysoft.jmte.renderer.DefaultIterableRenderer;
import com.floreysoft.jmte.renderer.DefaultMapRenderer;
import com.floreysoft.jmte.renderer.DefaultObjectRenderer;
import com.floreysoft.jmte.template.Compiler;
import com.floreysoft.jmte.template.InterpretedTemplate;
import com.floreysoft.jmte.template.Template;
import com.floreysoft.jmte.token.Token;
import com.floreysoft.jmte.util.Tool;
import com.floreysoft.jmte.util.Util;

/**
 * <p>
 * The template engine - <b>THIS IS WHERE YOU START LOOKING</b>.
 * </p>
 * 
 * <p>
 * Usually this is the only class you need calling
 * {@link #transform(String, Map)}. Like this
 * </p>
 * 
 * <pre>
 * Engine engine = new Engine();
 * String transformed = engine.transform(input, model);
 * </pre>
 * 
 * <p>
 * You have to provide the template input written in the template language and a
 * model from String to Object. Maybe like this
 * </p>
 * 
 * <pre>
 * String input = &quot;${name}&quot;;
 * Map&lt;String, Object&gt; model = new HashMap&lt;String, Object&gt;();
 * model.put(&quot;name&quot;, &quot;Minimal Template Engine&quot;);
 * Engine engine = new Engine();
 * String transformed = engine.transform(input, model);
 * assert (transformed.equals(&quot;Minimal Template Engine&quot;));
 * </pre>
 * 
 * where <code>input</code> contains the template and <code>model</code> the
 * model. <br>
 * 
 * <p>
 * Use {@link #setUseCompilation(boolean)} to switch on compilation mode. This
 * will compile the template into Java byte code before execution. Especially
 * when the template is used often this will speed up the execution by a factor
 * between 2 and 10. However, each compiled template results in a new class
 * definition and a new globally cached singleton instance of it.
 * </p>
 * 
 * @see ErrorHandler
 * @see Tool
 * @see Renderer
 * @see NamedRenderer
 * @see ModelAdaptor
 * @see ProcessListener
 */
public final class Engine {
	private String exprStartToken = "${";
	private String exprEndToken = "}";
	private double expansionSizeFactor = 2;
	private ErrorHandler errorHandler = new DefaultErrorHandler();
	private boolean useCompilation = false;
	private boolean enabledInterpretedTemplateCache = true;
	private ModelAdaptor modelAdaptor = new DefaultModelAdaptor();

	// As classes will never be unloaded and are thus global, it might be a good
	// idea to have
	// the templates in a static, shared location as well
	private final static Map<String, Template> compiledTemplates = new HashMap<String, Template>();

	// interpreted templates cache lives as long as this engine
	private final Map<String, Template> interpretedTemplates = new HashMap<String, Template>();

	private final Map<Class<?>, Renderer<?>> renderers = new HashMap<Class<?>, Renderer<?>>();
	private final Map<Class<?>, Renderer<?>> resolvedRendererCache = new HashMap<Class<?>, Renderer<?>>();

	private final Map<String, NamedRenderer> namedRenderers = new HashMap<String, NamedRenderer>();
	private final Map<Class<?>, Set<NamedRenderer>> namedRenderersForClass = new HashMap<Class<?>, Set<NamedRenderer>>();

	final List<ProcessListener> listeners = new ArrayList<ProcessListener>();

	/**
	 * Creates a new engine having <code>${</code> and <code>}</code> as start
	 * and end strings for expressions.
	 */
	public Engine() {
		init();
	}

	private void init() {
		registerRenderer(Object.class, new DefaultObjectRenderer());
		registerRenderer(Map.class, new DefaultMapRenderer());
		registerRenderer(Collection.class, new DefaultCollectionRenderer());
		registerRenderer(Iterable.class, new DefaultIterableRenderer());
	}

	/**
	 * Transforms a template into an expanded output using the given model.
	 * 
	 * @param template
	 *            the template to expand
	 * @param sourceName
	 *            the name of the current template (if there is anything like
	 *            that)
	 * @param model
	 *            the model used to evaluate expressions inside the template
	 * @return the expanded output
	 */
	public String transform(String template, String sourceName,
			Map<String, Object> model) {
		Template templateImpl = getTemplate(template, sourceName);
		String output = templateImpl.transform(model);
		return output;
	}

	/**
	 * Transforms a template into an expanded output using the given model.
	 * 
	 * @param template
	 *            the template to expand
	 * @param model
	 *            the model used to evaluate expressions inside the template
	 * @return the expanded output
	 */
	public String transform(String template, Map<String, Object> model) {
		return transform(template, null, model);
	}

	/**
	 * Gets all variables used in the given template.
	 */
	public Set<String> getUsedVariables(String template) {
		Template templateImpl = getTemplate(template, null);
		return templateImpl.getUsedVariables();
	}

	public Engine registerNamedRenderer(NamedRenderer renderer) {
		namedRenderers.put(renderer.getName(), renderer);
		Set<Class<?>> supportedClasses = Util.asSet(renderer
				.getSupportedClasses());
		for (Class<?> clazz : supportedClasses) {
			Class<?> classInHierarchy = clazz;
			while (classInHierarchy != null) {
				addSupportedRenderer(classInHierarchy, renderer);
				classInHierarchy = classInHierarchy.getSuperclass();
			}
		}
		return this;
	}

	public Engine deregisterNamedRenderer(NamedRenderer renderer) {
		namedRenderers.remove(renderer.getName());
		Set<Class<?>> supportedClasses = Util.asSet(renderer
				.getSupportedClasses());
		for (Class<?> clazz : supportedClasses) {
			Class<?> classInHierarchy = clazz;
			while (classInHierarchy != null) {
				Set<NamedRenderer> renderers = namedRenderersForClass
						.get(classInHierarchy);
				renderers.remove(renderer);
				classInHierarchy = classInHierarchy.getSuperclass();
			}
		}
		return this;
	}

	private void addSupportedRenderer(Class<?> clazz, NamedRenderer renderer) {
		Collection<NamedRenderer> compatibleRenderers = getCompatibleRenderers(clazz);
		compatibleRenderers.add(renderer);
	}

	public Collection<NamedRenderer> getCompatibleRenderers(Class<?> inputType) {
		Set<NamedRenderer> renderers = namedRenderersForClass.get(inputType);
		if (renderers == null) {
			renderers = new HashSet<NamedRenderer>();
			namedRenderersForClass.put(inputType, renderers);
		}
		return renderers;
	}

	public Collection<NamedRenderer> getAllNamedRenderers() {
		Collection<NamedRenderer> values = namedRenderers.values();
		return values;
	}

	public NamedRenderer resolveNamedRenderer(String rendererName) {
		return namedRenderers.get(rendererName);
	}

	public <C> Engine registerRenderer(Class<C> clazz, Renderer<C> renderer) {
		renderers.put(clazz, renderer);
		resolvedRendererCache.clear();
		return this;
	}

	public Engine deregisterRenderer(Class<?> clazz) {
		renderers.remove(clazz);
		resolvedRendererCache.clear();
		return this;
	}

	@SuppressWarnings("unchecked")
	public Renderer<Object> resolveRendererForClass(Class<?> clazz) {
		Renderer resolvedRenderer = resolvedRendererCache.get(clazz);
		if (resolvedRenderer != null) {
			return resolvedRenderer;
		}

		resolvedRenderer = renderers.get(clazz);
		if (resolvedRenderer == null) {
			Class<?>[] interfaces = clazz.getInterfaces();
			for (Class<?> interfaze : interfaces) {
				resolvedRenderer = resolveRendererForClass(interfaze);
				if (resolvedRenderer != null) {
					break;
				}
			}
		}
		if (resolvedRenderer == null) {
			Class<?> superclass = clazz.getSuperclass();
			if (superclass != null) {
				resolvedRenderer = resolveRendererForClass(superclass);
			}
		}
		if (resolvedRenderer != null) {
			resolvedRendererCache.put(clazz, resolvedRenderer);
		}
		return resolvedRenderer;
	}

	public void addProcessListener(ProcessListener listener) {
		listeners.add(listener);
	}

	public void removeProcessListener(ProcessListener listener) {
		listeners.remove(listener);
	}

	protected void notifyProcessListeners(Token token, Action action) {
		for (ProcessListener processListener : listeners) {
			processListener.log(token, action);
		}
	}

	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public String getExprStartToken() {
		return exprStartToken;
	}

	public String getExprEndToken() {
		return exprEndToken;
	}

	public void setExprStartToken(String exprStartToken) {
		this.exprStartToken = exprStartToken;
	}

	public void setExprEndToken(String exprEndToken) {
		this.exprEndToken = exprEndToken;
	}

	public void setExpansionSizeFactor(double expansionSizeFactor) {
		this.expansionSizeFactor = expansionSizeFactor;
	}

	public double getExpansionSizeFactor() {
		return expansionSizeFactor;
	}

	public boolean isUseCompilation() {
		return useCompilation;
	}

	public void setUseCompilation(boolean useCompilation) {
		this.useCompilation = useCompilation;
	}

	public void setModelAdaptor(ModelAdaptor modelAdaptor) {
		this.modelAdaptor = modelAdaptor;
	}

	public ModelAdaptor getModelAdaptor() {
		return modelAdaptor;
	}

	public boolean isEnabledInterpretedTemplateCache() {
		return enabledInterpretedTemplateCache;
	}

	public void setEnabledInterpretedTemplateCache(
			boolean enabledInterpretedTemplateCache) {
		this.enabledInterpretedTemplateCache = enabledInterpretedTemplateCache;
	}

	private Template getTemplate(String template, String sourceName) {
		Template templateImpl;
		if (useCompilation) {
			templateImpl = compiledTemplates.get(template);
			if (templateImpl == null) {
				templateImpl = new Compiler(template, sourceName, this)
						.compile();
				compiledTemplates.put(template, templateImpl);
			}
			return templateImpl;
		} else {
			if (enabledInterpretedTemplateCache) {
				templateImpl = interpretedTemplates.get(template);
				if (templateImpl == null) {
					templateImpl = new InterpretedTemplate(template,
							sourceName, this);
					interpretedTemplates.put(template, templateImpl);
				}
			} else {
				templateImpl = new InterpretedTemplate(template, sourceName,
						this);
			}
		}
		return templateImpl;
	}

}
