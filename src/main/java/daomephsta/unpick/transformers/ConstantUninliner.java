package daomephsta.unpick.transformers;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import daomephsta.unpick.AbstractInsnNodes;
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
			Map<AbstractInsnNode, InsnList> replacements = new HashMap<>();
			Frame<SourceValue>[] frames = analyzer.analyze(methodOwner, method);
			for (int i = 0; i < method.instructions.size(); i++)
			{
				AbstractInsnNode insn = method.instructions.get(i);
				if (insn instanceof MethodInsnNode)
					processMethodInvocation(replacements, method, (MethodInsnNode) insn, frames, i);
			}
			for (Map.Entry<AbstractInsnNode, InsnList> replacement : replacements.entrySet())
			{
				method.instructions.insert(replacement.getKey(), replacement.getValue());
				method.instructions.remove(replacement.getKey());
			}
		}
		catch (AnalyzerException e)
		{
			throw new RuntimeException("Could not analyse " + methodOwner + '.' + method.name + method.desc, e);
		}
	}

	private void processMethodInvocation(Map<AbstractInsnNode, InsnList> replacements, MethodNode enclosingMethod, MethodInsnNode methodInvocation, Frame<SourceValue>[] frames, int instructionIndex)
	{
		if (!mapper.targets(methodInvocation.owner, methodInvocation.name, methodInvocation.desc))
			return;
		Type[] parameterTypes = Type.getArgumentTypes(methodInvocation.desc);
		for (int parameterIndex = 0; parameterIndex < parameterTypes.length; parameterIndex++)
		{
			if (!mapper.targets(methodInvocation.owner, methodInvocation.name, methodInvocation.desc, parameterIndex))
				continue;
			Frame<SourceValue> frame = frames[instructionIndex];
			for (AbstractInsnNode sourceInsn : frame.getStack(frame.getStackSize() - parameterTypes.length + parameterIndex).insns)
			{
				Object constantValue = AbstractInsnNodes.getLiteralValue(sourceInsn);
				if (constantValue != null)
				{
					InsnList replacementInstructions = mapper.map(methodInvocation.owner, methodInvocation.name, methodInvocation.desc, parameterIndex, constantValue);
					if (replacementInstructions != null)
						replacements.put(sourceInsn, replacementInstructions);
				}
			}
		}
	}
}
