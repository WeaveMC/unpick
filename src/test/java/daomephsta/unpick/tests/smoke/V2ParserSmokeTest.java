package daomephsta.unpick.tests.smoke;

import java.io.IOException;
import java.io.InputStream;

import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Definitions;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Definitions.TargetMethodDefinitionVisitor;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Definitions.Visitor;

public class V2ParserSmokeTest
{
	private static class PrintingVisitor implements Visitor
	{	
		@Override
		public void visitFlagConstantDefinition(String group, String owner, String name, String value, String descriptor)
		{
			System.out.printf("%s.%s:%s=%s in %s\n", owner, name.replace('/', '.'), descriptor, value, group);
		}
		
		@Override
		public void visitSimpleConstantDefinition(String group, String owner, String name, String value, String descriptor)
		{
			System.out.printf("%s.%s:%s=%s in %s\n", owner, name.replace('/', '.'), descriptor, value, group);
		}
		
		@Override
		public TargetMethodDefinitionVisitor visitTargetMethodDefinition(String owner, String name, String descriptor)
		{
			System.out.printf("%s.%s%s\n", owner, name.replace('/', '.'), descriptor);
			return new TargetMethodDefinitionVisitor()
			{
				@Override
				public void visitParameterGroupDefinition(int parameterIndex, String group)
				{
					System.out.printf("%s.%s%s#%d=%s\n", owner, name.replace('/', '.'), descriptor, parameterIndex, group);
				}
				
				@Override
				public void visitReturnGroupDefinition(String group)
				{
					System.out.printf("%s.%s%s#return=%s\n", owner, name.replace('/', '.'), descriptor, group);
				}
			};
		}
	}

	public static void main(String[] args)
	{
		InputStream testResource = V2ParserSmokeTest.class.getClassLoader().getResourceAsStream("test.unpick");
		try(UnpickV2Definitions unpickDefinitions = new UnpickV2Definitions(testResource))
		{
			unpickDefinitions.accept(new PrintingVisitor());
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public class Test
	{
		public int intFunction(int i) {return 3;}
	}
}
