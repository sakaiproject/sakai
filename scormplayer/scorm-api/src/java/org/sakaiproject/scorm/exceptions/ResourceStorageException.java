package org.sakaiproject.scorm.exceptions;

/**
 * Exception related to an unexpected problem in the underlying storage system 
 * @author roland
 *
 */
public class ResourceStorageException extends RuntimeException {

	public ResourceStorageException() {
	}

	public ResourceStorageException(String message) {
		super(message);
	}

	public ResourceStorageException(Throwable cause) {
		super(cause);
	}

	public ResourceStorageException(String message, Throwable cause) {
		super(message, cause);
	}

}
