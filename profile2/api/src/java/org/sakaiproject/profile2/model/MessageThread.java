package org.sakaiproject.profile2.model;

import java.io.Serializable;
import java.util.List;

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
	private long id;
	
	/**
	 * subject of the thread
	 */
	private String subject;
	
	/**
	 * the most recent message in this thread - not persisted.
	 */
	private Message mostRecentMessage;
	
	
	
	/**
	 * No-arg constructor
	 */
	public MessageThread() {
	}
	
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}


	public Message getMostRecentMessage() {
		return mostRecentMessage;
	}
	public void setMostRecentMessage(Message mostRecentMessage) {
		this.mostRecentMessage = mostRecentMessage;
	}
	

	@Override 
	public String toString() {
		return new ToStringBuilder(this).
			append("id", id).
			append("subject", subject).
			append("message", mostRecentMessage.toString()).
			toString();
	}
	
}
