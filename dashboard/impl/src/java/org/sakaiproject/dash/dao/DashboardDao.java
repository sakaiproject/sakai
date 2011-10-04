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

import org.sakaiproject.dash.model.AvailabilityCheck;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.PersonContext;
import org.sakaiproject.dash.model.PersonContextSourceType;
import org.sakaiproject.dash.model.PersonSourceType;
import org.sakaiproject.dash.model.Realm;
import org.sakaiproject.dash.model.SourceType;

/**
 * DAO interface for our project
 * 
 *
 */
public interface DashboardDao {
	
	public boolean addAvailabilityCheck(AvailabilityCheck availabilityCheck);
	
	public boolean addCalendarItem(CalendarItem calendarItem);
	
	public boolean addCalendarLink(CalendarLink calendarLink);
	
	public boolean addContext(Context context);
	
	public boolean addNewsItem(NewsItem newsItem);
	
	public boolean addNewsLink(NewsLink newsLink);
	
	public boolean addPerson(Person person);
	
	public boolean addPersonContext(PersonContext personContext);
	
	public boolean addPersonContextSourceType(PersonContextSourceType personContextSourceType);
	
	public boolean addPersonSourceType(PersonSourceType personSourceType);
	
	public boolean addRealm(Realm realm);
	
	public boolean addSourceType(SourceType identifier);
	
	public SourceType getSourceType(long sourceTypeId);

	public SourceType getSourceType(String identifier);

	public CalendarItem getCalendarItem(String entityReference, String calendarTimeLabelKey);
	
	public List<CalendarItem> getCalendarItems(String entityReference);
	
	public List<CalendarItem> getCalendarItems(String sakaiUserId, String contextId, boolean saved, boolean hidden);

	/**
	 * Access a list of non-sticky, non-hidden calendar items for a user in which
	 * the calendar-time is in the future. The list will be sorted in ascending order 
	 * by calendar-time (i.e. item with earliest calendar-time first).
	 * @param sakaiUserId
	 * @param contextId
	 * @return
	 */
	public List<CalendarItem> getFutureCalendarItems(String sakaiUserId, String contextId);

	/**
	 * Access a list of non-sticky, non-hidden calendar items for a user in which
	 * the calendar-time is in the past. The list will be sorted in descending order 
	 * by calendar-time (i.e. item with most recent calendar-time first).
	 * @param sakaiUserId
	 * @param contextId
	 * @return
	 */
	public List<CalendarItem> getPastCalendarItems(String sakaiUserId, String contextId);

	public List<CalendarItem> getCalendarItemsByContext(String contextId);

	public Context getContext(long id);
	
	public Context getContext(String contextId);
	
	public Person getPersonBySakaiId(String sakaiId);

	public Realm getRealm(long id);
	
	public NewsItem getNewsItem(String entityReference);

	public List<NewsItem> getNewsItems(String sakaiUserId, String contextId, boolean saved, boolean hidden);

	public List<NewsItem> getNewsItemsByContext(String contextId);

	public NewsItem getNewsItem(long id);

	public CalendarItem getCalendarItem(long id);

	public boolean deleteAvailabilityChecks(String entityReference);

	public boolean deleteAvailabilityChecksBeforeTime(Date time);

	/**
	 * Removes the CalendarItem with the id indicated, if it exists. 
	 * @param id
	 * @return true if an item is removed, false otherwise. 
	 */
	public boolean deleteCalendarItem(Long id);
	
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
	 * Removes the NewsItem with the id indicated, if it exists. 
	 * @param id
	 * @return true if an item is removed, false otherwise. 
	 */
	public boolean deleteNewsItem(Long id);

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
	 * Revise the title property of one CalendarItem, if it exists.
	 * @param id
	 * @param newTitle
	 * @return true if any items are revised, false otherwise.
	 */
	public boolean updateCalendarItem(Long id, String newTitle, Date newTime);
	
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
	 * Revise the title property of one NewsItem, if it exists.
	 * @param id
	 * @param newTitle
	 * @return true if any items are revised, false otherwise.
	 */
	public boolean updateNewsItemTitle(Long id, String newTitle);

	public abstract List<AvailabilityCheck> getAvailabilityChecksBeforeTime(Date time);

	public Set<String> getSakaIdsForUserWithCalendarLinks(String entityReference);

	public Set<String> getSakaiIdsForUserWithNewsLinks(String entityReference);

	public CalendarLink getCalendarLink(long calendarItemId, long personId);

	public boolean updateCalendarLink(CalendarLink link);

	public NewsLink getNewsLink(long newsItemId, long personId);

	public boolean updateNewsLink(NewsLink link);

}
