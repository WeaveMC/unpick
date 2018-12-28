package daomephsta.unpick.datadriven;

import java.util.*;

public class ConstantGroup
{
	private final Map<Object, ConstantDefinition> resolvedConstantDefinitions = new HashMap<>();
	private final Collection<ConstantDefinition> unresolvedConstantDefinitions = new ArrayList<>();

	public void add(ConstantDefinition constantDefinition)
	{
		if (constantDefinition.isResolved())
			resolvedConstantDefinitions.put(constantDefinition.getValue(), constantDefinition);
		else 
			unresolvedConstantDefinitions.add(constantDefinition);
	}
	
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
