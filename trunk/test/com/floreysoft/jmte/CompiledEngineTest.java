package com.floreysoft.jmte;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.junit.Test;

import com.floreysoft.jmte.template.Template;

public class CompiledEngineTest extends AbstractEngineTest {

	protected Engine newEngine() {
		Engine engine = new Engine();
		engine.setUseCompilation(true);
		return engine;
	}

	@Test
	public void compiledClassLoaders() throws Exception {
		String templateSource = "${address}";
		Engine engine1 = new Engine();
		engine1.setUseCompilation(true);
		Engine engine2 = new Engine();
		engine2.setUseCompilation(true);

		// each engine has a class loader of its own leading to two classes
		// having the same name
		Template template1 = engine1.getTemplate(templateSource);
		Template template2 = engine2.getTemplate(templateSource);
		assertEquals(template1.getClass().getName(), template2.getClass()
				.getName());
		// sill, both classes are not the same
		assertNotSame(template1.getClass(), template2.getClass());
		// but, both still work
		String transformed1 = template1.transform(DEFAULT_MODEL);
		String transformed2 = template2.transform(DEFAULT_MODEL);
		// and give the same result
		assertEquals(transformed1, transformed2);
	}
}