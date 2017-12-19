/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.messageforums.entityproviders.sparsepojos;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.Message;

import lombok.Getter;
import lombok.Setter;

public class SparseMessage{
	
	@Getter
	private Long messageId;
	
	@Getter
	private Long topicId;
	
	@Getter
	private String title;
	
	@Getter
	private String body;
	
	@Getter
	private Long lastModified;
	
	@Getter @Setter
	private List<SparseAttachment> attachments = new ArrayList<SparseAttachment>();
	
	@Getter @Setter
	private List<SparseMessage> replies = new ArrayList<SparseMessage>();
	
	@Getter
	private String authoredBy;
	
	@Getter
	private String authorId;
	
	@Getter @Setter
	private int indentIndex = 0;
	
	@Getter
	private Long replyTo;
	
	@Getter
	private Long createdOn;
	
	@Getter @Setter
	private boolean read;
	
	@Getter @Setter
	private Integer totalMessages = 0;
	
	@Getter @Setter
	private Integer readMessages = 0;
	
	@Getter
	private boolean isDraft;
	
	@Getter
	private boolean isDeleted;

	@Getter @Setter
	private String modifiedBy;

	@Getter @Setter
	private String createdBy;

	//this is used for displaying recent messages in the lessons
	private Long forumId;
	 
	public SparseMessage(Message fatMessage, Boolean readStatus, boolean addAttachments, String serverUrl) {
		
		super();
		
		this.messageId = fatMessage.getId();
		this.topicId = fatMessage.getTopic().getId();
		this.title = fatMessage.getTitle();
		this.body = fatMessage.getBody();
		this.lastModified = fatMessage.getModified().getTime()/1000;
		this.authoredBy = fatMessage.getAuthor();
		this.authorId = fatMessage.getAuthorId();
		this.isDraft = fatMessage.getDraft();
		this.isDeleted = fatMessage.getDeleted();
		
		Message parent = fatMessage.getInReplyTo();
		if(parent != null) {
			this.replyTo = parent.getId();
		}
		
		this.createdOn = fatMessage.getCreated().getTime()/1000;
		this.read = readStatus;
		
		if(addAttachments && fatMessage.getHasAttachments()) {
			List<SparseAttachment> sparseAttachments = new ArrayList<SparseAttachment>();
			for(Attachment fatAttachment : (List<Attachment>)fatMessage.getAttachments()) {
				String url = serverUrl + "/access/content" + fatAttachment.getAttachmentId();
				attachments.add(new SparseAttachment(fatAttachment.getAttachmentName(),url));
			}
		}
		this.modifiedBy = fatMessage.getModifiedBy();
		this.createdBy = fatMessage.getCreatedBy();
	}
	
	public SparseMessage(SparseMessage that) {
		
		super();
		
		this.messageId = that.getMessageId();
		this.topicId = that.getTopicId();
		this.title = that.getTitle();
		this.body = that.getBody();
		this.lastModified = that.getLastModified();
		this.authoredBy = that.getAuthoredBy();
		this.authorId = that.getAuthorId();
		this.isDraft = that.isDraft();
		this.isDeleted = that.isDeleted();
		
		this.replyTo = that.getReplyTo();
		
		this.createdOn = that.getCreatedOn();
		this.read = that.isRead();
		this.attachments = that.getAttachments();
	}
	
	public void addReply(SparseMessage reply) {
		
		if(replies == null) {
			replies = new ArrayList<SparseMessage>();
		}
		replies.add(reply);
	}
	public Long getForumId() {
		return forumId;
	}

	public void setForumId(Long forumId) {
		this.forumId = forumId;
	}
}
