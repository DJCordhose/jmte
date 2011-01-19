package com.floreysoft.jmte;

import static org.objectweb.asm.Opcodes.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import com.floreysoft.jmte.util.UniqueNameGenerator;

/**
 * 
 * @author olli
 * 
 * @see http://asm.ow2.org/
 * @see http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html
 * @see http://java.sun.com/docs/books/jvms/second_edition/html/Instructions.doc.html
>>>>>>> .r103
 */
public class Compiler {

	@SuppressWarnings("unchecked")
	protected static <T> Class<T> loadClass(byte[] b, Class<T> type) {
		return MY_CLASS_LOADER.defineClass(null, b);
	}

	protected static Class<?> loadClass(byte[] b) {
		return MY_CLASS_LOADER.defineClass(null, b);
	}

	@SuppressWarnings("unchecked")
	private static class MyClassLoader extends ClassLoader {
		public Class defineClass(String name, byte[] b) {
			return defineClass(name, b, 0, b.length);
		}
	};

	private final static MyClassLoader MY_CLASS_LOADER = new MyClassLoader();

	private final static String COMPILED_TEMPLATE_NAME_PREFIX = "com/floreysoft/jmte/compiledTemplates/Template";

	// must be globally unique
	private final static UniqueNameGenerator<String, String> uniqueNameGenerator = new UniqueNameGenerator<String, String>(
			COMPILED_TEMPLATE_NAME_PREFIX);

	protected final String template;
	protected final Engine engine;
	protected final Lexer lexer = new Lexer();
	protected final Set<String> usedVariables = new HashSet<String>();
	protected transient ClassVisitor classVisitor;
	protected transient ClassWriter classWriter;
	protected final String superClassName = "com/floreysoft/jmte/AbstractCompiledTemplate";
	protected transient String className;
	protected transient String typeDescriptor;
	protected transient StringWriter writer;
	protected transient MethodVisitor mv;

	protected transient Label startLabel = new Label();
	protected transient Label endLabel = new Label();

	protected transient TokenStream tokenStream;

	public Compiler(String template, Engine engine) {
		this.template = template;
		this.engine = engine;
	}

	private void initCompilation() {
		usedVariables.clear();
		className = uniqueNameGenerator.nextUniqueName();
		typeDescriptor = "L" + className + ";";
		classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		writer = new StringWriter();
		classVisitor = new TraceClassVisitor(classWriter,
				new PrintWriter(writer));
		// CheckClassAdapter needs tree stuff
//		classVisitor = new CheckClassAdapter(new TraceClassVisitor(classWriter,
//				new PrintWriter(writer)));
	}

	private void foreach() {
		ForEachToken feToken = (ForEachToken) tokenStream.currentToken();
		tokenStream.consume();
		codeGenerateForeachStart(feToken);
		String variableName = feToken.getVarName();
		usedVariables.add(variableName);
		Token contentToken;
		while ((contentToken = tokenStream.currentToken()) != null
				&& !(contentToken instanceof EndToken)) {
			content();
		}
		if (contentToken == null) {
			engine.getErrorHandler().error("missing-end", feToken);
		} else {
			tokenStream.consume();
			codeGenerateForeachEnd();
		}
	}

	private void condition() {
		IfToken ifToken = (IfToken) tokenStream.currentToken();
		tokenStream.consume();
		codeGenerateIfStart(ifToken);
		String variableName = ifToken.getExpression();
		usedVariables.add(variableName);
		Token contentToken;
		while ((contentToken = tokenStream.currentToken()) != null
				&& !(contentToken instanceof EndToken)
				&& !(contentToken instanceof ElseToken)) {
			content();
		}

		if (contentToken instanceof ElseToken) {
			tokenStream.consume();
			while ((contentToken = tokenStream.currentToken()) != null
					&& !(contentToken instanceof EndToken)) {
				content();
			}
		}
		if (contentToken == null) {
			engine.getErrorHandler().error("missing-end", ifToken);
		} else {
			tokenStream.consume();
			codeGenerateIfEnd();
		}
	}

	private void content() {
		Token token = tokenStream.currentToken();
		if (token instanceof PlainTextToken) {
			PlainTextToken plainTextToken = (PlainTextToken) token;
			tokenStream.consume();
			String text = plainTextToken.getText();
			codeGenerateText(text);
		} else if (token instanceof StringToken) {
			StringToken stringToken = (StringToken) token;
			tokenStream.consume();
			String variableName = stringToken.getExpression();
			usedVariables.add(variableName);
			codeGenerateStringToken(stringToken);
		} else if (token instanceof ForEachToken) {
			foreach();
		} else if (token instanceof IfToken) {
			condition();
		} else if (token instanceof ElseToken) {
			tokenStream.consume();
			engine.getErrorHandler().error("else-out-of-scope", token);
		} else if (token instanceof EndToken) {
			tokenStream.consume();
			engine.getErrorHandler().error("unmatched-end", token, null);
		}

	}

	public Template compile() {
		initCompilation();

		openCompilation();

		List<StartEndPair> scan = engine.scan(template);
		tokenStream = new TokenStream(engine.sourceName, template, scan, lexer,
				engine.getExprStartToken(), engine.getExprEndToken());
		tokenStream.nextToken();
		while (tokenStream.currentToken() != null) {
			content();
		}

		closeCompilation();

		classWriter.visitEnd();
		classVisitor.visitEnd();
		
		// FIXME: Only for debugging
		System.out.println(writer.toString());
		byte[] byteArray = classWriter.toByteArray();
		Class<?> myClass = Compiler.loadClass(byteArray);
		try {
			AbstractCompiledTemplate compiledTemplate = (AbstractCompiledTemplate) myClass
					.newInstance();
			compiledTemplate.setEngine(engine);
			compiledTemplate.usedVariables.addAll(this.usedVariables);
			return compiledTemplate;

		} catch (InstantiationException e) {
			throw new RuntimeException("Internal error " + e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Internal error " + e);
		}
	}

	private void createCtor() {
		// ctor no args
		// public SampleSimpleExpressionCompiledTemplate()
		MethodVisitor mv = classVisitor.visitMethod(ACC_PUBLIC, "<init>",
				"()V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", "()V");
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitInsn(RETURN);
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitLocalVariable("this", typeDescriptor, null, l0, l2, 0);
		// we can pass whatever we like as we have set ClassWriter.COMPUTE_FRAMES to ClassWriter
		mv.visitMaxs(1, 1);
		mv.visitEnd();

	}

	private void closeCompilation() {
		returnStringBuilder();

		mv.visitLabel(endLabel);

		mv
				.visitLocalVariable(
						"this",
						"Lcom/floreysoft/jmte/SampleComplexExpressionCompiledTemplate;",
						null, startLabel, endLabel, 0);
		mv.visitLocalVariable("model", "Lcom/floreysoft/jmte/ScopedMap;", null,
				startLabel, endLabel, 1);
		mv.visitLocalVariable("buffer", "Ljava/lang/StringBuilder;", null,
				startLabel, endLabel, 2);
		// we can pass whatever we like as we have set ClassWriter.COMPUTE_FRAMES to ClassWriter
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	// StringBuilder buffer = new StringBuilder();
	private void createStringBuilder() {
		mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>",
				"()V");
		mv.visitVarInsn(ASTORE, 2);
	}

	// return buffer.toString();

	private void returnStringBuilder() {
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder",
				"toString", "()Ljava/lang/String;");
		mv.visitInsn(ARETURN);
	}

	private void pushConstant(String parameter) {
		if (parameter != null) {
			mv.visitLdcInsn(parameter);
		} else {
			mv.visitInsn(ACONST_NULL);
		}
	}

	private void openCompilation() {

		classVisitor.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null,
				superClassName, null);

		createCtor();

		mv = classVisitor.visitMethod(ACC_PROTECTED, "transformCompiled",
				"(Lcom/floreysoft/jmte/ScopedMap;)Ljava/lang/String;", null,
				null);

		mv.visitLabel(startLabel);

		mv.visitCode();
		createStringBuilder();
	}

	private void codeGenerateStringToken(StringToken stringToken) {
		mv.visitVarInsn(ALOAD, 2);
		mv.visitTypeInsn(NEW, "com/floreysoft/jmte/StringToken");
		mv.visitInsn(DUP);
		pushConstant(stringToken.getExpression());
		pushConstant(stringToken.getExpression());
		pushConstant(stringToken.getDefaultValue());
		pushConstant(stringToken.getPrefix());
		pushConstant(stringToken.getSuffix());
		pushConstant(stringToken.getRendererName());
		pushConstant(stringToken.getParameters());
		mv
				.visitMethodInsn(
						INVOKESPECIAL,
						"com/floreysoft/jmte/StringToken",
						"<init>",
						"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "getEngine",
				"()Lcom/floreysoft/jmte/Engine;");
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "getEngine",
				"()Lcom/floreysoft/jmte/Engine;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/floreysoft/jmte/Engine",
				"getErrorHandler", "()Lcom/floreysoft/jmte/ErrorHandler;");
		mv
				.visitMethodInsn(
						INVOKEVIRTUAL,
						"com/floreysoft/jmte/StringToken",
						"evaluate",
						"(Lcom/floreysoft/jmte/Engine;Ljava/util/Map;Lcom/floreysoft/jmte/ErrorHandler;)Ljava/lang/Object;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString",
				"()Ljava/lang/String;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
				"(Ljava/lang/String;)Ljava/lang/StringBuilder;");
		mv.visitInsn(POP);

	}

	private void codeGenerateText(String text) {
		mv.visitVarInsn(ALOAD, 2);
		pushConstant(text);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
				"(Ljava/lang/String;)Ljava/lang/StringBuilder;");
		mv.visitInsn(POP);
	}

	private void codeGenerateIfEnd() {
		// TODO Auto-generated method stub

	}

	private void codeGenerateIfStart(IfToken ifToken) {

		// FIXME
		if (true) return;
		// IfToken ifToken = new IfToken("empty", false);
		mv.visitTypeInsn(NEW, "com/floreysoft/jmte/IfToken");
		mv.visitInsn(DUP);
		mv.visitLdcInsn(ifToken.getExpression());
		mv.visitInsn(

		// ifToken.isNegated()

				ICONST_0

				);
		mv.visitMethodInsn(INVOKESPECIAL, "com/floreysoft/jmte/IfToken",
				"<init>", "(Ljava/lang/String;Z)V");
		mv.visitVarInsn(ASTORE, 3);

		// Boolean condition = (Boolean) ifToken.evaluate(getEngine(), model,
		// getEngine().getErrorHandler());
		mv.visitVarInsn(ALOAD, 3);
		mv.visitVarInsn(ALOAD, 0);
		mv
				.visitMethodInsn(
						INVOKEVIRTUAL,
						"com/floreysoft/jmte/SampleIfEmptyFalseExpressionCompiledTemplate",
						"getEngine", "()Lcom/floreysoft/jmte/Engine;");
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 0);
		mv
				.visitMethodInsn(
						INVOKEVIRTUAL,
						"com/floreysoft/jmte/SampleIfEmptyFalseExpressionCompiledTemplate",
						"getEngine", "()Lcom/floreysoft/jmte/Engine;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/floreysoft/jmte/Engine",
				"getErrorHandler", "()Lcom/floreysoft/jmte/ErrorHandler;");
		mv
				.visitMethodInsn(
						INVOKEVIRTUAL,
						"com/floreysoft/jmte/IfToken",
						"evaluate",
						"(Lcom/floreysoft/jmte/Engine;Ljava/util/Map;Lcom/floreysoft/jmte/ErrorHandler;)Ljava/lang/Object;");
		mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
		mv.visitVarInsn(ASTORE, 4);
		mv.visitVarInsn(ALOAD, 4);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue",
				"()Z");

	}

	private void codeGenerateForeachEnd() {
		// TODO Auto-generated method stub

	}

	private void codeGenerateForeachStart(ForEachToken feToken) {
		// TODO Auto-generated method stub

	}

}
