package org.sakaiproject.gradebookng.business.exception;

/**
 * An exception that methods can throw to indicate something went wrong.
 * The message will give more detail.
 */
public class GbException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public GbException(String message, Throwable cause) {
		super(message, cause);
	}
}
