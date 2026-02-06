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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.ResourceLoader;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
@Slf4j
@Getter @Setter
public class DiscussionForumBean
{
  private DiscussionForum forum;
  private boolean markForDeletion;
  private boolean markForDuplication;
  private DiscussionForumManager forumManager;
  private boolean readFullDesciption; 
  private List<String> contributorsList = null;
  private List<String> accessorList = null;
  private String gradeAssign;
  private Boolean nonePermission = null;
  private boolean createTask = false;
  
  private boolean newTopic = false;
  private boolean changeSettings = false;
  private List<DecoratedAttachment> decoAttachList = new ArrayList<>();
  private Boolean hasExtendedDescription = null;
  private String locked;
  
  private SimpleDateFormat datetimeFormat;
  private UserTimeService userTimeService;
  
  private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";
  private static final ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);    
   
  private static RubricsService rubricsService = ComponentManager.get(RubricsService.class);

  private SimpleDateFormat ourDateFormat() {
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      df.setTimeZone(userTimeService.getLocalTimeZone());
      return df;
  }

  private String postFirst = null;
   
  private List<DiscussionTopicBean> topics = new ArrayList<>();

  public DiscussionForumBean(DiscussionForum forum, DiscussionForumManager forumManager, UserTimeService userTimeService) {
    log.debug("DiscussionForumBean(DiscussionForum {})", forum);
    this.forum = forum;
    this.forumManager = forumManager;
    this.userTimeService = userTimeService;
    datetimeFormat = ourDateFormat();
  }

    /**
   * @return Returns count of topics in the forum
   */
  public int getTopicCount()
  {
    log.debug("getTopics()");
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

  public void addTopic(DiscussionTopicBean decoTopic)
  {
    if(log.isDebugEnabled())
    {
      log.debug("addTopic(DiscussionTopicBean"+ decoTopic+")");
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
    log.debug("getLocked()");
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

    handleLockedAfterClosedCondition();

    return locked;
  }

  /**
   * @param String locked
   *          The locked to set.
   */
  public void setLocked(String locked)
  {
    log.debug("setLocked(String"+ locked+")");
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
    log.debug("getForumLocked()");
    return forum != null && forum.getLocked() != null && forum.getLocked();
  }

  /**
   * @param Boolean locked
   *          The locked to set.
   */
  public void setForumLocked(Boolean locked)
  {
    log.debug("setForumLocked(String"+ locked+")");
    forum.setLocked(locked);
    this.locked = String.valueOf(Boolean.TRUE.equals(locked));
  }

  private void handleLockedAfterClosedCondition() {
    Boolean availabilityRestricted = getForum().getAvailabilityRestricted();

    if (Boolean.TRUE.equals(availabilityRestricted) && Boolean.FALSE.toString().equals(locked)) {
      Date closeDate = getForum().getCloseDate();
      if(closeDate != null) {
        if (Boolean.TRUE.equals(getForum().getLockedAfterClosed()) && closeDate.before(new Date())) {
          locked = Boolean.TRUE.toString();
        }
      }
    }
  }
  
  private String moderated = null;
  /**
   * Returns whether the forum is moderated or not
   * @return
   */
  public String getModerated()
  {
	  log.debug("getModerated()");
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
	  log.debug("setModerated()");
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
	  log.debug("getForumModerated()");
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
	  log.debug("setForumModerated()");
	  forum.setModerated(moderated);
  }
  
  /**
   * Returns whether the forum is postFirst or not
   * @return
   */
  public String getPostFirst()
  {
	  log.debug("getPostFirst()");
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
	  log.debug("setPostFirst()");
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
	  log.debug("getForumPostFirst()");
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
	  log.debug("setForumPostFirst()");
	  forum.setPostFirst(postFirst);
  }

  /**
   * Return whether or not the forum automatically marks all posts in a topic as read.
   */
  public String getAutoMarkThreadsRead()
  {
	  log.debug("getAutoMarkThreadsRead()");
	  return Boolean.toString(forum.getAutoMarkThreadsRead());
  }
  
  /**
   * Set the autoMarkThreadsRead setting for the forum.
   */
  public void setAutoMarkThreadsRead(String autoMarkThreadsRead)
  {
	  log.debug("setAutoMarkThreadsRead()");
	  forum.setAutoMarkThreadsRead(Boolean.parseBoolean(autoMarkThreadsRead));
  }
  
  /**
   * Return boolean, whether or not the forum automatically marks all posts in a topic as read.
   */
  public Boolean getForumAutoMarkThreadsRead()
  {
	  log.debug("getForumAutoMarkThreadsRead()");
	  return forum.getAutoMarkThreadsRead();
  }
  
  /**
   * Set the boolean autoMarkThreadsRead setting for the forum.
   */
  public void setForumAutoMarkThreadsRead(Boolean autoMarkThreadsRead)
  {
	  log.debug("setForumAutoMarkThreadsRead()");
	  forum.setAutoMarkThreadsRead(autoMarkThreadsRead);
  }

  /**
   * Return whether or not the forum will use specific group permissions.
   */
  public String getRestrictPermissionsForGroups()
  {
	  log.debug("getRestrictPermissionsForGroups()");
	  return Boolean.toString(forum.getRestrictPermissionsForGroups());
  }
  
  /**
   * Set the restrictPermissionsForGroups setting for the forum.
   */
  public void setRestrictPermissionsForGroups(String restrictPermissionsForGroups)
  {
	  log.debug("setRestrictPermissionsForGroups()");
	  forum.setRestrictPermissionsForGroups(Boolean.parseBoolean(restrictPermissionsForGroups));
  }

  /**
   * @return Returns the if ExtendedDesciption is available
   */
  public boolean getHasExtendedDesciption()
  {
    log.debug("getHasExtendedDesciption()");
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
    log.debug("isReadFullDesciption()");
    return readFullDesciption;
  }

    public List<String> getContributorsList() {
        log.debug("getContributorsList()");
        if (contributorsList == null) {
            contributorsList = forumManager.getContributorsList(forum);
        }
        return contributorsList;
    }

    public List<String> getAccessorList() {
        log.debug("getAccessorList()");
        if (accessorList == null) {
            accessorList = forumManager.getAccessorsList(forum);
        }
        return accessorList;
    }

    public void setAccessorList(List<String> accessorList) {
        log.debug("setAccessorList(List{})", accessorList);
        if (!accessorList.equals(this.accessorList)) {
            forum.getActorPermissions().setAccessors(forumManager.decodeAccessorsList(accessorList));
            this.accessorList = accessorList;
        }
    }

    public void setContributorsList(List<String> contributorsList) {
        log.debug("setContributorsList(List{})", contributorsList);
        if (!contributorsList.equals(this.contributorsList)) {
            forum.getActorPermissions().setContributors(forumManager.decodeContributorsList(contributorsList));
            this.contributorsList = contributorsList;
        }
    }

    public boolean getNonePermission() {
        if (nonePermission == null) {
            nonePermission = true;
            if (changeSettings || newTopic) {
                nonePermission = false;
            } else {
                if (topics != null) {
                    Predicate<DiscussionTopicBean> ifNonePermission = DiscussionTopicBean::getNonePermission;
                    if (topics.stream().anyMatch(ifNonePermission.negate())) {
                        nonePermission = false;
                    }
                }
            }
        }
        return nonePermission;
    }

    public List<DecoratedAttachment> getAttachList() {
        return decoAttachList;
    }

    public String getAvailabilityRestricted()
	  {
		  log.debug("getAvailabilityRestricted()");
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
		  log.debug("setAvailabilityRestricted()");
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
		log.debug("getAvailability()");
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
		log.debug("setAvailability()");
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
			return userTimeService.dateTimeFormat(forum.getOpenDate().toInstant(), FormatStyle.SHORT, null);
		}
	}

	public void setOpenDate(String openDateStr){
		if (StringUtils.isNotBlank(openDateStr)) {
			try{
				// Get the ISO8601 value directly from the request
				String hiddenOpenDate = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("openDateISO8601");
				if (StringUtils.isNotBlank(hiddenOpenDate)) {
					Date openDate = (Date) datetimeFormat.parse(hiddenOpenDate);
					forum.setOpenDate(openDate);
				}
			} catch (ParseException e) {
				log.error("Couldn't convert open date", e);
			}
		} else {
			forum.setOpenDate(null);
		}
	}

	public String getCloseDate(){
		if(forum == null || forum.getCloseDate() == null){
			return "";
		}else{
			return userTimeService.dateTimeFormat(forum.getCloseDate().toInstant(), FormatStyle.SHORT, null);
		}
	}

	public void setCloseDate(String closeDateStr){
		if (StringUtils.isNotBlank(closeDateStr)) {
			try{
				// Get the ISO8601 value directly from the request
				String hiddenCloseDate = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("closeDateISO8601");
				if (StringUtils.isNotBlank(hiddenCloseDate)) {
					Date closeDate = (Date) datetimeFormat.parse(hiddenCloseDate);
					forum.setCloseDate(closeDate);
				}
			} catch (ParseException e) {
				log.error("Couldn't convert close date", e);
			}
		} else {
			forum.setCloseDate(null);
		}
	}

	public String getFormattedCloseDate(){
		if(forum == null || forum.getCloseDate() == null){
			return "";
		}else{
			SimpleDateFormat formatter_date = new SimpleDateFormat(rb.getString("date_format"), new ResourceLoader().getLocale());
			formatter_date.setTimeZone(userTimeService.getLocalTimeZone());
            return formatter_date.format(forum.getCloseDate());
		}
	}	

	public String getFormattedOpenDate(){
		if(forum == null || forum.getOpenDate() == null){
			return "";
		}else{
			SimpleDateFormat formatter_date = new SimpleDateFormat(rb.getString("date_format"), new ResourceLoader().getLocale());
			formatter_date.setTimeZone(userTimeService.getLocalTimeZone());
            return formatter_date.format(forum.getOpenDate());
		}
	}

	public String getHasRubric(){
		return rubricsService.hasAssociatedRubric(RubricsConstants.RBCS_TOOL_GRADEBOOKNG, forum.getDefaultAssignName()) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
	}
}
