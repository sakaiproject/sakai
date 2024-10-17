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

  public PermissionBean(DBMembershipItem item,
      PermissionLevelManager permissionLevelManager)
  {
    this.permissionLevelManager = permissionLevelManager;
    this.item = item;
    selectedLevel= item.getPermissionLevel().getName();
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
       PermissionLevel permLevel= permissionLevelManager.getPermissionLevelByName(selectedLevel);
       this.item.setPermissionLevel(permLevel);
     }
     else
     {
    	 MessageForumsTypeManager typeManager = (MessageForumsTypeManager) ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsTypeManager");
    	 if(!this.item.getPermissionLevel().getTypeUuid().equals(typeManager.getCustomLevelType()))
    	 {
    		 PermissionLevel permLevel = permissionLevelManager.createPermissionLevel(selectedLevel, typeManager.getCustomLevelType(), new PermissionsMask());
    		 this.item.setPermissionLevel(permLevel);
    	 }
     }
    }
  }

  public boolean getChangeSettings()
  {
    if (item != null && item.getPermissionLevel() != null
        && item.getPermissionLevel().getChangeSettings() != null)
      return item.getPermissionLevel().getChangeSettings().booleanValue();
    else
      return false;
  }

  public void setChangeSettings(boolean changeSettings)
  {
    this.item.getPermissionLevel().setChangeSettings(
        Boolean.valueOf(changeSettings));
  }

  public boolean getDeleteAny()
  {
    if (item != null && item.getPermissionLevel() != null
        && item.getPermissionLevel().getDeleteAny() != null)
      return item.getPermissionLevel().getDeleteAny().booleanValue();
    else
      return false;
  }

  public void setDeleteAny(boolean deleteAny)
  {
    this.item.getPermissionLevel().setDeleteAny(Boolean.valueOf(deleteAny));
  }

  public boolean getDeleteOwn()
  {
    if (item != null && item.getPermissionLevel() != null
        && item.getPermissionLevel().getDeleteOwn() != null)
      return item.getPermissionLevel().getDeleteOwn().booleanValue();

    else
      return false;
  }

  public void setDeleteOwn(boolean deleteOwn)
  {
    this.item.getPermissionLevel().setDeleteOwn(Boolean.valueOf(deleteOwn));
  }

  public boolean getMarkAsNotRead()
  {
    if (item != null && item.getPermissionLevel() != null
        && item.getPermissionLevel().getMarkAsNotRead() != null)
      return item.getPermissionLevel().getMarkAsNotRead().booleanValue();
    else
      return false;
  }

  public void setMarkAsNotRead(boolean markAsNotRead)
  {
    this.item.getPermissionLevel().setMarkAsNotRead(Boolean.valueOf(markAsNotRead));
  }

  public boolean getModeratePostings()
  {
    if (item != null && item.getPermissionLevel() != null
        && item.getPermissionLevel().getModeratePostings() != null)
      return item.getPermissionLevel().getModeratePostings().booleanValue();
    else
      return false;
  }

  public void setModeratePostings(boolean moderatePostings)
  {
    this.item.getPermissionLevel().setModeratePostings(
        Boolean.valueOf(moderatePostings));
  }

  public boolean getIdentifyAnonAuthors()
  {
    return item != null && item.getPermissionLevel() != null 
        && item.getPermissionLevel().getIdentifyAnonAuthors() != null 
        && item.getPermissionLevel().getIdentifyAnonAuthors();
  }

  public void setIdentifyAnonAuthors(boolean identifyAnonAuthors)
  {
    this.item.getPermissionLevel().setIdentifyAnonAuthors(
        Boolean.valueOf(identifyAnonAuthors));
  }

  public boolean getMovePosting()
  {
    if (item != null && item.getPermissionLevel() != null
        && item.getPermissionLevel().getMovePosting() != null)
      return item.getPermissionLevel().getMovePosting().booleanValue();
    else
      return false;
  }

  public void setMovePosting(boolean movePosting)
  {
    this.item.getPermissionLevel().setMovePosting(Boolean.valueOf(movePosting));
  }

  public boolean getNewForum()
  {
    if (item != null && item.getPermissionLevel() != null
        && item.getPermissionLevel().getNewForum() != null)
      return item.getPermissionLevel().getNewForum().booleanValue();
    else
      return false;
  }

  public void setNewForum(boolean newForum)
  {
    this.item.getPermissionLevel().setNewForum(Boolean.valueOf(newForum));
  }

  public boolean getNewResponse()
  {
    if (item != null && item.getPermissionLevel() != null
        && item.getPermissionLevel().getNewResponse() != null)
      return item.getPermissionLevel().getNewResponse().booleanValue();
    else
      return false;
  }

  public void setNewResponse(boolean newResponse)
  {
    this.item.getPermissionLevel().setNewResponse(Boolean.valueOf(newResponse));
  }

  public boolean getNewTopic()
  {
    if (item != null && item.getPermissionLevel() != null
        && item.getPermissionLevel().getNewTopic() != null)
      return item.getPermissionLevel().getNewTopic().booleanValue();
    else
      return false;
  }

  public void setNewTopic(boolean newTopic)
  {
    this.item.getPermissionLevel().setNewTopic(Boolean.valueOf(newTopic));
  }

  public boolean getPostToGradebook()
  {
    if (item != null && item.getPermissionLevel() != null
        && item.getPermissionLevel().getPostToGradebook() != null)
      return item.getPermissionLevel().getPostToGradebook() .booleanValue();
    else
      return false;
  }

  public void setPostToGradebook(boolean postGrades)
  {
    this.item.getPermissionLevel().setPostToGradebook(Boolean.valueOf(postGrades));
  }

  public boolean getRead()
  {
    if (item != null && item.getPermissionLevel() != null
        && item.getPermissionLevel().getRead() != null)
      return item.getPermissionLevel().getRead().booleanValue();
    else
      return false;
  }

  public void setRead(boolean read)
  {
    this.item.getPermissionLevel().setRead(Boolean.valueOf(read));
  }

  public boolean getResponseToResponse()
  {
    if (item != null && item.getPermissionLevel() != null
        && item.getPermissionLevel().getNewResponseToResponse() != null)
      return item.getPermissionLevel().getNewResponseToResponse().booleanValue();
    else
      return false;
  }

  public void setResponseToResponse(boolean responseToResponse)
  {
    this.item.getPermissionLevel().setNewResponseToResponse(
        Boolean.valueOf(responseToResponse));
  }

  public boolean getReviseAny()
  {
    if (item != null && item.getPermissionLevel() != null
        && item.getPermissionLevel().getReviseAny() != null)
      return item.getPermissionLevel().getReviseAny().booleanValue();
    else
      return false;
  }

  public void setReviseAny(boolean reviseAny)
  {
    this.item.getPermissionLevel().setReviseAny(Boolean.valueOf(reviseAny));
  }

  public boolean getReviseOwn()
  {
    if (item != null && item.getPermissionLevel() != null
        && item.getPermissionLevel().getReviseOwn() != null)
      return item.getPermissionLevel().getReviseOwn().booleanValue();
    else
      return false;
  }

  public void setReviseOwn(boolean reviseOwn)
  {
    this.item.getPermissionLevel().setReviseOwn(Boolean.valueOf(reviseOwn));
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
