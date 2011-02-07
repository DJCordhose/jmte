package com.floreysoft.jmte.template;

import static org.objectweb.asm.Opcodes.*;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes; //import org.objectweb.asm.util.CheckClassAdapter;
//import org.objectweb.asm.util.TraceClassVisitor;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.token.AnnotationToken;
import com.floreysoft.jmte.token.ElseToken;
import com.floreysoft.jmte.token.EndToken;
import com.floreysoft.jmte.token.ForEachToken;
import com.floreysoft.jmte.token.IfCmpToken;
import com.floreysoft.jmte.token.IfToken;
import com.floreysoft.jmte.token.InvalidToken;
import com.floreysoft.jmte.token.PlainTextToken;
import com.floreysoft.jmte.token.StringToken;
import com.floreysoft.jmte.token.Token;
import com.floreysoft.jmte.token.TokenStream;
import com.floreysoft.jmte.util.UniqueNameGenerator;

/**
 * 
 * @author olli
 * 
 * @see http://asm.ow2.org/
 * @see http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html
 * @see http://java.sun.com/docs/books/jvms/second_edition/html/Instructions.doc.html
 * @see http://stackoverflow.com/questions/148681/unloading-classes-in-java
 */
public class DynamicBytecodeCompiler implements TemplateCompiler {

	@SuppressWarnings("unchecked")
	protected <T> Class<T> loadClass(byte[] b, Class<T> type) {
		return cloadLoader.defineClass(null, b);
	}

	protected Class<?> loadClass(byte[] b) {
		return cloadLoader.defineClass(null, b);
	}

	@SuppressWarnings("unchecked")
	private static class DelegatingClassLoader extends ClassLoader {
		private final ClassLoader parentClassLoader;

		public DelegatingClassLoader(ClassLoader parentClassLoader) {
			this.parentClassLoader = parentClassLoader;
		}

		public Class defineClass(String name, byte[] b) {
			return defineClass(name, b, 0, b.length);
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			return parentClassLoader.loadClass(name);
		}
	};

	private final static String COMPILED_TEMPLATE_NAME_PREFIX = "com/floreysoft/jmte/template/CompiledTemplate";

	private final static int THIS = 0;
	private final static int CONTEXT = 1;
	private final static int BUFFER = 2;
	private final static int EXCEPTION = 3;
	private final static int HIGHEST = EXCEPTION;

	// all the compiled classes live as long as this class loader lives
	// this class loader lives as long as this compiler
	private final DelegatingClassLoader cloadLoader = new DelegatingClassLoader(
			DynamicBytecodeCompiler.class.getClassLoader());

	private final UniqueNameGenerator<String, String> uniqueNameGenerator = new UniqueNameGenerator<String, String>(
			COMPILED_TEMPLATE_NAME_PREFIX);

	protected transient Set<String> usedVariables;
	protected final List<String> localVarStack = new LinkedList<String>();
	protected transient ClassVisitor classVisitor;
	protected transient ClassWriter classWriter;
	protected final String superClassName = "com/floreysoft/jmte/template/AbstractCompiledTemplate";
	protected transient String className;
	protected transient String typeDescriptor;
	protected transient StringWriter writer;
	protected transient MethodVisitor mv;

	protected transient Label startLabel = new Label();
	protected transient Label endLabel = new Label();

	protected transient TokenStream tokenStream;
	protected transient int tokenLocalVarIndex = HIGHEST + 1;

	protected transient Engine engine;

	private void initCompilation() {
		usedVariables = new TreeSet<String>();
		localVarStack.clear();
		className = uniqueNameGenerator.nextUniqueName();
		typeDescriptor = "L" + className + ";";
		classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// only for debugging
		// writer = new StringWriter();
		// TraceClassVisitor traceClassVisitor = new TraceClassVisitor(
		// classWriter, new PrintWriter(writer));
		// classVisitor = new CheckClassAdapter(traceClassVisitor);
		// classVisitor = traceClassVisitor;
		classVisitor = classWriter;
	}

	private void addUsedVariableIfNotLocal(String variableName) {
		if (!localVarStack.contains(variableName)) {
			usedVariables.add(variableName);
		}
	}

	private void foreach() {
		ForEachToken feToken = (ForEachToken) tokenStream.currentToken();
		tokenStream.consume();

		localVarStack.add(0, feToken.getVarName());

		Label loopStart = new Label();
		Label loopEnd = new Label();
		Label tryEndLabel = new Label();

		codeGenerateForeachBlockStart(feToken, loopStart, loopEnd, tryEndLabel);

		addUsedVariableIfNotLocal(feToken.getExpression());
		Token contentToken;
		while ((contentToken = tokenStream.currentToken()) != null
				&& !(contentToken instanceof EndToken)) {
			content();
		}
		if (contentToken == null) {
			engine.getErrorHandler().error("missing-end", feToken);
		} else {
			tokenStream.consume();
		}
		codeGenerateForeachBlockEnd(loopStart, loopEnd, tryEndLabel);

		localVarStack.remove(0);
	}

	private void condition() {
		IfToken ifToken = (IfToken) tokenStream.currentToken();
		tokenStream.consume();

		Label tryEndLabel = new Label();
		Label finalLabel = new Label();
		Label elseLabel = new Label();

		codeGenerateIfBlockStart(ifToken, tryEndLabel, elseLabel);

		String variableName = ifToken.getExpression();
		addUsedVariableIfNotLocal(variableName);
		Token contentToken;

		codeGenerateIfBranchStart();
		while ((contentToken = tokenStream.currentToken()) != null
				&& !(contentToken instanceof EndToken)
				&& !(contentToken instanceof ElseToken)) {
			content();
		}
		codeGenerateIfBranchEnd(finalLabel);

		codeGenerateElseBranchStart(elseLabel);
		if (contentToken instanceof ElseToken) {
			tokenStream.consume();

			while ((contentToken = tokenStream.currentToken()) != null
					&& !(contentToken instanceof EndToken)) {
				content();
			}

		}
		codeGenerateElseBranchEnd(finalLabel);

		if (contentToken == null) {
			engine.getErrorHandler().error("missing-end", ifToken);
		} else {
			tokenStream.consume();
		}

		codeGenerateIfBlockEnd(tryEndLabel, finalLabel);
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
			addUsedVariableIfNotLocal(variableName);
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
		} else if (token instanceof InvalidToken) {
			tokenStream.consume();
			engine.getErrorHandler().error("invalid-expression", token, null);
		} else if (token instanceof AnnotationToken) {
			tokenStream.consume();
			codeGenerateAnnotationToken((AnnotationToken) token);
		} else {
			// what ever else there may be, we just ignore it
			tokenStream.consume();
		}

	}

	public Template compile(String template, String sourceName, Engine engine) {
		try {
		this.engine = engine;
		initCompilation();
		openCompilation();

		tokenStream = new TokenStream(sourceName, template, engine
				.getExprStartToken(), engine.getExprEndToken());
		tokenStream.nextToken();
		while (tokenStream.currentToken() != null) {
			content();
		}

		closeCompilation();

		classWriter.visitEnd();
		classVisitor.visitEnd();

		// FIXME: Only for debugging
		// System.out.println(writer.toString());
		byte[] byteArray = classWriter.toByteArray();
		Class<?> myClass = loadClass(byteArray);
		try {
			AbstractCompiledTemplate compiledTemplate = (AbstractCompiledTemplate) myClass
					.newInstance();
			compiledTemplate.setEngine(engine);
			compiledTemplate.setTemplate(template);
			compiledTemplate.setSourceName(sourceName);
			compiledTemplate.usedVariables = this.usedVariables;
			return compiledTemplate;

		} catch (InstantiationException e) {
			throw new RuntimeException("Internal error " + e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Internal error " + e);
		}
		} finally {
			// free resources as soon as possible
			this.engine = null;
			this.tokenStream = null;
		}
	}

	private void createCtor() {
		// ctor no args
		// public SampleSimpleExpressionCompiledTemplate()
		MethodVisitor mv = classVisitor.visitMethod(ACC_PUBLIC, "<init>",
				"()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, THIS);
		mv.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", "()V");
		mv.visitInsn(RETURN);

		// we can pass whatever we like as we have set
		// ClassWriter.COMPUTE_FRAMES to ClassWriter
		mv.visitMaxs(1, 1);
		mv.visitEnd();

	}

	private void closeCompilation() {
		returnStringBuilder();

		mv.visitLabel(endLabel);

		// we can pass whatever we like as we have set
		// ClassWriter.COMPUTE_FRAMES to ClassWriter
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	// StringBuilder buffer = new StringBuilder();
	private void createStringBuilder() {

		mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>",
				"()V");
		mv.visitVarInsn(ASTORE, BUFFER);
	}

	// return buffer.toString();

	private void returnStringBuilder() {
		mv.visitVarInsn(ALOAD, BUFFER);
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
				"(Lcom/floreysoft/jmte/TemplateContext;)Ljava/lang/String;",
				null, null);

		mv.visitCode();
		mv.visitLabel(startLabel);
		createStringBuilder();
	}

	private void codeGenerateAnnotationToken(AnnotationToken annotationToken) {

		mv.visitVarInsn(ALOAD, BUFFER);
		mv.visitTypeInsn(NEW, "com/floreysoft/jmte/token/AnnotationToken");
		mv.visitInsn(DUP);
		pushConstant(annotationToken.getReceiver());
		pushConstant(annotationToken.getArguments());
		mv.visitMethodInsn(INVOKESPECIAL,
				"com/floreysoft/jmte/token/AnnotationToken", "<init>",
				"(Ljava/lang/String;Ljava/lang/String;)V");

		mv.visitVarInsn(ALOAD, CONTEXT);
		mv.visitMethodInsn(INVOKEVIRTUAL,
				"com/floreysoft/jmte/token/AnnotationToken", "evaluate",
				"(Lcom/floreysoft/jmte/TemplateContext;)Ljava/lang/Object;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString",
				"()Ljava/lang/String;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
				"(Ljava/lang/String;)Ljava/lang/StringBuilder;");
		mv.visitInsn(POP);

	}

	private void codeGenerateStringToken(StringToken stringToken) {

		mv.visitVarInsn(ALOAD, BUFFER);
		mv.visitTypeInsn(NEW, "com/floreysoft/jmte/token/StringToken");
		mv.visitInsn(DUP);
		pushConstant(stringToken.getExpression());
		pushList(stringToken.getSegments());
		pushConstant(stringToken.getExpression());
		pushConstant(stringToken.getDefaultValue());
		pushConstant(stringToken.getPrefix());
		pushConstant(stringToken.getSuffix());
		pushConstant(stringToken.getRendererName());
		pushConstant(stringToken.getParameters());
		mv
				.visitMethodInsn(
						INVOKESPECIAL,
						"com/floreysoft/jmte/token/StringToken",
						"<init>",
						"(Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");

		mv.visitVarInsn(ALOAD, CONTEXT);
		mv.visitMethodInsn(INVOKEVIRTUAL,
				"com/floreysoft/jmte/token/StringToken", "evaluate",
				"(Lcom/floreysoft/jmte/TemplateContext;)Ljava/lang/Object;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString",
				"()Ljava/lang/String;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
				"(Ljava/lang/String;)Ljava/lang/StringBuilder;");
		mv.visitInsn(POP);

	}

	private void codeGenerateText(String text) {
		mv.visitVarInsn(ALOAD, BUFFER);
		pushConstant(text);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
				"(Ljava/lang/String;)Ljava/lang/StringBuilder;");
		mv.visitInsn(POP);
	}

	private void codeGenerateElseBranchStart(Label elseLabel) {

		// } else {
		mv.visitLabel(elseLabel);
	}

	private void codeGenerateElseBranchEnd(Label finalLabel) {
		// end of else branch
		mv.visitJumpInsn(GOTO, finalLabel);

	}

	private void codeGenerateIfBranchStart() {
		// TODO Auto-generated method stub

	}

	private void codeGenerateIfBranchEnd(Label finalLabel) {
		// end of if branch
		mv.visitJumpInsn(GOTO, finalLabel);
	}

	private void codeGenerateIfBlockEnd(Label tryEndLabel, Label finalLabel) {

		tokenLocalVarIndex--;

		// try end block rethrowing exception
		mv.visitLabel(tryEndLabel);
		mv.visitVarInsn(ASTORE, EXCEPTION);

		codeGeneratePopContext();

		mv.visitVarInsn(ALOAD, EXCEPTION);
		mv.visitInsn(ATHROW);

		// } finally {
		mv.visitLabel(finalLabel);

		// context.pop();

		codeGeneratePopContext();

	}

	private void codeGenerateIfToken(IfToken ifToken) {
		if (ifToken instanceof IfCmpToken) {

			// IfCmpToken token1 = new IfCmpToken(Arrays
			// .asList(new String[] { "address" }), "address", "Fillbert",
			// false);
			mv.visitTypeInsn(NEW, "com/floreysoft/jmte/token/IfCmpToken");
			mv.visitInsn(DUP);
			pushList(ifToken.getSegments());
			pushConstant(ifToken.getExpression());
			pushConstant(((IfCmpToken) ifToken).getOperand());
			mv.visitInsn(ifToken.isNegated() ? ICONST_1 : ICONST_0);
			mv.visitMethodInsn(INVOKESPECIAL,
					"com/floreysoft/jmte/token/IfCmpToken", "<init>",
					"(Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Z)V");

		} else {
			// IfToken token1 = new IfToken(Arrays.asList(new String[] { "bean",
			// "trueCond" }), "bean.trueCond", true);

			mv.visitTypeInsn(NEW, "com/floreysoft/jmte/token/IfToken");
			mv.visitInsn(DUP);
			pushList(ifToken.getSegments());
			pushConstant(ifToken.getExpression());
			mv.visitInsn(ifToken.isNegated() ? ICONST_1 : ICONST_0);
			mv.visitMethodInsn(INVOKESPECIAL,
					"com/floreysoft/jmte/token/IfToken", "<init>",
					"(Ljava/util/List;Ljava/lang/String;Z)V");

		}
		mv.visitVarInsn(ASTORE, tokenLocalVarIndex);

	}

	private void pushList(List<String> list) {
		mv.visitIntInsn(BIPUSH, list.size());
		mv.visitTypeInsn(ANEWARRAY, "java/lang/String");

		for (int i = 0; i < list.size(); i++) {

			mv.visitInsn(DUP);
			mv.visitIntInsn(BIPUSH, i);
			mv.visitLdcInsn(list.get(i));
			mv.visitInsn(AASTORE);
		}
		mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "asList",
				"([Ljava/lang/Object;)Ljava/util/List;");

	}

	private void codeGenerateIfBlockStart(IfToken ifToken, Label tryEndLabel,
			Label elseLabel) {
		Label tryStartLabel = new Label();

		mv.visitTryCatchBlock(tryStartLabel, tryEndLabel, tryEndLabel, null);

		codeGenerateIfToken(ifToken);

		// context.push(token1);
		mv.visitVarInsn(ALOAD, CONTEXT);
		mv.visitVarInsn(ALOAD, tokenLocalVarIndex);
		mv.visitMethodInsn(INVOKEVIRTUAL,
				"com/floreysoft/jmte/TemplateContext", "push",
				"(Lcom/floreysoft/jmte/token/Token;)V");

		// try {

		mv.visitLabel(tryStartLabel);
		// token1.evaluate(context)
		mv.visitVarInsn(ALOAD, tokenLocalVarIndex);
		mv.visitVarInsn(ALOAD, CONTEXT);
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/floreysoft/jmte/token/IfToken",
				"evaluate",
				"(Lcom/floreysoft/jmte/TemplateContext;)Ljava/lang/Object;");

		// (Boolean)
		mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue",
				"()Z");

		// if ((Boolean) token1.evaluate(context)) {
		// if the condition is 0 meaning false
		mv.visitJumpInsn(IFEQ, elseLabel);

		tokenLocalVarIndex++;

	}

	private void codeGeneratePopContext() {
		// context.pop();
		mv.visitVarInsn(ALOAD, CONTEXT);
		mv.visitMethodInsn(INVOKEVIRTUAL,
				"com/floreysoft/jmte/TemplateContext", "pop",
				"()Lcom/floreysoft/jmte/token/Token;");
		mv.visitInsn(POP);
	}

	private void codeGenerateForeachBlockEnd(Label loopStart, Label loopEnd,
			Label tryEndLabel) {

		this.tokenLocalVarIndex--;

		// if (!token1.isLast()) {
		// buffer.append(token1.getSeparator());
		// }
		mv.visitVarInsn(ALOAD, this.tokenLocalVarIndex);
		mv.visitMethodInsn(INVOKEVIRTUAL,
				"com/floreysoft/jmte/token/ForEachToken", "isLast", "()Z");
		mv.visitJumpInsn(IFNE, loopEnd);
		mv.visitVarInsn(ALOAD, BUFFER);
		mv.visitVarInsn(ALOAD, this.tokenLocalVarIndex);
		mv.visitMethodInsn(INVOKEVIRTUAL,
				"com/floreysoft/jmte/token/ForEachToken", "getSeparator",
				"()Ljava/lang/String;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
				"(Ljava/lang/String;)Ljava/lang/StringBuilder;");
		mv.visitInsn(POP);

		// while (token1.iterator().hasNext()) {
		mv.visitLabel(loopEnd);
		mv.visitVarInsn(ALOAD, this.tokenLocalVarIndex);
		mv.visitMethodInsn(INVOKEVIRTUAL,
				"com/floreysoft/jmte/token/ForEachToken", "iterator",
				"()Ljava/util/Iterator;");
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext",
				"()Z");
		mv.visitJumpInsn(IFNE, loopStart);

		Label noExceptionFinallyLabel = new Label();
		mv.visitJumpInsn(GOTO, noExceptionFinallyLabel);

		// exception occurred => first finally block, then throw exception
		mv.visitLabel(tryEndLabel);
		mv.visitVarInsn(ASTORE, EXCEPTION);

		codeGenerateExitScope();
		codeGeneratePopContext();

		mv.visitVarInsn(ALOAD, EXCEPTION);
		mv.visitInsn(ATHROW);

		// no exception occurred => execute finally block only
		mv.visitLabel(noExceptionFinallyLabel);
		codeGenerateExitScope();
		codeGeneratePopContext();
	}

	private void codeGenerateForeachToken(ForEachToken feToken) {
		// ForEachToken token1 = new ForEachToken(Arrays
		// .asList(new String[] { "list" }),"list", "item", "\n");
		mv.visitTypeInsn(NEW, "com/floreysoft/jmte/token/ForEachToken");
		mv.visitInsn(DUP);
		pushList(feToken.getSegments());
		pushConstant(feToken.getExpression());
		pushConstant(feToken.getVarName());
		pushConstant(feToken.getSeparator());
		mv
				.visitMethodInsn(INVOKESPECIAL,
						"com/floreysoft/jmte/token/ForEachToken", "<init>",
						"(Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
		mv.visitVarInsn(ASTORE, this.tokenLocalVarIndex);

		// token1.setIterable((Iterable) token1.evaluate(context));
		mv.visitVarInsn(ALOAD, this.tokenLocalVarIndex);
		mv.visitVarInsn(ALOAD, this.tokenLocalVarIndex);
		mv.visitVarInsn(ALOAD, CONTEXT);
		mv.visitMethodInsn(INVOKEVIRTUAL,
				"com/floreysoft/jmte/token/ForEachToken", "evaluate",
				"(Lcom/floreysoft/jmte/TemplateContext;)Ljava/lang/Object;");
		mv.visitTypeInsn(CHECKCAST, "java/lang/Iterable");
		mv.visitMethodInsn(INVOKEVIRTUAL,
				"com/floreysoft/jmte/token/ForEachToken", "setIterable",
				"(Ljava/lang/Iterable;)V");

	}

	private void codeGenerateForeachBlockStart(ForEachToken feToken,
			Label loopStart, Label loopEnd, Label tryEndLabel) {
		Label tryStartLabel = new Label();
		mv.visitTryCatchBlock(tryStartLabel, tryEndLabel, tryEndLabel, null);

		codeGenerateForeachToken(feToken);

		// context.model.enterScope();
		mv.visitVarInsn(ALOAD, CONTEXT);
		mv.visitFieldInsn(GETFIELD, "com/floreysoft/jmte/TemplateContext",
				"model", "Lcom/floreysoft/jmte/ScopedMap;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/floreysoft/jmte/ScopedMap",
				"enterScope", "()V");

		// context.push(token1);
		mv.visitVarInsn(ALOAD, CONTEXT);
		mv.visitVarInsn(ALOAD, this.tokenLocalVarIndex);
		mv.visitMethodInsn(INVOKEVIRTUAL,
				"com/floreysoft/jmte/TemplateContext", "push",
				"(Lcom/floreysoft/jmte/token/Token;)V");

		// try {
		mv.visitLabel(tryStartLabel);

		// while (token1.iterator().hasNext()) {

		mv.visitJumpInsn(GOTO, loopEnd);
		mv.visitLabel(loopStart);

		// context.model.put(token1.getVarName(), token1.advance());
		mv.visitVarInsn(ALOAD, CONTEXT);
		mv.visitFieldInsn(GETFIELD, "com/floreysoft/jmte/TemplateContext",
				"model", "Lcom/floreysoft/jmte/ScopedMap;");
		mv.visitVarInsn(ALOAD, this.tokenLocalVarIndex);
		mv.visitMethodInsn(INVOKEVIRTUAL,
				"com/floreysoft/jmte/token/ForEachToken", "getVarName",
				"()Ljava/lang/String;");
		mv.visitVarInsn(ALOAD, this.tokenLocalVarIndex);
		mv.visitMethodInsn(INVOKEVIRTUAL,
				"com/floreysoft/jmte/token/ForEachToken", "advance",
				"()Ljava/lang/Object;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/floreysoft/jmte/ScopedMap",
				"put",
				"(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;");
		mv.visitInsn(POP);

		// addSpecialVariables(token1, context.model);
		mv.visitVarInsn(ALOAD, THIS);
		mv.visitVarInsn(ALOAD, this.tokenLocalVarIndex);
		mv.visitVarInsn(ALOAD, CONTEXT);
		mv.visitFieldInsn(GETFIELD, "com/floreysoft/jmte/TemplateContext",
				"model", "Lcom/floreysoft/jmte/ScopedMap;");
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "addSpecialVariables",
				"(Lcom/floreysoft/jmte/token/ForEachToken;Ljava/util/Map;)V");

		this.tokenLocalVarIndex++;
	}

	private void codeGenerateExitScope() {
		// context.model.exitScope();
		mv.visitVarInsn(ALOAD, CONTEXT);
		mv.visitFieldInsn(GETFIELD, "com/floreysoft/jmte/TemplateContext",
				"model", "Lcom/floreysoft/jmte/ScopedMap;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/floreysoft/jmte/ScopedMap",
				"exitScope", "()V");
	}

}
