package org.sakaiproject.api.app.messageforums.entity;

public class DecoratedTopicInfo{
	private String topicTitle;
	private Long topicId;
	private int unreadMessagesCount = 0;
	private int messagesCount = 0;
	private String typeUuid;
	
	public String getTypeUuid() {
		return typeUuid;
	}

	public void setTypeUuid(String typeUuid) {
		this.typeUuid = typeUuid;
	}

	public DecoratedTopicInfo(Long topicId, String topicTitle, int unreadMessagesCount, int messagesCount, String typeUuid){
		this.topicId = topicId;
		this.topicTitle = topicTitle;
		this.unreadMessagesCount = unreadMessagesCount;
		this.messagesCount = messagesCount;
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

}
