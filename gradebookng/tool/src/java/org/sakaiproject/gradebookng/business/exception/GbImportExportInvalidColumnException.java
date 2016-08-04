package org.sakaiproject.gradebookng.business.exception;

/**
 * A generic exception that can be thrown from the Import/Export process
 */
public class GbImportExportInvalidColumnException extends GbException {

	private static final long serialVersionUID = 1L;

	public GbImportExportInvalidColumnException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public GbImportExportInvalidColumnException(final String message) {
		super(message);
	}
}
