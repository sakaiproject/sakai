package org.sakaiproject.gradebookng.business.exception;

/**
 * An exception indicating that a comment column was imported but it was missing the corresponding gb item
 */
public class GbImportCommentMissingItemException extends GbException {

	private static final long serialVersionUID = 1L;

	public GbImportCommentMissingItemException(final String message) {
		super(message);
	}
}
