package uk.ac.lancs.e_science.profile2.api;

public class ProfileException extends RuntimeException {
	/**
	 * <p>
	 * ProfileException is thrown whenever an attempt is made to access a profile that is not defined
	 * </p>
	 */
	
	private String message = null;
	
	
	public ProfileException(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public String toString() {
		return toString() + " message=" + message;
	}
}


