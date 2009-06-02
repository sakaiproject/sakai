package org.sakaiproject.profile2.api.exception;

public class ProfilePreferencesNotDefinedException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	private final transient String message;
	
	public ProfilePreferencesNotDefinedException(final String message){
		this.message = message;
	}

	public String toString() {
		return ProfilePreferencesNotDefinedException.class + ": " +  message;
	}
}


