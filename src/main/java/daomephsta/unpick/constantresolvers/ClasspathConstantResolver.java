package daomephsta.unpick.constantresolvers;

import java.io.IOException;

import org.objectweb.asm.ClassReader;

/**
 * Resolves constants by looking for them on the classpath.
 * @author Daomephsta
 */
public class ClasspathConstantResolver extends ASMVisitingConstantResolver
{
	@Override
	protected ClassReader createClassReader(String owner) throws IOException
	{
		return new ClassReader(owner);
	}
}