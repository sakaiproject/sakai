package org.sakaiproject.api.app.messageforums.entity;

public class DecoratedTopicInfo{
	private String topicTitle;
	private Long topicId;
	private int unreadMessagesCount = 0;
	private int messagesCount = 0;
	private String typeUuid;
	private Boolean isLocked;
	private Boolean isPostFirst;
	private Boolean isAvailabilityRestricted;
	/**
         * An epoch date in seconds. NOT milliseconds.
         */
        private Long openDate;
        /**
         * An epoch date in seconds. NOT milliseconds.
         */
        private Long closeDate;
        private String gradebookItemName;


	public DecoratedTopicInfo(Long topicId, String topicTitle, int unreadMessagesCount, int messagesCount, String typeUuid){
		this.topicId = topicId;
		this.topicTitle = topicTitle;
		this.unreadMessagesCount = unreadMessagesCount;
		this.messagesCount = messagesCount;
		this.typeUuid = typeUuid;
	}
	
	public DecoratedTopicInfo(Long topicId, String topicTitle, int unreadMessagesCount, 
	        int messagesCount, String typeUuid, Boolean isLocked, Boolean isPostFirst,
	        Boolean isAvailabilityRestricted, Long openDate, Long closeDate, String gradebookItemName) {

	    this(topicId, topicTitle, unreadMessagesCount, messagesCount, typeUuid);
	    this.isLocked = isLocked;
	    this.isPostFirst = isPostFirst;
	    this.isAvailabilityRestricted = isAvailabilityRestricted;
	    this.openDate = openDate;
	    this.closeDate = closeDate;
	    this.gradebookItemName = gradebookItemName;
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

}
