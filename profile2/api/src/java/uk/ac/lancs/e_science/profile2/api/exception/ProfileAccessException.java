package uk.ac.lancs.e_science.profile2.api.exception;

public class ProfileAccessException extends RuntimeException {
		
	private String message = null;
	
	public ProfileAccessException(String message){
		this.message = message;
	}

	public String toString() {
		return ProfileAccessException.class + ": " +  message;
	}
}


