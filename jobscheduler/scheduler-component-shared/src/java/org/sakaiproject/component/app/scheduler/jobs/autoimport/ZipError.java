package org.sakaiproject.component.app.scheduler.jobs.autoimport;

import java.io.File;

/**
 * An error that occured with the import.
 * Easiest to just call {@see #toString()}.
 * @author buckett
 *
 */
public class ZipError {
	
	private File file;
	private String error;

	public ZipError(File file, String error) {
		this.file = file;
		this.error = error;
	}
	
	/**
	 * @return The file which had the problem, can be <code>null</code>.
	 */
	public File getFile() {
		return file;
	}
	
	public String getError() {
		return error;
	}
	
	public String toString() {
		return (file == null)?
				"Zipfile problem: "+ error:
				"Error: "+ error+ " with "+ file.getAbsolutePath();
	}
}