package uk.ac.lancs.e_science.profile2.api.exception;

public class ProfileIllegalAccessException extends RuntimeException {
		
	private String message = null;
	
	public ProfileIllegalAccessException(String message){
		this.message = message;
	}

	public String toString() {
		return ProfileIllegalAccessException.class + ": " +  message;
	}
}


