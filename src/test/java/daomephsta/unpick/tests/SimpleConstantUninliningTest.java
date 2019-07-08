package daomephsta.unpick.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import daomephsta.unpick.AbstractInsnNodes;
import daomephsta.unpick.constantmappers.IConstantMapper;
import daomephsta.unpick.constantresolvers.ClasspathConstantResolver;
import daomephsta.unpick.tests.lib.*;
import daomephsta.unpick.transformers.ConstantUninliner;

public class SimpleConstantUninliningTest
{
	@SuppressWarnings("unused")
	private static class Methods
	{
		private static void intConsumer(int test) {}

		private static void longConsumer(long test) {}

		private static void floatConsumer(float test) {}

		private static void doubleConsumer(double test) {}

		private static void stringConsumer(String test) {}
	}
	
	@SuppressWarnings("unused")
	private static class Constants
	{
		public static final int INT_CONST_M1 = -1,
								INT_CONST_0 = 0,
								INT_CONST_1 = 1,
								INT_CONST_2 = 2,
								INT_CONST_3 = 3,
								INT_CONST_4 = 4,
								INT_CONST_5 = 5,
								INT_CONST = 257;

		public static final long LONG_CONST_0 = 0,
								 LONG_CONST_1 = 1,
								 LONG_CONST = 1234567890;
		
		public static final float FLOAT_CONST_0 = 0F,
								  FLOAT_CONST_1 = 1F,
								  FLOAT_CONST_2 = 2F,
								  FLOAT_CONST = 5.3F;
		
		public static final double DOUBLE_CONST_0 = 0D,
								   DOUBLE_CONST_1 = 1D,
								   DOUBLE_CONST = 5.3D;
		
		public static final String STRING_CONST_FOO = "foo",
						   		   STRING_CONST_BAR = "bar";
	}
	
	@Test
	public void testIntConstants()
	{
		Integer[] constants = 
			{Constants.INT_CONST_M1, Constants.INT_CONST_0, Constants.INT_CONST_1, Constants.INT_CONST_2, Constants.INT_CONST_3, Constants.INT_CONST_4, Constants.INT_CONST_5};
		String[] constantNames = 
			{"INT_CONST_M1", "INT_CONST_0", "INT_CONST_1", "INT_CONST_2", "INT_CONST_3", "INT_CONST_4", "INT_CONST_5"};
		
		testConstants(constants, constantNames, "intConsumer", "(I)V");
	}
	
	@Test
	public void testLongConstants()
	{
		Long[] constants = 
			{Constants.LONG_CONST_0, Constants.LONG_CONST_1, Constants.LONG_CONST};
		String[] constantNames = 
			{"LONG_CONST_0", "LONG_CONST_1", "LONG_CONST"};
		
		testConstants(constants, constantNames, "longConsumer", "(J)V");
	}
	
	@Test
	public void testFloatConstants()
	{
		Float[] constants = 
			{Constants.FLOAT_CONST_0, Constants.FLOAT_CONST_1, Constants.FLOAT_CONST_2, Constants.FLOAT_CONST};
		String[] constantNames = 
			{"FLOAT_CONST_0", "FLOAT_CONST_1", "FLOAT_CONST_2", "FLOAT_CONST"};
		
		testConstants(constants, constantNames, "floatConsumer", "(F)V");
	}
	
	@Test
	public void testDoubleConstants()
	{
		Double[] constants = 
			{Constants.DOUBLE_CONST_0, Constants.DOUBLE_CONST_1, Constants.DOUBLE_CONST};
		String[] constantNames = 
			{"DOUBLE_CONST_0", "DOUBLE_CONST_1", "DOUBLE_CONST"};
		
		testConstants(constants, constantNames, "doubleConsumer", "(D)V");
	}
	
	@Test
	public void testStringConstants()
	{
		String[] constants = {Constants.STRING_CONST_FOO, Constants.STRING_CONST_BAR};
		String[] constantNames = {"STRING_CONST_FOO", "STRING_CONST_BAR"};
		
		testConstants(constants, constantNames, "stringConsumer", "(Ljava/lang/String;)V");
	}

	private void testConstants(Object[] constants, String[] constantNames, String constantConsumerName, String constantConsumerDescriptor)
	{
		IConstantMapper mapper = MockConstantMapper.builder(new ClasspathConstantResolver())
				.simpleConstantGroup("test")
					.defineAll(Constants.class, constantNames)
				.add()
				.targetMethod(Methods.class, constantConsumerName, constantConsumerDescriptor)
					.remapParameter(0, "test")
				.add()
				.build();

		String constantTypeDescriptor = Type.getDescriptor(TestUtils.unboxedType(constants.getClass().getComponentType()));
		ConstantUninliner uninliner = new ConstantUninliner(mapper);
		for (int i = 0; i < constants.length; i++)
		{
			Object expectedLiteralValue = constants[i];
			MethodNode mockInvocation = InstructionMocker.mockInvokeStatic(Methods.class, constantConsumerName, constantConsumerDescriptor, 
					expectedLiteralValue);
			int invocationInsnIndex = 2;
			ASMAssertions.assertInvokesMethod(mockInvocation.instructions.get(invocationInsnIndex), Methods.class, constantConsumerName, 
					constantConsumerDescriptor);
			assertEquals(expectedLiteralValue, AbstractInsnNodes.getLiteralValue(mockInvocation.instructions.get(invocationInsnIndex - 1)));
			uninliner.transformMethod(InstructionMocker.CLASS_NAME, mockInvocation);
			ASMAssertions.assertReadsField(mockInvocation.instructions.get(invocationInsnIndex - 1), Constants.class, constantNames[i], 
					constantTypeDescriptor);
		}
	}
}
