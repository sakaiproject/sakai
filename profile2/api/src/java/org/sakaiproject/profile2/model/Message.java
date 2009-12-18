package org.sakaiproject.profile2.model;

import java.io.Serializable;
import java.util.Date;
/**
 * Message.java
 * 
 * A message object to represent a message posted to someone
 * This is a persistent Hibernate model
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class Message implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private String to;
	private String from;
	private String subject;
	private String message;
	private long inReplyTo;
	private boolean read;
	private Date datePosted;
	
	/**
	 * No-arg constructor
	 */
	public Message() {
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	public long getInReplyTo() {
		return inReplyTo;
	}
	public void setInReplyTo(long inReplyTo) {
		this.inReplyTo = inReplyTo;
	}
	public boolean isRead() {
		return read;
	}
	public void setRead(boolean read) {
		this.read = read;
	}
	public Date getDatePosted() {
		return datePosted;
	}
	public void setDatePosted(Date datePosted) {
		this.datePosted = datePosted;
	}
	
	
}
