/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/ui/PermissionBean.java $
 * $Id: PermissionBean.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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

import javax.faces.model.SelectItem;

import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.PermissionLevel;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;

public class PermissionBean {
  
  public static final String OWN = "Own";
  public static final String ALL = "All";
  public static final String NONE = "None";
  
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
     if (!selectedLevel.equals("Custom"))
     {
       PermissionLevel permLevel= permissionLevelManager.getPermissionLevelByName(selectedLevel);
       this.item.setPermissionLevel(permLevel);
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
        new Boolean(changeSettings));
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
    this.item.getPermissionLevel().setDeleteAny(new Boolean(deleteAny));
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
    this.item.getPermissionLevel().setDeleteOwn(new Boolean(deleteOwn));
  }

  public boolean getMarkAsRead()
  {
    if (item != null && item.getPermissionLevel() != null
        && item.getPermissionLevel().getMarkAsRead() != null)
      return item.getPermissionLevel().getMarkAsRead().booleanValue();
    else
      return false;
  }

  public void setMarkAsRead(boolean markAsRead)
  {
    this.item.getPermissionLevel().setMarkAsRead(new Boolean(markAsRead));
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
        new Boolean(moderatePostings));
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
    this.item.getPermissionLevel().setMovePosting(new Boolean(movePosting));
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
    this.item.getPermissionLevel().setNewForum(new Boolean(newForum));
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
    this.item.getPermissionLevel().setNewResponse(new Boolean(newResponse));
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
    this.item.getPermissionLevel().setNewTopic(new Boolean(newTopic));
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
    this.item.getPermissionLevel().setPostToGradebook(new Boolean(postGrades));
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
    this.item.getPermissionLevel().setRead(new Boolean(read));
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
        new Boolean(responseToResponse));
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
    this.item.getPermissionLevel().setReviseAny(new Boolean(reviseAny));
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
    this.item.getPermissionLevel().setReviseOwn(new Boolean(reviseOwn));
  }

  /**
   * @return Returns the deletePosting.
   */
  public String getDeletePostings()
  {
    if(getDeleteAny())
    {
      return ALL;
    }
    if(getDeleteOwn())
    {
      return OWN;
    }         
    return NONE;
  }

  /**
   * @param deletePosting The deletePosting to set.
   */
  public void setDeletePostings(String deletePosting)
  {
    if(deletePosting.equals(ALL))
    {
      setDeleteAny(true);
      setDeleteOwn(true);
    }
    else if(deletePosting.equals(OWN))
    {
      setDeleteAny(false);
      setDeleteOwn(true);
    }
    else if(deletePosting.equals(NONE))
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
    if(getReviseAny())
    {
      return ALL;
    }
    if(getReviseOwn())
    {
      return OWN;
    }         
    return NONE;
  }

  /**
   * @param revisePostings The revisePostings to set.
   */
  public void setRevisePostings(String revisePostings)
  {
    if(revisePostings.equals(ALL))
    {
      setReviseAny(true);
      setReviseOwn(true);
    }
    else if(revisePostings.equals(OWN))
    {
      setReviseAny(false);
      setReviseOwn(true);
    }
    else if(revisePostings.equals(NONE))
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

 
}
