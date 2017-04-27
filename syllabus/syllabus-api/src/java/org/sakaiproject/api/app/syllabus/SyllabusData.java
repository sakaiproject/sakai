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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.api.app.syllabus;

import java.util.Date;
import java.util.Set;

public interface SyllabusData
{
 	public final String ITEM_POSTED="posted";
 	public final String ITEM_DRAFT= "draft";
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
  
  public Set<SyllabusAttachment> getAttachments();
  
  public void setAttachments(Set<SyllabusAttachment> attachments);
  
  /**
   * @return Returns the syllabus' start time
   */
  public Date getStartDate();
  
  /**
   * @param startDate The syllabus' start time
   */
  public void setStartDate(Date date);
  
  /**
   * @return end date, the syllabus' end time
   */
  public Date getEndDate();
  
  /**
   * @param end date, the syllabus' end time
   */
  public void setEndDate(Date dateDuration);
  
  /**
   * flag used to associate date to the calendar tool
   * @return
   */
  public Boolean isLinkCalendar();
  public Boolean getLinkCalendar();
  
  /**
   * flag used to associate date to the calendar tool
   * @param linkCalendar
   */
  public void setLinkCalendar(Boolean linkCalendar);
  
  /**
   * keep track of the calendar event ID so you can edit/remove it
   * @param calendarEventId
   */
  public String getCalendarEventIdStartDate();
  
  /**
   * keep track of the calendar event ID so you can edit/remove it
   * @param calendarEventIdStartDate
   */
  public void setCalendarEventIdStartDate(String calendarEventIdStartDate);
  
  /**
   * keep track of the calendar event ID so you can edit/remove it
   * @return
   */
  public String getCalendarEventIdEndDate();
  
  /**
   * keep track of the calendar event ID so you can edit/remove it
   * @param calendarEventIdEndDate
   */
  public void setCalendarEventIdEndDate(String calendarEventIdEndDate);
}



