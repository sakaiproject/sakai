/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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

package org.sakaiproject.dash.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.HashMap;

import org.sakaiproject.dash.logic.TaskLock;
import org.sakaiproject.dash.model.AvailabilityCheck;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;

/**
 * DAO interface for our project
 * 
 *
 */
public interface DashboardDao {
	
	/**
	 * @param availabilityCheck
	 * @return
	 */
	public boolean addAvailabilityCheck(AvailabilityCheck availabilityCheck);
	
	/**
	 * If an entity uses some form of scheduled release, this method is called to 
	 * update the scheduled-release time
	 */
	public boolean updateAvailabilityCheck(AvailabilityCheck availabilityCheck);
	
	/**
	 * checks if an entity is scheduled for release has entry in the database table or not
	 */
	public boolean isScheduleAvailabilityCheckMade(String entityReference);
	
	/**
	 * @param calendarItem
	 * @return
	 */
	public boolean addCalendarItem(CalendarItem calendarItem);
	
	/**
	 * @param calendarLink
	 * @return
	 */
	public boolean addCalendarLink(CalendarLink calendarLink);
	
	/**
	 * Save each calendar link in a collection of calendar-links. 
	 * @param calendarLinks
	 * @return the number of links that were added.
	 */
	public int addCalendarLinks(List<CalendarLink> calendarLinks);
	
	/**
	 * @param context
	 * @return
	 */
	public boolean addContext(Context context);
	
	/**
	 * @param newsItem
	 * @return
	 */
	public boolean addNewsItem(NewsItem newsItem);
	
	/**
	 * @param newsLink
	 * @return
	 */
	public boolean addNewsLink(NewsLink newsLink);
	
	/**
	 * Save each news link in a collection of news-links.  
	 * @param newsLinks
	 * @return the number of links that were added.
	 */
	public int addNewsLinks(List<NewsLink> newsLinks);

	/**
	 * @param person
	 * @return
	 */
	public boolean addPerson(Person person);
		
	/**
	 * @param repeatingCalendarItem
	 * @return
	 */
	public boolean addRepeatingCalendarItem(RepeatingCalendarItem repeatingCalendarItem);

	/**
	 * @param sourceType
	 * @return
	 */
	public boolean addSourceType(SourceType sourceType);
	
	/**
	 * @param identifier
	 * @return
	 */
	public SourceType getSourceType(String identifier);

	/**
	 * @param entityReference
	 * @param calendarTimeLabelKey
	 * @param sequenceNumber
	 * @return
	 */
	public CalendarItem getCalendarItem(String entityReference, String calendarTimeLabelKey, Integer sequenceNumber);

	/**
	 * 
	 * @param repeatingEvent
	 * @return
	 */
	public List<CalendarItem> getCalendarItems(RepeatingCalendarItem repeatingEvent);

	/**
	 * @param entityReference
	 * @return
	 */
	public List<CalendarItem> getCalendarItems(String entityReference);
	
	/**
	 * @param sakaiUserId
	 * @param contextId
	 * @param saved
	 * @param hidden
	 * @return
	 */
	public List<CalendarItem> getCalendarItems(String sakaiUserId, String contextId, boolean saved, boolean hidden);

	/**
	 * @param contextId
	 * @return
	 */
	public List<CalendarItem> getCalendarItemsByContext(String contextId);
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public CalendarLink getCalendarLink(long id);
	
	/**
	 * 
	 * @param sakaiUserId
	 * @param contextId
	 * @param hidden 
	 * @return
	 */
	public List<CalendarLink> getFutureCalendarLinks(String sakaiUserId, String contextId, boolean hidden);

	/**
	 * 
	 * @param sakaiUserId
	 * @param contextId
	 * @param hidden
	 * @return
	 */
	public List<CalendarLink> getPastCalendarLinks(String sakaiUserId, String contextId, boolean hidden);

	/**
	 * 
	 * @param sakaiUserId
	 * @param contextId
	 * @return
	 */
	public List<CalendarLink> getStarredCalendarLinks(String sakaiUserId, String contextId);
	
	/**
	 * @param contextId
	 * @return
	 */
	public Context getContext(String contextId);

	/**
	 * @param motdContextId
	 * @return
	 */
	public List<NewsItem> getMOTD(String motdContextId);

	/**
	 * @param sakaiId
	 * @return
	 */
	public Person getPersonBySakaiId(String sakaiId);

	/**
	 * @param entityReference
	 * @return
	 */
	public NewsItem getNewsItem(String entityReference);
	
	/**
	 * @param contextId
	 * @return
	 */
	public List<NewsItem> getNewsItemsByContext(String contextId);

	/**
	 * @param id
	 * @return
	 */
	public NewsItem getNewsItem(long id);

	/**
	 * @param sakaiId
	 * @param siteId
	 * @return
	 */
	public List<NewsLink> getCurrentNewsLinks(String sakaiId, String siteId);

	/**
	 * @param sakaiId
	 * @param siteId
	 * @return
	 */
	public List<NewsLink> getStarredNewsLinks(String sakaiId, String siteId);

	/**
	 * @param sakaiId
	 * @param siteId
	 * @return
	 */
	public List<NewsLink> getHiddenNewsLinks(String sakaiId, String siteId);

	/**
	 * @param id
	 * @return
	 */
	public CalendarItem getCalendarItem(long id);

	/**
	 * @param sakaiUserId
	 * @param groupId
	 * @return
	 */
	public int countNewsLinksByGroupId(String sakaiUserId, String groupId);

	/**
	 * @param sakaiUserId
	 * @param groupId
	 * @param limit
	 * @param offset
	 * @return
	 */
	public List<NewsLink> getNewsLinksByGroupId(String sakaiUserId,
			String groupId, int limit, int offset);

	/**
	 * @param entityReference
	 * @param calendarTimeLabelKey
	 * @return
	 */
	public RepeatingCalendarItem getRepeatingCalendarItem(String entityReference, String calendarTimeLabelKey);

	/**
	 * @return
	 */
	public List<RepeatingCalendarItem> getRepeatingCalendarItems();

	/**
	 * @param entityReference
	 * @param calendarTimeLabelKey
	 * @param sequenceNumber TODO
	 * @return
	 */
	public SortedSet<Integer> getFutureSequenceNumbers(String entityReference,
			String calendarTimeLabelKey, Integer firstSequenceNumber);

	/**
	 * @param entityReference
	 * @return
	 */
	public boolean deleteAvailabilityChecks(String entityReference);

	/**
	 * @param time
	 * @return
	 */
	public boolean deleteAvailabilityChecksBeforeTime(Date time);

	/**
	 * Removes the CalendarItem with the id indicated, if it exists. 
	 * @param id
	 * @return true if an item is removed, false otherwise. 
	 */
	public boolean deleteCalendarItem(Long id);
	
	/**
	 * Remove all calendar items for which no calendar links exist.
	 * @return
	 */
	public boolean deleteCalendarItemsWithoutLinks();

	/**
	 * Remove a link between a person and a calendar item.
	 * @param personId
	 * @param calendarItemId
	 */
	public boolean deleteCalendarLink(Long personId, Long calendarItemId);

	/**
	 * Removes all CalendarLink objects referencing a CalendarItem with the id indicated, if it exists. 
	 * @param id
	 * @return true if any items are removed, false otherwise. 
	 */
	public boolean deleteCalendarLinks(Long calendarItemId);

	/**
	 * Remove all calendar links for a particular user in a particular context.
	 * @param personId
	 * @param contextId
	 * @return
	 */
	public boolean deleteCalendarLinks(Long personId, Long contextId);

	/**
	 * Delete calendar links prior to a specified time with the specified values 
	 * for properties "starred" and "hidden".
	 * @param expireBefore
	 * @param starred
	 * @param hidden
	 * @return
	 */
	public boolean deleteCalendarLinksBefore(Date expireBefore, boolean starred,
			boolean hidden);
	
	/**
	 * Delete calendar or news links associated with given context 
	 * @param context
	 * @param type TYPE_NEWS or TYPE_CALENDAR
	 * @return
	 */
	public boolean deleteLinksByContext(String context, String type);

	/**
	 * Removes the NewsItem with the id indicated, if it exists. 
	 * @param id
	 * @return true if an item is removed, false otherwise. 
	 */
	public boolean deleteNewsItem(Long id);

	/**
	 * Remove all news items for which no news links exist.
	 * @return
	 */
	public boolean deleteNewsItemsWithoutLinks();

	/**
	 * Remove a link between a person and a new item.
	 * @param personId
	 * @param newsItemId
	 */
	public boolean deleteNewsLink(Long personId, Long newsItemId);

	/**
	 * Removes all NewsLink objects referencing a NewsItem with the id indicated, if it exists. 
	 * @param id
	 * @return true if any items are removed, false otherwise. 
	 */
	public boolean deleteNewsLinks(Long newsItemId);

	/**
	 * Remove all news links for a particular user in a particular context.
	 * @param personId
	 * @param contextId
	 * @return
	 */
	public boolean deleteNewsLinks(Long personId, Long contextId);

	/**
	 * Delete news links prior to a specified time with the specified values 
	 * for properties "starred" and "hidden".
	 * @param expireBefore
	 * @param starred
	 * @param hidden
	 * @return
	 */
	public boolean deleteNewsLinksBefore(Date expireBefore, boolean starred,
			boolean hidden);

	/**
	 * 
	 * @param eventDate
	 * @param event
	 * @param itemRef
	 * @param contextId
	 * @param sessionId
	 * @param eventCode
	 * @return
	 */
	public boolean addEvent(Date eventDate, String event, String itemRef,
			String contextId, String sessionId, String eventCode);

	/**
	 * 
	 * @param calendarItem
	 * @return
	 */
	public boolean updateCalendarItem(CalendarItem calendarItem);

	/**
	 * Revise the calendarTime property of one CalendarItem, if it exists.
	 * @param id
	 * @param newTime
	 * @return true if any items are revised, false otherwise.
	 */
	public boolean updateCalendarItemTime(Long id, Date newTime);

	/**
	 * Revise the title property of one CalendarItem, if it exists.
	 * @param id
	 * @param newTitle
	 * @return true if any items are revised, false otherwise.
	 */
	public boolean updateCalendarItemTitle(Long id, String newTitle);
	
	/**
	 * 
	 * @param entityReference
	 * @param oldLabelKey
	 * @param newLabelKey
	 * @return true if any items are revised, false otherwise.
	 */
	public boolean updateCalendarItemsLabelKey(String entityReference, String oldLabelKey, String newLabelKey);

	/**
	 * Revise the title property of one NewsItem, if it exists.
	 * @param id
	 * @param newTitle
	 * @param newNewsTime TODO
	 * @param newLabelKey TODO
	 * @param newGroupingIdentifier TODO
	 * @return true if any items are revised, false otherwise.
	 */
	public boolean updateNewsItemTitle(Long id, String newTitle, Date newNewsTime, String newLabelKey, String newGroupingIdentifier);

	/**
	 * @param entityReference
	 * @param oldLabelKey
	 * @param newLabelKey
	 * @return
	 */
	public boolean updateRepeatingCalendarItemsLabelKey(String entityReference, String oldLabelKey, String newLabelKey);
	
	/**
	 * @param time
	 * @return
	 */
	public abstract List<AvailabilityCheck> getAvailabilityChecksBeforeTime(Date time);

	/**
	 * @param entityReference
	 * @return
	 */
	public Set<String> getSakaIdsForUserWithCalendarLinks(String entityReference);

	/**
	 * @param entityReference
	 * @return
	 */
	public Set<String> getSakaiIdsForUserWithNewsLinks(String entityReference);

	/**
	 * @param calendarItemId
	 * @param personId
	 * @return
	 */
	public CalendarLink getCalendarLink(long calendarItemId, long personId);

	/**
	 * @param link
	 * @return
	 */
	public boolean updateCalendarLink(CalendarLink link);

	/**
	 * @param contextId
	 * @param newContextTitle
	 * @return
	 */
	public boolean updateContextTitle(String contextId, String newContextTitle);

	/**
	 * @param newsItemId
	 * @param personId
	 * @return
	 */
	public NewsLink getNewsLink(long newsItemId, long personId);

	/**
	 * @param link
	 * @return
	 */
	public boolean updateNewsLink(NewsLink link);

	/**
	 * @param id
	 * @param newTime
	 * @param newGroupingIdentifier
	 * @return
	 */
	public boolean updateNewsItemTime(Long id, Date newTime, String newGroupingIdentifier);

	/**
	 * @param entityReference
	 * @param labelKey
	 * @param sequenceNumber
	 * @param newDate
	 * @return
	 */
	public boolean updateCalendarItemTime(String entityReference, String labelKey,
			Integer sequenceNumber, Date newDate);

	/**
	 * @param entityReference
	 * @param frequency
	 * @return
	 */
	public boolean updateRepeatingCalendarItemFrequency(String entityReference,
			String frequency);

	/**
	 * @param entityReference
	 * @param newFirstTime
	 * @param newLastTime
	 * @return
	 */
	public boolean updateRepeatingCalendarItemTime(String entityReference,
			Date newFirstTime, Date newLastTime);

	/**
	 * @param entityReference
	 * @param newTitle
	 * @return
	 */
	public boolean updateRepeatingCalendarItemTitle(String entityReference,
			String newTitle);

	/**
	 * @param propertyName
	 * @return
	 */
	public Integer getConfigProperty(String propertyName);

	/**
	 * @param propertyName
	 * @param propertyValue
	 */
	public void setConfigProperty(String propertyName,
			Integer propertyValue);

	public Set<String> listUsersWithLinks(CalendarItem calendarItem);

	public List<TaskLock> getAssignedTaskLocks();

	public List<TaskLock> getTaskLocks(String task);

	public boolean updateTaskLock(long id, boolean hasLock, Date lastUpdate);

	public boolean deleteTaskLocks(String task);

	public boolean addTaskLock(TaskLock taskLock);

	public boolean updateTaskLock(String task, String serverId, Date lastUpdate);
	
	/**
	 * delete the CalendarRepeatingEvent entry based on id
	 * @param id
	 * @return
	 */
	public boolean deleteRepeatingEvent(Long id);

	/**
	 * construct HashMap
	 * keyed with context id
	 * and value is set of user ids that has rows in DASHBOARD_CALENDAR_LINK table
	 * @return HashMap object
	 */
	public HashMap<String, Set<String>> getDashboardCalendarContextUserMap();
	
	/**
	 * construct HashMap 
	 * keyed with context id
	 * and value is set of user ids that has rows in DASHBOARD_NEWS_LINK table
	 * @return HashMap object
	 */
	public HashMap<String, Set<String>> getDashboardNewsContextUserMap();
}
