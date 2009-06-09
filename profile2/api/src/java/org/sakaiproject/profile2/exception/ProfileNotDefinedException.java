package org.sakaiproject.profile2.exception;

public class ProfileNotDefinedException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	private final transient String message;
	
	public ProfileNotDefinedException(final String message){
		this.message = message;
	}

	public String toString() {
		return ProfileNotDefinedException.class + ": " +  message;
	}
}


