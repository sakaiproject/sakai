package org.sakaiproject.microsoft.api.exceptions;

public class MicrosoftInvalidTeamException extends MicrosoftGenericException {
	private static final long serialVersionUID = 1L;
	
	public MicrosoftInvalidTeamException() {
		super("error.invalid_team");
	}
}
