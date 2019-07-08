package daomephsta.unpick.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.Opcodes.IOR;
import static org.objectweb.asm.Opcodes.LOR;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import daomephsta.unpick.AbstractInsnNodes;
import daomephsta.unpick.constantmappers.IConstantMapper;
import daomephsta.unpick.constantresolvers.ClasspathConstantResolver;
import daomephsta.unpick.tests.lib.*;
import daomephsta.unpick.transformers.ConstantUninliner;

public class FlagUninliningTest
{
	@SuppressWarnings("unused")
	private static class Constants
	{	
		public static final int INT_FLAG_BIT_0 = 1 << 0,
								INT_FLAG_BIT_1 = 1 << 1,
								INT_FLAG_BIT_2 = 1 << 2,
								INT_FLAG_BIT_3 = 1 << 3;
		
		public static final long LONG_FLAG_BIT_0 = 1 << 0,
								 LONG_FLAG_BIT_1 = 1 << 1,
								 LONG_FLAG_BIT_2 = 1 << 2,
								 LONG_FLAG_BIT_3 = 1 << 3;
	}
	
	@SuppressWarnings("unused")
	private static class Methods
	{
		private static void intConsumer(int test) {}

		private static void longConsumer(long test) {}
	}
	
	@Test
	public void testIntFlags()
	{
		Integer[] testConstants = {0b1100, 0b0100, 0b1010, 0b0111};
		String[][] expectedConstantCombinations = 
			{
				new String[] {"INT_FLAG_BIT_2", "INT_FLAG_BIT_3"},
				new String[] {"INT_FLAG_BIT_2"},
				new String[] {"INT_FLAG_BIT_1", "INT_FLAG_BIT_3"},
				new String[] {"INT_FLAG_BIT_0", "INT_FLAG_BIT_1", "INT_FLAG_BIT_2"},
			};
		String[] constantNames = {"INT_FLAG_BIT_0", "INT_FLAG_BIT_1", "INT_FLAG_BIT_2", "INT_FLAG_BIT_3"};
		testConstants(testConstants, expectedConstantCombinations, constantNames, "intConsumer", "(I)V");
	}

	@Test
	public void testLongFlags()
	{
		Long[] testConstants = {0b1100L, 0b0100L, 0b1010L, 0b0111L};
		String[][] expectedConstantCombinations = 
			{
				new String[] {"LONG_FLAG_BIT_2", "LONG_FLAG_BIT_3"},
				new String[] {"LONG_FLAG_BIT_2"},
				new String[] {"LONG_FLAG_BIT_1", "LONG_FLAG_BIT_3"},
				new String[] {"LONG_FLAG_BIT_0", "LONG_FLAG_BIT_1", "LONG_FLAG_BIT_2"},
			};
		String[] constantNames = {"LONG_FLAG_BIT_0", "LONG_FLAG_BIT_1", "LONG_FLAG_BIT_2", "LONG_FLAG_BIT_3"};
		testConstants(testConstants, expectedConstantCombinations, constantNames, "longConsumer", "(J)V");
	}

	private void testConstants(Object[] testConstants, String[][] expectedConstantCombinations, String[] constantNames, String constantConsumerName, String constantConsumerDescriptor)
	{
		IConstantMapper mapper = MockConstantMapper.builder(new ClasspathConstantResolver())
				.flagConstantGroup("test")
					.defineAll(Constants.class, constantNames)
				.add()
				.targetMethod(Methods.class, constantConsumerName, constantConsumerDescriptor)
					.remapParameter(0, "test")
				.add()
				.build();
		
		Class<?> unboxedType = TestUtils.unboxedType(testConstants.getClass().getComponentType());
		String constantTypeDescriptor = Type.getDescriptor(unboxedType);
		int orOpcode;
		if (constantTypeDescriptor.equals(Type.LONG_TYPE.getDescriptor()))
			orOpcode = LOR;
		else if(constantTypeDescriptor.equals(Type.INT_TYPE.getDescriptor()))
			orOpcode = IOR;
		else
			throw new IllegalArgumentException("Expected long or int test constants but got " + unboxedType.getName() + " test constants");
		ConstantUninliner uninliner = new ConstantUninliner(mapper);
		
		for (int i = 0; i < testConstants.length; i++)
		{
			Object expectedLiteralValue = testConstants[i];
			String[] expectedConstantCombination = expectedConstantCombinations[i];
			MethodNode mockInvocation = InstructionMocker.mockInvokeStatic(Methods.class, 
					constantConsumerName, constantConsumerDescriptor, testConstants[i]);
			int invocationInsnIndex = 1;
			ASMAssertions.assertInvokesMethod(mockInvocation.instructions.get(invocationInsnIndex), Methods.class, constantConsumerName, 
					constantConsumerDescriptor);
			assertEquals(expectedLiteralValue, AbstractInsnNodes.getLiteralValue(mockInvocation.instructions.get(invocationInsnIndex - 1)));
			uninliner.transformMethod(InstructionMocker.CLASS_NAME, mockInvocation);
			int minimumInsnCount = 2 * (expectedConstantCombination.length - 1) + 1;
			assertTrue(mockInvocation.instructions.size() >=  minimumInsnCount, 
					String.format("Expected at least %d instructions, found %d", minimumInsnCount, mockInvocation.instructions.size()));
			invocationInsnIndex += minimumInsnCount - 1;
			ASMAssertions.assertInvokesMethod(mockInvocation.instructions.get(invocationInsnIndex), Methods.class, 
					constantConsumerName, constantConsumerDescriptor);
			ASMAssertions.assertReadsField(mockInvocation.instructions.get(0), Constants.class, 
					expectedConstantCombination[0], constantTypeDescriptor);
			for (int j = 1; j < expectedConstantCombination.length; j += 2)
			{
				ASMAssertions.assertReadsField(mockInvocation.instructions.get(j), Constants.class, 
						expectedConstantCombination[j], constantTypeDescriptor);
				ASMAssertions.assertOpcode(mockInvocation.instructions.get(j + 1), orOpcode);
			}
		}
	}
}
