package daomephsta.unpick.datadriven;

import java.util.Map.Entry;

import org.objectweb.asm.Type;

import daomephsta.unpick.Types;
import daomephsta.unpick.datadriven.format.UnpickSyntaxException;

public class ConstantDefinition
{
	private final String owner,
						 name;
	private Type descriptor;
	private Object value;
	
	public ConstantDefinition(String owner, String name)
	{
		this.owner = owner;
		this.name = name;
	}

	public ConstantDefinition(String owner, String name, Type descriptor, String valueString)
	{
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
		this.value = parseValue(valueString);
	}
	
	private Object parseValue(String valueString)
	{
		try 
		{ 
			if (descriptor == Type.INT_TYPE)
				return Integer.parseInt(valueString);
			else if (descriptor == Type.LONG_TYPE)
				return Long.parseLong(valueString); 
			else if (descriptor == Type.FLOAT_TYPE)
				return Float.parseFloat(valueString);
			else if (descriptor == Type.DOUBLE_TYPE)
				return Double.parseDouble(valueString);
			else if (descriptor.equals(Types.STRING_TYPE))
				return valueString;
			else if (descriptor.equals(Types.TYPE_TYPE))
				return Type.getType(valueString);
			else throw new UnpickSyntaxException("Cannot parse value " + valueString + " with descriptor " + descriptor);
		}
		catch (IllegalArgumentException e) 
		{
			throw new UnpickSyntaxException("Cannot parse value " + valueString + " with descriptor " + descriptor, e); 
		}
	}
	
	boolean isResolved()
	{
		return value != null;
	}
	
	ConstantDefinition resolve(IConstantResolver constantResolver)
	{
		Entry<Type, Object> resolvedData = constantResolver.resolveConstant(owner, name);
		this.descriptor = resolvedData.getKey();
		this.value = resolvedData.getValue();
		return this;
	}
	
	public String getOwner()
	{
		return owner;
	}
	
	public String getName()
	{
		return name;
	}
	
	public Type getDescriptor()
	{
		return descriptor;
	}
	
	public String getDescriptorString()
	{
		return descriptor.getDescriptor();
	}

	public Object getValue()
	{
		return value;
	}
	
	@Override
	public String toString()
	{
		return String.format("ConstantDefinition {Qualified Name: %s.%s, Descriptor: %s, Value: %s}", 
			owner, name, descriptor, value);
	}
}
