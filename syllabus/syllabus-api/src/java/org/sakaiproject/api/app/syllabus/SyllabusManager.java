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
import java.util.List;
import java.util.Set;

public interface SyllabusManager
{
  /**
   * creates an SyllabusItem
   */
  public SyllabusItem createSyllabusItem(String userId, String contextId,
      String redirectURL);

  public SyllabusItem getSyllabusItemByUserAndContextIds(final String userId,
      final String contextId);

  public void saveSyllabusItem(SyllabusItem item);
  
  public void addSyllabusToSyllabusItem(final SyllabusItem syllabusItem, final SyllabusData syllabusData);
  public void addSyllabusToSyllabusItem(final SyllabusItem syllabusItem, final SyllabusData syllabusData, boolean updateCalendar);
  
  public void removeSyllabusFromSyllabusItem(final SyllabusItem syllabusItem, final SyllabusData syllabusData);
  
  public SyllabusData createSyllabusDataObject(String title, Integer position,
      String assetId, String view, String status, String emailNotification, Date startDate, Date endDate, boolean linkCalendar, String calendarEventIdStartDate,
      String calendarEventIdEndDate);
  
  public void removeSyllabusDataObject(SyllabusData o);
  
  public Set getSyllabiForSyllabusItem(final SyllabusItem syllabusItem);
  
  public void swapSyllabusDataPositions(final SyllabusItem syllabusItem, final SyllabusData d1, final SyllabusData d2);
  
  public void saveSyllabus(SyllabusData data);
  public void saveSyllabus(SyllabusData data, boolean updateCalendar);
  
  public Integer findLargestSyllabusPosition(final SyllabusItem syllabusItem);
  
  public SyllabusItem getSyllabusItemByContextId(final String contextId);
  
  public SyllabusData getSyllabusData(final String dataId);
  
  public SyllabusAttachment createSyllabusAttachmentObject(String attachId, String name);      

  public void saveSyllabusAttachment(SyllabusAttachment attach);

  public void addSyllabusAttachToSyllabusData(final SyllabusData syllabusData, final SyllabusAttachment syllabusAttach);

  public void removeSyllabusAttachmentObject(SyllabusAttachment o);

  public void removeSyllabusAttachSyllabusData(final SyllabusData syllabusData, final SyllabusAttachment syllabusAttach);

  public Set<SyllabusAttachment> getSyllabusAttachmentsForSyllabusData(final SyllabusData syllabusData);

  public SyllabusAttachment getSyllabusAttachment(final String syllabusAttachId);
  
  public Set<SyllabusData> findPublicSyllabusData();

  public void updateSyllabusAttachmentsViewState(final SyllabusData syllabusData);
  
  public void updateAllCalendarEvents(long syllabusId);

  public void addCalendarAttachments(String siteId, String calendarEventId, List<SyllabusAttachment> attachments);
  
  public boolean removeCalendarEvents(SyllabusData data);
  
  public void removeCalendarAttachments(String siteId, String calendarEventId, SyllabusAttachment attachment);

  public SyllabusData createSyllabusDataObject(String title, Integer position, String asset, String view, String status, String emailNotification, Date startDate, Date endDate, 
		  boolean linkCalendar, String calendarEventIdStartDate, String calendarEventIdEndDate, SyllabusItem syllabusItem);

  public void updateSyllabudDataPosition(final SyllabusData d, final Integer position);

  public SyllabusItem getSyllabusItem(final Long itemId);
  
  //public SyllabusAttachment creatSyllabusAttachmentResource(String attachId, String name);
}
