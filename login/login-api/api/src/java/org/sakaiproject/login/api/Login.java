package org.sakaiproject.login.api;

public interface Login {

	/**
	 * The default login name if none is specified.
	 */
	public static final String DEFAULT_LOGIN_CONTEXT = "default";
	
	public static final String EXCEPTION_INVALID_CREDENTIALS = "invalid-credentials";
	
	public static final String EXCEPTION_MISSING_CREDENTIALS = "missing-credentials";
	
	public static final String EXCEPTION_INVALID_WITH_PENALTY = "invalid-credentials-with-penalty";
	
	public static final String EXCEPTION_INVALID = "invalid";
	
}
