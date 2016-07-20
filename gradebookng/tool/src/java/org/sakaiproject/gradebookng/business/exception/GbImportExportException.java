package org.sakaiproject.gradebookng.business.exception;

/**
 * An exception that can be thrown from the Import/Export process
 */
public class GbImportExportException extends Exception {

	private static final long serialVersionUID = 1L;

	public GbImportExportException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public GbImportExportException(final String message) {
		super(message);
	}
}
