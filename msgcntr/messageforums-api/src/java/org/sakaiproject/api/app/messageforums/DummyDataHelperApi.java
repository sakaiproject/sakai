package org.sakaiproject.api.app.messageforums;

import java.util.List;

public interface DummyDataHelperApi
{
  public Area getPrivateArea();
  public Area getDiscussionForumArea();
  public boolean isPrivateAreaUnabled();
  public DiscussionForum getForumById(Long forumId);
  public List getMessagesByTopicId(Long topicId);
  public DiscussionTopic getTopicById(Long topicId);
  public boolean hasNextTopic(DiscussionTopic topic);
  public boolean hasPreviousTopic(DiscussionTopic topic);
  public DiscussionTopic getNextTopic(DiscussionTopic topic);
  public DiscussionTopic getPreviousTopic(DiscussionTopic topic);
  public Message getMessageById(Long id);
 
}