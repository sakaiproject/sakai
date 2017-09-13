/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.api.app.messageforums.entity;

import java.util.List;

public class DecoratedTopicInfo {
	private String topicTitle;
	private Long topicId;
	private int unreadMessagesCount = 0;
	private int messagesCount = 0;
	private String typeUuid;
	private List<DecoratedAttachment> attachments;
	private String shortDescription;
	private String description;

	private Boolean isLocked;
	private Boolean isDraft;


	private Boolean isPostFirst;
	private Boolean isAvailabilityRestricted;
	private Long openDate; // An epoch date in seconds. NOT milliseconds.
	private Long closeDate; // An epoch date in seconds. NOT milliseconds.
	private String gradebookItemName;
	private Long gradebookItemId;
	

	public DecoratedTopicInfo(Long topicId, String topicTitle,
			int unreadMessagesCount, int messagesCount, String typeUuid,
			List<DecoratedAttachment> attachments, String shortDescription,
			String description) {
		this.topicId = topicId;
		this.topicTitle = topicTitle;
		this.unreadMessagesCount = unreadMessagesCount;
		this.messagesCount = messagesCount;
		this.typeUuid = typeUuid;
		this.attachments = attachments;
		this.shortDescription = shortDescription;
		this.description = description;

	}

	public DecoratedTopicInfo(Long topicId, String topicTitle,
			int unreadMessagesCount, int messagesCount, String typeUuid,
			List<DecoratedAttachment> attachments, String shortDescription,
			String description, Boolean isLocked, Boolean isDraft, Boolean isPostFirst,
			Boolean isAvailabilityRestricted, Long openDate, Long closeDate,
			String gradebookItemName, Long gradebookItemId) {

		this(topicId, topicTitle, unreadMessagesCount, messagesCount, typeUuid,
				attachments, shortDescription, description);
		this.isLocked = isLocked;
		this.isDraft=isDraft;
		this.isPostFirst = isPostFirst;
		this.isAvailabilityRestricted = isAvailabilityRestricted;
		this.openDate = openDate;
		this.closeDate = closeDate;
		this.gradebookItemName = gradebookItemName;
		this.gradebookItemId = gradebookItemId;
	}

	public String getTypeUuid() {
		return typeUuid;
	}

	public void setTypeUuid(String typeUuid) {
		this.typeUuid = typeUuid;
	}

	public Long getTopicId() {
		return topicId;
	}

	public void setTopicId(Long topicId) {
		this.topicId = topicId;
	}

	public String getTopicTitle() {
		return topicTitle;
	}

	public void setTopicTitle(String topicTitle) {
		this.topicTitle = topicTitle;
	}

	public int getUnreadMessagesCount() {
		return unreadMessagesCount;
	}

	public void setUnreadMessagesCount(int unreadMessagesCount) {
		this.unreadMessagesCount = unreadMessagesCount;
	}

	public int getMessagesCount() {
		return messagesCount;
	}

	public void setMessagesCount(int messagesCount) {
		this.messagesCount = messagesCount;
	}

	public Boolean getIsLocked() {
		return isLocked;
	}

	public void setIsLocked(Boolean isLocked) {
		this.isLocked = isLocked;
	}
	public Boolean getIsDraft() {
		return isDraft;
	}
	
	public void setIsDraft(Boolean isDraft) {
		this.isDraft = isDraft;
	}
	
	

	public Boolean getIsPostFirst() {
		return isPostFirst;
	}

	public void setIsPostFirst(Boolean isPostFirst) {
		this.isPostFirst = isPostFirst;
	}

	public Boolean getIsAvailabilityRestricted() {
		return isAvailabilityRestricted;
	}

	public void setIsAvailabilityRestricted(Boolean isAvailabilityRestricted) {
		this.isAvailabilityRestricted = isAvailabilityRestricted;
	}

	public Long getOpenDate() {
		return openDate;
	}

	public void setOpenDate(Long openDate) {
		this.openDate = openDate;
	}

	public Long getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Long closeDate) {
		this.closeDate = closeDate;
	}

	public String getGradebookItemName() {
		return gradebookItemName;
	}

	public void setGradebookItemName(String gradebookItemName) {
		this.gradebookItemName = gradebookItemName;
	}

	public Long getGradebookItemId() {
	    return gradebookItemId;
	}

	public void setGradebookItemId(Long gradebookItemId) {
	    this.gradebookItemId = gradebookItemId;
	}

	public List<DecoratedAttachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<DecoratedAttachment> attachments) {
		this.attachments = attachments;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
