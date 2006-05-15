/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/ui/DiscussionForumBean.java $
 * $Id: DiscussionForumBean.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
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
  private ArrayList contributorsList = new ArrayList();
  private ArrayList accessorList = new ArrayList();
  private String gradeAssign;
   
   
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
  public ArrayList getContributorsList()
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
  public ArrayList getAccessorList()
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
  public void setAccessorList(ArrayList accessorList)
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
  public void setContributorsList(ArrayList contributorsList)
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

  public String getGradeAssign()
  {
    return gradeAssign;
  }

  public void setGradeAssign(String gradeAssign)
  {
    this.gradeAssign = gradeAssign;
  }

 
  
}
