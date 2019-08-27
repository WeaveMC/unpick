package daomephsta.unpick.api.constantresolvers;

import daomephsta.unpick.api.IClassResolver;
import daomephsta.unpick.impl.constantresolvers.BytecodeAnalysisConstantResolver;

public class ConstantResolvers
{
	public static IConstantResolver bytecodeAnalysis(IClassResolver classResolver)
	{
		return new BytecodeAnalysisConstantResolver(classResolver);
	}
}
