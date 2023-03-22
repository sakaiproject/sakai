package org.sakaiproject.microsoft.api.exceptions;

public class MicrosoftInvalidCredentialsException extends MicrosoftCredentialsException {
	private static final long serialVersionUID = 1L;
	
	public MicrosoftInvalidCredentialsException() {
		super("error.invalid_credentials_provided");
	}
}
