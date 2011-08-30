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

package org.sakaiproject.dash.logic;

import java.util.Date;
import java.util.List;

import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.model.Realm;
import org.sakaiproject.dash.model.NewsItem;

/**
 * DashboardLogic
 *
 */
public interface DashboardLogic {

	/**
	 * Add links to calendar items in a context for a particular user. Links
	 * will be limited to items referencing entities for which the user has 
	 * access permission. This action may be limited by time or number, depending 
	 * on system settings defining policies for removal of calendar links. 
	 * @param sakaiUserId
	 * @param contextId
	 */
	public void addCalendarLinks(String sakaiUserId, String contextId);

	/**
	 * Add links to news items in a context for a particular user. Links
	 * will be limited to items referencing entities for which the user has 
	 * access permission. This action may be limited by time or number, depending 
	 * on system settings defining policies for removal of news links. 
	 * @param sakaiUserId
	 * @param contextId
	 */
	public void addNewsLinks(String sakaiUserId, String contextId);

	public CalendarItem createCalendarItem(String title, Date calendarTime, String entityReference, String entityUrl, Context context, SourceType sourceType);

	public void createCalendarLinks(CalendarItem calendarItem);

	public Context createContext(String contextId);
	
	public NewsItem createNewsItem(String title, Date newsTime, String entityReference, String entityUrl, Context context, SourceType sourceType);

	public void createNewsLinks(NewsItem newsItem);
	
	public SourceType createSourceType(String identifier, String accessPermission);

	public CalendarItem getCalendarItem(long id);

	public List<CalendarItem> getCalendarItems(String sakaiUserId);

	public List<CalendarItem> getCalendarItems(String sakaiUserId, String contextId);
	
	/**
	 * Retrieve the Context with a particular contextId 
	 * @param contextId
	 * @return the Context object, or null if it is not defined.
	 */
	public Context getContext(String contextId);
	
	public NewsItem getNewsItem(long id);
	
	public List<NewsItem> getNewsItems(String sakaiUserId);
	
	public List<NewsItem> getNewsItems(String sakaiUserId, String contextId);
	
	/**
	 * Retrieve the SourceType with a particular identifier  
	 * @param identifier 
	 * @return the SourceType object, or null if it is not defined.
	 */
	public SourceType getSourceType(String identifier);
	
	public void registerEventProcessor(EventProcessor eventProcessor);

	/** 
	 * Remove all calendar links and the calendar item referencing a particular entity.
	 * @param entityReference
	 */
	public void removeCalendarItem(String entityReference);
	
	/**
	 * Remove all news links and the news item referencing a particular entity.
	 * @param entityReference
	 */
	public void removeNewsItem(String entityReference);
	
	/**
	 * Remove all calendar links to a particular entity.
	 * @param entityReference
	 */
	public void removeCalendarLinks(String entityReference);
	
	/**
	 * Remove all calendar links for a particular user in a particular context.
	 * @param sakaiUserId
	 * @param contextId
	 */
	public void removeCalendarLinks(String sakaiUserId, String contextId);
	
	/**
	 * Remove all news links to a particular entity.
	 * @param entityReference
	 */
	public void removeNewsLinks(String entityReference);
	
	/**
	 * Remove all news links for a particular user in a particular context.
	 * @param sakaiUserId
	 * @param contextId
	 */
	public void removeNewsLinks(String sakaiUserId, String contextId);

	public void reviseCalendarItem(String entityReference, String newTitle, Date newTime);
	public void reviseCalendarItemTime(String entityReference, Date newTime);
	public void reviseCalendarItemTitle(String entityReference, String newTitle);
	
	public void reviseNewsItemTitle(String entityReference, String newTitle);
	
	// todo:
	// add methods to revise news items, calendar items, news links, calendar links, etc.
	
	
	// add methods to delete news items, calendar items, news links, calendar links, etc.
	
}
