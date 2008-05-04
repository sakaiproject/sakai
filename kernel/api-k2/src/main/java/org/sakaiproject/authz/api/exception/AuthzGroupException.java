/**
 * 
 */
package org.sakaiproject.authz.api.exception;

/**
 * @author ieb
 *
 */
public class AuthzGroupException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6871539598516944120L;

	/**
	 * 
	 */
	public AuthzGroupException() {
	}

	/**
	 * @param arg0
	 */
	public AuthzGroupException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public AuthzGroupException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public AuthzGroupException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
