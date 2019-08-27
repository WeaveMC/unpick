package daomephsta.unpick.api.constantmappers;

import daomephsta.unpick.impl.representations.ReplacementInstructionGenerator.Context;

/**
 * Defines a mapping of inlined values to replacement instructions.
 * @author Daomephsta
 */
public interface IConstantMapper
{
	/**
	 * @param methodOwner the internal name of the class that owns the method.
	 * @param methodName the name of the method.
	 * @param methodDescriptor the descriptor of the method.
	 * @return true if this mapper targets the method.
	 */
	public boolean targets(String methodOwner, String methodName, String methodDescriptor);
	
	/**
	 * @param methodOwner the internal name of the class that owns the method.
	 * @param methodName the name of the method.
	 * @param methodDescriptor the descriptor of the method.
	 * @param parameterIndex the index of the parameter being checked
	 * @return true if this mapper targets the parameter of the method with a 
	 * parameter index of {@code parameterIndex}.
	 */
	public boolean targetsParameter(String methodOwner, String methodName, String methodDescriptor, int parameterIndex);
	
	/**
	 * Maps an inlined value to replacement instructions, for a given target method. 
	 * @param methodOwner the internal name of the class that owns the target method.
	 * @param methodName the name of the target method.
	 * @param methodDescriptor the descriptor of the target method.
	 * @param parameterIndex the index of the parameter of the target method that {@code value} is passed as.
	 * @param context the inlined value.
	 */
	public void mapParameter(String methodOwner, String methodName, String methodDescriptor, int parameterIndex, Context context);

	public boolean targetsReturn(String methodOwner, String methodName, String methodDescriptor);

	public void mapReturn(String methodOwner, String methodName, String methodDescriptor, Context context);
}
