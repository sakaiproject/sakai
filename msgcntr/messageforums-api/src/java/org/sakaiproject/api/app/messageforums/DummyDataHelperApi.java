package org.sakaiproject.api.app.messageforums;

import java.util.List;

public interface DummyDataHelperApi
{
  public Area getPrivateArea();
  public Area getDiscussionForumArea();
  public boolean isPrivateAreaUnabled();
  public DiscussionForum getForumById(String forumId);
  public List getMessagesByTopicId(String topicId);
  public DiscussionTopic getTopicById(String topicId);
  public boolean hasNextTopic(DiscussionTopic topic);
  public boolean hasPreviousTopic(DiscussionTopic topic);
  public DiscussionTopic getNextTopic(DiscussionTopic topic);
  public DiscussionTopic getPreviousTopic(DiscussionTopic topic);
  public Message getMessageById(String id);
 
}