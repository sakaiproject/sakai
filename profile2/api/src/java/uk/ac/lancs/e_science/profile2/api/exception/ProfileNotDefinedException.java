package uk.ac.lancs.e_science.profile2.api.exception;

public class ProfileNotDefinedException extends RuntimeException {
		
	private final transient String message;
	
	public ProfileNotDefinedException(final String message){
		this.message = message;
	}

	public String toString() {
		return ProfileNotDefinedException.class + ": " +  message;
	}
}


