package daomephsta.unpick.constantmappers;

import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Defines a mapping of inlined values to replacement instructions.
 * @author Daomephsta
 */
public interface IConstantMapper
{
	/**
	 * Maps an inlined value to a replacement instruction, for a given target method. 
	 * @param methodOwner the internal name of the class that owns the target method.
	 * @param methodName the name of the target method.
	 * @param methodDescriptor the descriptor of the target method.
	 * @param parameterIndex the index of the parameter of the target method that {@code value} is passed as.
	 * @param value the inlined value.
	 * @return An instruction to replace the inlined value with.
	 */
	public FieldInsnNode map(String methodOwner, String methodName, String methodDescriptor, int parameterIndex, Object value);
	
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
	 * @return true if this mapper targets the parameter of the method with a 
	 * parameter index of {@code parameterIndex}.
	 */
	public boolean targets(String methodOwner, String methodName, String methodDescriptor, int parameterIndex);
}
