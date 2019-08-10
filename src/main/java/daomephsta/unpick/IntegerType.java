package daomephsta.unpick;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;

public enum IntegerType
{
	INT(Type.INT_TYPE, Opcodes.IAND) 
	{
		@Override
		public AbstractInsnNode createLiteralPushInsn(long literal)
			{ return InstructionFactory.pushesInt((int) literal); }

		@Override
		public void appendLiteralPushInsn(MethodVisitor mv, long literal)
			{ InstructionFactory.pushesInt(mv, (int) literal); }
		
		@Override
		public Number box(long value)
			{ return new Integer((int) value); }

		@Override
		public Number binaryNegate(Number value)
			{ return ~value.intValue(); }
	},
	LONG(Type.LONG_TYPE, Opcodes.LAND) 
	{
		@Override
		public AbstractInsnNode createLiteralPushInsn(long literal)
			{ return InstructionFactory.pushesLong(literal); }

		@Override
		public void appendLiteralPushInsn(MethodVisitor mv, long literal)
			{ InstructionFactory.pushesLong(mv, literal); }
		
		@Override
		public Number box(long value)
			{ return new Long(value); }

		@Override
		public Number binaryNegate(Number value)
			{ return ~value.longValue(); }
	};
	
	private final Type type;
	private final int logicalOpcodesStart;
	
	private IntegerType(Type type, int startOpcode)
	{
		this.type = type;
		this.logicalOpcodesStart = startOpcode;
	}
	
	public static IntegerType from(Class<?> clazz)
	{
		if (clazz == Integer.class || clazz == int.class)
			return INT;
		else if (clazz == Long.class || clazz == long.class)
			return LONG;
		else
			throw new IllegalArgumentException("Expected an integer or long, got " + clazz);
	}
	
	public AbstractInsnNode createAndInsn()
	{
		return new InsnNode(getAndOpcode());
	}
	
	public void appendAndInsn(MethodVisitor mv)
	{
		mv.visitInsn(getAndOpcode());
	}

	public int getAndOpcode()
	{
		return logicalOpcodesStart + 0;
	}
	
	public AbstractInsnNode createOrInsn()
	{
		return new InsnNode(getOrOpcode());
	}
	
	public void appendOrInsn(MethodVisitor mv)
	{
		mv.visitInsn(getOrOpcode());
	}

	public int getOrOpcode()
	{
		return logicalOpcodesStart + 2;
	}
	
	public AbstractInsnNode createXorInsn()
	{
		return new InsnNode(getXorOpcode());
	}
	
	public void appendXorInsn(MethodVisitor mv)
	{
		mv.visitInsn(getXorOpcode());
	}

	public int getXorOpcode()
	{
		return logicalOpcodesStart + 4;
	}
	
	public abstract AbstractInsnNode createLiteralPushInsn(long literal);
	
	public abstract void appendLiteralPushInsn(MethodVisitor mv, long literal);
	
	public String getTypeDescriptor()
	{
		return type.getDescriptor();
	}

	public abstract Number box(long value);
	
	public abstract Number binaryNegate(Number value);
}
