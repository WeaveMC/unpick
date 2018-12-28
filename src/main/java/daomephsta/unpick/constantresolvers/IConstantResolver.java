package daomephsta.unpick.constantresolvers;

import java.util.Map;

import org.objectweb.asm.Type;

/**
 * Defines a method of resolving constants from their owning class and name.
 * @author Daomephsta
 */
public interface IConstantResolver
{
	/**
	 * Resolves the type and value of a constant 
	 * from its owning class and name.
	 * @param owner the internal name of the class that owns the constant.
	 * @param name the name of the constant.
	 * @return the type and value of the constant.
	 */
	public Map.Entry<Type, Object> resolveConstant(String owner, String name);
}
