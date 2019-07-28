package daomephsta.unpick.constantmappers;

import java.util.Map;

import daomephsta.unpick.Utils;
import daomephsta.unpick.constantmappers.datadriven.parser.UnpickSyntaxException;
import daomephsta.unpick.representations.*;
import daomephsta.unpick.representations.ReplacementInstructionGenerator.Context;

public abstract class SimpleAbstractConstantMapper implements IConstantMapper
{
	protected final Map<String, ReplacementInstructionGenerator> constantGroups;
	protected final Map<String, TargetMethod> targetMethods;
	
	protected SimpleAbstractConstantMapper(Map<String, ReplacementInstructionGenerator> constantGroups, Map<String, TargetMethod> targetMethods)
	{
		this.constantGroups = constantGroups;
		this.targetMethods = targetMethods;
	}

	@Override
	public boolean targets(String methodOwner, String methodName, String methodDescriptor)
	{
		return targetMethods.containsKey(Utils.getMethodKey(methodOwner, methodName, methodDescriptor));
	}
	
	@Override
	public boolean targets(String methodOwner, String methodName, String methodDescriptor, int parameterIndex)
	{
		return targetMethods.get(Utils.getMethodKey(methodOwner, methodName, methodDescriptor)).hasParameterConstantGroup(parameterIndex);
	}
	
	@Override
	public void map(String methodOwner, String methodName, String methodDescriptor, int parameterIndex, Context context)
	{	
		String methodKey = Utils.getMethodKey(methodOwner, methodName, methodDescriptor);
		String constantGroupID = targetMethods.get(methodKey).getParameterConstantGroup(parameterIndex);
		ReplacementInstructionGenerator constantGroup = constantGroups.get(constantGroupID);
		if (constantGroup == null)
		{
			throw new UnpickSyntaxException(String.format("The constant group '%s' does not exist. Target Method: %s Parameter Index: %d",
				constantGroupID, methodKey, parameterIndex));
		}
		if (!constantGroup.canReplace(context))
			return;
		
		constantGroup.generateReplacements(context);
	}
}
