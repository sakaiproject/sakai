package org.sakaiproject.profile2.model;

import java.io.Serializable;

/**
 * A helper model that contains all fields that a new message should contain. 
 * This is not persisted - it is separated out into its constituent parts instead.
 * 
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 */
public class NewMessageHelper implements Serializable {

	private static final long serialVersionUID = 1L;
	private String to;
	private String from;
	private String subject;
	private String message;
	
	/**
	 * No arg constructor
	 */
	public NewMessageHelper() {
	}

	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
