package daomephsta.unpick.representations;

import java.util.*;

import org.objectweb.asm.Type;

/**
 * Represents a method with parameters that may be inlined constants
 * @author Daomephsta
 */
public class TargetMethod
{
	private final String owner,
						 name;
	private final Type descriptor;
	private final Map<Integer, String> parameterConstantGroups;
	
	/**
	 * Constructs a new instance of TargetMethod with the specified parameters.
	 * @param owner the internal name of the class that owns the represented method.
	 * @param name the name of the represented method
	 * @param descriptor the descriptor of the represented method.
	 * @param parameterConstantGroups a Map that maps a parameter index to the name
	 * of the constant group that contains all valid constants for that parameter. 
	 */
	public TargetMethod(String owner, String name, Type descriptor, Map<Integer, String> parameterConstantGroups)
	{
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
		this.parameterConstantGroups = parameterConstantGroups;
	}
	
	/**
	 * @param parameterIndex the index of the parameter.
	 * @return the name of the constant group that contains all valid constants 
	 * for the parameter with an index of {@code parameterIndex}
	 */
	public String getParameterConstantGroup(int parameterIndex)
	{
		return parameterConstantGroups.get(parameterIndex);
	}
	
	/**
	 * @param parameterIndex the index of the parameter.
	 * @return true if a constant group mapping exists for the parameter 
	 * with an index of {@code parameterIndex}
	 */ 
	public boolean hasParameterConstantGroup(int parameterIndex)
	{
		return parameterConstantGroups.containsKey(parameterIndex);
	}
	
	/**@return the internal name of the class that owns the represented method*/
	public String getOwner()
	{
		return owner;
	}
	
	/**@return the name of the represented method*/
	public String getName()
	{
		return name;
	}
	
	/**@return the descriptor of the represented method*/
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
