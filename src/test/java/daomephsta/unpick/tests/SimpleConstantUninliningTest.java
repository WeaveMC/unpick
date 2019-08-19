package daomephsta.unpick.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.objectweb.asm.ClassReader;
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
	
	@ParameterizedTest(name = "testKnownLong {1}={0}")
	@MethodSource("knownIntsProvider")
	public void testKnownIntConstants(Integer constant, String constantName)
	{	
		testKnownConstant(constant, constantName, "intConsumer", "(I)V");
	}
	
	private static Stream<Arguments> knownIntsProvider()
	{
		return Stream.of
		(
			Arguments.of(Constants.INT_CONST_M1, "INT_CONST_M1"),
			Arguments.of(Constants.INT_CONST_0, "INT_CONST_0"),
			Arguments.of(Constants.INT_CONST_1, "INT_CONST_1"),
			Arguments.of(Constants.INT_CONST_2, "INT_CONST_2"),
			Arguments.of(Constants.INT_CONST_3, "INT_CONST_3"),
			Arguments.of(Constants.INT_CONST_4, "INT_CONST_4"),
			Arguments.of(Constants.INT_CONST_5, "INT_CONST_5")
		);
	}
	
	@ParameterizedTest(name = "testUnknownInt {0}")
	@ValueSource(ints = {8, 13, 42, -1, -7, -23})
	public void testUnknownIntConstants(Integer constant)
	{
		testUnknownConstant(constant, "intConsumer", "(I)V");
	}
	
	@ParameterizedTest(name = "testKnownLong {1}={0}L")
	@MethodSource("knownLongsProvider")
	public void testKnownLongConstants(Long constant, String constantName)
	{
		testKnownConstant(constant, constantName, "longConsumer", "(J)V");
	}
	
	private static Stream<Arguments> knownLongsProvider()
	{
		return Stream.of
		(
			Arguments.of(Constants.LONG_CONST_0, "LONG_CONST_0"), 
			Arguments.of(Constants.LONG_CONST_1, "LONG_CONST_1"),
			Arguments.of(Constants.LONG_CONST, "LONG_CONST")
		);
	}
	
	@ParameterizedTest(name = "testUnknownLong {0}L")
	@ValueSource(longs = {8L, 13L, 42L, -1L, -7L, -23L})
	public void testUnknownLongConstants(Long constant)
	{	
		testUnknownConstant(constant, "longConsumer", "(J)V");
	}
	
	@ParameterizedTest(name = "testKnownFloat {1}={0}F")
	@MethodSource("knownFloatsProvider")
	public void testKnownFloatConstants(Float constant, String constantName)
	{	
		testKnownConstant(constant, constantName, "floatConsumer", "(F)V");
	}
	
	private static Stream<Arguments> knownFloatsProvider()
	{
		return Stream.of
		(
			Arguments.of(Constants.FLOAT_CONST_0, "FLOAT_CONST_0"), 
			Arguments.of(Constants.FLOAT_CONST_1, "FLOAT_CONST_1"),
			Arguments.of(Constants.FLOAT_CONST_2, "FLOAT_CONST_2"),
			Arguments.of(Constants.FLOAT_CONST, "FLOAT_CONST")
		);
	}
	
	@ParameterizedTest(name = "testUnknownFloat {0}F")
	@ValueSource(floats = {0.15F, 1.973F, 24.5F, -0.64F, -2.3F, -21.0F})
	public void testUnknownFloatConstants(Float constant)
	{	
		testUnknownConstant(constant, "floatConsumer", "(F)V");
	}
	
	@ParameterizedTest(name = "testKnownDouble {1}={0}D")
	@MethodSource("knownDoublesProvider")
	public void testKnownDoubleConstants(Double constant, String constantName)
	{
		testKnownConstant(constant, constantName, "doubleConsumer", "(D)V");
	}
	
	private static Stream<Arguments> knownDoublesProvider()
	{
		return Stream.of
		(
			Arguments.of(Constants.DOUBLE_CONST_0, "DOUBLE_CONST_0"), 
			Arguments.of(Constants.DOUBLE_CONST_1, "DOUBLE_CONST_1"), 
			Arguments.of(Constants.DOUBLE_CONST, "DOUBLE_CONST")
		);
	}
	
	@ParameterizedTest(name = "testUnknownDouble {0}D")
	@ValueSource(doubles = {0.15D, 1.973D, 24.5D, -0.64D, -2.3D, -21.0D})
	public void testUnknownDoubleConstants(Double constant)
	{
		testUnknownConstant(constant, "doubleConsumer", "(D)V");
	}
	
	@ParameterizedTest(name = "testKnownString {1}=\"{0}\"")
	@MethodSource("knownStringsProvider")
	public void testKnownStringConstants(String constant, String constantName)
	{
		testKnownConstant(constant, constantName, "stringConsumer", "(Ljava/lang/String;)V");
	}
	
	private static Stream<Arguments> knownStringsProvider()
	{
		return Stream.of
		(
			Arguments.of(Constants.STRING_CONST_FOO, "STRING_CONST_FOO"),
			Arguments.of(Constants.STRING_CONST_BAR, "STRING_CONST_BAR")
		);
	}
	
	@ParameterizedTest(name = "testUnknownString \"{0}\"")
	@ValueSource(strings = {"baz", "QUX", "1_QuZ_3", "PotatoesareGREAT"})
	public void testUnknownStringConstants(String constant)
	{
		testUnknownConstant(constant, "stringConsumer", "(Ljava/lang/String;)V");
	}

	private void testKnownConstant(Object constant, String expectedConstant, String constantConsumerName, String constantConsumerDescriptor)
	{
		IConstantMapper mapper = MockConstantMapper.builder(ClassReader::new)
				.simpleConstantGroup("test")
					.define(Constants.class, expectedConstant)
				.add()
				.targetMethod(Methods.class, constantConsumerName, constantConsumerDescriptor)
					.remapParameter(0, "test")
				.add()
				.build();

		String constantTypeDescriptor = Type.getDescriptor(TestUtils.unboxedType(constant.getClass()));
		ConstantUninliner uninliner = new ConstantUninliner(mapper, new ClasspathConstantResolver());
		MethodNode mockInvocation = TestUtils.mockInvokeStatic(Methods.class, constantConsumerName, constantConsumerDescriptor, 
				constant).getMockMethod();
		int invocationInsnIndex = 1;
		checkMockInvocationStructure(constantConsumerName, constantConsumerDescriptor, constant, mockInvocation, 
				invocationInsnIndex);
		uninliner.transformMethod(MethodMocker.CLASS_NAME, mockInvocation);
		ASMAssertions.assertReadsField(mockInvocation.instructions.get(invocationInsnIndex - 1), Constants.class, expectedConstant, 
				constantTypeDescriptor);
	}
	
	private void testUnknownConstant(Object constant, String constantConsumerName, String constantConsumerDescriptor)
	{
		IConstantMapper mapper = MockConstantMapper.builder(ClassReader::new)
				.simpleConstantGroup("test")
				.add()
				.targetMethod(Methods.class, constantConsumerName, constantConsumerDescriptor)
					.remapParameter(0, "test")
				.add()
				.build();

		ConstantUninliner uninliner = new ConstantUninliner(mapper, new ClasspathConstantResolver());
		MethodNode mockInvocation = TestUtils.mockInvokeStatic(Methods.class, constantConsumerName, constantConsumerDescriptor, 
				constant).getMockMethod();
		int invocationInsnIndex = 1;
		checkMockInvocationStructure(constantConsumerName, constantConsumerDescriptor, constant, mockInvocation, 
				invocationInsnIndex);
		uninliner.transformMethod(MethodMocker.CLASS_NAME, mockInvocation);
		//Should be unchanged, so this should still pass
		checkMockInvocationStructure(constantConsumerName, constantConsumerDescriptor, constant, mockInvocation, 
				invocationInsnIndex);
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
