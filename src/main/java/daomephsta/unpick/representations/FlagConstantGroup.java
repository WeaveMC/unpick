package daomephsta.unpick.representations;

import java.util.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import daomephsta.unpick.constantresolvers.IConstantResolver;

/**
 * A group of flags represented by {@link FlagDefinition}s.
 * @author Daomephsta
 */
public class FlagConstantGroup implements ReplacementInstructionGenerator
{	
	private final Collection<FlagDefinition> resolvedConstantDefinitions = new ArrayList<>();
	private final Collection<FlagDefinition> unresolvedConstantDefinitions = new ArrayList<>();
	
	/**
	 * Adds a flag to this group.
	 * @param flagDefinition a constant definition.
	 */
	public void add(FlagDefinition flagDefinition)
	{
		if (flagDefinition.isResolved())
			resolvedConstantDefinitions.add(flagDefinition);
		else 
			unresolvedConstantDefinitions.add(flagDefinition);
	}
	
	@Override
	public InsnList createReplacementInstructions(IConstantResolver constantResolver, Object value)
	{
		resolveAllConstants(constantResolver);
		long lValue = ((Number) value).longValue(); 
		List<FlagDefinition> matches = new ArrayList<>();
		for (FlagDefinition definition : resolvedConstantDefinitions)
		{
			long defLValue = definition.getValue().longValue();
			if ((defLValue & lValue) == defLValue)
				matches.add(definition);
		}
		InsnList replacementInstructions = new InsnList();
		if (matches.isEmpty())
			return null;
		if (matches.size() >= 1)
		{
			FlagDefinition match0 = matches.get(0);
			replacementInstructions.add(
				new FieldInsnNode(Opcodes.GETSTATIC, match0.getOwner(), match0.getName(), match0.getDescriptorString()));
		}
		if (matches.size() >= 2)
		{
			for (int i = 1; i < matches.size(); i++)
			{
				FlagDefinition match = matches.get(i);
				replacementInstructions.add(
					new FieldInsnNode(Opcodes.GETSTATIC, match.getOwner(), match.getName(), match.getDescriptorString()));
				replacementInstructions.add(new InsnNode(value instanceof Long ? Opcodes.LOR : Opcodes.IOR));
			}
		}
		return replacementInstructions;
	}
	
	private void resolveAllConstants(IConstantResolver constantResolver)
	{
		if (!unresolvedConstantDefinitions.isEmpty())
		{
			for (FlagDefinition definition : unresolvedConstantDefinitions)
			{
				resolvedConstantDefinitions.add(definition.resolve(constantResolver));
			}
			unresolvedConstantDefinitions.clear();
		}
	}

	@Override
	public String toString()
	{
		return String.format("FlagGroup [Resolved Flag Definitions: %s, Unresolved Flag Definitions: %s]",
			resolvedConstantDefinitions, unresolvedConstantDefinitions);
	}
}
