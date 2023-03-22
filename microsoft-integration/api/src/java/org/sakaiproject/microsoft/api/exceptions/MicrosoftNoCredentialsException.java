package org.sakaiproject.microsoft.api.exceptions;

public class MicrosoftNoCredentialsException extends MicrosoftCredentialsException {
	private static final long serialVersionUID = 1L;
	
	public MicrosoftNoCredentialsException() {
		super("error.no_credentials_provided");
	}
}
