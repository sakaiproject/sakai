package uk.ac.lancs.e_science.profile2.api.exception;

public class ProfilePrototypeNotDefinedException extends RuntimeException {
		
	private String message = null;
	
	public ProfilePrototypeNotDefinedException(String message){
		this.message = message;
	}

	public String toString() {
		return ProfilePrototypeNotDefinedException.class + ": " +  message;
	}
}


