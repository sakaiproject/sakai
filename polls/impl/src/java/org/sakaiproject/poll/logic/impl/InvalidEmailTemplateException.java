package org.sakaiproject.poll.logic.impl;

public class InvalidEmailTemplateException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String key;
	private String fileName;
	
	public InvalidEmailTemplateException(String key, String fileName, Throwable e) {
		super.initCause(e);
		this.key = key;
		this.fileName = fileName;
		
	}

	public InvalidEmailTemplateException(String key, String fileName) {
		this.key = key;
		this.fileName = fileName;
		
	}
	public String getKey() {
		return key;
	}

	public String getFileName() {
		return fileName;
	}
	
	
	
}
