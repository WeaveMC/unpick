package unpick;

public class SimpleConstantUninliningTest
{
	public void test()
	{
		intTest(-1);
		intTest(0);
		intTest(1);
		intTest(2);
		intTest(3);
		intTest(4);
		intTest(5);
		intTest(257);
		
		longTest(0);
		longTest(1);
		longTest(1234567890);
		
		floatTest(0);
		floatTest(1);
		floatTest(2);
		floatTest(5.3F);
		
		doubleTest(0);
		doubleTest(1);
		doubleTest(5.3D);
		
		stringTest("foo");
		stringTest("bar");
	}
	
	private static void intTest(int test) {}

	private static void longTest(long test) {}

	private static void floatTest(float test) {}

	private static void doubleTest(double test) {}

	private static void stringTest(String test) {}
}
