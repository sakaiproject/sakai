/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/ui/PermissionBean.java $
 * $Id: PermissionBean.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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

import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PermissionLevel;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.PermissionsMask;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.ResourceLoader;

public class PermissionBean {
  
  /** Path to bundle messages */
  private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";
  private static final ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);

  /** Keys for bundle messages */
  public static final String OWN = "perm_own";
  public static final String ALL = "perm_all";
  public static final String NONE = "perm_none";
  
  private String selectedLevel;
  private DBMembershipItem item;
  private PermissionLevelManager permissionLevelManager;
  private PermissionLevel displayLevel;

  public PermissionBean(DBMembershipItem item,
      PermissionLevelManager permissionLevelManager)
  {
    this.permissionLevelManager = permissionLevelManager;
    this.item = item;
    this.selectedLevel = item.getPermissionLevelName();
    PermissionLevel level = item.getPermissionLevel();
    this.displayLevel = (level != null) ? level : permissionLevelManager.getPermissionLevelByName(selectedLevel);
    if (this.displayLevel == null) {
      setPermissionsForLevel(selectedLevel);
    }
  }

  /**
   * @return Returns the selectedLevel.
   */
  public String getSelectedLevel()
  {
    return selectedLevel;
  }

  /**
   * @param selectedLevel
   *          The selectedLevel to set.
   */
  public void setSelectedLevel(String selectedLevel)
  {
    this.selectedLevel = selectedLevel;
    setPermissionsForLevel(selectedLevel);
  }

  private void setPermissionsForLevel(String selectedLevel)
  {
    if (selectedLevel != null)
    {
      if (!"Custom".equals(selectedLevel))
      {
        PermissionLevel level = permissionLevelManager.getPermissionLevelByName(selectedLevel);
        if (level != null) {
          this.displayLevel = level;
        }
      }
      else
      {
        MessageForumsTypeManager typeManager = (MessageForumsTypeManager) ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsTypeManager");
        if (this.displayLevel == null || !typeManager.getCustomLevelType().equals(this.displayLevel.getTypeUuid()))
        {
          this.displayLevel = permissionLevelManager.createPermissionLevel(selectedLevel, typeManager.getCustomLevelType(), new PermissionsMask());
        }
      }
    }
  }

  public boolean getChangeSettings()
  {
    if (item != null && displayLevel != null
        && displayLevel.getChangeSettings() != null)
      return displayLevel.getChangeSettings().booleanValue();
    else
      return false;
  }

  public void setChangeSettings(boolean changeSettings)
  {
    this.displayLevel.setChangeSettings(
        Boolean.valueOf(changeSettings));
  }

  public boolean getDeleteAny()
  {
    if (item != null && displayLevel != null
        && displayLevel.getDeleteAny() != null)
      return displayLevel.getDeleteAny().booleanValue();
    else
      return false;
  }

  public void setDeleteAny(boolean deleteAny)
  {
    this.displayLevel.setDeleteAny(Boolean.valueOf(deleteAny));
  }

  public boolean getDeleteOwn()
  {
    if (item != null && displayLevel != null
        && displayLevel.getDeleteOwn() != null)
      return displayLevel.getDeleteOwn().booleanValue();

    else
      return false;
  }

  public void setDeleteOwn(boolean deleteOwn)
  {
    this.displayLevel.setDeleteOwn(Boolean.valueOf(deleteOwn));
  }

  public boolean getMarkAsNotRead()
  {
    if (item != null && displayLevel != null
        && displayLevel.getMarkAsNotRead() != null)
      return displayLevel.getMarkAsNotRead().booleanValue();
    else
      return false;
  }

  public void setMarkAsNotRead(boolean markAsNotRead)
  {
    this.displayLevel.setMarkAsNotRead(Boolean.valueOf(markAsNotRead));
  }

  public boolean getModeratePostings()
  {
    if (item != null && displayLevel != null
        && displayLevel.getModeratePostings() != null)
      return displayLevel.getModeratePostings().booleanValue();
    else
      return false;
  }

  public void setModeratePostings(boolean moderatePostings)
  {
    this.displayLevel.setModeratePostings(
        Boolean.valueOf(moderatePostings));
  }

  public boolean getIdentifyAnonAuthors()
  {
    return item != null && displayLevel != null 
        && displayLevel.getIdentifyAnonAuthors() != null 
        && displayLevel.getIdentifyAnonAuthors();
  }

  public void setIdentifyAnonAuthors(boolean identifyAnonAuthors)
  {
    this.displayLevel.setIdentifyAnonAuthors(
        Boolean.valueOf(identifyAnonAuthors));
  }

  public boolean getMovePosting()
  {
    if (item != null && displayLevel != null
        && displayLevel.getMovePosting() != null)
      return displayLevel.getMovePosting().booleanValue();
    else
      return false;
  }

  public void setMovePosting(boolean movePosting)
  {
    this.displayLevel.setMovePosting(Boolean.valueOf(movePosting));
  }

  public boolean getNewForum()
  {
    if (item != null && displayLevel != null
        && displayLevel.getNewForum() != null)
      return displayLevel.getNewForum().booleanValue();
    else
      return false;
  }

  public void setNewForum(boolean newForum)
  {
    this.displayLevel.setNewForum(Boolean.valueOf(newForum));
  }

  public boolean getNewResponse()
  {
    if (item != null && displayLevel != null
        && displayLevel.getNewResponse() != null)
      return displayLevel.getNewResponse().booleanValue();
    else
      return false;
  }

  public void setNewResponse(boolean newResponse)
  {
    this.displayLevel.setNewResponse(Boolean.valueOf(newResponse));
  }

  public boolean getNewTopic()
  {
    if (item != null && displayLevel != null
        && displayLevel.getNewTopic() != null)
      return displayLevel.getNewTopic().booleanValue();
    else
      return false;
  }

  public void setNewTopic(boolean newTopic)
  {
    this.displayLevel.setNewTopic(Boolean.valueOf(newTopic));
  }

  public boolean getPostToGradebook()
  {
    if (item != null && displayLevel != null
        && displayLevel.getPostToGradebook() != null)
      return displayLevel.getPostToGradebook() .booleanValue();
    else
      return false;
  }

  public void setPostToGradebook(boolean postGrades)
  {
    this.displayLevel.setPostToGradebook(Boolean.valueOf(postGrades));
  }

  public boolean getRead()
  {
    if (item != null && displayLevel != null
        && displayLevel.getRead() != null)
      return displayLevel.getRead().booleanValue();
    else
      return false;
  }

  public void setRead(boolean read)
  {
    this.displayLevel.setRead(Boolean.valueOf(read));
  }

  public boolean getResponseToResponse()
  {
    if (item != null && displayLevel != null
        && displayLevel.getNewResponseToResponse() != null)
      return displayLevel.getNewResponseToResponse().booleanValue();
    else
      return false;
  }

  public void setResponseToResponse(boolean responseToResponse)
  {
    this.displayLevel.setNewResponseToResponse(
        Boolean.valueOf(responseToResponse));
  }

  public boolean getReviseAny()
  {
    if (item != null && displayLevel != null
        && displayLevel.getReviseAny() != null)
      return displayLevel.getReviseAny().booleanValue();
    else
      return false;
  }

  public void setReviseAny(boolean reviseAny)
  {
    this.displayLevel.setReviseAny(Boolean.valueOf(reviseAny));
  }

  public boolean getReviseOwn()
  {
    if (item != null && displayLevel != null
        && displayLevel.getReviseOwn() != null)
      return displayLevel.getReviseOwn().booleanValue();
    else
      return false;
  }

  public void setReviseOwn(boolean reviseOwn)
  {
    this.displayLevel.setReviseOwn(Boolean.valueOf(reviseOwn));
  }

  /**
   * @return Returns the deletePosting.
   */
  public String getDeletePostings()
  {
    if(getDeleteAny())
    {
      return getResourceBundleString(ALL);
    }
    if(getDeleteOwn())
    {
      return getResourceBundleString(OWN);
    }         
    return getResourceBundleString(NONE);
  }

  /**
   * @param deletePosting The deletePosting to set.
   */
  public void setDeletePostings(String deletePosting)
  {
    if(deletePosting.equals(getResourceBundleString(ALL)))
    {
      setDeleteAny(true);
      setDeleteOwn(true);
    }
    else if(deletePosting.equals(getResourceBundleString(OWN)))
    {
      setDeleteAny(false);
      setDeleteOwn(true);
    }
    else if(deletePosting.equals(getResourceBundleString(NONE)))
    {
      setDeleteAny(false);
      setDeleteOwn(false);
    }
  }

  /**
   * @return Returns the revisePostings.
   */
  public String getRevisePostings()
  {
    String test = getResourceBundleString(NONE);
	  
    if(getReviseAny())
    {
      test = getResourceBundleString(ALL);
    }
    if(getReviseOwn())
    {
      test = getResourceBundleString(OWN);
    }         
//    return getResourceBundleString(NONE);
    return test;
  }

  /**
   * @param revisePostings The revisePostings to set.
   */
  public void setRevisePostings(String revisePostings)
  {
    if(revisePostings.equals(getResourceBundleString(ALL)))
    {
      setReviseAny(true);
      setReviseOwn(false);
    }
    else if(revisePostings.equals(getResourceBundleString(OWN)))
    {
      setReviseAny(false);
      setReviseOwn(true);
    }
    else if(revisePostings.equals(getResourceBundleString(NONE)))
    {
      setReviseAny(false);
      setReviseOwn(false);
    }
  }

  /**
   * @return Returns the item.
   */
  public DBMembershipItem getItem()
  {
    return item;
  }

  /**
   * @return Returns the role or group name.
   */
  public String getName()
  {
	  return item.getName();
  }

	/**
	 * Pulls messages from bundle
	 * 
	 * @param key
	 * 			Key of message to get
	 * 
	 * @return
	 * 			String for key passed in or [missing: key] if not found
	 */
  public static String getResourceBundleString(String key) {
      return rb.getString(key);
  }

 
}
