package org.sakaiproject.profile2.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
/**
 * MessageThread.java
 * 
 * An object to represent info about a participant in a message thread
 * This is a persistent Hibernate model
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class MessageRecipient implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * autoincrement ID
	 */
	private long id;
	
	/**
	 * message Id
	 */
	private long messageId;
	
	/**
	 * receipient uuid
	 */
	private String to;
	
	/**
	 * Has this message been read by the user?
	 */
	private boolean read;
	
	/**
	 * Has this message been deleted by the user?
	 */
	private boolean deleted;
	
	
	/**
	 * No-arg constructor
	 */
	public MessageRecipient() {
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getMessageId() {
		return messageId;
	}
	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public boolean isRead() {
		return read;
	}
	public void setRead(boolean read) {
		this.read = read;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}


	@Override 
	public String toString() {
		return new ToStringBuilder(this).
			append("id", id).
			append("messageId", messageId).
			append("to", to).
			append("read", read).
			append("deleted", deleted).
			toString();
	}
	
}
