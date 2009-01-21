package uk.ac.lancs.e_science.profile2.api.exception;

public class ProfilePrototypeNotDefinedException extends RuntimeException {
		
	private final transient String message;
	
	public ProfilePrototypeNotDefinedException(final String message){
		this.message = message;
	}

	public String toString() {
		return ProfilePrototypeNotDefinedException.class + ": " +  message;
	}
}


