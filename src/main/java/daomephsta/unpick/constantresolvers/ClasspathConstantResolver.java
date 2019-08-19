package daomephsta.unpick.constantresolvers;

import org.objectweb.asm.ClassReader;

/**
 * Resolves constants by looking for them on the classpath.
 * @author Daomephsta
 */
public class ClasspathConstantResolver extends BytecodeAnalysisConstantResolver
{
	public ClasspathConstantResolver()
	{
		super(ClassReader::new);
	}
}