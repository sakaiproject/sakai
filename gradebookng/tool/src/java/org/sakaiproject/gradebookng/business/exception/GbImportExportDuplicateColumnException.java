package org.sakaiproject.gradebookng.business.exception;

/**
 * An exception that indicates that a duplicate column was present in the file
 */
public class GbImportExportDuplicateColumnException extends GbException {

	private static final long serialVersionUID = 1L;

	public GbImportExportDuplicateColumnException(final String message) {
		super(message);
	}
}
