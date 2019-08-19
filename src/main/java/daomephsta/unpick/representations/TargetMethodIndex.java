package daomephsta.unpick.representations;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

import daomephsta.unpick.constantmappers.IClassResolver;

public class TargetMethodIndex
{
	private final Map<String, TargetMethod> methods;
	private final IClassResolver classResolver;

	private TargetMethodIndex(IClassResolver classResolver, Map<String, TargetMethod> methods)
	{
		this.classResolver = classResolver;
		this.methods = methods;
	}

	public boolean targets(String methodOwner, String methodName, String methodDescriptor)
	{
		TargetMethod targetMethod = methods.get(methodName + methodDescriptor);
		if (targetMethod == null)
			return false;
		return targetMethod.implementedBy(classResolver, methodOwner);	
	}

	public boolean targets(String methodOwner, String methodName, String methodDescriptor, int parameterIndex)
	{
		TargetMethod targetMethod = methods.get(methodName + methodDescriptor);
		return targetMethod.hasParameterConstantGroup(parameterIndex);
	}

	public String getParameterConstantGroup(String methodOwner, String methodName, String methodDescriptor, int parameterIndex)
	{
		TargetMethod targetMethod = methods.get(methodName + methodDescriptor);
		return targetMethod.getParameterConstantGroup(parameterIndex);
	}
	
	public static class Builder
	{
		private final IClassResolver classResolver;
		private final Map<String, TargetMethod> targetMethods = new HashMap<>();

		public Builder(IClassResolver classResolver)
		{
			this.classResolver = classResolver;
		}

		public void putMethod(String owner, String name, Type descriptor, Map<Integer, String> parameterConstantGroups)
		{
			targetMethods.put(name + descriptor, new TargetMethod(owner, name, descriptor, parameterConstantGroups));
		}

		public TargetMethodIndex build()
		{
			return new TargetMethodIndex(classResolver, targetMethods);
		}
	}
}
