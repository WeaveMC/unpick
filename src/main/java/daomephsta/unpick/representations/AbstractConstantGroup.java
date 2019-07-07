package daomephsta.unpick.representations;

public abstract class AbstractConstantGroup<T extends AbstractConstantDefinition<T>> implements ReplacementInstructionGenerator
{
	/**
	 * Adds a constant definition to this group.
	 * @param constantDefinition a constant definition.
	 */
	public abstract void add(T constantDefinition);
}
