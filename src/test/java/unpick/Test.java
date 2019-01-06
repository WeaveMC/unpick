package unpick;

import java.io.*;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import daomephsta.unpick.constantmappers.datadriven.DataDrivenConstantMapper;
import daomephsta.unpick.constantresolvers.ClasspathConstantResolver;
import daomephsta.unpick.transformers.ConstantUninliner;

public class Test
{
	public static void main(String[] args)
	{
		DataDrivenConstantMapper mapper = new DataDrivenConstantMapper(ClassLoader.getSystemClassLoader().getResourceAsStream("test.unpick"), 
			new ClasspathConstantResolver());
		ConstantUninliner uninliner = new ConstantUninliner(mapper);
		processTestFile(uninliner, SimpleConstantUninliningTest.class);
		processTestFile(uninliner, FlagUninliningTest.class);
	}

	private static void processTestFile(ConstantUninliner uninliner, Class<?> clazz)
	{
		try
		{
			ClassReader classReader = new ClassReader(clazz.getName());
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			uninliner.transform(classNode);
			
			ClassWriter classWriter = new ClassWriter(classReader, 0);
			classNode.accept(classWriter);
			
			File outFile = new File(System.getProperty("user.dir") + "/test-out/" + clazz.getSimpleName() + ".class");
			outFile.getParentFile().mkdirs();
			outFile.createNewFile();
			
			OutputStream out = new FileOutputStream(outFile);
			out.write(classWriter.toByteArray());
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
