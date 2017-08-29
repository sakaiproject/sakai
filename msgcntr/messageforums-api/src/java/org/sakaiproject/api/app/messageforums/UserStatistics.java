/**
 * Copyright (c) 2003-2013 The Apereo Foundation
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
package org.sakaiproject.api.app.messageforums;

import java.util.Date;
import java.util.List;

public class UserStatistics {
	private String siteName;
	private String siteId;
	private String siteUser;
	private String siteUserId;
	private String forumTitle;
	private String topicTitle;
	private Date forumDate;
	private String forumSubject;
	private String message;
	private String msgId;
	private String topicId;
	private Boolean msgDeleted;
	private String forumId;
	private List decoAttachmentsList;

	public UserStatistics(){}
	
	public UserStatistics(String forumTitle, String topicTitle, Date forumDate, String forumSubject,
			String msgId, String topicId, String forumId, String siteUserId){
		this.forumTitle = forumTitle;
		this.topicTitle = topicTitle;
		this.forumDate = forumDate;
		this.forumSubject = forumSubject;
		this.msgId = msgId;
		this.topicId = topicId;
		this.forumId = forumId;
		this.siteUserId = siteUserId;
	}
	
	public String getSiteName(){
		return this.siteName;
	}
	
	public void setSiteName(String newValue){
		this.siteName = newValue;
	}
	
	public String getSiteId(){
		return this.siteId;
	}
	
	public void setSiteId(String newValue){
		this.siteId = newValue;
	}
	
	public String getSiteUser(){
		return this.siteUser;
	}
	
	public void setSiteUser(String newValue){
		this.siteUser = newValue;
	}
	
	public String getSiteUserId(){
		return this.siteUserId;
	}
	
	public void setSiteUserId(String newValue){
		this.siteUserId = newValue;
	}
	
	public String getForumTitle(){
		return this.forumTitle;
	}
	
	public void setForumTitle(String newValue){
		this.forumTitle = newValue;
	}
	
	public Date getForumDate(){
		return forumDate;
	}
	
	public void setForumDate(Date newValue){
		this.forumDate = newValue;
	}
	
	public String getForumSubject(){
		return forumSubject;
	}
	
	public void setForumSubject(String newValue){
		this.forumSubject = newValue;
	}

	public String getTopicTitle() {
		return topicTitle;
	}

	public void setTopicTitle(String topicTitle) {
		this.topicTitle = topicTitle;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getTopicId() {
		return topicId;
	}

	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}

	public Boolean getMsgDeleted() {
		return msgDeleted;
	}

	public void setMsgDeleted(Boolean msgDeleted) {
		this.msgDeleted = msgDeleted;
	}

	public String getForumId() {
		return forumId;
	}

	public void setForumId(String forumId) {
		this.forumId = forumId;
	}

	public List getDecoAttachmentsList() {
		return decoAttachmentsList;
	}

	public void setDecoAttachmentsList(List decoAttachmentsList) {
		this.decoAttachmentsList = decoAttachmentsList;
	}
}
