package uk.ac.lancs.e_science.profile2.api.exception;

public class ProfileMismatchException extends RuntimeException {
		
	private static final long serialVersionUID = 1L;
	private final transient String message;
	
	public ProfileMismatchException(final String message){
		this.message = message;
	}

	public String toString() {
		return ProfilePrototypeNotDefinedException.class + ": " +  message;
	}
}


