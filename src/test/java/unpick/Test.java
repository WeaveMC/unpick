package unpick;

import java.io.*;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import daomephsta.unpick.ConstantUninliner;
import daomephsta.unpick.datadriven.ClasspathConstantResolver;
import daomephsta.unpick.datadriven.DataDrivenConstantMapper;

public class Test
{
	public static void main(String[] args)
	{
		DataDrivenConstantMapper mapper = new DataDrivenConstantMapper(Test.class.getResourceAsStream("test.unpick"), 
			new ClasspathConstantResolver());
		ConstantUninliner uninliner = new ConstantUninliner(mapper);
		try
		{
			ClassReader classReader = new ClassReader("unpick.TestClass");
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			uninliner.transform(classNode);
			
			ClassWriter classWriter = new ClassWriter(classReader, 0);
			classNode.accept(classWriter);
			
			OutputStream out = new FileOutputStream("TestClass.class");
			out.write(classWriter.toByteArray());
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
