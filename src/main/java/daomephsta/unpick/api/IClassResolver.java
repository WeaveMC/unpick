package daomephsta.unpick.api;

import java.io.IOException;

import org.objectweb.asm.ClassReader;

/**
 * Resolves classes as {@link ClassReader}s, by their internal name
 * @author Daomephsta
 */
public interface IClassResolver
{
	/**
	 * @param internalName the internal name of the class to resolve
	 * @return a {@link ClassReader} for the resolved class
	 */
	public ClassReader resolveClass(String internalName) throws IOException;
}
