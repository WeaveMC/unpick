package daomephsta.unpick.api.constantmappers;

import java.io.InputStream;

import daomephsta.unpick.api.IClassResolver;
import daomephsta.unpick.impl.constantmappers.datadriven.DataDrivenConstantMapper;

public class ConstantMappers
{
	public static IConstantMapper dataDriven(IClassResolver classResolver, InputStream... mappingSources)
	{
		return new DataDrivenConstantMapper(classResolver, mappingSources);
	}
}
