package daomephsta.unpick.constantmappers.datadriven;

import java.io.*;
import java.util.HashMap;

import daomephsta.unpick.constantmappers.SimpleAbstractConstantMapper;
import daomephsta.unpick.constantmappers.datadriven.parser.UnpickSyntaxException;
import daomephsta.unpick.constantmappers.datadriven.parser.V1Parser;

/**
 * Maps inlined values to constants, using a mapping defined in a file
 * @author Daomephsta
 */
public class DataDrivenConstantMapper extends SimpleAbstractConstantMapper
{	
	/**
	 * Constructs a new data driven constant mapper, using the mappings in {@code mappingSource}
	 * and resolving constants using {@code constantResolver}.
	 * @param mappingSource an input stream of text in .unpick format
	 */
	public DataDrivenConstantMapper(InputStream mappingSource)
	{
		super(new HashMap<>(), new HashMap<>());
		try(LineNumberReader reader = new LineNumberReader(new InputStreamReader(mappingSource)))
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
}
