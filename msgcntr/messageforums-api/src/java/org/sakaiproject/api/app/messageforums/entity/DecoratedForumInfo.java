package org.sakaiproject.api.app.messageforums.entity;

import java.util.ArrayList;
import java.util.List;

public class DecoratedForumInfo{

	private Long forumId;
	private String forumTitle;
	private List<DecoratedTopicInfo> topics = new ArrayList<DecoratedTopicInfo>();


	public Long getForumId() {
		return forumId;
	}


	public void setForumId(Long forumId) {
		this.forumId = forumId;
	}


	public String getForumTitle() {
		return forumTitle;
	}


	public void setForumTitle(String forumTitle) {
		this.forumTitle = forumTitle;
	}


	public List<DecoratedTopicInfo> getTopics() {
		return topics;
	}


	public void setTopics(List<DecoratedTopicInfo> topics) {
		this.topics = topics;
	}


	public DecoratedForumInfo(Long forumId, String forumTitle){
		this.forumId = forumId;
		this.forumTitle = forumTitle;
	}

	public void addTopic(DecoratedTopicInfo topic){
		this.topics.add(topic);
	}
}
