package org.sakaiproject.tool.messageforums.ui;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
public class DiscussionForumBean
{
  private DiscussionForum forum;
  private boolean markForDeletion;
  private UIPermissionsManager uiPermissionsManager;
   
  /**
   * List of decorated topics
   */
  private List topics = new ArrayList();

  public DiscussionForumBean(DiscussionForum forum, UIPermissionsManager uiPermissionsManager)
  {
    this.forum = forum;
    this.uiPermissionsManager=uiPermissionsManager;
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

  /**
   * @return Returns the locked.
   */
  public String getLocked()
  {
    if (forum == null || forum.getLocked() == null
        || forum.getLocked().booleanValue() == false)
    {
      return Boolean.FALSE.toString();
    }
    return Boolean.TRUE.toString();
  }

  /**
   * @param locked
   *          The locked to set.
   */
  public void setLocked(String locked)
  {
    if (locked.equals(Boolean.TRUE.toString()))
    {
      forum.setLocked(new Boolean(true));
    }
    else
    {
      forum.setLocked(new Boolean(false));
    }
  }

  /**
   * @return Returns the markForDeletion.
   */
  public boolean isMarkForDeletion()
  {
    return markForDeletion;
  }

  /**
   * @param markForDeletion
   *          The markForDeletion to set.
   */
  public void setMarkForDeletion(boolean markForDeletion)
  {
    this.markForDeletion = markForDeletion;
  }

  /**
   * @return
   */
  public boolean getChangeSettings()
  {
    return uiPermissionsManager.isChangeSettings(forum); 
  }
   
  /**
   * @return
   */
  public boolean isNewTopic()
  {
    return uiPermissionsManager.isNewTopic(forum);
  }
}
