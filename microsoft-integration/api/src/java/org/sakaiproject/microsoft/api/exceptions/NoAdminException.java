package org.sakaiproject.microsoft.api.exceptions;

public class NoAdminException extends MicrosoftGenericException {
	private static final long serialVersionUID = 1L;
	
	public NoAdminException() {
		super("error.no_admin");
	}
}
