package daomephsta.unpick.impl.representations;

import java.util.Map.Entry;

import org.objectweb.asm.Type;

import daomephsta.unpick.api.constantresolvers.IConstantResolver;
import daomephsta.unpick.constantmappers.datadriven.parser.UnpickSyntaxException;

/**
 * Represents a flag field. The value and descriptor may be
 * lazily resolved at runtime.
 * @author Daomephsta
 */
public class FlagDefinition extends AbstractConstantDefinition<FlagDefinition>
{	
	/**
	 * Constructs an instance of FlagDefinition that will
	 * have its value and descriptor lazily resolved.
	 * @param owner the internal name of the class that owns 
	 * the represented flag.
	 * @param name the name of the represented flag.
	 */
	public FlagDefinition(String owner, String name)
	{
		super(owner, name);
	}

	/**
	 * Constructs an instance of FlagDefinition with the 
	 * specified value and descriptor.
	 * @param owner the internal name of the class that owns 
	 * the represented flag.
	 * @param name the name of the represented flag.
	 * @param descriptor the descriptor of the represented flag.
	 * @param valueString the value of the the represented flag, as a String.
	 */
	public FlagDefinition(String owner, String name, Type descriptor, String valueString)
	{
		super(owner, name, descriptor, valueString);
	}
	
	@Override
	protected Number parseValue(String valueString)
	{
		try 
		{ 
			if (descriptor == Type.INT_TYPE)
				return Integer.parseInt(valueString);
			else if (descriptor == Type.LONG_TYPE)
				return Long.parseLong(valueString); 
			else throw new UnpickSyntaxException("Cannot parse value " + valueString + " with descriptor " + descriptor);
		}
		catch (IllegalArgumentException e) 
		{
			throw new UnpickSyntaxException("Cannot parse value " + valueString + " with descriptor " + descriptor, e); 
		}
	}
	
	@Override
	FlagDefinition resolve(IConstantResolver constantResolver)
	{
		Entry<Type, Object> resolvedData = constantResolver.resolveConstant(owner, name);
		this.descriptor = resolvedData.getKey();
		Object value = resolvedData.getValue();
		if (value instanceof Long || value instanceof Integer)
			this.value = value;
		else 
			throw new UnpickSyntaxException(owner + '.' + name + " is not of a valid flag type. Flags must be ints or longs.");
		return this;
	}
	
	@Override
	public Number getValue()
	{
		return (Number) super.getValue();
	}
	
	@Override
	public String toString()
	{
		return String.format("FlagDefinition {Qualified Name: %s.%s, Descriptor: %s, Bits: %s}", 
			owner, name, descriptor, Long.toBinaryString(getValue().longValue()));
	}
}
