package org.sakaiproject.profile2.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
/**
 * MessageThread.java
 * 
 * An object to represent info about a thread of messages
 * This is a persistent Hibernate model
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class MessageThread implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * ID for this thread.
	 */
	private String id;
	
	/**
	 * subject of the thread
	 */
	private String subject;
	
	
	/**
	 * reference to the most recent message in this thread - not persisted.
	 */
	private long mostRecentMessageId;
	
	
	
	/**
	 * No-arg constructor
	 */
	public MessageThread() {
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public long getMostRecentMessageId() {
		return mostRecentMessageId;
	}
	public void setMostRecentMessageId(long mostRecentMessageId) {
		this.mostRecentMessageId = mostRecentMessageId;
	}

	

	@Override 
	public String toString() {
		return new ToStringBuilder(this).
			append("id", id).
			append("subject", subject).
			toString();
	}
	
}
