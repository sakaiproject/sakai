package org.sakaiproject.tool.messageforums.ui;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.api.app.messageforums.DiscussionForum;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 *
 */
public class DiscussionForumBean
{
  private DiscussionForum forum;

  /**
   * List of decorated topics
   */
  private List topics = new ArrayList();

  public DiscussionForumBean(DiscussionForum forum)
  {
    this.forum = forum;
  }

  /**
   * @return
   */
  public DiscussionForum getForum()
  {
    return forum;
  }

  /**
   * @return Returns the decorated topic.
   */
  public List getTopics()
  {
    return topics;
  }

  public void addTopic(DiscussionTopicBean decoTopic)
  {
    if (!topics.contains(decoTopic))
    {
      topics.add(decoTopic);
    }
  }
}
