package uk.ac.lancs.e_science.profile2.api.exception;

public class ProfileNotDefinedException extends RuntimeException {
		
	private String message = null;
	
	public ProfileNotDefinedException(String message){
		this.message = message;
	}

	public String toString() {
		return ProfileNotDefinedException.class + ": " +  message;
	}
}


