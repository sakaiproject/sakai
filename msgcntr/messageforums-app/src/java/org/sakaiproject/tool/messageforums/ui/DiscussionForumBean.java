/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/ui/DiscussionForumBean.java $
 * $Id: DiscussionForumBean.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.messageforums.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
public class DiscussionForumBean
{
  private static final Log LOG = LogFactory
  .getLog(DiscussionForumBean.class);
  private DiscussionForum forum;
  private boolean markForDeletion;
  private boolean markForDuplication;
  private UIPermissionsManager uiPermissionsManager;
  private DiscussionForumManager forumManager;
  private boolean readFullDesciption; 
  private ArrayList contributorsList = new ArrayList();
  private ArrayList accessorList = new ArrayList();
  private String gradeAssign;
  private Boolean nonePermission = null;
  
  private Boolean newTopic = null;
  private Boolean changeSettings = null;
  private ArrayList decoAttachList = null;
  private Boolean hasExtendedDescription = null;
  private String locked;
  
  private SimpleDateFormat datetimeFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
   
  private String postFirst = null;
   
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
   * @return Returns count of topics in the forum
   */
  public int getTopicCount()
  {
    LOG.debug("getTopics()");
    return (topics == null) ? 0 : topics.size();
  }

  /**
   * @return List of SelectItem
   */
  public List getTopicSelectItems()
  {
     List f = getTopics();
     int num = (f == null) ? 0 : f.size();

     List retSort = new ArrayList();
     for(int i = 1; i <= num; i++) {
        Integer index = Integer.valueOf(i);
        retSort.add(new SelectItem(index, index.toString()));
     }
     
     return retSort;
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
   * @return Returns the locked as String.
   */
  public String getLocked()
  {
    LOG.debug("getLocked()");
    if (locked == null || "".equals(locked)){
	    if (forum == null || forum.getLocked() == null
	        || forum.getLocked().booleanValue() == false)
	    {
	      locked = Boolean.FALSE.toString();
	    }
	    else
	    {
	    	locked = Boolean.TRUE.toString();
	    }
    }
    return locked;
  }

  /**
   * @param String locked
   *          The locked to set.
   */
  public void setLocked(String locked)
  {
    LOG.debug("setLocked(String"+ locked+")");
    if (locked.equals(Boolean.TRUE.toString()))
    {
      forum.setLocked(Boolean.valueOf(true));
    }
    else
    {
      forum.setLocked(Boolean.valueOf(false));
    }
  }
  
  /**
   * @return Returns the locked as boolean
   */
  public Boolean getForumLocked()
  {
    LOG.debug("getForumLocked()");
    if (locked == null || "".equals(locked)){
	    if (forum == null || forum.getLocked() == null
	        || forum.getLocked().booleanValue() == false)
	    {
	      locked = Boolean.FALSE.toString();
	    }
	    else
	    {
	    	locked = Boolean.TRUE.toString();
	    }
    }
    return Boolean.parseBoolean(locked);
  }

  /**
   * @param Boolean locked
   *          The locked to set.
   */
  public void setForumLocked(Boolean locked)
  {
    LOG.debug("setForumLocked(String"+ locked+")");
    forum.setLocked(locked);
  }
  
  private String moderated = null;
  /**
   * Returns whether the forum is moderated or not
   * @return
   */
  public String getModerated()
  {
	  LOG.debug("getModerated()");
	  if (moderated == null){
		  if (forum == null || forum.getModerated() == null || 
			  forum.getModerated().booleanValue() == false)
		  {
			  moderated = Boolean.FALSE.toString();
		  }
		  else
		  {
			  moderated = Boolean.TRUE.toString();
		  }
	  }
	  return moderated;
  }
  
  /**
   * Set the "moderated" setting for the forum
   * @param moderated
   */
  public void setModerated(String moderated)
  {
	  LOG.debug("setModerated()");
	  if (moderated.equals(Boolean.TRUE.toString()))
	  {
		  forum.setModerated(Boolean.valueOf(true));
	  }
	  else
	  {
		  forum.setModerated(Boolean.valueOf(false));
	  }
  }
  
  /**
   * Returns boolean value, whether the forum is moderated or not
   * @return
   */
  public Boolean getForumModerated()
  {
	  LOG.debug("getForumModerated()");
	  if (moderated == null){
		  if (forum == null || forum.getModerated() == null || 
			  forum.getModerated().booleanValue() == false)
		  {
			  moderated = Boolean.FALSE.toString();
		  }
		  else
		  {
			  moderated = Boolean.TRUE.toString();
		  }
	  }
	  return Boolean.parseBoolean(moderated);
  }
  
  /**
   * Set the boolean "moderated" setting for the forum
   * @param moderated
   */
  public void setForumModerated(Boolean moderated)
  {
	  LOG.debug("setForumModerated()");
	  forum.setModerated(moderated);
  }
  
  /**
   * Returns whether the forum is postFirst or not
   * @return
   */
  public String getPostFirst()
  {
	  LOG.debug("getPostFirst()");
	  if (postFirst == null){
		  if (forum == null || forum.getPostFirst() == null || 
			  forum.getPostFirst().booleanValue() == false)
		  {
			  postFirst = Boolean.FALSE.toString();
		  }
		  else
		  {
			  postFirst = Boolean.TRUE.toString();
		  }
	  }
	  return postFirst;
  }
  
  /**
   * Set the "postFirst" setting for the forum
   * @param postFirst
   */
  public void setPostFirst(String postFirst)
  {
	  LOG.debug("setPostFirst()");
	  if (postFirst.equals(Boolean.TRUE.toString()))
	  {
		  forum.setPostFirst(Boolean.valueOf(true));
	  }
	  else
	  {
		  forum.setPostFirst(Boolean.valueOf(false));
	  }
  }
  
  /**
   * Returns a boolean, whether the forum is postFirst or not
   * @return
   */
  public Boolean getForumPostFirst()
  {
	  LOG.debug("getForumPostFirst()");
	  if (postFirst == null){
		  if (forum == null || forum.getPostFirst() == null || 
			  forum.getPostFirst().booleanValue() == false)
		  {
			  postFirst = Boolean.FALSE.toString();
		  }
		  else
		  {
			  postFirst = Boolean.TRUE.toString();
		  }
	  }
	  return Boolean.parseBoolean(postFirst);
  }
  
  /**
   * Set the boolean "postFirst" setting for the forum
   * @param postFirst
   */
  public void setForumPostFirst(Boolean postFirst)
  {
	  LOG.debug("setForumPostFirst()");
	  forum.setPostFirst(postFirst);
  }

  /**
   * Return whether or not the forum automatically marks all posts in a topic as read.
   */
  public String getAutoMarkThreadsRead()
  {
	  LOG.debug("getAutoMarkThreadsRead()");
	  return Boolean.toString(forum.getAutoMarkThreadsRead());
  }
  
  /**
   * Set the autoMarkThreadsRead setting for the forum.
   */
  public void setAutoMarkThreadsRead(String autoMarkThreadsRead)
  {
	  LOG.debug("setAutoMarkThreadsRead()");
	  forum.setAutoMarkThreadsRead(Boolean.parseBoolean(autoMarkThreadsRead));
  }
  
  /**
   * Return boolean, whether or not the forum automatically marks all posts in a topic as read.
   */
  public Boolean getForumAutoMarkThreadsRead()
  {
	  LOG.debug("getForumAutoMarkThreadsRead()");
	  return forum.getAutoMarkThreadsRead();
  }
  
  /**
   * Set the boolean autoMarkThreadsRead setting for the forum.
   */
  public void setForumAutoMarkThreadsRead(Boolean autoMarkThreadsRead)
  {
	  LOG.debug("setForumAutoMarkThreadsRead()");
	  forum.setAutoMarkThreadsRead(autoMarkThreadsRead);
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
   * @return Returns the markForDuplication.
   */
  public boolean isMarkForDuplication()
  {
    LOG.debug("isMarkForDuplication()");
    return markForDuplication;
  }

  /**
   * @param markForDuplication
   *          The markForDuplication to set.
   */
  public void setMarkForDuplication(boolean markForDuplication)
  {
    if(LOG.isDebugEnabled())
    {
      LOG.debug("setMarkForDuplication(boolean"+ markForDuplication+")");
    }
    this.markForDuplication = markForDuplication;
  }

  /**
   * @return
   */
  public boolean getChangeSettings()
  {
    LOG.debug("getChangeSettings()");
    if (changeSettings == null){
    	changeSettings = uiPermissionsManager.isChangeSettings(forum);
    }
    return changeSettings.booleanValue();
  }
   
  /**
   * @return
   */
  public boolean isNewTopic()
  {
    LOG.debug("isNewTopic()");
    if (newTopic == null){
    	newTopic = uiPermissionsManager.isNewTopic(forum);
    }
    return newTopic.booleanValue();
  }

  /**
   * @return Returns the if ExtendedDesciption is available
   */
  public boolean getHasExtendedDesciption()
  {
    LOG.debug("getHasExtendedDesciption()");
    if (hasExtendedDescription == null){
	    if (forum.getExtendedDescription() != null
	        && forum.getExtendedDescription().trim().length() > 0
	        && (!readFullDesciption))
	    {
	      hasExtendedDescription = true;
	    }
	    hasExtendedDescription = false;
    }
    return hasExtendedDescription.booleanValue();
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
  
	public boolean getNonePermission()
	{
		if (nonePermission == null){
			nonePermission = true;
			if(uiPermissionsManager.isChangeSettings(forum) ||  uiPermissionsManager.isNewTopic(forum))
			{
				nonePermission = false;
				return nonePermission.booleanValue();
			}
			
			if(topics != null)
			{
				for(int i=0; i<topics.size(); i++)
				{
					DiscussionTopicBean dtb = (DiscussionTopicBean) topics.get(i);
					if(!dtb.getNonePermission())
					{
						nonePermission = false;
						break;
					}
				}
			}
		}
		return nonePermission.booleanValue();
	}

	public void setNonePermission(boolean nonePermission)
	{
		this.nonePermission = nonePermission;
	}
	
	public ArrayList getAttachList()
	{
		if (decoAttachList == null){
			decoAttachList = new ArrayList();
			List attachList = forum.getAttachments(); 
			if(attachList != null)
			{
				for(int i=0; i<attachList.size(); i++)
				{
					DecoratedAttachment decoAttach = new DecoratedAttachment((Attachment)attachList.get(i));
					decoAttachList.add(decoAttach);
				}
			}
		}
		return decoAttachList;
	}
	
	public String getAvailabilityRestricted()
	  {
		  LOG.debug("getAvailabilityRestricted()");
		  if (forum == null || forum.getAvailabilityRestricted() == null || 
				  forum.getAvailabilityRestricted().booleanValue() == false)
		  {
			  return Boolean.FALSE.toString();
		  }

		  return Boolean.TRUE.toString();
	  }
	  
	  /**
	   * Set the "availabilityRestricted" setting for the forum
	   * @param restricted
	   */
	  public void setAvailabilityRestricted(String restricted)
	  {
		  LOG.debug("setAvailabilityRestricted()");
		  if (restricted.equals(Boolean.TRUE.toString()))
		  {
			  forum.setAvailabilityRestricted(Boolean.valueOf(true));
		  }
		  else
		  {
			  forum.setAvailabilityRestricted(Boolean.valueOf(false));
		  }
	  }
	
	public String getAvailability()
	{
		LOG.debug("getAvailability()");
		if (forum == null || forum.getAvailability() == null || 
				forum.getAvailability().booleanValue() == false)
		{
			return Boolean.FALSE.toString();
		}

		return Boolean.TRUE.toString();
	}

	/**
	 * Set the "Availability" setting for the forum
	 * @param restricted
	 */
	public void setAvailability(String restricted)
	{
		LOG.debug("setAvailability()");
		if (restricted.equals(Boolean.TRUE.toString()))
		{
			forum.setAvailability(Boolean.valueOf(true));
		}
		else
		{
			forum.setAvailability(Boolean.valueOf(false));
		}
	}

	public String getOpenDate(){
		if(forum == null || forum.getOpenDate() == null){
			return "";
		}else{
			StringBuilder dateTimeOpenDate = new StringBuilder( datetimeFormat.format( forum.getOpenDate() ) );			
			return dateTimeOpenDate.toString();
		}
	}	  

	public void setOpenDate(String openDateStr){
		if(!"".equals(openDateStr) && openDateStr != null){
			try{
				Date openDate = (Date) datetimeFormat.parse(openDateStr);
				forum.setOpenDate(openDate);
			}catch (ParseException e) {
				LOG.error("Couldn't convert open date", e);
			}
		}else{
			forum.setOpenDate(null);
		}
	}

	public String getCloseDate(){
		if(forum == null || forum.getCloseDate() == null){
			return "";
		}else{
			StringBuilder dateTimeCloseDate = new StringBuilder( datetimeFormat.format( forum.getCloseDate() ) );
			return dateTimeCloseDate.toString();
		}
	}	  

	public void setCloseDate(String closeDateStr){
		if(!"".equals(closeDateStr) && closeDateStr != null){
			try{
				Date CloseDate = (Date) datetimeFormat.parse(closeDateStr);
				forum.setCloseDate(CloseDate);
			}catch (ParseException e) {
				LOG.error("Couldn't convert Close date", e);
			}
		}else{
			forum.setCloseDate(null);
		}
	}
}
