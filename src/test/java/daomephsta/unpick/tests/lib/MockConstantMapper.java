package daomephsta.unpick.tests.lib;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.objectweb.asm.Type;

import daomephsta.unpick.constantmappers.IClassResolver;
import daomephsta.unpick.constantmappers.SimpleAbstractConstantMapper;
import daomephsta.unpick.representations.*;

public class MockConstantMapper extends SimpleAbstractConstantMapper
{
	private final TargetMethodIndex targetMethodIndex;

	private MockConstantMapper(Map<String, ReplacementInstructionGenerator> constantGroups, TargetMethodIndex methodIndex)
	{
		super(constantGroups);
		this.targetMethodIndex = methodIndex;
	}

	public static Builder builder(IClassResolver classResolver)
	{
		return new Builder(classResolver);
	}

	@Override
	protected TargetMethodIndex getTargetMethodIndex()
	{
		return targetMethodIndex;
	}
	
	public static class Builder
	{
		private final Map<String, ReplacementInstructionGenerator> constantGroups = new HashMap<>();
		private final TargetMethodIndex.Builder methodIndexBuilder;
		
		public Builder(IClassResolver classResolver)
		{
			methodIndexBuilder = new TargetMethodIndex.Builder(classResolver);
		}

		public TargetMethodBuilder targetMethod(Class<?> owner, String name, String descriptor)
		{
			return new TargetMethodBuilder(this, owner.getName().replace('.', '/'), name, descriptor);
		}
		
		public ConstantGroupBuilder<SimpleConstantDefinition> simpleConstantGroup(String name)
		{
			return new ConstantGroupBuilder<>(this, name, SimpleConstantDefinition::new, SimpleConstantGroup::new);
		}
		
		public ConstantGroupBuilder<FlagDefinition> flagConstantGroup(String name)
		{
			return new ConstantGroupBuilder<>(this, name, FlagDefinition::new, FlagConstantGroup::new);
		}
		
		public MockConstantMapper build()
		{
			return new MockConstantMapper(constantGroups, methodIndexBuilder.build());
		}
	}
	
	public static abstract class ChildBuilder
	{
		protected final Builder parent;

		ChildBuilder(Builder parent)
		{
			this.parent = parent;
		}
	}
	
	public static class TargetMethodBuilder extends ChildBuilder
	{
		private final String owner, name, descriptor;
		private final Map<Integer, String> parameterConstantGroups;
		
		TargetMethodBuilder(Builder parent, String owner, String name, String descriptor)
		{
			super(parent);
			this.owner = owner;
			this.name = name;
			this.descriptor = descriptor;
			this.parameterConstantGroups = new HashMap<>(Type.getArgumentTypes(descriptor).length);
		}
		
		public TargetMethodBuilder remapParameter(int parameterIndex, String constantGroup)
		{
			if (parameterConstantGroups.putIfAbsent(parameterIndex, constantGroup) != null)
				throw new IllegalStateException("Parameter " + parameterIndex + " is already mapped to a constant group");
			return this;
		}
		
		public Builder add()
		{
			parent.methodIndexBuilder.putMethod(owner, name, Type.getMethodType(descriptor), parameterConstantGroups);
			return parent;
		}
	}
	
	public static class ConstantGroupBuilder<T extends AbstractConstantDefinition<T>> extends ChildBuilder
	{
		private final String name;
		private final Collection<T> constantDefinitions = new ArrayList<>();
		private final BiFunction<String, String, T> definitionFactory;
		private final Supplier<AbstractConstantGroup<T>> groupFactory;
		
		ConstantGroupBuilder(Builder parent, String name,
				BiFunction<String, String, T> definitionFactory,
				Supplier<AbstractConstantGroup<T>> groupFactory)
		{
			super(parent);
			this.name = name;
			this.definitionFactory = definitionFactory;
			this.groupFactory = groupFactory;
		}

		public ConstantGroupBuilder<T> define(Class<?> owner, String name)
		{
			constantDefinitions.add(definitionFactory.apply(owner.getName().replace('.', '/'), name));
			return this;
		}
		
		public ConstantGroupBuilder<T> defineAll(Class<?> owner, String... names)
		{
			for (String name : names)
			{
				define(owner, name);
			}
			return this;
		}
		
		public Builder add()
		{
			AbstractConstantGroup<T> group = groupFactory.get();
			for (T constant : constantDefinitions)
			{
				group.add(constant);
			}
			if (parent.constantGroups.putIfAbsent(name, group) != null)
				throw new IllegalStateException("A constant group named " + name + " already exists");
			return parent;
		}
	}
}
