package daomephsta.unpick.representations;

import java.util.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;

import daomephsta.unpick.constantresolvers.IConstantResolver;

/**
 * A group of constants represented by {@link SimpleConstantDefinition}s.
 * @author Daomephsta
 */
public class SimpleConstantGroup implements ReplacementInstructionGenerator
{
	private final Map<Object, SimpleConstantDefinition> resolvedConstantDefinitions = new HashMap<>();
	private final Collection<SimpleConstantDefinition> unresolvedConstantDefinitions = new ArrayList<>();
	
	/**
	 * Adds a constant definition to this group.
	 * @param constantDefinition a constant definition.
	 */
	public void add(SimpleConstantDefinition constantDefinition)
	{
		if (constantDefinition.isResolved())
			resolvedConstantDefinitions.put(constantDefinition.getValue(), constantDefinition);
		else 
			unresolvedConstantDefinitions.add(constantDefinition);
	}
	
	/**
	 * Gets a constant definition by value, resolving all 
	 * unresolved constant definitions first.
	 * @param constantResolver an instance of IConstantResolver
	 * to use to resolve unresolved constant definitions.
	 * @param value the value to retrieve a constant definition for.
	 * @return a resolved constant definition with a value equal to {@code value}
	 */
	@Override
	public InsnList createReplacementInstructions(IConstantResolver constantResolver, Object value)
	{
		resolveAllConstants(constantResolver);
		
		SimpleConstantDefinition constantDefinition = resolvedConstantDefinitions.get(value);
		
		InsnList replacementInstructions = new InsnList();
		replacementInstructions.add(new FieldInsnNode(Opcodes.GETSTATIC, constantDefinition.getOwner(), constantDefinition.getName(), 
			constantDefinition.getDescriptorString()));
		
		return replacementInstructions;
	}
	
	private void resolveAllConstants(IConstantResolver constantResolver)
	{
		if (!unresolvedConstantDefinitions.isEmpty())
		{
			for (SimpleConstantDefinition definition : unresolvedConstantDefinitions)
			{
				resolvedConstantDefinitions.put(definition.resolve(constantResolver).getValue(), definition);
			}
			unresolvedConstantDefinitions.clear();
		}
	}

	@Override
	public String toString()
	{
		return String.format("ConstantGroup [Resolved Constant Definitions: %s, Unresolved Constant Definitions: %s]",
			resolvedConstantDefinitions, unresolvedConstantDefinitions);
	}
}
