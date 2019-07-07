package daomephsta.unpick.tests.lib;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;

import java.io.PrintWriter;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

public class MethodInvocationFactory
{
	public static final String CLASS_NAME = "Invoker";
	private static final String[] NO_EXCEPTIONS = null,
								  NO_INTERFACES = null;
	private static final String NO_SIGNATURE = null,
								OBJECT_SUPERCLASS = "java/lang/Object";
	private static final boolean NOT_INTERFACE = false;
	
	public static MethodNode staticMethod(Class<?> methodOwner, String methodName, String methodDescriptor, Object constant)
	{
		Type expectedType = Type.getArgumentTypes(methodDescriptor)[0];
		Type actualType = Type.getType(TestUtils.unboxedType(constant.getClass()));
		if (!expectedType.equals(actualType))
		{
			throw new IllegalArgumentException(String.format("Expected constant of type %s, actual type %s", 
					expectedType.getClassName(), actualType.getClassName()));
		}
		
		ClassWriter invokerClassWriter = new ClassWriter(COMPUTE_FRAMES);
		invokerClassWriter.visit(V1_8, ACC_PUBLIC, CLASS_NAME, NO_SIGNATURE, OBJECT_SUPERCLASS, NO_INTERFACES);
		{
			MethodVisitor invokerWriter = invokerClassWriter.visitMethod(ACC_PUBLIC, "invoker", "()V", NO_SIGNATURE, NO_EXCEPTIONS);
			invokerWriter.visitVarInsn(ALOAD, 0); // Push "this" on the stack
			InstructionFactory.pushesValue(invokerWriter, constant);
			invokerWriter.visitMethodInsn(INVOKESTATIC, methodOwner.getName().replace('.', '/'), methodName, methodDescriptor, NOT_INTERFACE);
			invokerWriter.visitInsn(RETURN); // return;
			invokerWriter.visitMaxs(0, 0); //Trigger computation of stack size and local variable count
			invokerWriter.visitEnd();
		}
		invokerClassWriter.visitEnd();
		ClassReader classReader = new ClassReader(invokerClassWriter.toByteArray());
		//Check that sane code was generated
		CheckClassAdapter.verify(classReader, false, new PrintWriter(System.out));
		ClassNode invokerClass = new ClassNode();
		classReader.accept(invokerClass, 0);
		return MethodFetcher.fetch(invokerClass, "invoker()V");
	}
}
