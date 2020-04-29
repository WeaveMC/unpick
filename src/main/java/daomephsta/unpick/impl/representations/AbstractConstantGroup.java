package daomephsta.unpick.impl.representations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import daomephsta.unpick.api.constantresolvers.IConstantResolver;
import daomephsta.unpick.impl.representations.AbstractConstantDefinition.ResolutionException;

public abstract class AbstractConstantGroup<T extends AbstractConstantDefinition<T>> implements ReplacementInstructionGenerator
{
	private static final Logger LOGGER = Logger.getLogger("unpick");
	protected final Collection<T> unresolvedConstantDefinitions = new ArrayList<>();
	
	/**
	 * Adds a constant definition to this group.
	 * @param constantDefinition a constant definition.
	 */
	public abstract void add(T constantDefinition);
	
	protected final void resolveAllConstants(IConstantResolver constantResolver)
	{
		if (!unresolvedConstantDefinitions.isEmpty())
		{
			for (T definition : unresolvedConstantDefinitions)
			{
				LOGGER.info("Resolving " + definition);
				try
				{
					acceptResolved(definition.resolve(constantResolver));
				} 
				catch (ResolutionException e)
				{
					LOGGER.warning(e.getMessage());
				}
			}
			unresolvedConstantDefinitions.clear();
		}
	}
	
	protected abstract void acceptResolved(T definition);
}
