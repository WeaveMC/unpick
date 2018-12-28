package daomephsta.unpick.datadriven.format;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;

import org.objectweb.asm.Type;

import daomephsta.unpick.datadriven.*;

public enum V1Parser
{
	INSTANCE;

	public void parse(LineNumberReader reader, Map<String, ConstantGroup> constantGroups, Map<String, TargetMethod> targetMethods) throws IOException
	{
		String line;
		while ((line = reader.readLine()) != null)
		{
			line = stripComment(line).trim();
			if (line.isEmpty()) continue;

			String[] tokens = tokenize(line);
			if (tokens.length == 0) continue;

			switch (tokens[0])
			{
				case "constant":
					if (tokens.length != 4 && tokens.length != 6)
						throw new UnpickSyntaxException(reader.getLineNumber(), "Unexpected token count. Expected 4 or 6. Found " + tokens.length);

					String group = tokens[1];
					ConstantDefinition parsedConstant = parseConstantDefinition(tokens, reader.getLineNumber());
					ConstantGroup constantGroup = constantGroups.computeIfAbsent(group, k -> new ConstantGroup());
					constantGroup.add(parsedConstant);
					break;
				case "unpick":
					TargetMethod parsedTargetMethod = parseTargetMethodDefinition(tokens, reader.getLineNumber());
					targetMethods.put(parsedTargetMethod.getOwner() + '.' + parsedTargetMethod.getName() + parsedTargetMethod.getMethodDescriptor().getDescriptor(), parsedTargetMethod);
					break;
				default:
					throw new UnpickSyntaxException(reader.getLineNumber(), "Unknown start token " + tokens[0]);
			}
		}
	}

	private String stripComment(String in)
	{
		int c = in.indexOf(';');
		return c == -1 ? in : in.substring(0, c);
	}

	private String[] tokenize(String in)
	{
		List<String> result = new ArrayList<>();

		for (String s : in.split("\\s"))
		{
			if (!s.isEmpty()) result.add(s);
		}

		return result.toArray(new String[0]);
	}

	private ConstantDefinition parseConstantDefinition(String[] tokens, int lineNumber)
	{
		String owner = tokens[2];
		String name = tokens[3];

		if (tokens.length > 4)
		{
			try
			{
				Type descriptor = Type.getType(tokens[5]);
				String value = tokens[4];
				return new ConstantDefinition(owner, name, descriptor, value);
			}
			catch (IllegalArgumentException e)
			{
				throw new UnpickSyntaxException(lineNumber, "Unable to parse descriptor " + tokens[4]);
			}
		}

		return new ConstantDefinition(owner, name);
	}

	private TargetMethod parseTargetMethodDefinition(String[] tokens, int lineNumber)
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
				catch (NumberFormatException e)
				{
					throw new UnpickSyntaxException(lineNumber, "Could not parse " + tokens[p - 1] + " as integer", e);
				}
			}
			return new TargetMethod(owner, name, methodType, parameterConstantGroups);
		}
		catch (IllegalArgumentException e)
		{
			throw new UnpickSyntaxException(lineNumber, "Unable to parse method descriptor " + tokens[3]);
		}
	}
}
