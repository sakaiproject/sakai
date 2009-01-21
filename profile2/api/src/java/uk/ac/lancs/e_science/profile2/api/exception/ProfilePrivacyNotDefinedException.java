package uk.ac.lancs.e_science.profile2.api.exception;

public class ProfilePrivacyNotDefinedException extends RuntimeException {
		
	private final transient String message;
	
	public ProfilePrivacyNotDefinedException(final String message){
		this.message = message;
	}

	public String toString() {
		return ProfilePrivacyNotDefinedException.class + ": " +  message;
	}
}


