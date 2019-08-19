package daomephsta.unpick.constantmappers;

import java.io.IOException;

import org.objectweb.asm.ClassReader;

public interface IClassResolver
{
	public ClassReader resolveClass(String internalName) throws IOException;
}
