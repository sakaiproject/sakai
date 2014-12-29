package org.sakaiproject.tool.assessment.qti.exception;

public class RespondusMatchingException extends RuntimeException {
	/**
	 * Creates a new Iso8601FormatException object.
	 *
	 * @param message DOCUMENTATION PENDING
	 */
	public RespondusMatchingException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new Iso8601FormatException object.
	 *
	 * @param message DOCUMENTATION PENDING
	 * @param cause DOCUMENTATION PENDING
	 */
	public RespondusMatchingException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Creates a new Iso8601FormatException object.
	 *
	 * @param cause DOCUMENTATION PENDING
	 */
	public RespondusMatchingException(Throwable cause)
	{
		super(cause);
	}
}
