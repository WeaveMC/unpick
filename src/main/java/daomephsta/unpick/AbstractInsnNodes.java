package daomephsta.unpick;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;

public class AbstractInsnNodes
{
	public static Object getLiteralValue(AbstractInsnNode insn)
	{
		switch (insn.getOpcode())
		{
		case ICONST_M1:
		case ICONST_0:
		case ICONST_1:
		case ICONST_2:
		case ICONST_3:
		case ICONST_4:
		case ICONST_5:
			return insn.getOpcode() - ICONST_0; //Neat trick that works because the opcodes are sequential
			
		case LCONST_0:
		case LCONST_1:
			return (long) insn.getOpcode() - LCONST_0;
		
		case FCONST_0:
		case FCONST_1:
		case FCONST_2:
			return (float) insn.getOpcode() - FCONST_0;
			
		case DCONST_0:
		case DCONST_1:
			return (double) insn.getOpcode() - DCONST_0;
			
		case Opcodes.BIPUSH:
		case Opcodes.SIPUSH:
			return ((IntInsnNode) insn).operand;
			
		case Opcodes.LDC:
			return ((LdcInsnNode) insn).cst;

		default :
			throw new UnsupportedOperationException("No value retrieval method programmed for " + Printer.OPCODES[insn.getOpcode()]);
		}
	}
	
	public static boolean isLiteral(AbstractInsnNode insn, long i)
	{
		switch (insn.getOpcode())
		{
		case ICONST_M1:
			return i == -1;
		case ICONST_0:
		case LCONST_0:
			return i == 0;
		case ICONST_1:
		case LCONST_1:
			return i == 1;
		case ICONST_2:
			return i == 2;
		case ICONST_3:
			return i == 3;
		case ICONST_4:
			return i == 4;
		case ICONST_5:
			return i == 5;
			
		case Opcodes.BIPUSH:
		case Opcodes.SIPUSH:
			return ((IntInsnNode) insn).operand == i;
			
		case Opcodes.LDC:
			Object value = ((LdcInsnNode) insn).cst;
			return value instanceof Number 
				&& ((Number) value).longValue() == i;

		default :
			throw new UnsupportedOperationException("No value retrieval method programmed for " + Printer.OPCODES[insn.getOpcode()]);
		}
	}
}
