package com.floreysoft.jmte;

import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_6;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

import com.floreysoft.jmte.util.UniqueNameGenerator;

/**
 * 
 * @author olli
 * 
 * @see http://asm.ow2.org/
 * @see http
 *      ://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html
 * @see http 
 *      ://java.sun.com/docs/books/jvms/second_edition/html/Instructions.doc.
 *      html
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

	private final static UniqueNameGenerator<String, String> uniqueNameGenerator = new UniqueNameGenerator<String, String>(
			COMPILED_TEMPLATE_NAME_PREFIX, 0);

	protected final String template;
	protected final Engine engine;
	protected final Lexer lexer = new Lexer();
	protected final LinkedList<Token> scopes = new LinkedList<Token>();
	protected transient ClassVisitor classVisitor;
	protected transient ClassWriter classWriter;
	protected final String superClassName = "com/floreysoft/jmte/AbstractCompiledTemplate";
	protected transient String className;
	protected transient String typeDescriptor;
	protected transient StringWriter writer;

	public Compiler(String template, Engine engine) {
		this.template = template;
		this.engine = engine;
	}

	private void initCompilation() {
		scopes.clear();
		className = uniqueNameGenerator.nextUniqueName();
		typeDescriptor = "L" + className + ";";
		classWriter = new ClassWriter(0);
		writer = new StringWriter();
		classVisitor = new TraceClassVisitor(classWriter, new PrintWriter(
				writer));
	}

	public Template compile() {
		initCompilation();
		writeHeader();

		List<StartEndPair> scan = engine.scan(template);
		final TokenStream tokenStream = new TokenStream(engine.sourceName,
				template, scan, lexer, engine.getExprStartToken(), engine
						.getExprEndToken());
		List<Token> allTokens = tokenStream.getAllTokens();
		for (Token token : allTokens) {
			if (token instanceof PlainTextToken) {
			} else if (token instanceof StringToken) {
			} else if (token instanceof ForEachToken) {
				ForEachToken feToken = (ForEachToken) token;
				push(feToken);
			} else if (token instanceof IfToken) {
				push(token);
			} else if (token instanceof ElseToken) {
				Token poppedToken = pop();
				if (!(poppedToken instanceof IfToken)) {
					engine.getErrorHandler().error("else-out-of-scope", token,
							Engine.toModel("surroundingToken", poppedToken));
				} else {
					ElseToken elseToken = (ElseToken) token;
					elseToken.setIfToken((IfToken) poppedToken);
					push(elseToken);
				}
			} else if (token instanceof EndToken) {
				Token poppedToken = pop();
				if (poppedToken == null) {
					engine.getErrorHandler()
							.error("unmatched-end", token, null);
				} else if (poppedToken instanceof ForEachToken) {
					// END OF FOREACH
				} else {
					// END OF IF OR ELSE
				}
			}
		}

		classVisitor.visitEnd();
		// FIXME: Only for debugging
		System.out.println(writer.toString());
		byte[] byteArray = classWriter.toByteArray();
		Class<?> myClass = Compiler.loadClass(byteArray);
		try {
			AbstractCompiledTemplate compiledTemplate = (AbstractCompiledTemplate) myClass
					.newInstance();
			compiledTemplate.setEngine(engine);
			return compiledTemplate;

		} catch (InstantiationException e) {
			throw new RuntimeException("Internal error " + e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Internal error " + e);
		}
	}

	private void push(Token token) {
		scopes.add(token);
	}

	private Token pop() {
		if (scopes.isEmpty()) {
			return null;
		} else {
			Token token = scopes.removeLast();
			return token;
		}
	}

	private void writeHeader() {

		MethodVisitor mv;

		classVisitor.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null,
				superClassName, null);
		{
			mv = classVisitor.visitMethod(ACC_PUBLIC, "<init>", "()V", null,
					null);
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
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}

		{
			mv = classVisitor.visitMethod(ACC_PUBLIC, "<init>",
					"(Lcom/floreysoft/jmte/Engine;)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>",
					"(Lcom/floreysoft/jmte/Engine;)V");
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "usedVariables",
					"Ljava/util/Set;");
			mv.visitLdcInsn("address");
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "add",
					"(Ljava/lang/Object;)Z");
			mv.visitInsn(POP);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitInsn(RETURN);
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitLocalVariable("this", typeDescriptor, null, l0, l3, 0);
			mv.visitLocalVariable("engine", "Lcom/floreysoft/jmte/Engine;",
					null, l0, l3, 1);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			mv = classVisitor.visitMethod(ACC_PROTECTED, "transformCompiled",
					"(Lcom/floreysoft/jmte/ScopedMap;)Ljava/lang/String;",
					null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitTypeInsn(NEW, "com/floreysoft/jmte/StringToken");
			mv.visitInsn(DUP);
			mv.visitLdcInsn("address");
			mv.visitLdcInsn("address");
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ACONST_NULL);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitInsn(ACONST_NULL);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv
					.visitMethodInsn(
							INVOKESPECIAL,
							"com/floreysoft/jmte/StringToken",
							"<init>",
							"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
			mv.visitVarInsn(ASTORE, 2);
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitVarInsn(ALOAD, 2);
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
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString",
					"()Ljava/lang/String;");
			Label l5 = new Label();
			mv.visitLabel(l5);
			mv.visitInsn(ARETURN);
			Label l6 = new Label();
			mv.visitLabel(l6);
			mv.visitLocalVariable("this", typeDescriptor, null, l0, l6, 0);
			mv.visitLocalVariable("model", "Lcom/floreysoft/jmte/ScopedMap;",
					null, l0, l6, 1);
			mv.visitLocalVariable("stringToken",
					"Lcom/floreysoft/jmte/StringToken;", null, l3, l6, 2);
			mv.visitMaxs(9, 3);
			mv.visitEnd();
		}
	}

}
