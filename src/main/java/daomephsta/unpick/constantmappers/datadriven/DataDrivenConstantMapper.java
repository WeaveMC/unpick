package daomephsta.unpick.constantmappers.datadriven;

import java.io.*;
import java.util.HashMap;

import daomephsta.unpick.constantmappers.IClassResolver;
import daomephsta.unpick.constantmappers.SimpleAbstractConstantMapper;
import daomephsta.unpick.constantmappers.datadriven.parser.UnpickSyntaxException;
import daomephsta.unpick.constantmappers.datadriven.parser.V1Parser;
import daomephsta.unpick.representations.TargetMethodIndex;

/**
 * Maps inlined values to constants, using a mapping defined in a file
 * @author Daomephsta
 */
public class DataDrivenConstantMapper extends SimpleAbstractConstantMapper
{	
	private final TargetMethodIndex targetMethodIndex;
	/**
	 * Constructs a new data driven constant mapper, using the mappings in {@code mappingSource}
	 * and resolving constants using {@code constantResolver}.
	 * @param mappingSources InputStreams of text in .unpick format
	 * @param classResolver 
	 */
	public DataDrivenConstantMapper(IClassResolver classResolver, InputStream... mappingSources)
	{
		super(new HashMap<>());
		TargetMethodIndex.Builder targetMethodIndexBuilder = new TargetMethodIndex.Builder(classResolver);
		for (InputStream mappingSource : mappingSources)
		{
			try(LineNumberReader reader = new LineNumberReader(new InputStreamReader(mappingSource)))
			{
				String line1 = reader.readLine();
				if ("v1".equals(line1))
					V1Parser.INSTANCE.parse(reader, constantGroups, targetMethodIndexBuilder);
				else
					throw new UnpickSyntaxException("Unknown version " + line1);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		this.targetMethodIndex = targetMethodIndexBuilder.build();
	}

	@Override
	protected TargetMethodIndex getTargetMethodIndex()
	{
		return targetMethodIndex;
	}
}
