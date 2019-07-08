package daomephsta.unpick.tests.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;

public class ASMAssertions
{
	public static void assertInvokesMethod(AbstractInsnNode insn, Class<?> owner, String name, String descriptor)
	{
		assertInvokesMethod(insn, getInternalName(owner), name, descriptor);
	}
	
	public static void assertInvokesMethod(AbstractInsnNode insn, String owner, String name, String descriptor)
	{
		assertTrue(insn.getOpcode() >= INVOKEVIRTUAL && insn.getOpcode() <= INVOKEINTERFACE, 
				"Instruction " + Printer.OPCODES[insn.getOpcode()] + " does not invoke a method");
		MethodInsnNode invocationInsn = (MethodInsnNode) insn;
		assertEquals(owner, invocationInsn.owner);
		assertEquals(name, invocationInsn.name);
		assertEquals(descriptor, invocationInsn.desc);
	}
	
	public static void assertReadsField(AbstractInsnNode insn, Class<?> owner, String name, String descriptor)
	{
		assertReadsField(insn, getInternalName(owner), name, descriptor);
	}
	
	public static void assertReadsField(AbstractInsnNode insn, String owner, String name, String descriptor)
	{
		assertTrue(insn.getOpcode() == GETFIELD | insn.getOpcode() == GETSTATIC, 
				"Instruction "  + Printer.OPCODES[insn.getOpcode()] +  " does not read a field");
		FieldInsnNode fieldInsn = (FieldInsnNode) insn;
		assertEquals(owner, fieldInsn.owner);
		assertEquals(name, fieldInsn.name);
		assertEquals(descriptor, fieldInsn.desc);
	}
	
	public static void assertOpcode(AbstractInsnNode node, int expectedOpcode)
	{
		assertEquals(expectedOpcode, node.getOpcode(), 
				String.format("expected: <%s> but was: <%s>", Printer.OPCODES[expectedOpcode], Printer.OPCODES[node.getOpcode()]));
	}
	
	private static String getInternalName(Class<?> clazz)
	{
		return clazz.getName().replace('.', '/');
	}
}
