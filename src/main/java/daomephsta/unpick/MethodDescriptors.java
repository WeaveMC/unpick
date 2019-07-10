package daomephsta.unpick;

public class MethodDescriptors
{

	public static String getMethodKey(String methodOwner, String methodName, String methodDescriptor)
	{
		return methodOwner + '.' + methodName + methodDescriptor;
	}

}
