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

import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.Realm;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.dash.model.Thing;

/**
 * DAO interface for our project
 * 
 *
 */
public interface DashboardDao {

	/**
	 * Gets a single Thing from the db
	 * 
	 * @return an item or null if no result
	 */
	public Thing getThing(long id);
	
	/**
	 * Get all Things
	 * @return a list of items, an empty list if no items
	 */
	public List<Thing> getThings();
		
	/**
	 * Add a new Thing record to the database. Only the name property is actually used.
	 * @param t	Thing
	 * @return	true if success, false if not
	 */
	public boolean addThing(Thing t);
	
	
	public boolean addCalendarItem(CalendarItem calendarItem);
	
	public boolean addCalendarLink(CalendarLink calendarLink);
	
	public boolean addContext(Context context);
	
	public boolean addNewsItem(NewsItem newsItem);
	
	public boolean addNewsLink(NewsLink newsLink);
	
	public boolean addPerson(Person person);
	
	public boolean addRealm(Realm realm);
	
	public boolean addSourceType(SourceType identifier);
	
	public SourceType getSourceType(String identifier);

	public CalendarItem getCalendarItem(String entityReference);

	public Context getContext(long id);
	
	public Context getContext(String contextId);
	
	public Person getPersonBySakaiId(String sakaiId);

	public Realm getRealm(long id);
	
	public NewsItem getNewsItem(String entityReference);

	public List<NewsItem> getNewsItems(String sakaiUserId, String contextId);

	public List<CalendarItem> getCalendarItems(String sakaiUserId, String contextId);

	public NewsItem getNewsItem(long id);

	public CalendarItem getCalendarItem(long id);

	/**
	 * Removes the CalendarItem with the id indicated, if it exists. 
	 * @param id
	 * @return true if an item is removed, false otherwise. 
	 */
	public boolean deleteCalendarItem(Long id);
	
	/**
	 * Removes all CalendarLink objects referencing a CalendarItem with the id indicated, if it exists. 
	 * @param id
	 * @return true if any items are removed, false otherwise. 
	 */
	public boolean deleteCalendarLinks(Long calendarItemId);

	/**
	 * Removes the NewsItem with the id indicated, if it exists. 
	 * @param id
	 * @return true if an item is removed, false otherwise. 
	 */
	public boolean deleteNewsItem(Long id);

	/**
	 * Removes all NewsLink objects referencing a NewsItem with the id indicated, if it exists. 
	 * @param id
	 * @return true if any items are removed, false otherwise. 
	 */
	public boolean deleteNewsLinks(Long newsItemId);

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

}
