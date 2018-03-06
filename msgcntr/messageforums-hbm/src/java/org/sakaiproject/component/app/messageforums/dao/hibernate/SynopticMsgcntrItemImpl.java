/**
 * Copyright (c) 2005-2009 The Apereo Foundation
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
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import java.util.Date;

import org.sakaiproject.api.app.messageforums.SynopticMsgcntrItem;

public class SynopticMsgcntrItemImpl implements SynopticMsgcntrItem{

	protected Long id;
	protected Integer version;
	private String userId;
	private String siteId;
	private String siteTitle;
	private int newMessagesCount = 0;
	private Date messagesLastVisit = new Date();
	private int newForumCount = 0;
	private Date forumLastVisit = new Date();
	private boolean hideItem = false;
	
	public SynopticMsgcntrItemImpl(){}
	
	public SynopticMsgcntrItemImpl(String userId, String siteId, String siteTitle){
		this.userId = userId;
		this.siteId = siteId;
		this.siteTitle = siteTitle;
	}
	
	public void resetNewMessagesCount(){
		newMessagesCount = 0;
	}
	
	public void resetNewForumCount(){
		newForumCount = 0;
	}
	
	public void incrementNewMessagesCount(){
		newMessagesCount++;
	}
	
	public void incrementNewForumCount(){
		newForumCount++;
	}
	
	public void decrementNewMessagesCount(){
		newMessagesCount--;
		if(newMessagesCount < 0)
			newMessagesCount = 0;
	}
	
	public void decrementNewForumCount(){
		newForumCount--;
		if(newForumCount < 0)
			newForumCount = 0;
	}
	
	public void setMessagesLastVisitToCurrentDt(){
		messagesLastVisit = new Date();
	}
	
	public void setForumLastVisitToCurrentDt(){
		forumLastVisit = new Date();
	}
	
	public String getSiteId() {
		return siteId;
	}
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	public String getSiteTitle() {
		return siteTitle;
	}
	public void setSiteTitle(String siteTitle) {
		this.siteTitle = siteTitle;
	}
	public int getNewMessagesCount() {
		return newMessagesCount;
	}
	public void setNewMessagesCount(int newMessagesCount) {
		this.newMessagesCount = newMessagesCount;
	}
	public Date getMessagesLastVisit() {
		return messagesLastVisit;
	}
	public void setMessagesLastVisit(Date messagesLastVisit) {
		this.messagesLastVisit = messagesLastVisit;
	}
	public int getNewForumCount() {
		return newForumCount;
	}
	public void setNewForumCount(int newForumCount) {
		this.newForumCount = newForumCount;
	}
	public Date getForumLastVisit() {
		return forumLastVisit;
	}
	public void setForumLastVisit(Date forumLastVisit) {
		this.forumLastVisit = forumLastVisit;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public boolean isHideItem() {
		return hideItem;
	}

	public void setHideItem(boolean hideItem) {
		this.hideItem = hideItem;
	}
	
}
