package org.sakaiproject.gradebookng.business.exception;

/**
 * An exception that indicates that the file type was invalid
 */
public class GbImportExportInvalidFileTypeException extends GbException {

	private static final long serialVersionUID = 1L;

	public GbImportExportInvalidFileTypeException(final String message) {
		super(message);
	}
}
