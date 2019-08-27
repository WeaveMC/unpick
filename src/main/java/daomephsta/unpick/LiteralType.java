package daomephsta.unpick;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;

public enum LiteralType
{
	INT(Integer.class, int.class, Type.INT_TYPE, Opcodes.IRETURN) 
	{
		@Override
		public AbstractInsnNode createLiteralPushInsn(Object literal)
			{ return InstructionFactory.pushesInt(((Number) literal).intValue()); }

		@Override
		public void appendLiteralPushInsn(MethodVisitor mv, Object literal)
			{ InstructionFactory.pushesInt(mv, ((Number) literal).intValue()); }
	},
	LONG(Long.class, long.class, Type.LONG_TYPE, Opcodes.LRETURN) 
	{
		@Override
		public AbstractInsnNode createLiteralPushInsn(Object literal)
			{ return InstructionFactory.pushesLong(((Number) literal).longValue()); }

		@Override
		public void appendLiteralPushInsn(MethodVisitor mv, Object literal)
			{ InstructionFactory.pushesLong(mv, ((Number) literal).longValue()); }
	},
	FLOAT(Float.class, float.class, Type.FLOAT_TYPE, Opcodes.FRETURN) 
	{
		@Override
		public AbstractInsnNode createLiteralPushInsn(Object literal)
			{ return InstructionFactory.pushesFloat(((Number) literal).floatValue()); }

		@Override
		public void appendLiteralPushInsn(MethodVisitor mv, Object literal)
			{ InstructionFactory.pushesFloat(mv, ((Number) literal).floatValue()); }
	},
	DOUBLE(Double.class, double.class, Type.DOUBLE_TYPE, Opcodes.DRETURN) 
	{
		@Override
		public AbstractInsnNode createLiteralPushInsn(Object literal)
			{ return InstructionFactory.pushesDouble(((Number) literal).doubleValue()); }

		@Override
		public void appendLiteralPushInsn(MethodVisitor mv, Object literal)
			{ InstructionFactory.pushesDouble(mv, ((Number) literal).doubleValue()); }
	},
	STRING(String.class, String.class, Type.getType(String.class), Opcodes.ARETURN) 
	{
		@Override
		public AbstractInsnNode createLiteralPushInsn(Object literal)
			{ return InstructionFactory.pushesString((String) literal); }

		@Override
		public void appendLiteralPushInsn(MethodVisitor mv, Object literal)
			{ InstructionFactory.pushesString(mv, (String) literal); }
	};
	
	private final Class<?> boxed, primitive;
	private final Type type;
	private final int returnOpcode;
	
	private LiteralType(Class<?> boxed, Class<?> primitive, Type type, int returnOpcode)
	{
		this.boxed = boxed;
		this.primitive = primitive;
		this.type = type;
		this.returnOpcode = returnOpcode;
	}
	
	public static LiteralType from(Class<?> clazz)
	{
		if (clazz == Integer.class || clazz == int.class)
			return INT;
		else if (clazz == Long.class || clazz == long.class)
			return LONG;
		if (clazz == Float.class || clazz == float.class)
			return FLOAT;
		else if (clazz == Double.class || clazz == double.class)
			return DOUBLE;
		else if (clazz == String.class)
			return STRING;
		else
			throw new IllegalArgumentException("Expected an integer, long, float, double, or String; got " + clazz);
	}
	
	public AbstractInsnNode createReturnInsn()
	{
		return new InsnNode(getReturnOpcode());
	}
	
	public void appendReturnInsn(MethodVisitor mv)
	{
		mv.visitInsn(getReturnOpcode());
	}
	
	public int getReturnOpcode()
	{
		return returnOpcode;
	}

	public abstract AbstractInsnNode createLiteralPushInsn(Object literal);
	
	public abstract void appendLiteralPushInsn(MethodVisitor mv, Object literal);
	
	public String getTypeDescriptor()
	{
		return type.getDescriptor();
	}
	
	public Class<?> getBoxClass()
	{
		return boxed;
	}
	
	public Class<?> getPrimitiveClass()
	{
		return primitive;
	}
}
