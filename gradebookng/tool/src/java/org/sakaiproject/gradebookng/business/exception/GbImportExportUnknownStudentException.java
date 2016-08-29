package org.sakaiproject.gradebookng.business.exception;

/**
 * An exception that indicates that a student was found in the file but they do not exist in the site
 */
public class GbImportExportUnknownStudentException extends GbException {

	private static final long serialVersionUID = 1L;

	public GbImportExportUnknownStudentException(final String message) {
		super(message);
	}
}
