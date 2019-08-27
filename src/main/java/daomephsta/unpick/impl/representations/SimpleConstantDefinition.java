package daomephsta.unpick.impl.representations;

import java.util.Map.Entry;

import org.objectweb.asm.Type;

import daomephsta.unpick.api.constantresolvers.IConstantResolver;

/**
 * Represents a constant field. The value and descriptor may be
 * lazily resolved at runtime.
 * @author Daomephsta
 */
public class SimpleConstantDefinition extends AbstractConstantDefinition<SimpleConstantDefinition>
{	
	/**
	 * Constructs an instance of ConstantDefinition that will
	 * have its value and descriptor lazily resolved.
	 * @param owner the internal name of the class that owns 
	 * the represented constant.
	 * @param name the name of the represented constant.
	 */
	public SimpleConstantDefinition(String owner, String name)
	{
		super(owner, name);
	}

	/**
	 * Constructs an instance of ConstantDefinition with the 
	 * specified value and descriptor.
	 * @param owner the internal name of the class that owns 
	 * the represented constant.
	 * @param name the name of the represented constant.
	 * @param descriptor the descriptor of the represented constant.
	 * @param valueString the value of the the represented constant, as a String.
	 */
	public SimpleConstantDefinition(String owner, String name, Type descriptor, String valueString)
	{
		super(owner, name, descriptor, valueString);
	}
	
	@Override
	SimpleConstantDefinition resolve(IConstantResolver constantResolver)
	{
		Entry<Type, Object> resolvedData = constantResolver.resolveConstant(owner, name);
		this.descriptor = resolvedData.getKey();
		this.value = resolvedData.getValue();
		return this;
	}
	
	@Override
	public String toString()
	{
		return String.format("SimpleConstantDefinition {Qualified Name: %s.%s, Descriptor: %s, Value: %s}", 
			owner, name, descriptor, value);
	}
}
