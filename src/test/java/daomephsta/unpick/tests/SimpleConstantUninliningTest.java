package daomephsta.unpick.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.objectweb.asm.Opcodes.RETURN;

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
	public void testKnownIntConstants()
	{
		Integer[] constants = 
			{Constants.INT_CONST_M1, Constants.INT_CONST_0, Constants.INT_CONST_1, Constants.INT_CONST_2, Constants.INT_CONST_3, Constants.INT_CONST_4, Constants.INT_CONST_5};
		String[] constantNames = 
			{"INT_CONST_M1", "INT_CONST_0", "INT_CONST_1", "INT_CONST_2", "INT_CONST_3", "INT_CONST_4", "INT_CONST_5"};
		
		testKnownConstants(constants, constantNames, "intConsumer", "(I)V");
	}
	
	@Test
	public void testUnknownIntConstants()
	{
		Integer[] constants = {8, 13, 42, -1, -7, -23};
		
		testUnknownConstants(constants, "intConsumer", "(I)V");
	}
	
	@Test
	public void testKnownLongConstants()
	{
		Long[] constants = 
			{Constants.LONG_CONST_0, Constants.LONG_CONST_1, Constants.LONG_CONST};
		String[] constantNames = 
			{"LONG_CONST_0", "LONG_CONST_1", "LONG_CONST"};
		
		testKnownConstants(constants, constantNames, "longConsumer", "(J)V");
	}
	
	@Test
	public void testUnknownLongConstants()
	{
		Long[] constants = {8L, 13L, 42L, -1L, -7L, -23L};
		
		testUnknownConstants(constants, "longConsumer", "(J)V");
	}
	
	@Test
	public void testKnownFloatConstants()
	{
		Float[] constants = 
			{Constants.FLOAT_CONST_0, Constants.FLOAT_CONST_1, Constants.FLOAT_CONST_2, Constants.FLOAT_CONST};
		String[] constantNames = 
			{"FLOAT_CONST_0", "FLOAT_CONST_1", "FLOAT_CONST_2", "FLOAT_CONST"};
		
		testKnownConstants(constants, constantNames, "floatConsumer", "(F)V");
	}
	
	@Test
	public void testUnknownFloatConstants()
	{
		Float[] constants = {0.15F, 1.973F, 24.5F, -0.64F, -2.3F, -21.0F};
		
		testUnknownConstants(constants, "floatConsumer", "(F)V");
	}
	
	@Test
	public void testKnownDoubleConstants()
	{
		Double[] constants = 
			{Constants.DOUBLE_CONST_0, Constants.DOUBLE_CONST_1, Constants.DOUBLE_CONST};
		String[] constantNames = 
			{"DOUBLE_CONST_0", "DOUBLE_CONST_1", "DOUBLE_CONST"};
		
		testKnownConstants(constants, constantNames, "doubleConsumer", "(D)V");
	}
	
	@Test
	public void testUnknownDoubleConstants()
	{
		Double[] constants = {0.15D, 1.973D, 24.5D, -0.64D, -2.3D, -21.0D};
		
		testUnknownConstants(constants, "doubleConsumer", "(D)V");
	}
	
	@Test
	public void testKnownStringConstants()
	{
		String[] constants = {Constants.STRING_CONST_FOO, Constants.STRING_CONST_BAR};
		String[] constantNames = {"STRING_CONST_FOO", "STRING_CONST_BAR"};
		
		testKnownConstants(constants, constantNames, "stringConsumer", "(Ljava/lang/String;)V");
	}
	
	@Test
	public void testUnknownStringConstants()
	{
		String[] constants = {"baz", "QUX", "1_QuZ_3", "PotatoesareGREAT"};
		
		testUnknownConstants(constants, "stringConsumer", "(Ljava/lang/String;)V");
	}

	private void testKnownConstants(Object[] constants, String[] constantNames, String constantConsumerName, String constantConsumerDescriptor)
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
			int invocationInsnIndex = 1;
			checkMockInvocationStructure(constantConsumerName, constantConsumerDescriptor, expectedLiteralValue, mockInvocation, 
					invocationInsnIndex);
			uninliner.transformMethod(InstructionMocker.CLASS_NAME, mockInvocation);
			ASMAssertions.assertReadsField(mockInvocation.instructions.get(invocationInsnIndex - 1), Constants.class, constantNames[i], 
					constantTypeDescriptor);
		}
	}
	
	private void testUnknownConstants(Object[] constants, String constantConsumerName, String constantConsumerDescriptor)
	{
		IConstantMapper mapper = MockConstantMapper.builder(new ClasspathConstantResolver())
				.simpleConstantGroup("test")
				.add()
				.targetMethod(Methods.class, constantConsumerName, constantConsumerDescriptor)
					.remapParameter(0, "test")
				.add()
				.build();

		ConstantUninliner uninliner = new ConstantUninliner(mapper);
		for (int i = 0; i < constants.length; i++)
		{
			Object expectedLiteralValue = constants[i];
			MethodNode mockInvocation = InstructionMocker.mockInvokeStatic(Methods.class, constantConsumerName, constantConsumerDescriptor, 
					expectedLiteralValue);
			int invocationInsnIndex = 1;
			checkMockInvocationStructure(constantConsumerName, constantConsumerDescriptor, expectedLiteralValue, mockInvocation, 
					invocationInsnIndex);
			uninliner.transformMethod(InstructionMocker.CLASS_NAME, mockInvocation);
			//Should be unchanged, so this should still pass
			checkMockInvocationStructure(constantConsumerName, constantConsumerDescriptor, expectedLiteralValue, mockInvocation, 
					invocationInsnIndex);
		}
	}

	private void checkMockInvocationStructure(String constantConsumerName,
			String constantConsumerDescriptor, Object expectedLiteralValue,
			MethodNode mockInvocation, int invocationInsnIndex)
	{
		int expectedInstructionCount = 3;
		assertEquals(expectedInstructionCount, mockInvocation.instructions.size(), 
				String.format("Expected %d instructions, found %d", expectedInstructionCount, mockInvocation.instructions.size()));
		assertEquals(expectedLiteralValue, AbstractInsnNodes.getLiteralValue(mockInvocation.instructions.get(invocationInsnIndex - 1)));
		ASMAssertions.assertInvokesMethod(mockInvocation.instructions.get(invocationInsnIndex), Methods.class, 
				constantConsumerName, constantConsumerDescriptor);
		ASMAssertions.assertOpcode(mockInvocation.instructions.get(invocationInsnIndex + 1), RETURN);
	}
}
