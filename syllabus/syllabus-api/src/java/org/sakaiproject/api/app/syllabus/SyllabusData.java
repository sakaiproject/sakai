/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.api.app.syllabus;

import java.util.Set;

public interface SyllabusData
{
  /**
   * @return Returns the emailNotification.
   */
  public String getEmailNotification();

  /**
   * @param emailNotification The emailNotification to set.
   */
  public void setEmailNotification(String emailNotification);

  /**
   * @return Returns the status.
   */
  public String getStatus();

  /**
   * @param status The status to set.
   */
  public void setStatus(String status);

  /**
   * @return Returns the title.
   */
  public String getTitle();

  /**
   * @param title The title to set.
   */
  public void setTitle(String title);

  /**
   * @return Returns the view.
   */
  public String getView();

  /**
   * @param view The view to set.
   */
  public void setView(String view);

  /**
   * @return Returns the assetId.
   */
  public String getAsset();

  /**
   * @param assetId The assetId to set.
   */
  public void setAsset(String assetId);

  /**
   * @return Returns the lockId.
   */
  public Integer getLockId();

  /**
   * @return Returns the position.
   */
  public Integer getPosition();

  /**
   * @param position The position to set.
   */
  public void setPosition(Integer position);

  /**
   * @return Returns the syllabusId.
   */
  public Long getSyllabusId();

  /**
   * @return Returns the syllabusItem.
   */
  public SyllabusItem getSyllabusItem();

  /**
   * @param syllabusItem The syllabusItem to set.
   */
  public void setSyllabusItem(SyllabusItem syllabusItem);
  
  public Set getAttachments();
  
  public void setAttachments(Set attachments);
}



