package org.sakaiproject.scorm.exceptions;

public class ResourceNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String path) {
		super(path);
	}
	
}
