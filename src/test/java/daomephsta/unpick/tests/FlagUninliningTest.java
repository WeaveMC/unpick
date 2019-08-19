package daomephsta.unpick.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.ListIterator;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import daomephsta.unpick.AbstractInsnNodes;
import daomephsta.unpick.IntegerType;
import daomephsta.unpick.constantmappers.IConstantMapper;
import daomephsta.unpick.constantresolvers.ClasspathConstantResolver;
import daomephsta.unpick.tests.lib.*;
import daomephsta.unpick.tests.lib.MethodMocker.MockMethod;
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
	
	@ParameterizedTest(name = "testKnownFlag {0}")
	@MethodSource("intFlagsProvider")
	public void testKnownIntFlags(Integer testConstant, String[] expectedConstantCombination, String[] constantNames)
	{
		testKnownFlag(testConstant, expectedConstantCombination, constantNames, "intConsumer", "(I)V");
	}
	
	@ParameterizedTest(name = "testKnownFlag {0}L")
	@MethodSource("longFlagsProvider")
	public void testKnownLongFlags(Long testConstant, String[] expectedConstantCombination, String[] constantNames)
	{
		testKnownFlag(testConstant, expectedConstantCombination, constantNames, "longConsumer", "(J)V");
	}

	private void testKnownFlag(Object testConstant, String[] expectedConstantCombination, String[] constantNames, String constantConsumerName, String constantConsumerDescriptor)
	{
		IConstantMapper mapper = MockConstantMapper.builder(ClassReader::new)
				.flagConstantGroup("test")
					.defineAll(Constants.class, constantNames)
				.add()
				.targetMethod(Methods.class, constantConsumerName, constantConsumerDescriptor)
					.remapParameter(0, "test")
				.add()
				.build();
		
		IntegerType integerType = IntegerType.from(testConstant.getClass());
		ConstantUninliner uninliner = new ConstantUninliner(mapper, new ClasspathConstantResolver());
		
		MockMethod mockMethod = TestUtils.mockInvokeStatic(Methods.class, 
				constantConsumerName, constantConsumerDescriptor, testConstant);
		MethodNode mockInvocation = mockMethod.getMockMethod();
		int invocationInsnIndex = 1;
		checkMockInvocationStructure(constantConsumerName, constantConsumerDescriptor, testConstant, mockInvocation, invocationInsnIndex);
		uninliner.transformMethod(MethodMocker.CLASS_NAME, mockInvocation);
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
	
	@ParameterizedTest(name = "testNegatedFlag ~{0}")
	@MethodSource("intFlagsProvider")
	public void testNegatedIntFlags(Integer testConstant, String[] expectedConstantCombination, String[] constantNames)
	{
		testNegatedFlag(testConstant, expectedConstantCombination, constantNames, "intConsumer", "(I)V");
	}
	
	@ParameterizedTest(name = "testNegatedFlag ~{0}L")
	@MethodSource("longFlagsProvider")
	public void testNegatedLongFlags(Long testConstant, String[] expectedConstantCombination, String[] constantNames)
	{
		testNegatedFlag(testConstant, expectedConstantCombination, constantNames, "longConsumer", "(J)V");
	}
	
	private void testNegatedFlag(Number testConstant, String[] expectedConstantCombination, String[] constantNames, String consumerName, String consumerDescriptor)
	{
		IConstantMapper mapper = MockConstantMapper.builder(ClassReader::new)
				.flagConstantGroup("test")
				.defineAll(Constants.class, constantNames)
				.add()
				.targetMethod(Methods.class, consumerName, consumerDescriptor)
				.remapParameter(0, "test")
				.add()
				.build();

		ConstantUninliner uninliner = new ConstantUninliner(mapper, new ClasspathConstantResolver());
		IntegerType integerType = IntegerType.from(testConstant.getClass());

		MockMethod mockMethod = MethodMocker.mock(mockWriter -> 
		{
			mockWriter.visitFieldInsn(Opcodes.GETSTATIC, "Foo", "bar", integerType.getTypeDescriptor());
			integerType.appendLiteralPushInsn(mockWriter, ~testConstant.longValue());
			integerType.appendAndInsn(mockWriter);
			mockWriter.visitMethodInsn(INVOKESTATIC, Methods.class.getName().replace('.', '/'), consumerName, consumerDescriptor, false);
		});
		MethodNode mockInvocation = mockMethod.getMockMethod();
		uninliner.transformMethod(MethodMocker.CLASS_NAME, mockInvocation);
		ListIterator<AbstractInsnNode> instructions = mockInvocation.instructions.iterator(0);
		ASMAssertions.assertReadsField(instructions.next(), "Foo", "bar", integerType.getTypeDescriptor());

		ASMAssertions.assertReadsField(instructions.next(), Constants.class, 
				expectedConstantCombination[0], integerType.getTypeDescriptor());
		for (int j = 1; j < expectedConstantCombination.length; j += 2)
		{
			ASMAssertions.assertReadsField(instructions.next(), Constants.class, 
					expectedConstantCombination[j], integerType.getTypeDescriptor());
			ASMAssertions.assertOpcode(instructions.next(), integerType.getOrOpcode());
		}
	}
	
	@ParameterizedTest(name = "testUnknownFlag {0}")
	@ValueSource(ints = {0b0000, 0b100000, 0b01000, 0b11000})
	public void testUnknownIntFlags(Integer testConstant)
	{
		testUnknownConstant(testConstant, "intConsumer", "(I)V");
	}
	
	@ParameterizedTest(name = "testUnknownFlag {0}L")
	@ValueSource(longs = {0b0000L, 0b100000L, 0b01000L, 0b11000L})
	public void testUnknownLongFlags(Long testConstant)
	{
		testUnknownConstant(testConstant, "longConsumer", "(J)V");
	}
	
	private void testUnknownConstant(Number constant, String constantConsumerName, String constantConsumerDescriptor)
	{
		IConstantMapper mapper = MockConstantMapper.builder(ClassReader::new)
				.flagConstantGroup("test")
				.add()
				.targetMethod(Methods.class, constantConsumerName, constantConsumerDescriptor)
					.remapParameter(0, "test")
				.add()
				.build();

		ConstantUninliner uninliner = new ConstantUninliner(mapper, new ClasspathConstantResolver());

		MethodNode mockInvocation = TestUtils.mockInvokeStatic(Methods.class, 
				constantConsumerName, constantConsumerDescriptor, constant).getMockMethod();
		int invocationInsnIndex = 1;
		checkMockInvocationStructure(constantConsumerName, constantConsumerDescriptor, constant, mockInvocation, 
				invocationInsnIndex);
		uninliner.transformMethod(MethodMocker.CLASS_NAME, mockInvocation);
		//Should be unchanged, so this should still pass
		checkMockInvocationStructure(constantConsumerName, constantConsumerDescriptor, constant, mockInvocation, 
				invocationInsnIndex);
	}
	
	private static Stream<Arguments> intFlagsProvider()
	{
		String[] constantNames = {"INT_FLAG_BIT_0", "INT_FLAG_BIT_1", "INT_FLAG_BIT_2", "INT_FLAG_BIT_3"};
		return Stream.of
		(
			Arguments.of(0b0100, new String[] {"INT_FLAG_BIT_2"}, constantNames),
			Arguments.of(0b1100, new String[] {"INT_FLAG_BIT_2", "INT_FLAG_BIT_3"}, constantNames),
			Arguments.of(0b1010, new String[] {"INT_FLAG_BIT_1", "INT_FLAG_BIT_3"}, constantNames),
			Arguments.of(0b0111, new String[] {"INT_FLAG_BIT_0", "INT_FLAG_BIT_1", "INT_FLAG_BIT_2"}, constantNames)
		);	
	}
	
	private static Stream<Arguments> longFlagsProvider()
	{
		String[] constantNames = {"LONG_FLAG_BIT_0", "LONG_FLAG_BIT_1", "LONG_FLAG_BIT_2", "LONG_FLAG_BIT_3"};
		return Stream.of
		(
			Arguments.of(0b0100L, new String[] {"LONG_FLAG_BIT_2"}, constantNames),
			Arguments.of(0b1100L, new String[] {"LONG_FLAG_BIT_2", "LONG_FLAG_BIT_3"}, constantNames),
			Arguments.of(0b1010L, new String[] {"LONG_FLAG_BIT_1", "LONG_FLAG_BIT_3"}, constantNames),
			Arguments.of(0b0111L, new String[] {"LONG_FLAG_BIT_0", "LONG_FLAG_BIT_1", "LONG_FLAG_BIT_2"}, constantNames)
		);	
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
