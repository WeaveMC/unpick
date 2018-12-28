package daomephsta.unpick.representations;

import java.util.Map.Entry;

import org.objectweb.asm.Type;

import daomephsta.unpick.Types;
import daomephsta.unpick.constantmappers.datadriven.parser.UnpickSyntaxException;
import daomephsta.unpick.constantresolvers.IConstantResolver;

/**
 * Represents a constant field. The value and descriptor may be
 * lazily resolved at runtime.
 * @author Daomephsta
 */
public class ConstantDefinition
{
	private final String owner,
						 name;
	private Type descriptor;
	private Object value;
	
	/**
	 * Constructs an instance of ConstantDefinition that will
	 * have its value and descriptor lazily resolved.
	 * @param owner the internal name of the class that owns 
	 * the represented constant.
	 * @param name the name of the represented constant.
	 */
	public ConstantDefinition(String owner, String name)
	{
		this.owner = owner;
		this.name = name;
	}

	/**
	 * Constructs an instance of ConstantDefinition with the 
	 * specified value and descriptor.
	 * @param owner the internal name of the class that owns 
	 * the represented constant.
	 * @param name the name of the represented constant.
	 * @param descriptor the descriptor of the represented constant.
	 * @param the value of the the represented constant, as a String.
	 */
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
	
	/**@return the internal name of the class that owns the represented constant*/
	public String getOwner()
	{
		return owner;
	}
	
	/**@return the name of the represented constant*/
	public String getName()
	{
		return name;
	}
	
	/**@return the descriptor of the represented constant*/
	public Type getDescriptor()
	{
		return descriptor;
	}
	
	/**@return the descriptor of the represented constant, as a string*/
	public String getDescriptorString()
	{
		return descriptor.getDescriptor();
	}

	/**@return the value of the represented constant*/
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
