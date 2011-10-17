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
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.dash.entity.EntityLinkStrategy;
import org.sakaiproject.dash.entity.EntityType;
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

	/**
	 * 
	 * @param title
	 * @param calendarTime
	 * @param calendarTimeLabelKey
	 * @param entityReference
	 * @param context
	 * @param sourceType
	 * @param repeatingCalendarItem The RepeatingCalendarItem of which this is an instance, or null if this item does not repeat.
	 * @param sequenceNumber The index of this item in the sequence of repeating events, or null if this item does not repeat.
	 * @return
	 */
	public CalendarItem createCalendarItem(String title, Date calendarTime, String calendarTimeLabelKey, String entityReference, Context context, SourceType sourceType, RepeatingCalendarItem repeatingCalendarItem, Integer sequenceNumber);

	public void createCalendarLinks(CalendarItem calendarItem);

	public Context createContext(String contextId);
	
	public NewsItem createNewsItem(String title, Date newsTime, String entityReference, Context context, SourceType sourceType);

	public void createNewsLinks(NewsItem newsItem);
	
	public RepeatingCalendarItem createRepeatingCalendarItem(String title, Date firstTime,
			Date lastTime, String calendarTimeLabelKey, String entityReference, Context context, 
			SourceType sourceType, String frequency, int count);
	
	public SourceType createSourceType(String identifier, String accessPermission, EntityLinkStrategy entityLinkStrategy);

	public CalendarItem getCalendarItem(long id);
	
	/**
	 * 
	 * @param entityReference
	 * @param calendarTimeLabelKey
	 * @param sequenceNumber
	 * @return
	 */
	public CalendarItem getCalendarItem(String entityReference, String calendarTimeLabelKey, Integer sequenceNumber);

	/**
	 * 
	 * @param sakaiUserId
	 * @param showFuture If true, the results will include items in the future. Otherwise they will not.
	 * @param showPast If true, the results will include items in the past. Otherwise they will not.
	 * @param saved If true, the results will include only items that have sticky true for 
	 * 		the user. Otherwise the results will not include items with sticky true.
	 * @param hidden If true, the results will include items that are individually marked 
	 * 		with hidden true. Otherwise, the results will not include items that are 
	 * 		individually hidden or hidden because they have a source-type that is hidden 
	 * 		for the user or because they are from a context that is hidden for the user. 
	 * @return
	 */
	public List<CalendarItem> getCalendarItems(String sakaiUserId, boolean showFuture, boolean showPast, boolean saved, boolean hidden);

	/**
	 * Returns a list (possibly empty) of calendar-items for the specified user in the specified context.  
	 * If contextId is null, the list will include items from all of the user's sites. If showFuture is true, 
	 * the other three boolean parameters are ignored, and the results will include only non-sticky, 
	 * non-hidden items with calendar-times in the future, in ascending order by calendar-time (i.e. 
	 * earlier items before later items). Otherwise, if showPast is true, the other two boolean parameters 
	 * will be ignored and the results will include only non-sticky, non-hidden items with calendar-times 
	 * in the past, in descending order by calendar-time (i.e. items with most recent calendar-time first). 
	 * Otherwise, if sticky is true, the results will include only items that have sticky true for the user. 
	 * And if hidden is true, the results will include items that are individually marked with hidden true. 
	 * If hidden is false, the results will not include items that are individually hidden or hidden because 
	 * they have a source-type that is hidden for the user or because they are from a context that is hidden 
	 * for the user. 
	 * @param sakaiUserId
	 * @param contextId
	 * @param showFuture 
	 * @param showPast 
	 * @param saved 
	 * @param hidden 
	 * @return
	 */
	public List<CalendarItem> getCalendarItems(String sakaiUserId, String contextId, boolean showFuture, boolean showPast, boolean saved, boolean hidden);
	
	/**
	 * Retrieve the Context with a particular contextId 
	 * @param contextId
	 * @return the Context object, or null if it is not defined.
	 */
	public Context getContext(String contextId);
	
	public NewsItem getNewsItem(long id);
	
	public NewsItem getNewsItem(String entityReference);
	
	public List<NewsItem> getNewsItems(String sakaiUserId, boolean saved, boolean hidden);
	
	public List<NewsItem> getNewsItems(String sakaiUserId, String contextId, boolean saved, boolean hidden);
	
	/**
	 * Retrieve the SourceType with a particular identifier  
	 * @param identifier 
	 * @return the SourceType object, or null if it is not defined.
	 */
	public SourceType getSourceType(String identifier);
	
	public Map<String, Object> getEntityMapping(String entityType, String entityReference, Locale locale);

	//public Date getReleaseDate(String entityReference, String entityTypeId);
	
	//public Date getRetractDate(String entityReference, String entityTypeId);
	
	/**
	 * Retrieve a localized string value specific to a particular type of entity using
	 * the provided key. 
	 * @param key
	 * @param dflt TODO
	 * @param entityTypeId
	 * @return the value or null if no value is found
	 */
	public String getString(String key, String dflt, String entityTypeId);

	/**
	 * Hide a calendar item from views of calendar items for a particular user.
	 * @param sakaiUserId
	 * @param calendarItemId
	 * @return
	 */
	public boolean hideCalendarItem(String sakaiUserId, long calendarItemId);

	/**
	 * Hide calendar items of a particular type from views of calendar items for a particular user.
	 * @param sakaiUserId
	 * @param sourceTypeId
	 * @return
	 */
	public boolean hideCalendarItemsByContext(String sakaiUserId, long contextId);

	/**
	 * Hide calendar items of a particular type from views of calendar items for a particular user.
	 * @param sakaiUserId
	 * @param contextId
	 * @return
	 */
	public boolean hideCalendarItemsByContextSourceType(String sakaiUserId, long contextId, long sourceTypeId);

	/**
	 * Hide calendar items of a particular type from views of calendar items for a particular user.
	 * @param sakaiUserId
	 * @param contextId
	 * @param sourceTypeId
	 * @return
	 */
	public boolean hideCalendarItemsBySourceType(String sakaiUserId, long sourceTypeId);

	/**
	 * Hide a news item from views of news items by a particular user.
	 * @param sakaiUserId
	 * @param newsItemId
	 * @return
	 */
	public boolean hideNewsItem(String sakaiUserId, long newsItemId);
	
	/**
	 * Hide news items of a particular type from views of news items for a particular user.
	 * @param sakaiUserId
	 * @param contextId
	 * @return
	 */
	public boolean hideNewsItemsByContext(String sakaiUserId, long contextId);

	/**
	 * Hide news items of a particular type from views of news items for a particular user.
	 * @param sakaiUserId
	 * @param contextId
	 * @param sourceTypeId
	 * @return
	 */
	public boolean hideNewsItemsByContextSourceType(String sakaiUserId, long contextId, long sourceTypeId);

	/**
	 * Hide news items of a particular type from views of news items for a particular user.
	 * @param sakaiUserId
	 * @param sourceTypeId
	 * @return
	 */
	public boolean hideNewsItemsBySourceType(String sakaiUserId, long sourceTypeId);

	/**
	 * Check whether an entity is fully available to users with permission to access it 
	 * (i.e. it is not hidden or restricted through some form of conditional release).
	 * @param entityReference
	 * @param entityTypeId
	 * @return
	 */
	public boolean isAvailable(String entityReference, String entityTypeId);
	
	/**
	 * Mark a calendar item to be highlighted and kept in the calendar display for a particular user even after it expires.
	 * @param sakaiUserId
	 * @param calendarItemId
	 * @return
	 */
	public boolean keepCalendarItem(String sakaiUserId, long calendarItemId);

	/**
	 * Mark a news item to be highlighted and kept in the news display for a particular user even after it expires.
	 * @param sakaiUserId
	 * @param newsItemId
	 * @return
	 */
	public boolean keepNewsItem(String sakaiUserId, long newsItemId);

	public void registerEntityType(EntityType entityType);
	
	public void registerEventProcessor(EventProcessor eventProcessor);

	public void removeCalendarItem(String entityReference,
			String calendarTimeLabelKey, Integer sequenceNumber);

	/** 
	 * Remove all calendar links and the calendar item referencing a particular entity.
	 * @param entityReference
	 */
	public void removeCalendarItems(String entityReference);
	
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

	public void reviseCalendarItems(String entityReference, String newTitle, Date newTime);
	public void reviseCalendarItemsTime(String entityReference, Date newTime);
	public void reviseCalendarItemsTitle(String entityReference, String newTitle);
	
	public void reviseNewsItemTitle(String entityReference, String newTitle);
	
	/**
	 * If an entity uses some form of scheduled release, this method is called to 
	 * perform a check at the scheduled-release time to add links for that item. 
	 * 
	 * @param entityReference
	 * @param entityTypeId TODO
	 * @param scheduledTime
	 */
	public void scheduleAvailabilityCheck(String entityReference, String entityTypeId, Date scheduledTime);

	/**
	 * Remove a scheduled check for availability.
	 * @param entityReference
	 */
	public void removeAllScheduledAvailabilityChecks(String entityReference);

	/**
	 * Restore a calendar item to views of calendar items by a particular user.
	 * @param sakaiUserId
	 * @param calendarItemId
	 * @return
	 */
	public boolean unhideCalendarItem(String sakaiUserId, long calendarItemId);

	/**
	 * Restore a news item to views of news items by a particular user.
	 * @param sakaiUserId
	 * @param newsItemId
	 * @return
	 */
	public boolean unhideNewsItem(String sakaiUserId, long newsItemId);

	/**
	 * Remove the marking for a calendar item to be highlighted and kept in the calendar display for a particular user even after it expires.
	 * @param sakaiUserId
	 * @param calendarItemId
	 * @return
	 */
	public boolean unkeepCalendarItem(String sakaiUserId, long calendarItemId);

	/**
	 * Remove the marking for a news item to be highlighted and kept in the news display for a particular user even after it expires.
	 * @param sakaiUserId
	 * @param newsItemId
	 * @return
	 */
	public boolean unkeepNewsItem(String sakaiUserId, long newsItemId);

	public void updateNewsLinks(String entityReference);

	public void updateCalendarLinks(String entityReference);

	

	// todo:
	// add methods to revise news items, calendar items, news links, calendar links, etc.
	
	
	// add methods to delete news items, calendar items, news links, calendar links, etc.
	
}
