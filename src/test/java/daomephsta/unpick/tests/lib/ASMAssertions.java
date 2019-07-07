package daomephsta.unpick.tests.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.tree.*;

public class ASMAssertions
{
	public static void assertInvokesMethod(AbstractInsnNode insn, Class<?> owner, String name, String descriptor)
	{
		assertInvokesMethod(insn, getInternalName(owner), name, descriptor);
	}
	
	public static void assertInvokesMethod(AbstractInsnNode insn, String owner, String name, String descriptor)
	{
		assertTrue(insn.getOpcode() >= INVOKEVIRTUAL && insn.getOpcode() <= INVOKEINTERFACE, "Instruction does not invoke a method");
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
		assertTrue(insn.getOpcode() == GETFIELD | insn.getOpcode() == GETSTATIC, "Instruction does not read a field");
		FieldInsnNode fieldInsn = (FieldInsnNode) insn;
		assertEquals(owner, fieldInsn.owner);
		assertEquals(name, fieldInsn.name);
		assertEquals(descriptor, fieldInsn.desc);
	}
	
	private static String getInternalName(Class<?> clazz)
	{
		return clazz.getName().replace('.', '/');
	}
}
