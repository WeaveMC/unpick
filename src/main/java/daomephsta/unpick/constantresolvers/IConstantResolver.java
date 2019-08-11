package daomephsta.unpick.constantresolvers;

import java.util.AbstractMap;

import org.objectweb.asm.Type;

/**
 * Defines a method of resolving constants from their owning class and name.
 * @author Daomephsta
 */
public interface IConstantResolver
{
	public static class ResolvedConstant extends AbstractMap.SimpleImmutableEntry<Type, Object>
	{
		public ResolvedConstant(Type type, Object value)
		{
			super(type, value);
		}

		public Type getType()
		{
			return super.getKey();
		}

		@Override
		public Object getValue()
		{
			return super.getValue();
		}

		@Override
		public String toString()
		{
			return String.format("ResolvedConstant [Type Descriptor: %s, Value: %s]", getType(), getValue());
		}
	}

	/**
	 * Resolves the type and value of a constant 
	 * from its owning class and name.
	 * @param owner the internal name of the class that owns the constant.
	 * @param name the name of the constant.
	 * @return the type and value of the constant.
	 */
	public ResolvedConstant resolveConstant(String owner, String name);
}
