package org.sakaibrary.xserver;

public class XServerException extends Exception {

	private static final long serialVersionUID = 1L;

	// errorCode holds error_code from X-server
	private String errorCode;
	
	/**
	 * Constructs a new MetaLibException with given errorCode and errorText
	 * 
	 * @param errorCode String representing error_code sent from X-server
	 * @param errorText String representing error_text sent from X-server
	 */
	public XServerException( String errorCode, String errorText ) {
		super( errorText );
		
		this.errorCode = errorCode;
	}
	
	/**
	 * Gets error_text sent from X-server
	 * 
	 * @return String representing error_text
	 */
	public String getErrorText() {
		return getMessage();
	}

	/**
	 * Gets error_code sent from X-server
	 * 
	 * @return String representing error_code
	 */
	public String getErrorCode() {
		return errorCode;
	}
}
