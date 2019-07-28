package daomephsta.unpick.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.ListIterator;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import daomephsta.unpick.AbstractInsnNodes;
import daomephsta.unpick.IntegerType;
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
	public void testKnownIntFlags()
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
	public void testUnknownIntFlags()
	{
		Integer[] testConstants = {0b0000, 0b100000, 0b01000, 0b11000};
		testUnknownConstants(testConstants, "intConsumer", "(I)V");
	}
	
	@Test
	public void testNegatedIntFlags()
	{
		Integer[] constants = {0b0001, 0b0010, 0b0100, 0b1000};
		String[] constantNames = {"INT_FLAG_BIT_0", "INT_FLAG_BIT_1", "INT_FLAG_BIT_2", "INT_FLAG_BIT_3"};
		testNegatedFlags(constants, constantNames, "intConsumer", "(I)V");
	}
	
	@Test
	public void testNegatedLongFlags()
	{
		Long[] constants = {0b0001L, 0b0010L, 0b0100L, 0b1000L};
		String[] constantNames = {"LONG_FLAG_BIT_0", "LONG_FLAG_BIT_1", "LONG_FLAG_BIT_2", "LONG_FLAG_BIT_3"};
		testNegatedFlags(constants, constantNames, "longConsumer", "(J)V");
	}
	
	private void testNegatedFlags(Number[] constants, String[] constantNames, String consumerName, String consumerDescriptor)
	{
		IConstantMapper mapper = MockConstantMapper.builder()
				.flagConstantGroup("test")
					.defineAll(Constants.class, constantNames )
				.add()
				.targetMethod(Methods.class, consumerName, consumerDescriptor)
					.remapParameter(0, "test")
				.add()
				.build();
		
		ConstantUninliner uninliner = new ConstantUninliner(mapper, new ClasspathConstantResolver());
		IntegerType integerType = IntegerType.from(constants.getClass().getComponentType());
		
		for (int i = 0; i < constants.length; i++)
		{
			Number constant = constants[i];
			MethodNode mockInvocation = InstructionMocker.mock(mockWriter -> 
			{
				mockWriter.visitFieldInsn(Opcodes.GETSTATIC, "Foo", "bar", integerType.getTypeDescriptor());
				integerType.appendLiteralPushInsn(mockWriter, ~constant.longValue());
				integerType.appendAndInsn(mockWriter);
				mockWriter.visitMethodInsn(INVOKESTATIC, Methods.class.getName().replace('.', '/'), consumerName, consumerDescriptor, false);
			});
			uninliner.transformMethod(InstructionMocker.CLASS_NAME, mockInvocation);
			ListIterator<AbstractInsnNode> instructions = mockInvocation.instructions.iterator(0);
			ASMAssertions.assertReadsField(instructions.next(), "Foo", "bar", integerType.getTypeDescriptor());
			ASMAssertions.assertReadsField(instructions.next(), Constants.class, constantNames[i], integerType.getTypeDescriptor());
			ASMAssertions.assertIsLiteral(instructions.next(), integerType.box(-1));
			ASMAssertions.assertOpcode(instructions.next(), integerType.getXorOpcode());
			ASMAssertions.assertOpcode(instructions.next(), integerType.getAndOpcode());
			ASMAssertions.assertInvokesMethod(instructions.next(), Methods.class, consumerName, consumerDescriptor);
		}
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
	
	@Test
	public void testUnknownLongFlags()
	{
		Long[] testConstants = {0b0000L, 0b100000L, 0b01000L, 0b11000L};
		testUnknownConstants(testConstants, "longConsumer", "(J)V");
	}

	private void testConstants(Object[] testConstants, String[][] expectedConstantCombinations, String[] constantNames, String constantConsumerName, String constantConsumerDescriptor)
	{
		IConstantMapper mapper = MockConstantMapper.builder()
				.flagConstantGroup("test")
					.defineAll(Constants.class, constantNames)
				.add()
				.targetMethod(Methods.class, constantConsumerName, constantConsumerDescriptor)
					.remapParameter(0, "test")
				.add()
				.build();
		
		IntegerType integerType = IntegerType.from(testConstants.getClass().getComponentType());
		ConstantUninliner uninliner = new ConstantUninliner(mapper, new ClasspathConstantResolver());
		
		for (int i = 0; i < testConstants.length; i++)
		{
			Object expectedLiteralValue = testConstants[i];
			String[] expectedConstantCombination = expectedConstantCombinations[i];
			MethodNode mockInvocation = InstructionMocker.mockInvokeStatic(Methods.class, 
					constantConsumerName, constantConsumerDescriptor, testConstants[i]);
			int invocationInsnIndex = 1;
			checkMockInvocationStructure(constantConsumerName, constantConsumerDescriptor, expectedLiteralValue, mockInvocation, invocationInsnIndex);
			uninliner.transformMethod(InstructionMocker.CLASS_NAME, mockInvocation);
			int minimumInsnCount = 2 * (expectedConstantCombination.length - 1) + 1;
			assertTrue(mockInvocation.instructions.size() >=  minimumInsnCount, 
					String.format("Expected at least %d instructions, found %d", minimumInsnCount, mockInvocation.instructions.size()));
			invocationInsnIndex += minimumInsnCount - 1;
			ASMAssertions.assertInvokesMethod(mockInvocation.instructions.get(invocationInsnIndex), Methods.class, 
					constantConsumerName, constantConsumerDescriptor);
			ASMAssertions.assertReadsField(mockInvocation.instructions.get(0), Constants.class, 
					expectedConstantCombination[0], integerType.getTypeDescriptor());
			for (int j = 1; j < expectedConstantCombination.length; j += 2)
			{
				ASMAssertions.assertReadsField(mockInvocation.instructions.get(j), Constants.class, 
						expectedConstantCombination[j], integerType.getTypeDescriptor());
				ASMAssertions.assertOpcode(mockInvocation.instructions.get(j + 1), integerType.getOrOpcode());
			}
		}
	}
	
	private void testUnknownConstants(Object[] constants, String constantConsumerName, String constantConsumerDescriptor)
	{
		IConstantMapper mapper = MockConstantMapper.builder()
				.flagConstantGroup("test")
				.add()
				.targetMethod(Methods.class, constantConsumerName, constantConsumerDescriptor)
					.remapParameter(0, "test")
				.add()
				.build();

		ConstantUninliner uninliner = new ConstantUninliner(mapper, new ClasspathConstantResolver());
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
