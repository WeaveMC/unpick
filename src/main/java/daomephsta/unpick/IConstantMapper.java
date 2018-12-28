package daomephsta.unpick;

import org.objectweb.asm.tree.FieldInsnNode;

public interface IConstantMapper
{
	public FieldInsnNode map(String methodOwner, String methodName, String methodDescriptor, int parameterIndex, Object value);
	
	public boolean targets(String methodOwner, String methodName, String methodDescriptor);
	
	public boolean targets(String methodOwner, String methodName, String methodDescriptor, int parameterIndex);
}
