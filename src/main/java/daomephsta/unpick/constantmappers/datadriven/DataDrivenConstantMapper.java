package daomephsta.unpick.constantmappers.datadriven;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;

import daomephsta.unpick.constantmappers.IConstantMapper;
import daomephsta.unpick.constantmappers.datadriven.parser.UnpickSyntaxException;
import daomephsta.unpick.constantmappers.datadriven.parser.V1Parser;
import daomephsta.unpick.constantresolvers.IConstantResolver;
import daomephsta.unpick.representations.*;

public class DataDrivenConstantMapper implements IConstantMapper
{
	private final Map<String, ConstantGroup> constantGroups = new HashMap<>();
	private final Map<String, TargetMethod> targetMethods = new HashMap<>();
	private final IConstantResolver constantResolver;
	
	public DataDrivenConstantMapper(InputStream input, IConstantResolver constantResolver)
	{
		this.constantResolver = constantResolver;
		try(LineNumberReader reader = new LineNumberReader(new InputStreamReader(input)))
		{
			String line1 = reader.readLine();
			if ("v1".equals(line1))
				V1Parser.INSTANCE.parse(reader, constantGroups, targetMethods);
			else
				throw new UnpickSyntaxException("Unknown version " + line1);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean targets(String methodOwner, String methodName, String methodDescriptor)
	{
		return targetMethods.containsKey(getMethodKey(methodOwner, methodName, methodDescriptor));
	}
	
	@Override
	public boolean targets(String methodOwner, String methodName, String methodDescriptor, int parameterIndex)
	{
		return targetMethods.get(getMethodKey(methodOwner, methodName, methodDescriptor)).hasParameterConstantGroup(parameterIndex);
	}
	
	@Override
	public FieldInsnNode map(String methodOwner, String methodName, String methodDescriptor, int parameterIndex, Object value)
	{	
		String methodKey = getMethodKey(methodOwner, methodName, methodDescriptor);
		String constantGroupID = targetMethods.get(methodKey).getParameterConstantGroup(parameterIndex);
		ConstantGroup constantGroup = constantGroups.get(constantGroupID);
		if (constantGroup == null)
		{
			throw new UnpickSyntaxException(String.format("The constant group '%s' does not exist. Target Method: %s Parameter Index: %d",
				constantGroupID, methodKey, parameterIndex));
		}
		ConstantDefinition constantDefinition = constantGroup.get(constantResolver, value);
		if (constantDefinition == null)
			return null;
		return new FieldInsnNode(Opcodes.GETSTATIC, constantDefinition.getOwner(), constantDefinition.getName(), 
			constantDefinition.getDescriptorString());
	}

	private String getMethodKey(String methodOwner, String methodName, String methodDescriptor)
	{
		return methodOwner + '.' + methodName + methodDescriptor;
	}
}
