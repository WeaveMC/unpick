package daomephsta.unpick.constantmappers.datadriven.parser.v2;

import static java.util.stream.Collectors.toSet;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import daomephsta.unpick.constantmappers.datadriven.parser.UnpickSyntaxException;

public class UnpickV2Definitions implements Closeable
{
	private static final Pattern WHITESPACE_SPLITTER = Pattern.compile("\\s");
	private final InputStream definitionsStream;
	
	private TargetMethodDefinitionVisitor lastTargetMethodVisitor = null;

	public UnpickV2Definitions(InputStream definitionsStream)
	{
		this.definitionsStream = definitionsStream;
	}
	
	private static final Set<String> TARGET_METHOD_ARGS = Stream.of("param", "return").collect(toSet());
	public void accept(Visitor visitor)
	{
		try(LineNumberReader reader = new LineNumberReader(new InputStreamReader(definitionsStream)))
		{
			Iterator<String[]> lineTokensIter = reader.lines()
				.skip(1) //Skip version
				.map(s -> stripComment(s.trim()))
				.filter(s -> !s.isEmpty()) //Discard empty lines & lines that are empty once comments are stripped
				.map(l -> Arrays.stream(WHITESPACE_SPLITTER.split(l)).filter(s -> !s.isEmpty()).toArray(String[]::new)) //Tokenise lines
				.iterator();
			visitor.startVisit();
			while(lineTokensIter.hasNext())
			{
				String[] tokens = lineTokensIter.next();
				visitor.visitLineNumber(reader.getLineNumber());
				if (tokens[0].equals("target_method"))
				{
					visitTargetMethodDefinition(visitor, tokens, reader.getLineNumber());
				}
				else
				{
					if (lastTargetMethodVisitor != null && !TARGET_METHOD_ARGS.contains(tokens[0]))
					{
						lastTargetMethodVisitor.endVisit();
						lastTargetMethodVisitor = null;
					}
					switch (tokens[0])
					{
					case "constant":
						visitSimpleConstantDefinition(visitor, tokens, reader.getLineNumber());
						break;
					
					case "flag":
						visitFlagConstantDefinition(visitor, tokens, reader.getLineNumber());
						break;
						
					case "param":
						visitParameterConstantGroupDefinition(visitor, tokens, reader.getLineNumber());
						break;
						
					case "return":
						visitReturnConstantGroupDefinition(visitor, tokens, reader.getLineNumber());
						break;
						
					default:
						throw new UnpickSyntaxException("\nUnknown start token Tokens: " + Arrays.toString(tokens));
					}
				}
			}
			if (lastTargetMethodVisitor != null)
			{
				lastTargetMethodVisitor.endVisit();
				lastTargetMethodVisitor = null;
			}
			visitor.endVisit();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void visitParameterConstantGroupDefinition(Visitor visitor, String[] tokens, int lineNumber)
	{
		if (lastTargetMethodVisitor == null)
			throw new UnpickSyntaxException(lineNumber, "Invalid parameter constant group definition, not part of target method definition");
		if (tokens.length != 3)
			throw new UnpickSyntaxException(lineNumber, "Unexpected token count. Expected 3. Found " + tokens.length);
		try
		{
			lastTargetMethodVisitor.visitParameterGroupDefinition(Integer.parseInt(tokens[1]), tokens[2]);
		}
		catch (NumberFormatException e)
		{
			throw new UnpickSyntaxException(lineNumber, "Could not parse " + tokens[1] + " as an integer");
		}
	}
	
	private void visitReturnConstantGroupDefinition(Visitor visitor, String[] tokens, int lineNumber)
	{
		if (lastTargetMethodVisitor == null)
			throw new UnpickSyntaxException(lineNumber, "Invalid return constant group definition, not part of target method definition");
		if (tokens.length != 2)
			throw new UnpickSyntaxException(lineNumber, "Unexpected token count. Expected 2. Found " + tokens.length);
		lastTargetMethodVisitor.visitReturnGroupDefinition(tokens[1]);
	}

	private void visitSimpleConstantDefinition(Visitor visitor, String[] tokens, int lineNumber)
	{ 
		if (tokens.length != 4 && tokens.length != 6)
			throw new UnpickSyntaxException(lineNumber, "Unexpected token count. Expected 4 or 6. Found " + tokens.length);
		if (tokens.length > 4)
		{
			visitor.visitSimpleConstantDefinition(tokens[1], tokens[2], tokens[3], tokens[4], tokens[5]);
		}
		visitor.visitSimpleConstantDefinition(tokens[1], tokens[2], tokens[3], null, null);
	}
	
	private void visitFlagConstantDefinition(Visitor visitor, String[] tokens, int lineNumber)
	{ 
		if (tokens.length != 4 && tokens.length != 6)
			throw new UnpickSyntaxException(lineNumber, "Unexpected token count. Expected 4 or 6. Found " + tokens.length);
		if (tokens.length > 4)
		{
			visitor.visitFlagConstantDefinition(tokens[1], tokens[2], tokens[3], tokens[4], tokens[5]);
		}
		visitor.visitFlagConstantDefinition(tokens[1], tokens[2], tokens[3], null, null);
	}
	
	private void visitTargetMethodDefinition(Visitor visitor, String[] tokens, int lineNumber)
	{ 
		if (tokens.length != 4)
			throw new UnpickSyntaxException(lineNumber, "Unexpected token count. Expected 4. Found " + tokens.length);
		lastTargetMethodVisitor = visitor.visitTargetMethodDefinition(tokens[1], tokens[2], tokens[3]);
	}

	private String stripComment(String in)
	{
		int c = in.indexOf('#');
		return c == -1 ? in : in.substring(0, c);
	}

	@Override
	public void close() throws IOException
	{
		definitionsStream.close();		
	}
	
	private static final TargetMethodDefinitionVisitor DEFAULT = new TargetMethodDefinitionVisitor() {};
	public static interface Visitor
	{
		public default void startVisit() {}
		
		public default void visitLineNumber(int lineNumber) {};
		
		public default void visitSimpleConstantDefinition(String group, String owner, String name, String value, String descriptor) {}

		public default void visitFlagConstantDefinition(String group, String owner, String name, String value, String descriptor) {}
		
		public default TargetMethodDefinitionVisitor visitTargetMethodDefinition(String owner, String name, String descriptor) 
		{
			return DEFAULT;
		}
		
		public default void endVisit() {}
	}
	
	public static interface TargetMethodDefinitionVisitor
	{	
		public default void visitParameterGroupDefinition(int parameterIndex, String group) {}
		
		public default void visitReturnGroupDefinition(String group) {}
		
		public default void endVisit() {}
	}
}
