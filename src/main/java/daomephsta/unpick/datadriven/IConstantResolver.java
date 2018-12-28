package daomephsta.unpick.datadriven;

import java.util.Map;

import org.objectweb.asm.Type;

public interface IConstantResolver
{
	public Map.Entry<Type, Object> resolveConstant(String owner, String name);
}
