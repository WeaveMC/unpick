package daomephsta.unpick.tests.lib;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.*;

public class TestUtils
{	
	public static void printMethod(MethodNode methodNode)
	{
		StringWriter w = new StringWriter();
		try(PrintWriter pw = new PrintWriter(w))
		{
			Printer printer = new Textifier();
			ClassVisitor tracer = new TraceClassVisitor(null, printer, null);
			methodNode.accept(tracer);
			printer.print(pw);
		}
		System.out.println(w);
	}
	
	public static void printInstructions(MethodNode methodNode)
	{
		StringWriter w = new StringWriter();
		try(PrintWriter pw = new PrintWriter(w))
		{
			Printer printer = new Textifier();
			MethodVisitor tracer = new TraceMethodVisitor(printer);
			methodNode.instructions.accept(tracer);
			printer.print(pw);
		}
		System.out.println(w);
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
}
