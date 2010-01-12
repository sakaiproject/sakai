package org.sakaiproject.profile2.model;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
/**
 * Message.java
 * 
 * An object to represent a message posted to someone
 * This is a persistent Hibernate model
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class Message implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * autoincrement ID for this message
	 */
	private long id;
	
	/**
	 * uuid to
	 */
	private String to;
	
	/**
	 * uuid from
	 */
	private String from;
	
	/**
	 * body of the message
	 */
	private String message;
	
	/**
	 * what thread ID this message is associated with
	 */
	private String thread;
	
	/**
	 * Has this message been read?
	 */
	private boolean read;
	
	/**
	 * date this message was posted
	 */
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
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getThread() {
		return thread;
	}
	public void setThread(String thread) {
		this.thread = thread;
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
	
	@Override 
	public String toString() {
		return new ToStringBuilder(this).
			append("id", id).
			append("to", to).
			append("from", from).
			append("message", message).
			append("thread", thread).
			append("read", read).
			append("datePosted", datePosted).
			toString();
	   }
	
}
