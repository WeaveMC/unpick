package daomephsta.unpick.constantmappers.datadriven.parser.v2;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import daomephsta.unpick.constantmappers.datadriven.parser.MethodKey;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Definitions.TargetMethodDefinitionVisitor;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Definitions.Visitor;

public class UnpickV2Remapper implements Visitor
{
	private static final Pattern OBJECT_SIGNATURE_FINDER = Pattern.compile("L([a-zA-Z0-9$_\\/]+);");
	private final Map<String, String> classMappings;
	private final Map<MethodKey, String> methodMappings;
	private final Visitor delegate;

	public UnpickV2Remapper(Map<String, String> classMappings, Map<MethodKey, String> methodMappings, Visitor delegate)
	{
		this.classMappings = classMappings;
		this.methodMappings = methodMappings;
		this.delegate = delegate;
	}

	public TargetMethodDefinitionVisitor visitTargetMethodDefinition(String owner, String name, String descriptor)
	{
		//Reassigning the parameters tends to cause bugs
		String remappedOwner = owner, 
			   remappedName = name, 
			   remappedDescriptor = descriptor;
		
		if (classMappings.containsKey(owner))
            remappedOwner = classMappings.get(owner);
		
		MethodKey methodKey = new MethodKey(owner, name, descriptor);
        if (methodMappings.containsKey(methodKey))
        	remappedName = methodMappings.get(methodKey);
        
		Matcher objectSignatureMatcher = OBJECT_SIGNATURE_FINDER.matcher(descriptor);
		while(objectSignatureMatcher.find())
		{
			String objectSignature = objectSignatureMatcher.group(1);
			if (classMappings.containsKey(objectSignature))
				remappedDescriptor = remappedDescriptor.replace(objectSignature, classMappings.get(objectSignature));
		}
		return delegate.visitTargetMethodDefinition(remappedOwner, remappedName, remappedDescriptor);
	}

	public void startVisit()
	{
		delegate.startVisit();
	}

	public void visitLineNumber(int lineNumber)
	{
		delegate.visitLineNumber(lineNumber);
	}

	public void visitSimpleConstantDefinition(String group, String owner, String name, String value, String descriptor)
	{
		delegate.visitSimpleConstantDefinition(group, owner, name, value, descriptor);
	}

	public void visitFlagConstantDefinition(String group, String owner, String name, String value, String descriptor)
	{
		delegate.visitFlagConstantDefinition(group, owner, name, value, descriptor);
	}

	public void endVisit()
	{
		delegate.endVisit();
	}
}
