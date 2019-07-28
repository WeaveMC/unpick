package daomephsta.unpick;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.*;

public class Utils
{
	public static String getMethodKey(String methodOwner, String methodName, String methodDescriptor)
	{
		return methodOwner + '.' + methodName + methodDescriptor;
	}

	public static String visitableToString(Consumer<MethodVisitor> visitable)
	{
		StringWriter w = new StringWriter();
		try(PrintWriter pw = new PrintWriter(w))
		{
			Printer printer = new Textifier();
			MethodVisitor tracer = new TraceMethodVisitor(printer);
			visitable.accept(tracer);
			printer.print(pw);
		}
		return w.toString();
	}
}
