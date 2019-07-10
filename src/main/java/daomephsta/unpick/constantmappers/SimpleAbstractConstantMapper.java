package daomephsta.unpick.constantmappers;

import java.util.Map;

import org.objectweb.asm.tree.InsnList;

import daomephsta.unpick.MethodDescriptors;
import daomephsta.unpick.constantmappers.datadriven.parser.UnpickSyntaxException;
import daomephsta.unpick.constantresolvers.IConstantResolver;
import daomephsta.unpick.representations.ReplacementInstructionGenerator;
import daomephsta.unpick.representations.TargetMethod;

public abstract class SimpleAbstractConstantMapper implements IConstantMapper
{
	protected final Map<String, ReplacementInstructionGenerator> constantGroups;
	protected final Map<String, TargetMethod> targetMethods;
	private final IConstantResolver constantResolver;
	
	protected SimpleAbstractConstantMapper(Map<String, ReplacementInstructionGenerator> constantGroups, Map<String, TargetMethod> targetMethods, 
			IConstantResolver constantResolver)
	{
		this.constantGroups = constantGroups;
		this.targetMethods = targetMethods;
		this.constantResolver = constantResolver;
	}

	@Override
	public boolean targets(String methodOwner, String methodName, String methodDescriptor)
	{
		return targetMethods.containsKey(MethodDescriptors.getMethodKey(methodOwner, methodName, methodDescriptor));
	}
	
	@Override
	public boolean targets(String methodOwner, String methodName, String methodDescriptor, int parameterIndex)
	{
		return targetMethods.get(MethodDescriptors.getMethodKey(methodOwner, methodName, methodDescriptor)).hasParameterConstantGroup(parameterIndex);
	}
	
	@Override
	public InsnList map(String methodOwner, String methodName, String methodDescriptor, int parameterIndex, Object value)
	{	
		String methodKey = MethodDescriptors.getMethodKey(methodOwner, methodName, methodDescriptor);
		String constantGroupID = targetMethods.get(methodKey).getParameterConstantGroup(parameterIndex);
		ReplacementInstructionGenerator constantGroup = constantGroups.get(constantGroupID);
		if (constantGroup == null)
		{
			throw new UnpickSyntaxException(String.format("The constant group '%s' does not exist. Target Method: %s Parameter Index: %d",
				constantGroupID, methodKey, parameterIndex));
		}
		ReplacementInstructionGenerator.Context context = new ReplacementInstructionGenerator.Context(constantResolver, value);
		if (!constantGroup.canReplace(context))
			return null;
		
		return constantGroup.createReplacementInstructions(context);
	}
}
