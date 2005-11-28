package org.sakaiproject.api.app.messageforums;

import java.util.List;

public interface DummyDataHelperApi
{
  public Area getPrivateArea();
  public Area getDiscussionForumArea();
  public boolean isPrivateAreaUnabled();
  public DiscussionForum getForumById(String forumId);
  public List getMessagesByTopicId(String topicId);
}