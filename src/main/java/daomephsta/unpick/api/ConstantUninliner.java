package daomephsta.unpick.api;

import java.io.IOException;
import java.util.logging.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import daomephsta.unpick.api.constantmappers.IConstantMapper;
import daomephsta.unpick.api.constantresolvers.IConstantResolver;
import daomephsta.unpick.impl.Utils;
import daomephsta.unpick.impl.representations.ReplacementSet;
import daomephsta.unpick.impl.representations.ReplacementInstructionGenerator.Context;
/**
 * Uninlines inlined values 
 * @author Daomephsta
 */
public class ConstantUninliner
{
	private final Logger logger;
	private final IConstantMapper mapper;
	private final IConstantResolver constantResolver;
	private final Analyzer<SourceValue> analyzer = new Analyzer<>(new SourceInterpreter());

	/**
	 * Constructs a new instance of ConstantUninliner that maps
	 * values to constants with {@code mapper}.
	 * @param mapper an instance of IConstantMapper.
	 * @param constantResolver an instance of IConstantResolver for resolving constant types and 
	 * values.
	 */
	public ConstantUninliner(IConstantMapper mapper, IConstantResolver constantResolver)
	{
		this.mapper = mapper;
		this.constantResolver = constantResolver;
		this.logger = Logger.getLogger("unpick");
		logger.setUseParentHandlers(false);
	}
	
	/**
	 * Constructs a new instance of ConstantUninliner that maps
	 * values to constants with {@code mapper}.
	 * @param mapper an instance of IConstantMapper.
	 * @param constantResolver an instance of IConstantResolver for resolving constant types and 
	 * values.
	 * @param logFile a file path to output debug logging to.
	 */
	public ConstantUninliner(IConstantMapper mapper, IConstantResolver constantResolver, String logFile)
	{
		this(mapper, constantResolver);
		try
		{
			FileHandler fileHandler = new FileHandler(logFile);
			Formatter formatter = new Formatter()
			{
				@Override
				public String format(LogRecord record)
				{
					return record.getLevel() + ": " + String.format(record.getMessage(), record.getParameters()) + System.lineSeparator();
				}
			};
			fileHandler.setFormatter(formatter);
			logger.addHandler(fileHandler);
		} 
		catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
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
	 * @param methodOwner the internal name of the class that owns {@code method}.
	 * @param method the class to transform, as a MethodNode.
	 */
	public void transformMethod(String methodOwner, MethodNode method)
	{
		logger.log(Level.INFO, String.format("Processing %s.%s%s", methodOwner, method.name, method.desc));
		try
		{ 
			ReplacementSet replacementSet = new ReplacementSet(method.instructions);
			Frame<SourceValue>[] frames = analyzer.analyze(methodOwner, method);
			for (int i = 0; i < method.instructions.size(); i++)
			{
				AbstractInsnNode insn = method.instructions.get(i);
				if (insn instanceof MethodInsnNode)
				{
					processMethodInvocation(method, (MethodInsnNode) insn, replacementSet, frames, i);
				}
				else if (insn instanceof InsnNode && insn.getOpcode() >= Opcodes.IRETURN && insn.getOpcode() <= Opcodes.ARETURN)
				{
					processMethodReturn(methodOwner, method, insn, replacementSet, frames, i);
				}
			}
			replacementSet.apply();
		}
		catch (AnalyzerException e)
		{
			logger.log(Level.WARNING, String.format("Processing %s.%s%s failed", methodOwner, method.name, method.desc), e);
		}
	}

	private void processMethodInvocation(MethodNode enclosingMethod, MethodInsnNode methodInvocation, ReplacementSet replacementSet, 
			Frame<SourceValue>[] frames, int instructionIndex)
	{
		if (!mapper.targets(methodInvocation.owner, methodInvocation.name, methodInvocation.desc))
			return;
		logger.log(Level.INFO, String.format("Considering target: %s.%s", methodInvocation.owner, methodInvocation.name));
		Frame<SourceValue> frame = frames[instructionIndex];
		Type[] parameterTypes = Type.getArgumentTypes(methodInvocation.desc);
		for (int parameterIndex = 0; parameterIndex < parameterTypes.length; parameterIndex++)
		{
			if (!mapper.targetsParameter(methodInvocation.owner, methodInvocation.name, methodInvocation.desc, parameterIndex))
				continue;
			for (AbstractInsnNode sourceInsn : frame.getStack(frame.getStackSize() - parameterTypes.length + parameterIndex).insns)
			{
				Context context = new Context(constantResolver, replacementSet, sourceInsn, enclosingMethod.instructions, frames, logger);
				logger.log(Level.INFO, String.format("Considering target: %s.%s#param-%d Arg Seed: %s", 
						methodInvocation.owner, methodInvocation.name, parameterIndex, Utils.visitableToString(sourceInsn::accept).trim()));
				mapper.mapParameter(methodInvocation.owner, methodInvocation.name, methodInvocation.desc, parameterIndex, context);
			}
		}
	}

	private void processMethodReturn(String methodOwner, MethodNode method, AbstractInsnNode returnInsn, ReplacementSet replacementSet, 
			Frame<SourceValue>[] frames, int instructionIndex)
	{
		if (mapper.targets(methodOwner, method.name, method.desc) 
				&& mapper.targetsReturn(methodOwner, method.name, method.desc))
		{
			Frame<SourceValue> frame = frames[instructionIndex];
			for (AbstractInsnNode sourceInsn : frame.getStack(0).insns) //Only one value on the stack before a return
			{
				logger.log(Level.INFO, String.format("Considering target: %s.%s#return Arg Seed: %s", 
						methodOwner, method.name, Utils.visitableToString(sourceInsn::accept).trim()));
				Context context = new Context(constantResolver, replacementSet, sourceInsn, method.instructions, frames, logger);
				mapper.mapReturn(methodOwner, method.name, method.desc, context );
			}
		}
	}
}
