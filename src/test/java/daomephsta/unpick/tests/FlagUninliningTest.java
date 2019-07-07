package daomephsta.unpick.tests;

public class FlagUninliningTest
{
	public void test()
	{
		intTest(0);
		intTest(0b1);
		intTest(0b11);
		intTest(0b1011);
		
		longTest(0);
		longTest(0b1);
		longTest(0b11);
		longTest(0b1011);
	}
	
	private static void intTest(int test) {}

	private static void longTest(long test) {}
}
