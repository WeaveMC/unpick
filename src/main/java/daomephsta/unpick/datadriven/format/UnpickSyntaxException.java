package daomephsta.unpick.datadriven.format;

public class UnpickSyntaxException extends RuntimeException
{
	private static final long serialVersionUID = -86704276968539185L;

	public UnpickSyntaxException()
	{
		super();
	}

	public UnpickSyntaxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public UnpickSyntaxException(int lineNumber, String message, Throwable cause)
	{
		super("Line " + lineNumber + ": " + message, cause);
	}

	public UnpickSyntaxException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public UnpickSyntaxException(int lineNumber, String message)
	{
		super("Line " + lineNumber + ": " + message);
	}

	public UnpickSyntaxException(String message)
	{
		super(message);
	}

	public UnpickSyntaxException(Throwable cause)
	{
		super(cause);
	}
}
