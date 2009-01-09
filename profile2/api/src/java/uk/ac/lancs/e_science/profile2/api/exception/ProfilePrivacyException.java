package uk.ac.lancs.e_science.profile2.api.exception;

public class ProfilePrivacyException extends RuntimeException {
		
	private String message = null;
	
	public ProfilePrivacyException(String message){
		this.message = message;
	}

	public String toString() {
		return ProfilePrivacyException.class + ": " +  message;
	}
}


