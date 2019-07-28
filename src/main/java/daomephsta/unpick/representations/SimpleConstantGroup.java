package daomephsta.unpick.representations;

import java.util.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;

import daomephsta.unpick.AbstractInsnNodes;
import daomephsta.unpick.constantresolvers.IConstantResolver;

/**
 * A group of constants represented by {@link SimpleConstantDefinition}s.
 * @author Daomephsta
 */
public class SimpleConstantGroup extends AbstractConstantGroup<SimpleConstantDefinition>
{
	private final Map<Object, SimpleConstantDefinition> resolvedConstantDefinitions = new HashMap<>();
	private final Collection<SimpleConstantDefinition> unresolvedConstantDefinitions = new ArrayList<>();
	
	/**
	 * Adds a constant definition to this group.
	 * @param constantDefinition a constant definition.
	 */
	@Override
	public void add(SimpleConstantDefinition constantDefinition)
	{
		if (constantDefinition.isResolved())
			resolvedConstantDefinitions.put(constantDefinition.getValue(), constantDefinition);
		else 
			unresolvedConstantDefinitions.add(constantDefinition);
	}

	@Override
	public boolean canReplace(Context context)
	{
		resolveAllConstants(context.getConstantResolver());
		return AbstractInsnNodes.hasLiteralValue(context.getArgSeed()) 
				&& resolvedConstantDefinitions.containsKey(AbstractInsnNodes.getLiteralValue(context.getArgSeed()));
	}
	
	@Override
	public void generateReplacements(Context context)
	{
		resolveAllConstants(context.getConstantResolver());
		
		Object literalValue = AbstractInsnNodes.getLiteralValue(context.getArgSeed());
		SimpleConstantDefinition constantDefinition = resolvedConstantDefinitions.get(literalValue);
		context.getReplacementSet().addReplacement(context.getArgSeed(), 
				new FieldInsnNode(Opcodes.GETSTATIC, constantDefinition.getOwner(), 
						constantDefinition.getName(), constantDefinition.getDescriptorString()));
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
