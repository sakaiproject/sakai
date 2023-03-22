package org.sakaiproject.microsoft.api.exceptions;

public class MicrosoftInvalidInvitationException extends MicrosoftGenericException {
	private static final long serialVersionUID = 1L;
	
	public MicrosoftInvalidInvitationException() {
		super("error.invalid_invitation");
	}
}
