package daomephsta.unpick.constantresolvers;

import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;

import org.objectweb.asm.*;

import daomephsta.unpick.LiteralType;
import daomephsta.unpick.constantmappers.IClassResolver;

public class BytecodeAnalysisConstantResolver implements IConstantResolver
{
	private static final Set<Type> VALID_CONSTANT_TYPES = Arrays.stream(LiteralType.values()).map(LiteralType::getType).collect(toSet());
	
	private final Map<String, ResolvedConstants> constantDataCache = new HashMap<>();
	private final IClassResolver classResolver;
	
	public BytecodeAnalysisConstantResolver(IClassResolver classResolver)
	{
		this.classResolver = classResolver;
	}

	@Override
	public ResolvedConstant resolveConstant(String owner, String name)
	{
		return constantDataCache.computeIfAbsent(owner, this::extractConstants).get(name);
	}
	
	private ResolvedConstants extractConstants(String owner)
	{
		try
		{
			ClassReader cr = classResolver.resolveClass(owner);
			if (cr == null)
				throw new NullPointerException("ClassReader cannot be null");
			ResolvedConstants resolvedConstants = new ResolvedConstants(Opcodes.ASM7);
			cr.accept(resolvedConstants, 0);
			return resolvedConstants;
		} 
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private static class ResolvedConstants extends ClassVisitor
	{
		public ResolvedConstants(int api)
		{
			super(api);
		}

		private final Map<String, ResolvedConstant> resolvedConstants = new HashMap<>();

		@Override
		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value)
		{
			if (Modifier.isStatic(access) && Modifier.isFinal(access))
			{
				Type fieldType = Type.getType(descriptor);
				if (VALID_CONSTANT_TYPES.stream().anyMatch(t -> t.equals(fieldType)))
					resolvedConstants.put(name, new ResolvedConstant(fieldType, value));
			}
			return super.visitField(access, name, descriptor, signature, value);
		}
		
		public ResolvedConstant get(Object key)
		{
			return resolvedConstants.get(key);
		}
	}
}
