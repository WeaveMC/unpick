package daomephsta.unpick.transformers;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import daomephsta.unpick.constantmappers.IConstantMapper;
import daomephsta.unpick.constantresolvers.IConstantResolver;
import daomephsta.unpick.representations.ReplacementInstructionGenerator.Context;
import daomephsta.unpick.representations.ReplacementSet;
/**
 * Uninlines inlined values, mapping them to constants using the specified
 * instance of {@link IConstantMapper} 
 * @author Daomephsta
 */
public class ConstantUninliner
{
	private final IConstantMapper mapper;
	private final IConstantResolver constantResolver;
	private final Analyzer<SourceValue> analyzer = new Analyzer<>(new SourceInterpreter());

	/**
	 * Constructs a new instance of ConstantUninliner that maps
	 * values to constants with {@code mapper}.
	 * @param mapper an instance of IConstantMapper.
	 */
	public ConstantUninliner(IConstantMapper mapper, IConstantResolver constantResolver)
	{
		this.mapper = mapper;
		this.constantResolver = constantResolver;
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
			ReplacementSet replacementSet = new ReplacementSet(method.instructions);
			Frame<SourceValue>[] frames = analyzer.analyze(methodOwner, method);
			for (int i = 0; i < method.instructions.size(); i++)
			{
				AbstractInsnNode insn = method.instructions.get(i);
				if (insn instanceof MethodInsnNode)
					processMethodInvocation(method, (MethodInsnNode) insn, replacementSet, frames, i);
			}
			replacementSet.apply();
		}
		catch (AnalyzerException e)
		{
			throw new RuntimeException("Could not analyse " + methodOwner + '.' + method.name + method.desc, e);
		}
	}

	private void processMethodInvocation(MethodNode enclosingMethod, MethodInsnNode methodInvocation, ReplacementSet replacementSet, 
			Frame<SourceValue>[] frames, int instructionIndex)
	{
		if (!mapper.targets(methodInvocation.owner, methodInvocation.name, methodInvocation.desc))
			return;
		
		Frame<SourceValue> frame = frames[instructionIndex];
		Type[] parameterTypes = Type.getArgumentTypes(methodInvocation.desc);
		for (int parameterIndex = 0; parameterIndex < parameterTypes.length; parameterIndex++)
		{
			if (!mapper.targets(methodInvocation.owner, methodInvocation.name, methodInvocation.desc, parameterIndex))
				continue;
			for (AbstractInsnNode sourceInsn : frame.getStack(frame.getStackSize() - parameterTypes.length + parameterIndex).insns)
			{
				Context context = new Context(constantResolver, replacementSet, sourceInsn, enclosingMethod.instructions, frames);
				mapper.map(methodInvocation.owner, methodInvocation.name, methodInvocation.desc, parameterIndex, context);
			}
		}
	}
}
