package org.sakaiproject.microsoft.api.exceptions;

public class MicrosoftInvalidTokenException extends MicrosoftCredentialsException {
	private static final long serialVersionUID = 1L;
	
	public MicrosoftInvalidTokenException() {
		super("error.invalid_token_provided");
	}
}
