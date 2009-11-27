package org.sakaiproject.user.api;

/**
 * Exception thrown when the user cannot be found to perform the authentication.
 * You should be careful about exposing this as it may allow people to enumerate accounts.
 * @author buckett
 *
 */
public class AuthenticationMissingException extends AuthenticationException {

	private Evidence evidence;
	
	public AuthenticationMissingException(Evidence evidence) {
		super();
		this.evidence = evidence;
	}
	
	public Evidence getEvidence() {
		return this.evidence;
	}
}
