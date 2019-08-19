package daomephsta.unpick.constantmappers.datadriven.parser;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;
import java.util.regex.Pattern;

import org.objectweb.asm.Type;

import daomephsta.unpick.representations.*;
import daomephsta.unpick.representations.TargetMethodIndex.Builder;

public enum V1Parser
{
	INSTANCE;
	
	private static final Pattern WHITESPACE_SPLITTER = Pattern.compile("\\s");
	
	public void parse(LineNumberReader reader, Map<String, ReplacementInstructionGenerator> constantGroups, TargetMethodIndex.Builder targetClassesBuilder) throws IOException
	{
		String line = "";
		while((line = reader.readLine()) != null)
		{
			line = stripComment(line).trim();
			if (line.isEmpty()) continue;

			String[] tokens = tokenize(line);
			if (tokens.length == 0) continue;
			
			switch (tokens[0])
			{
			case "constant":
			{
				if (tokens.length != 4 && tokens.length != 6)
					throw new UnpickSyntaxException(reader.getLineNumber(), "Unexpected token count. Expected 4 or 6. Found " + tokens.length);
				
				String group = tokens[1];
				SimpleConstantDefinition parsedConstant = parseConstantDefinition(tokens, reader.getLineNumber());
				ReplacementInstructionGenerator constantGroup = constantGroups.get(group);
				if (constantGroup == null)
				{
					constantGroups.put(group, (constantGroup = new SimpleConstantGroup()));
				}
				if (constantGroup instanceof SimpleConstantGroup)
					((SimpleConstantGroup) constantGroup).add(parsedConstant);
				else 
					throw new UnpickSyntaxException(reader.getLineNumber(), "Cannot add simple constant to non-simple constant group of type " + constantGroup.getClass().getSimpleName());
				break;
			}
			
			case "flag":
			{
				if (tokens.length != 4 && tokens.length != 6)
					throw new UnpickSyntaxException(reader.getLineNumber(), "Unexpected token count. Expected 4 or 6. Found " + tokens.length);
				
				String group = tokens[1];
				FlagDefinition parsedFlag = parseFlagDefinition(tokens, reader.getLineNumber());
				ReplacementInstructionGenerator constantGroup = constantGroups.get(group);
				if (constantGroup == null)
				{
					constantGroups.put(group, (constantGroup = new FlagConstantGroup()));
				}
				if (constantGroup instanceof FlagConstantGroup)
					((FlagConstantGroup) constantGroup).add(parsedFlag);
				else 
					throw new UnpickSyntaxException(reader.getLineNumber(), "Cannot add flag to non-flag group of type " + constantGroup.getClass().getSimpleName());
				break;
			}
				
			case "unpick":
				parseTargetMethodDefinition(targetClassesBuilder, tokens, reader.getLineNumber());
				break;
				
			default:
				throw new UnpickSyntaxException(reader.getLineNumber(), "Unknown start token " + tokens[0]);
			}
		}
	}
	
	private String stripComment(String in)
	{
		int c = in.indexOf('#');
		return c == -1 ? in : in.substring(0, c);
	}

	private String[] tokenize(String in)
	{
		List<String> result = new ArrayList<>();

		for (String s : WHITESPACE_SPLITTER.split(in))
		{
			if (!s.isEmpty()) result.add(s);
		}

		return result.toArray(new String[0]);
	}

	private SimpleConstantDefinition parseConstantDefinition(String[] tokens, int lineNumber)
	{ 
		String owner = tokens[2];
		String name = tokens[3];
		
		if (tokens.length > 4)
		{
			try 
			{
				Type descriptor = Type.getType(tokens[5]); 
				String value = tokens[4];
				return new SimpleConstantDefinition(owner, name, descriptor, value);
			}
			catch (IllegalArgumentException e)
			{
				throw new UnpickSyntaxException(lineNumber, "Unable to parse descriptor " + tokens[4]);
			}
		}
		
		return new SimpleConstantDefinition(owner, name);
	}
	
	private FlagDefinition parseFlagDefinition(String[] tokens, int lineNumber)
	{ 
		String owner = tokens[2];
		String name = tokens[3];
		
		if (tokens.length > 4)
		{
			try 
			{
				Type descriptor = Type.getType(tokens[5]); 
				String value = tokens[4];
				return new FlagDefinition(owner, name, descriptor, value);
			}
			catch (IllegalArgumentException e)
			{
				throw new UnpickSyntaxException(lineNumber, "Unable to parse descriptor " + tokens[4]);
			}
		}
		
		return new FlagDefinition(owner, name);
	}

	private void parseTargetMethodDefinition(Builder targetClassesBuilder, String[] tokens, int lineNumber)
	{
		if (tokens.length < 4 || tokens.length % 2 != 0)
			throw new UnpickSyntaxException(lineNumber, "Unexpected token count. Expected an even number greater than or equal to 4. Found " + tokens.length);
		
		String owner = tokens[1];
		String name = tokens[2];
		
		try 
		{
			Type methodType = Type.getMethodType(tokens[3]);
			Map<Integer, String> parameterConstantGroups = new HashMap<>();
			for (int p = 5; p < tokens.length; p += 2)
			{
				try
				{
					int parameterIndex = Integer.parseInt(tokens[p - 1]);
					if (parameterConstantGroups.get(parameterIndex) != null)
						throw new UnpickSyntaxException(lineNumber, "Duplicate parameter index " + parameterIndex);
					else
						parameterConstantGroups.put(parameterIndex, tokens[p]);
				}
				catch(NumberFormatException e)
				{
					throw new UnpickSyntaxException(lineNumber, "Could not parse " + tokens[p - 1] + " as integer", e);
				}
			}
			targetClassesBuilder.putMethod(owner, name, methodType, parameterConstantGroups);
		}
		catch (IllegalArgumentException e)
		{
			throw new UnpickSyntaxException(lineNumber, "Unable to parse method descriptor " + tokens[3]);
		}
	}
}
