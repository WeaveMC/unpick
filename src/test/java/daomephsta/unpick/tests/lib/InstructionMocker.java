package daomephsta.unpick.tests.lib;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;

import java.io.PrintWriter;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

public class InstructionMocker
{
	public static final String CLASS_NAME = "Invoker";
	private static final String[] NO_EXCEPTIONS = null,
								  NO_INTERFACES = null;
	private static final String NO_SIGNATURE = null,
								OBJECT_SUPERCLASS = "java/lang/Object";
	private static final boolean NOT_INTERFACE = false;
	
	public static MethodNode mockInvokeStatic(Class<?> methodOwner, String methodName, String methodDescriptor, Object constant)
	{
		Type expectedType = Type.getArgumentTypes(methodDescriptor)[0];
		Type actualType = Type.getType(TestUtils.unboxedType(constant.getClass()));
		if (!expectedType.equals(actualType))
		{
			throw new IllegalArgumentException(String.format("Expected constant of type %s, actual type %s", 
					expectedType.getClassName(), actualType.getClassName()));
		}
		
		ClassWriter mockClassWriter = new ClassWriter(COMPUTE_FRAMES);
		mockClassWriter.visit(V1_8, ACC_PUBLIC, CLASS_NAME, NO_SIGNATURE, OBJECT_SUPERCLASS, NO_INTERFACES);
		{
			MethodVisitor mockWriter = mockClassWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "invoker", "()V", NO_SIGNATURE, NO_EXCEPTIONS);
			InstructionFactory.pushesValue(mockWriter, constant);
			mockWriter.visitMethodInsn(INVOKESTATIC, methodOwner.getName().replace('.', '/'), methodName, methodDescriptor, NOT_INTERFACE);
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
		return MethodFetcher.fetch(mockClass, "invoker()V");
	}
}
