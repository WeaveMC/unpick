package daomephsta.unpick.transformers;

import org.objectweb.asm.*;

import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.tree.analysis.Frame;

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
					processMethodInvocation(method, (MethodInsnNode) insn, frames[i]);
			}
		}
		catch (AnalyzerException e)
		{
			throw new RuntimeException("Could not analyse " + methodOwner + '.' + method.name + method.desc, e);
		}
	}

	private void processMethodInvocation(MethodNode enclosingMethod, MethodInsnNode methodInvocation, Frame<SourceValue> frame)
	{
		if (!mapper.targets(methodInvocation.owner, methodInvocation.name, methodInvocation.desc))
			return;
		Type[] parameterTypes = Type.getArgumentTypes(methodInvocation.desc);
		for (int parameterIndex = 0; parameterIndex < parameterTypes.length; parameterIndex++)
		{
			if (!mapper.targets(methodInvocation.owner, methodInvocation.name, methodInvocation.desc, parameterIndex))
				continue;
			for (AbstractInsnNode sourceInsn : frame.getStack(parameterIndex).insns)
			{
				//TODO find all constant sources and make sure they work
				if (sourceInsn instanceof IntInsnNode && sourceInsn.getOpcode() == Opcodes.SIPUSH)
					transformIntInsn(enclosingMethod, methodInvocation, parameterIndex, (IntInsnNode) sourceInsn);
			}
		}
	}

	private void transformIntInsn(MethodNode enclosingMethod, MethodInsnNode methodInvocation, int parameterIndex, IntInsnNode insn)
	{
		FieldInsnNode constant = mapper.map(methodInvocation.owner, methodInvocation.name, methodInvocation.desc, parameterIndex, 
			insn.operand);
		if (constant != null)
			enclosingMethod.instructions.set(insn, constant);
	}
}
