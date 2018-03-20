package org.sakaiproject.gradebookng.business.exception;

/**
 * Exception indicating a user does not have access to perform the requested operation
 */
public class GbAccessDeniedException extends Exception {

	private static final long serialVersionUID = 1L;

	public GbAccessDeniedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public GbAccessDeniedException(final String message) {
		super(message);
	}

	public GbAccessDeniedException(final Throwable cause) {
		super(cause);
	}

	public GbAccessDeniedException() {
		super();
	}

}

