package org.sakaiproject.user.api;

/**
 * Exception thrown when the user cannot be found to perform the authentication.
 * You should be careful about exposing this as it may allow people to enumerate accounts.
 * @author buckett
 *
 */
public class AuthenticationMissingException extends AuthenticationException {

	private final Evidence evidence;
	
	public AuthenticationMissingException(Evidence evidence) {
		super();
		this.evidence = evidence;
	}

    public AuthenticationMissingException(String msg, Evidence evidence) {
        super(msg);
        this.evidence = evidence;
    }

    public AuthenticationMissingException(String message, Throwable cause, Evidence evidence) {
        super(message, cause);
        this.evidence = evidence;
    }

    public AuthenticationMissingException(Throwable cause, Evidence evidence) {
        super(cause);
        this.evidence = evidence;
    }

    public Evidence getEvidence() {
		return this.evidence;
	}
}
