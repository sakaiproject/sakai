package org.sakaiproject.scorm.exceptions;

public class LearnerNotDefinedException extends Exception {

	private static final long serialVersionUID = 1L;

	public LearnerNotDefinedException() {
	}

	public LearnerNotDefinedException(String message) {
		super(message);
	}

	public LearnerNotDefinedException(Throwable cause) {
		super(cause);
	}

	public LearnerNotDefinedException(String message, Throwable cause) {
		super(message, cause);
	}

}
