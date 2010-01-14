/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
public class MessageParticipant implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * autoincrement ID
	 */
	private long id;
	
	/**
	 * message Id (a uuid)
	 */
	private String messageId;
	
	/**
	 * participant uuid
	 */
	private String uuid;
	
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
	public MessageParticipant() {
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
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
			append("uuid", uuid).
			append("read", read).
			append("deleted", deleted).
			toString();
	}
	
}
