package daomephsta.unpick.representations;

import java.util.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import daomephsta.unpick.AbstractInsnNodes;
import daomephsta.unpick.FlagStatement;
import daomephsta.unpick.FlagStatement.BitOp;
import daomephsta.unpick.constantresolvers.IConstantResolver;
import daomephsta.unpick.tests.lib.InstructionFactory;
import daomephsta.unpick.tests.lib.TestUtils;

/**
 * A group of flags represented by {@link FlagDefinition}s.
 * @author Daomephsta
 */
public class FlagConstantGroup extends AbstractConstantGroup<FlagDefinition>
{	
	private final Collection<FlagDefinition> resolvedConstantDefinitions = new ArrayList<>();
	private final Collection<FlagDefinition> unresolvedConstantDefinitions = new ArrayList<>();
	
	/**
	 * Adds a flag to this group.
	 * @param flagDefinition a constant definition.
	 */
	@Override
	public void add(FlagDefinition flagDefinition)
	{
		if (flagDefinition.isResolved())
			resolvedConstantDefinitions.add(flagDefinition);
		else 
			unresolvedConstantDefinitions.add(flagDefinition);
	}

	@Override
	public boolean canReplace(Context context)
	{
		boolean isLiteral = AbstractInsnNodes.hasLiteralValue(context.getArgSeed()) 
				&& AbstractInsnNodes.getLiteralValue(context.getArgSeed()) instanceof Number;
		boolean isBitwiseOp = context.getArgSeed().getOpcode() >= Opcodes.IAND 
				&& context.getArgSeed().getOpcode() <= Opcodes.LXOR;
		return isLiteral || isBitwiseOp;
	}
	
	@Override
	public void generateReplacements(Context context)
	{
		resolveAllConstants(context.getConstantResolver());
		FlagStatement.create(context.getArgSeed(), context::getFrameForInstruction)
			.collectReplacements(context.getReplacementSet(), this::convertLiteral);
	}
	
	private Optional<InsnList> convertLiteral(Number literal, BitOp bitOp)
	{	 
		switch (bitOp)
		{
		case AND:
			return convertANDedLiteral(literal);
			
		//Assuming OR gives more useful results than assuming AND
		case NONE:
		case OR:
			return convertORedLiteral(literal);

		case NOT:
		case XOR:
		default:
			return Optional.empty();
		}
	}

	private Optional<InsnList> convertANDedLiteral(Number literal)
	{
		long lValue = literal.longValue();
		FlagDefinition match = null;
		for (FlagDefinition definition : resolvedConstantDefinitions)
		{
			if (~definition.getValue().longValue() == lValue)
				match = definition;
		}
		if (match == null)
			return Optional.empty();
		
		InsnList replacementInstructions = new InsnList();
		replacementInstructions.add(new FieldInsnNode(Opcodes.GETSTATIC, match.getOwner(), match.getName(), match.getDescriptorString()));
		replacementInstructions.add(literal instanceof Long ? InstructionFactory.pushesLong(-1) : InstructionFactory.pushesInt(-1));
		replacementInstructions.add(new InsnNode(literal instanceof Long ? Opcodes.LXOR : Opcodes.IXOR));
		return Optional.of(replacementInstructions);
	}

	private Optional<InsnList> convertORedLiteral(Number literal)
	{
		long longLiteral = literal.longValue();
		long remainder = longLiteral;
		List<FlagDefinition> matches = new ArrayList<>();
		for (FlagDefinition definition : resolvedConstantDefinitions)
		{
			long definitionLongValue = definition.getValue().longValue();
			if ((definitionLongValue & longLiteral) == definitionLongValue)
			{
				matches.add(definition);
				remainder &= ~definitionLongValue;
			}
		}
		if (matches.isEmpty())
			return Optional.empty();
		
		InsnList replacementInstructions = new InsnList();
		FlagDefinition match0 = matches.get(0);
		int orOpcode = literal instanceof Long ? Opcodes.LOR : Opcodes.IOR;
		replacementInstructions.add(new FieldInsnNode(Opcodes.GETSTATIC, match0.getOwner(), match0.getName(), match0.getDescriptorString()));
		for (int i = 1; i < matches.size(); i++)
		{
			FlagDefinition match = matches.get(i);
			replacementInstructions.add(new FieldInsnNode(Opcodes.GETSTATIC, match.getOwner(), match.getName(), match.getDescriptorString()));
			replacementInstructions.add(new InsnNode(orOpcode));
		}
		if (remainder != 0)
		{
			AbstractInsnNode remainderLiteral = literal instanceof Long 
					? InstructionFactory.pushesLong(literal.longValue()) 
					: InstructionFactory.pushesInt(literal.intValue());
			replacementInstructions.add(remainderLiteral);
			replacementInstructions.add(new InsnNode(orOpcode));
		}
		return Optional.of(replacementInstructions);
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
