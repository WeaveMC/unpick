package daomephsta.unpick.representations;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceValue;

import daomephsta.unpick.constantresolvers.IConstantResolver;

/**
 * @author Daomephsta
 */
public interface ReplacementInstructionGenerator
{
	public abstract boolean canReplace(Context context);
	
	/**
	 * Generates replacement instructions for the provided value
	 * @param context TODO
	 */
	public abstract void generateReplacements(Context context);
	
	public class Context
	{
		private final IConstantResolver constantResolver;
		private final ReplacementSet replacementSet;
		private final AbstractInsnNode argSeed;
		private final InsnList instructions;
		private final Frame<SourceValue>[] frames;

		public Context(IConstantResolver constantResolver, ReplacementSet replacementSet, AbstractInsnNode argSeed, 
				InsnList instructions, Frame<SourceValue>[] frames)
		{
			this.constantResolver = constantResolver;
			this.replacementSet = replacementSet;
			this.argSeed = argSeed;
			this.instructions = instructions;
			this.frames = frames;
		}

		public IConstantResolver getConstantResolver()
		{
			return constantResolver;
		}

		public ReplacementSet getReplacementSet()
		{
			return replacementSet;
		}

		public AbstractInsnNode getArgSeed()
		{
			return argSeed;
		}
		
		public Frame<SourceValue> getFrameForInstruction(AbstractInsnNode insn)
		{
			return frames[instructions.indexOf(insn)];
		}
	}
}
