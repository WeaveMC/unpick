package daomephsta.unpick.impl.representations;

import org.objectweb.asm.Type;

import daomephsta.unpick.api.constantresolvers.IConstantResolver;
import daomephsta.unpick.constantmappers.datadriven.parser.UnpickSyntaxException;
import daomephsta.unpick.impl.LiteralType;

/**
 * Represents an abstract constant field. The value and descriptor may be
 * lazily resolved at runtime.
 * @author Daomephsta
 */
public abstract class AbstractConstantDefinition<C extends AbstractConstantDefinition<C>>
{
	protected final String owner,
						 name;
	protected Type descriptor;
	protected Object value;
	
	/**
	 * Constructs an instance of AbstractConstantDefinition that will
	 * have its value and descriptor lazily resolved.
	 * @param owner the internal name of the class that owns 
	 * the represented constant.
	 * @param name the name of the represented constant.
	 */
	public AbstractConstantDefinition(String owner, String name)
	{
		this.owner = owner;
		this.name = name;
	}

	/**
	 * Constructs an instance of AbstractConstantDefinition with the 
	 * specified value and descriptor.
	 * @param owner the internal name of the class that owns 
	 * the represented constant.
	 * @param name the name of the represented constant.
	 * @param descriptor the descriptor of the represented constant.
	 * @param valueString the value of the the represented constant, as a String.
	 */
	public AbstractConstantDefinition(String owner, String name, Type descriptor, String valueString)
	{
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
		this.value = parseValue(valueString);
	}
	
	
	protected Object parseValue(String valueString)
	{
		try 
		{ 
			return LiteralType.from(descriptor).parse(valueString);
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
	
	abstract C resolve(IConstantResolver constantResolver);
	
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
}
