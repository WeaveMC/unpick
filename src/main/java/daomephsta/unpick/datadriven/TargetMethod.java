package daomephsta.unpick.datadriven;

import java.util.*;

import org.objectweb.asm.Type;

public class TargetMethod
{
	private final String owner,
						 name;
	private final Type descriptor;
	private final Map<Integer, String> parameterConstantGroups;
	
	public TargetMethod(String owner, String name, Type methodType, Map<Integer, String> parameterConstantGroups)
	{
		this.owner = owner;
		this.name = name;
		this.descriptor = methodType;
		this.parameterConstantGroups = parameterConstantGroups;
	}
	
	public String getParameterConstantGroup(int parameterIndex)
	{
		return parameterConstantGroups.get(parameterIndex);
	}
	
	public boolean hasParameterConstantGroup(int parameterIndex)
	{
		return parameterConstantGroups.containsKey(parameterIndex);
	}
	
	public String getOwner()
	{
		return owner;
	}
	
	public String getName()
	{
		return name;
	}
	
	public Type getMethodDescriptor()
	{
		return descriptor;
	}

	@Override
	public String toString()
	{
		return String.format("TargetMethod {Qualified Name: %s.%s, Descriptor: %s, Parameter Constant Groups: %s}", 
			owner, name, descriptor, parameterConstantGroups);
	}
}
