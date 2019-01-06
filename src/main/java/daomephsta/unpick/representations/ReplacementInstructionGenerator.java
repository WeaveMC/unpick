package daomephsta.unpick.representations;

import org.objectweb.asm.tree.InsnList;

import daomephsta.unpick.constantresolvers.IConstantResolver;

/**
 * @author Daomephsta
 */
public interface ReplacementInstructionGenerator
{
	/**
	 * Generates replacement instructions for the provided value
	 * @param constantResolver an instance of IConstantResolver
	 * to use to resolve unresolved constant definitions.
	 * @param value the value to retrieve a constant definition for.
	 * @return replacement instructions
	 */
	public abstract InsnList createReplacementInstructions(IConstantResolver constantResolver, Object value);
}
