package org.sakaiproject.gradebookng.business.exception;

/**
 * An exception that methods can throw to indicate something went wrong. The message will give more detail.
 * TODO clean this up, make it checked
 */
public class GbException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GbException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public GbException(final Throwable cause) {
		super(cause);
	}

	public GbException(final String message) {
		super(message);
	}

	public GbException() {
		super();
	}

}
