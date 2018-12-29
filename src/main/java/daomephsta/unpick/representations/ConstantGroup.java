package daomephsta.unpick.representations;

import java.util.*;

import daomephsta.unpick.constantresolvers.IConstantResolver;

/**
 * A group of constants represented by {@link ConstantDefinition}s.
 * @author Daomephsta
 */
public class ConstantGroup
{
	private final Map<Object, ConstantDefinition> resolvedConstantDefinitions = new HashMap<>();
	private final Collection<ConstantDefinition> unresolvedConstantDefinitions = new ArrayList<>();

	/**
	 * Adds a constant definition to this group.
	 * @param constantDefinition a constant definition.
	 */
	public void add(ConstantDefinition constantDefinition)
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
	public ConstantDefinition get(IConstantResolver constantResolver, Object value)
	{
		if (!unresolvedConstantDefinitions.isEmpty())
		{
			for (ConstantDefinition definition : unresolvedConstantDefinitions)
			{
				resolvedConstantDefinitions.put(definition.resolve(constantResolver).getValue(), definition);
			}
			unresolvedConstantDefinitions.clear();
		}
		return resolvedConstantDefinitions.get(value);
	}

	@Override
	public String toString()
	{
		return String.format("ConstantGroup [Resolved Constant Definitions: %s, Unresolved Constant Definitions: %s]",
			resolvedConstantDefinitions, unresolvedConstantDefinitions);
	}
}
