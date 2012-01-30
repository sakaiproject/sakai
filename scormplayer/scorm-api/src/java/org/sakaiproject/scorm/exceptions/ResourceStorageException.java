package org.sakaiproject.scorm.exceptions;

/**
 * Exception related to an unexpected problem in the underlying storage system 
 * @author roland
 *
 */
public class ResourceStorageException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ResourceStorageException() {
	}

	public ResourceStorageException(String message) {
		super(message);
	}

	public ResourceStorageException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceStorageException(Throwable cause) {
		super(cause);
	}

}
