package org.sakaiproject.tool.messageforums.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.component.app.messageforums.MembershipItem;
import org.sakaiproject.component.app.messageforums.ui.DiscussionForumManagerImpl;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
public class DiscussionForumBean
{
  private static final Log LOG = LogFactory
  .getLog(DiscussionForumBean.class);
  private DiscussionForum forum;
  private boolean markForDeletion;
  private UIPermissionsManager uiPermissionsManager;
  private DiscussionForumManager forumManager;
  private boolean readFullDesciption; 
  private List contributorsList = new ArrayList();
  private List accessorList = new ArrayList();
   
   
  /**
   * List of decorated topics
   */
  private List topics = new ArrayList();

  public DiscussionForumBean(DiscussionForum forum, UIPermissionsManager uiPermissionsManager, DiscussionForumManager forumManager)
  {
    if(LOG.isDebugEnabled())
    {
      LOG.debug("DiscussionForumBean(DiscussionForum "+forum+", UIPermissionsManager"  +uiPermissionsManager+ ")");
    }
    this.forum = forum;
    this.uiPermissionsManager=uiPermissionsManager;
    this.forumManager=forumManager; 
  }

  /**
   * @return
   */
  public DiscussionForum getForum()
  {
    LOG.debug("getForum()");
    return forum;
  }

  /**
   * @return Returns the decorated topic.
   */
  public List getTopics()
  {
    LOG.debug("getTopics()");
    return topics;
  }

  public void addTopic(DiscussionTopicBean decoTopic)
  {
    if(LOG.isDebugEnabled())
    {
      LOG.debug("addTopic(DiscussionTopicBean"+ decoTopic+")");
    }
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
    LOG.debug("getLocked()");
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
    LOG.debug("setLocked(String"+ locked+")");
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
    LOG.debug("isMarkForDeletion()");
    return markForDeletion;
  }

  /**
   * @param markForDeletion
   *          The markForDeletion to set.
   */
  public void setMarkForDeletion(boolean markForDeletion)
  {
    if(LOG.isDebugEnabled())
    {
      LOG.debug("setMarkForDeletion(boolean"+ markForDeletion+")");
    }
    this.markForDeletion = markForDeletion;
  }

  /**
   * @return
   */
  public boolean getChangeSettings()
  {
    LOG.debug("getChangeSettings()");
    return uiPermissionsManager.isChangeSettings(forum); 
  }
   
  /**
   * @return
   */
  public boolean isNewTopic()
  {
    LOG.debug("isNewTopic()");
    return uiPermissionsManager.isNewTopic(forum);
  }

  /**
   * @return Returns the if ExtendedDesciption is available
   */
  public boolean getHasExtendedDesciption()
  {
    LOG.debug("getHasExtendedDesciption()");
    if (forum.getExtendedDescription() != null
        && forum.getExtendedDescription().trim().length() > 0
        && (!readFullDesciption))
    {
      return true;
    }
    return false;
  }
  
  /**
   * @return Returns the readFullDesciption.
   */
  public boolean isReadFullDesciption()
  {
    LOG.debug("isReadFullDesciption()");
    return readFullDesciption;
  }

  /**
   * @param readFullDesciption The readFullDesciption to set.
   */
  public void setReadFullDesciption(boolean readFullDesciption)
  {
    if(LOG.isDebugEnabled())
    {
      LOG.debug("setReadFullDesciption(boolean" +readFullDesciption+")");
    }
    this.readFullDesciption = readFullDesciption;
  }
  
  /**
   * @return
   */
  public List getContributorsList()
  {
    LOG.debug("getContributorsList()");
  
    Iterator iter= forumManager.getContributorsList(forum).iterator();
    while (iter.hasNext())
    { 
      contributorsList.add((String)iter.next());
     }
    return contributorsList; 

  }
  
  /**
   * @return
   */
  public List getAccessorList()
  {
    LOG.debug("getAccessorList()");
    Iterator iter= forumManager.getAccessorsList(forum).iterator();
    while (iter.hasNext())
    { 
      accessorList.add((String)iter.next());
     }
    return accessorList; 
  }

  /**
   * @param accessorList The accessorList to set.
   */
  public void setAccessorList(List accessorList)
  {
    if(LOG.isDebugEnabled())
     {
        LOG.debug("setAccessorList(List"+ accessorList+")");
     }
     forum.getActorPermissions().setAccessors(forumManager.decodeAccessorsList(accessorList));
  }

  /**
   * @param contributorsList The contributorsList to set.
   */
  public void setContributorsList(List contributorsList)
  {
    if(LOG.isDebugEnabled())
    {
       LOG.debug("setContributorsList(List"+ contributorsList+")");
    }
    forum.getActorPermissions().setContributors(forumManager.decodeContributorsList(contributorsList));
  }

  /**
   * @param forumManager The forumManager to set.
   */
  public void setForumManager(DiscussionForumManager forumManager)
  {
    if(LOG.isDebugEnabled())
    {
       LOG.debug("setForumManager(DiscussionForumManager"+ forumManager+")");
    }
    this.forumManager = forumManager;
  }

 
  
}
