package org.sakaiproject.gradebookng.business.exception;

/**
 * An exception that indicates that an invalid column was present in the file
 */
public class GbImportExportInvalidColumnException extends GbException {

	private static final long serialVersionUID = 1L;

	public GbImportExportInvalidColumnException(final String message) {
		super(message);
	}
}
