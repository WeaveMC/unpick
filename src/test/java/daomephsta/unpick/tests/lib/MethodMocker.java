package daomephsta.unpick.tests.lib;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;

import java.io.PrintWriter;
import java.util.function.Consumer;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

public class MethodMocker
{
	public static final String CLASS_NAME = "Invoker";
	private static final String[] NO_EXCEPTIONS = null,
								  NO_INTERFACES = null;
	private static final String NO_SIGNATURE = null,
								OBJECT_SUPERCLASS = "java/lang/Object";
	
	public static class MockMethod 
	{
		private final MethodNode mockMethod;
		private final ClassNode mockClass;
		
		public MockMethod(MethodNode mockMethod, ClassNode mockClass)
		{
			this.mockMethod = mockMethod;
			this.mockClass = mockClass;
		}

		public MethodNode getMockMethod()
		{
			return mockMethod;
		}

		public ClassNode getMockClass()
		{
			return mockClass;
		}
	}
	
	public static MockMethod mock(Consumer<MethodVisitor> bodyGenerator)
	{	
		ClassWriter mockClassWriter = new ClassWriter(COMPUTE_FRAMES);
		mockClassWriter.visit(V1_8, ACC_PUBLIC, CLASS_NAME, NO_SIGNATURE, OBJECT_SUPERCLASS, NO_INTERFACES);
		{
			MethodVisitor mockWriter = mockClassWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "mock", "()V", NO_SIGNATURE, NO_EXCEPTIONS);
			bodyGenerator.accept(mockWriter);
			mockWriter.visitInsn(RETURN); // return;
			mockWriter.visitMaxs(0, 0); //Trigger computation of stack size and local variable count
			mockWriter.visitEnd();
		}
		mockClassWriter.visitEnd();
		ClassReader classReader = new ClassReader(mockClassWriter.toByteArray());
		//Check that sane code was generated
		CheckClassAdapter.verify(classReader, false, new PrintWriter(System.out));
		ClassNode mockClass = new ClassNode();
		classReader.accept(mockClass, 0);
		return new MockMethod(MethodFetcher.fetch(mockClass, "mock()V"), mockClass);
	}
}
