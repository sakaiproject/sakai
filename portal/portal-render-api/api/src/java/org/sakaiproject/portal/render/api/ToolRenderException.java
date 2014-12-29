package org.sakaiproject.portal.render.api;

import java.io.IOException;

/**
 * Exception thrown when an error occurs while preprocessing or rendering a
 * portlet.
 * 
 * @since Sakai 2.2.3
 * @version $Rev$
 */
public class ToolRenderException extends IOException
{

	/**
	 * Root cause;
	 */
	private Throwable throwable;

	/**
	 * Default constructor
	 * 
	 * @param message
	 *        the exception message
	 * @param throwable
	 *        the root cause.
	 */
	public ToolRenderException(String message, Throwable throwable)
	{
		super(message);
		this.throwable = throwable;
	}

	/**
	 * Alternate constructor indicating that this exception is the root cause.
	 * 
	 * @param message
	 *        the exception message
	 */
	public ToolRenderException(String message)
	{
		super(message);
	}

	/**
	 * Retrieve the exception which caused this exception to be rethrown.
	 * 
	 * @return the root cause
	 */
	public Throwable getThrowable()
	{
		return throwable;
	}

}
