package daomephsta.unpick.constantresolvers;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.*;

import daomephsta.unpick.Types;

/**
 * Resolves constants by looking for them on the classpath.
 * @author Daomephsta
 */
public class ClasspathConstantResolver implements IConstantResolver
{
	private static final Set<Type> VALID_CONSTANT_TYPES = 
		Set.of(Type.INT_TYPE, Type.LONG_TYPE, Type.FLOAT_TYPE, Type.DOUBLE_TYPE, Types.STRING_TYPE, Types.TYPE_TYPE);
	
	@Override
	public Map.Entry<Type, Object> resolveConstant(String owner, String name)
	{
		try
		{
			ConstantFinder constantFinder = new ConstantFinder(name);
			new ClassReader(owner).accept(constantFinder, 0);
			if (constantFinder.constantData != null)
				return constantFinder.constantData;
			throw new RuntimeException("Unable to find " + owner + '.' + name);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not resolve " + owner + '.' + name, e);
		}
	}
	
	private static class ConstantFinder extends ClassVisitor
	{
		public Map.Entry<Type, Object> constantData;
		private final String targetName;
		private String className;
		
		public ConstantFinder(String targetName)
		{
			super(Opcodes.ASM7);
			this.targetName = targetName;
		}
		
		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
		{
			className = name;
		}

		@Override
		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value)
		{
			if (name.equals(targetName))
			{	
				if (!Modifier.isStatic(access))
					throw new RuntimeException(className + '.' + name + " is not static");
				if (!Modifier.isFinal(access))
					throw new RuntimeException(className + '.' + name + " is not final");
				// Apparently people STILL haven't learnt to override hashCode when they override equals
				Type fieldType = Type.getType(descriptor);
				if (!VALID_CONSTANT_TYPES.stream().anyMatch(t -> t.equals(fieldType)))
					throw new RuntimeException(className + '.' + name + " is not of a valid constant type");
				constantData = new SimpleImmutableEntry<>(Type.getType(descriptor), value);
			}
			return null;
		}
	}
}