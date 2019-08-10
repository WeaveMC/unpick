package daomephsta.unpick.tests.lib;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Value;

import daomephsta.unpick.InstructionFactory;
import daomephsta.unpick.Utils;
import daomephsta.unpick.tests.lib.MethodMocker.MockMethod;

public class TestUtils
{	
	public static void printVisitable(Consumer<MethodVisitor> visitable)
	{
		System.out.println(Utils.visitableToString(visitable));
	}
	
	private static final Map<Class<?>, Class<?>> BOXED_TO_UNBOXED = new HashMap<>();
	static
	{
		BOXED_TO_UNBOXED.put(Void.class, void.class);
		BOXED_TO_UNBOXED.put(Float.class, float.class);
		BOXED_TO_UNBOXED.put(Double.class, double.class);
		BOXED_TO_UNBOXED.put(Long.class, long.class);
		BOXED_TO_UNBOXED.put(Integer.class, int.class);
		BOXED_TO_UNBOXED.put(Short.class, short.class);
		BOXED_TO_UNBOXED.put(Character.class, char.class);
		BOXED_TO_UNBOXED.put(Byte.class, byte.class);
		BOXED_TO_UNBOXED.put(Boolean.class, boolean.class);
	}
	
	public static Class<?> unboxedType(Class<?> boxed)
	{
		return BOXED_TO_UNBOXED.getOrDefault(boxed, boxed);
	}
	
	public static <V extends Value> Stream<V> streamStack(Frame<V> frame)
	{
		return IntStream.range(0, frame.getStackSize()).mapToObj(frame::getStack);
	}
	
	public static String toBase2And10(long l)
	{
		return l + " = 0b" + Long.toBinaryString(l);
	}

	public static MockMethod mockInvokeStatic(Class<?> methodOwner, String methodName, String methodDescriptor, Object constant)
	{
		Type expectedType = Type.getArgumentTypes(methodDescriptor)[0];
		Type actualType = Type.getType(unboxedType(constant.getClass()));
		if (!expectedType.equals(actualType))
		{
			throw new IllegalArgumentException(String.format("Expected constant of type %s, actual type %s", 
					expectedType.getClassName(), actualType.getClassName()));
		}
		
		return MethodMocker.mock(mockWriter -> 
		{
			InstructionFactory.pushesValue(mockWriter, constant);
			mockWriter.visitMethodInsn(INVOKESTATIC, methodOwner.getName().replace('.', '/'), methodName, methodDescriptor, false);
		});
	}
	
	public static void dumpClassNode(ClassNode clazz, File dumpPath, String dumpName)
	{
		ClassWriter cw = new ClassWriter(0);
		clazz.accept(cw);
		dumpPath.mkdirs();
		try (OutputStream out = new FileOutputStream(new File(dumpPath, dumpName + ".class")))
		{
			out.write(cw.toByteArray());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
