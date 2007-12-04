package org.sakaiproject.scorm.exceptions;

public class ValidationException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private String code;
	
	public ValidationException(String code) {
		super();
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
}
