package org.sakaiproject.profile2.api.exception;

public class ProfileIllegalAccessException extends RuntimeException {
		
	private static final long serialVersionUID = 1L;
	private final transient String message;
	
	public ProfileIllegalAccessException(final String message){
		this.message = message;
	}

	public String toString() {
		return ProfileIllegalAccessException.class + ": " +  message;
	}
}


