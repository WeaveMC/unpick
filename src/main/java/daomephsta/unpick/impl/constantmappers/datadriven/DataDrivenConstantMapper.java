package daomephsta.unpick.impl.constantmappers.datadriven;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import daomephsta.unpick.api.IClassResolver;
import daomephsta.unpick.constantmappers.datadriven.parser.UnpickSyntaxException;
import daomephsta.unpick.impl.constantmappers.SimpleAbstractConstantMapper;
import daomephsta.unpick.impl.constantmappers.datadriven.parser.V1Parser;
import daomephsta.unpick.impl.constantmappers.datadriven.parser.v2.V2Parser;
import daomephsta.unpick.impl.representations.TargetMethods;

/**
 * Maps inlined values to constants, using a mapping defined in a file
 * @author Daomephsta
 */
public class DataDrivenConstantMapper extends SimpleAbstractConstantMapper
{	
	private final TargetMethods targetMethods;
	/**
	 * Constructs a new data driven constant mapper, using the mappings in {@code mappingSource}
	 * and resolving constants using {@code constantResolver}.
	 * @param mappingSources InputStreams of text in .unpick format
	 * @param classResolver a class resolver that can resolve the classes of the target methods
	 */
	public DataDrivenConstantMapper(IClassResolver classResolver, InputStream... mappingSources)
	{
		super(new HashMap<>());
		TargetMethods.Builder targetMethodsBuilder = TargetMethods.builder(classResolver);
		for (InputStream mappingSource : mappingSources)
		{
			try
			{
				byte[] version = new byte [2];
				mappingSource.read(version);
				if (version[0] == 'v')
				{
					switch (version[1])
					{
					case '1':
						V1Parser.INSTANCE.parse(mappingSource, constantGroups, targetMethodsBuilder);
						break;
						
					case '2':
						V2Parser.parse(mappingSource, constantGroups, targetMethodsBuilder);
						break;

					default :
						throw new UnpickSyntaxException(1, "Unknown version " + (char) version[1]);
					}
				}
				else
					throw new UnpickSyntaxException(1, "Missing version");
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		this.targetMethods = targetMethodsBuilder.build();
		System.out.println(targetMethods);
	}

	@Override
	protected TargetMethods getTargetMethods()
	{
		return targetMethods;
	}
}
