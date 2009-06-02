package uk.ac.lancs.e_science.profile2.api.exception;

public class ProfileFriendsIllegalAccessException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;	
	private final transient String message;
	
	public ProfileFriendsIllegalAccessException(final String message){
		this.message = message;
	}

	public String toString() {
		return ProfileFriendsIllegalAccessException.class + ": " +  message;
	}
}


