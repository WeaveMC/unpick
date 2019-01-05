package daomephsta.unpick.transformers;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import daomephsta.unpick.constantmappers.IConstantMapper;
/**
 * Uninlines inlined values, mapping them to constants using the specified
 * instance of {@link IConstantMapper} 
 * @author Daomephsta
 */
public class ConstantUninliner
{
	private final IConstantMapper mapper;
	private final Analyzer<SourceValue> analyzer = new Analyzer<>(new SourceInterpreter());

	/**
	 * Constructs a new instance of ConstantUninliner that maps
	 * values to constants with {@code mapper}.
	 * @param mapper an instance of IConstantMapper.
	 */
	public ConstantUninliner(IConstantMapper mapper)
	{
		this.mapper = mapper;
	}

	/**
	 * Unlines all inlined values in the specified class.
	 * @param classNode the class to transform, as a ClassNode.
	 */
	public void transform(ClassNode classNode)
	{
		for (MethodNode method : classNode.methods)
		{
			transformMethod(classNode.name, method);
		}
	}

	/**
	 * Unlines all inlined values in the specified method.
	 * @param methodOwner the internal name of the class that owns
	 * the method represented by {@code method}.
	 * @param method the class to transform, as a MethodNode.
	 */
	public void transformMethod(String methodOwner, MethodNode method)
	{
		try
		{
			Frame<SourceValue>[] frames = analyzer.analyze(methodOwner, method);
			for (int i = 0; i < method.instructions.size(); i++)
			{
				AbstractInsnNode insn = method.instructions.get(i);
				if (insn instanceof MethodInsnNode)
					processMethodInvocation(method, (MethodInsnNode) insn, frames, i);
			}
		}
		catch (AnalyzerException e)
		{
			throw new RuntimeException("Could not analyse " + methodOwner + '.' + method.name + method.desc, e);
		}
	}

	private void processMethodInvocation(MethodNode enclosingMethod, MethodInsnNode methodInvocation, Frame<SourceValue>[] frames, int instructionIndex)
	{
		if (!mapper.targets(methodInvocation.owner, methodInvocation.name, methodInvocation.desc))
			return;
		Type[] parameterTypes = Type.getArgumentTypes(methodInvocation.desc);
		for (int parameterIndex = 0; parameterIndex < parameterTypes.length; parameterIndex++)
		{
			if (!mapper.targets(methodInvocation.owner, methodInvocation.name, methodInvocation.desc, parameterIndex))
				continue;
			for (AbstractInsnNode sourceInsn : frames[instructionIndex].getStack(parameterIndex).insns)
			{
				Object constantValue = getConstantValue(sourceInsn);
				if (constantValue != null)
					transformConstantSource(enclosingMethod, methodInvocation, parameterIndex, sourceInsn, constantValue);
			}
		}
	}
	
	private Object getConstantValue(AbstractInsnNode insn)
	{
		switch (insn.getOpcode())
		{
		case Opcodes.BIPUSH:
		case Opcodes.SIPUSH:
			return ((IntInsnNode) insn).operand;
		case Opcodes.LDC:
			return ((LdcInsnNode) insn).cst;
			
		case Opcodes.ICONST_M1:
			return -1;
		case Opcodes.ICONST_0:
			return 0;
		case Opcodes.ICONST_1:
			return 1;
		case Opcodes.ICONST_2:
			return 2;
		case Opcodes.ICONST_3:
			return 3;
		case Opcodes.ICONST_4:
			return 4;
		case Opcodes.ICONST_5:
			return 5;
			
		case Opcodes.LCONST_0:
			return 0L;
		case Opcodes.LCONST_1:
			return 1L;
			
		case Opcodes.FCONST_0:
			return 0F;
		case Opcodes.FCONST_1:
			return 1F;
		case Opcodes.FCONST_2:
			return 2F;
			
		case Opcodes.DCONST_0:
			return 0D;
		case Opcodes.DCONST_1:
			return 1D;
			
		default:
			return null;
		}
	}

	private void transformConstantSource(MethodNode enclosingMethod, MethodInsnNode methodInvocation, int parameterIndex, AbstractInsnNode insn, Object value)
	{	
		FieldInsnNode constant = mapper.map(methodInvocation.owner, methodInvocation.name, methodInvocation.desc, parameterIndex, value);
		if (constant != null)
			enclosingMethod.instructions.set(insn, constant);
	}
}
