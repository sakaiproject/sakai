package edu.amc.sakai.user;

@SuppressWarnings("serial")
public class InvalidEmailAddressException extends RuntimeException {

	public InvalidEmailAddressException() {
		super();
	}
	public InvalidEmailAddressException(String message) {
		super(message);
	}
	public InvalidEmailAddressException(String message, Throwable cause) {
		super(message,cause);
	}
	public InvalidEmailAddressException(Throwable cause) {
		super(cause);
	}
	
}
