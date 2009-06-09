package org.sakaiproject.profile2.exception;

public class ProfileBadConfigurationException extends RuntimeException {
		
	private static final long serialVersionUID = 1L;
	private final transient String message;
	
	public ProfileBadConfigurationException(final String message){
		this.message = message;
	}

	public String toString() {
		return ProfileIllegalAccessException.class + ": " +  message;
	}
}


