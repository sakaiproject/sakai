package org.sakaiproject.profile2.api.exception;

public class ProfilePrototypeNotDefinedException extends RuntimeException {
		
	private static final long serialVersionUID = 1L;
	private final transient String message;
	
	public ProfilePrototypeNotDefinedException(final String message){
		this.message = message;
	}

	public String toString() {
		return ProfilePrototypeNotDefinedException.class + ": " +  message;
	}
}


