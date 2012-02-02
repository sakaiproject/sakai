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
import java.util.SortedSet;

import org.sakaiproject.dash.entity.EntityLinkStrategy;
import org.sakaiproject.dash.entity.EntityType;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;

/**
 * DashboardLogic
 *
 */
public interface DashboardLogic {
	
	public static final String EVENT_DASH_VISIT = "dash.visit";
	public static final String EVENT_DASH_FOLLOW_TOOL_LINK = "dash.follow.tool.link";
	public static final String EVENT_DASH_FOLLOW_SITE_LINK = "dash.follow.site.link";
	public static final String EVENT_DASH_ACCESS_URL = "dash.access.url";
	public static final String EVENT_VIEW_ATTACHMENT = "dash.view.attachment";
	
	public static final String EVENT_DASH_TABBING = "dash.tabbing";
	public static final String EVENT_DASH_PAGING = "dash.paging";

	public static final String EVENT_DASH_ITEM_DETAILS = "dash.item.details";
	public static final String EVENT_DASH_VIEW_GROUP = "dash.view.group";
	
	public static final String EVENT_DASH_STAR = "dash.star.item";
	public static final String EVENT_DASH_UNSTAR = "dash.unstar.item";
	public static final String EVENT_DASH_HIDE = "dash.hide.item";
	public static final String EVENT_DASH_SHOW = "dash.show.item";
	public static final String EVENT_DASH_HIDE_MOTD = "dash.hide.motd";
		
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
	 * Add link to a previously persisted news item for a particular user. Creates "Person" 
	 * record for user if it doesn't exist (provided sakaiUserId identifies a valid sakai
	 * user).  Does not check permissions before adding the link.
	 * @param sakaiUserId
	 * @param newsItem
	 * @return
	 */
	public NewsLink addNewsLink(String sakaiUserId, NewsItem newsItem);

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
	 * Creates and persists a CalendarItem with specified attributes. Returns the complete CalendarItem object.
	 * @param title
	 * @param calendarTime
	 * @param calendarTimeLabelKey
	 * @param entityReference
	 * @param context The previously persisted Context object referencing the site within which the entity was created.
	 * @param sourceType The previously persisted SourceType object which describes the category of entity referenced. 
	 * @param subtype
	 * @param repeatingCalendarItem The previously persisted RepeatingCalendarItem of which this is an instance, or null if this item does not repeat.
	 * @param sequenceNumber The index of this item in the sequence of repeating events, or null if this item does not repeat.
	 * @return
	 */
	public CalendarItem createCalendarItem(String title, Date calendarTime, String calendarTimeLabelKey, String entityReference, Context context, SourceType sourceType, String subtype, RepeatingCalendarItem repeatingCalendarItem, Integer sequenceNumber);

	/**
	 * Finds all people with minimal rights to access the entity referenced by the 
	 * CalendarItem (i.e. at least that much access or better) and adds links for 
	 * each such person.  Also adds Person records for each user who is not already
	 * known to DashboardLogic.
	 * @param calendarItem
	 */
	public void createCalendarLinks(CalendarItem calendarItem);

	/**
	 * Retrieves information about a context (i.e. a sakai site) and stores it as a
	 * Context object.  Returns the Context object.
	 * @param contextId
	 * @return
	 */
	public Context createContext(String contextId);
	
	/**
	 * Creates and persists a NewsItem object with specified attributes.  Returns the complete NewsItem object.
	 * @param title
	 * @param newsTime
	 * @param labelKey
	 * @param entityReference
	 * @param context
	 * @param sourceType
	 * @param subtype
	 * @return
	 */
	public NewsItem createNewsItem(String title, Date newsTime, String labelKey, String entityReference, Context context, SourceType sourceType, String subtype);

	/**
	 * Finds all people with minimal rights to access the entity referenced by the 
	 * NewsItem (i.e. at least that much access or better) and adds links for 
	 * each such person.  Also adds Person records for each user who is not already
	 * known to DashboardLogic.
	 * @param newsItem
	 */
	public void createNewsLinks(NewsItem newsItem);
	
	/**
	 * Creates and persists a RepeatingCalendarItem with specified attributes.  Returns the complete RepeatingCalendarItem object.
	 * @param title
	 * @param firstTime
	 * @param lastTime
	 * @param calendarTimeLabelKey
	 * @param entityReference
	 * @param context
	 * @param sourceType
	 * @param frequency
	 * @param count
	 * @return
	 */
	public RepeatingCalendarItem createRepeatingCalendarItem(String title, Date firstTime,
			Date lastTime, String calendarTimeLabelKey, String entityReference, Context context, 
			SourceType sourceType, String frequency, int count);
	
	/**
	 * Creates and persists a SourceType object with specified attributes. Each registered EntityType must have one (and only one) SourceType definition. 
	 * Returns the complete SourceType object.
	 * @param identifier
	 * @param accessPermission
	 * @param entityLinkStrategy
	 * @return
	 */
	public SourceType createSourceType(String identifier, String accessPermission, EntityLinkStrategy entityLinkStrategy);

	/**
	 * Retrieve a CalendarItem object based on the unique identifier assigned to it when it was persisted.
	 * @param id
	 * @return
	 */
	public CalendarItem getCalendarItem(long id);
	
	/**
	 * Retrieve a CalendarItem object based on its sakai entity-reference, its label-key, and its (possibly null) sequence number.
	 * @param entityReference
	 * @param calendarTimeLabelKey
	 * @param sequenceNumber
	 * @return
	 */
	public CalendarItem getCalendarItem(String entityReference, String calendarTimeLabelKey, Integer sequenceNumber);

//	/**
//	 * 
//	 * @param sakaiUserId
//	 * @param showFuture If true, the results will include items in the future. Otherwise they will not.
//	 * @param showPast If true, the results will include items in the past. Otherwise they will not.
//	 * @param saved If true, the results will include only items that have sticky true for 
//	 * 		the user. Otherwise the results will not include items with sticky true.
//	 * @param hidden If true, the results will include items that are individually marked 
//	 * 		with hidden true. Otherwise, the results will not include items that are 
//	 * 		individually hidden or hidden because they have a source-type that is hidden 
//	 * 		for the user or because they are from a context that is hidden for the user. 
//	 * @return
//	 */
//	public List<CalendarItem> getCalendarItems(String sakaiUserId, boolean showFuture, boolean showPast, boolean saved, boolean hidden);

//	/**
//	 * Returns a list (possibly empty) of calendar-items for the specified user in the specified context.  
//	 * If contextId is null, the list will include items from all of the user's sites. If showFuture is true, 
//	 * the other three boolean parameters are ignored, and the results will include only non-sticky, 
//	 * non-hidden items with calendar-times in the future, in ascending order by calendar-time (i.e. 
//	 * earlier items before later items). Otherwise, if showPast is true, the other two boolean parameters 
//	 * will be ignored and the results will include only non-sticky, non-hidden items with calendar-times 
//	 * in the past, in descending order by calendar-time (i.e. items with most recent calendar-time first). 
//	 * Otherwise, if sticky is true, the results will include only items that have sticky true for the user. 
//	 * And if hidden is true, the results will include items that are individually marked with hidden true. 
//	 * If hidden is false, the results will not include items that are individually hidden or hidden because 
//	 * they have a source-type that is hidden for the user or because they are from a context that is hidden 
//	 * for the user. 
//	 * @param sakaiUserId
//	 * @param contextId
//	 * @param showFuture 
//	 * @param showPast 
//	 * @param saved 
//	 * @param hidden 
//	 * @return
//	 */
//	public List<CalendarItem> getCalendarItems(String sakaiUserId, String contextId, boolean showFuture, boolean showPast, boolean saved, boolean hidden);
	
	/**
	 * Returns the CalendarLink object based on the the unique integer identifer assigned to it when it was persisted.
	 * @param id
	 * @return
	 */
	public CalendarLink getCalendarLink(Long id);
	
	/**
	 * Returns a list of CalendarLink objects linking a particular person to calendar items 
	 * whose time attribute is in the current date or later (i.e. it will return items representing
	 * events that occurred earlier in the current day) and whose "hidden" state matches the 
	 * specified value. Results will be limited to a particular site if the contextId parameter 
	 * is not null.
	 * @param sakaiUserId
	 * @param contextId
	 * @param hidden 
	 * @return
	 */
	public List<CalendarLink> getFutureCalendarLinks(String sakaiUserId, String contextId, boolean hidden);

	/**
	 * Returns a list of CalendarLink objects linking a particular person to calendar items 
	 * whose time attribute is before the current instant and whose "hidden" state matches the 
	 * specified value. Results will be limited to a particular site if the contextId parameter 
	 * is not null.
	 * @param sakaiUserId
	 * @param contextId
	 * @param hidden
	 * @return
	 */
	public List<CalendarLink> getPastCalendarLinks(String sakaiUserId, String contextId, boolean hidden);

	/**
	 * Returns a list of CalendarLink objects linking a particular person to calendar items 
	 * that the specified user has "starred". Results will be limited to a particular site if 
	 * the contextId parameter is not null.
	 * @param sakaiUserId
	 * @param contextId
	 * @return
	 */
	public List<CalendarLink> getStarredCalendarLinks(String sakaiUserId, String contextId);
	
	/**
	 * Returns a previously persisted Context object representing a sakai site with the specified contextId. 
	 * @param contextId
	 * @return the Context object, or null if it is not defined.
	 */
	public Context getContext(String contextId);
	
	/**
	 * Returns a NewsItem object based on the the unique integer identifer assigned to it when it was persisted.
	 * @param id
	 * @return
	 */
	public NewsItem getNewsItem(long id);
	
	/**
	 * Returns a previously persisted NewsItem object based on its sakai entity-reference	 
	 * @param entityReference
	 * @return
	 */
	public NewsItem getNewsItem(String entityReference);
	
	//public List<NewsItem> getNewsItems(String sakaiUserId, String contextId, int collapseCount);
	
	//public List<NewsItem> getNewsItems(String sakaiUserId, boolean saved, boolean hidden);
	
	//public List<NewsItem> getNewsItems(String sakaiUserId, String contextId, boolean saved, boolean hidden);
	
	/**
	 * Returns the number on NewsLink objects representing items in a "group" that the specified person 
	 * has permission to access and has not "hidden". A group is a set of items of the same source type 
	 * in the same context with the same label-key (indicating the last action on the entity) and last 
	 * modified on the same calendar date.
	 * @param sakaiUserId
	 * @param groupId
	 * @return
	 */
	public int countNewsLinksByGroupId(String sakaiUserId, String groupId);

	/**
	 * Returns a paged list of NewsLink objects representing items in a "group" that the specified person 
	 * has permission to access and has not "hidden". A group is a set of items of the same source type 
	 * in the same context with the same label-key (indicating the last action on the entity) and last 
	 * modified on the same calendar date. The list is selected in descending order by the time of the 
	 * last action.
	 * @param sakaiUserId
	 * @param groupId
	 * @param limit The maximum number of items to be returned.
	 * @param offset The zero-based index of the first item within the entire set.
	 * @return
	 */
	public List<NewsLink> getNewsLinksByGroupId(String sakaiUserId,
			String groupId, int limit, int offset);
	
	/**
	 * Returns the NewsLink object based on the the unique integer identifer assigned to it when it was persisted.
	 * @param id
	 * @return
	 */
	public NewsLink getNewsLink(Long id);

	/**
	 * Returns a list of NewsLink objects which the specified person has permission to access and has not hidden. 
	 * If the contextId is not null, the results will be limited to the site indicated by that value. 
	 * @param sakaiUserId
	 * @param contextId
	 * @return
	 */
	public List<NewsLink> getCurrentNewsLinks(String sakaiUserId, String contextId);

	/**
	 * Returns a list of NewsLink objects which the specified person has permission to access and has "starred". 
	 * If the contextId is not null, the results will be limited to the site indicated by that value. 
	 * @param sakaiUserId
	 * @param contextId
	 * @return
	 */
	public List<NewsLink> getStarredNewsLinks(String sakaiUserId, String siteId);

	/**
	 * Returns a list of NewsLink objects which the specified person has permission to access and has hidden. 
	 * If the contextId is not null, the results will be limited to the site indicated by that value. 
	 * @param sakaiUserId
	 * @param contextId
	 * @return
	 */
	public List<NewsLink> getHiddenNewsLinks(String sakaiUserId, String siteId);

	/**
	 * Returns a RepeatingCalendarItem object that references the specified sakai entity and has 
	 * the specified label-key.
	 * @param entityReference
	 * @param calendarTimeLabelKey
	 * @return
	 */
	public RepeatingCalendarItem getRepeatingCalendarItem(String entityReference, String calendarTimeLabelKey);

	/**
	 * Retrieve the SourceType by its unique string identifier (corresponding to the identifier used when the corresponding EntityType was registered). 
	 * @param identifier 
	 * @return the SourceType object, or null if it is not defined.
	 */
	public SourceType getSourceType(String identifier);
	
	/**
	 * 
	 * @param entityType
	 * @param entityReference
	 * @param locale
	 * @return
	 */
	public Map<String, Object> getEntityMapping(String entityType, String entityReference, Locale locale);

	/**
	 * Retrieve a localized string value specific to a particular type of entity using
	 * the provided key. 
	 * @param key
	 * @param dflt 
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
	 * Hide a news item from views of news items by a particular user.
	 * @param sakaiUserId
	 * @param newsItemId
	 * @return
	 */
	public boolean hideNewsItem(String sakaiUserId, long newsItemId);
	
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
	
	/**
	 * Post or log an event according to the current settings in DashboardConfig. 
	 * May result in an "Event" being posted to sakai's EventTrackingService or in an 
	 * "Event" being persisted locally or both or neither, depending on the settings 
	 * in DashboardConfig for this particular event identifier (the first parameter). 
	 * @param event
	 * @param itemRef
	 */
	public void recordDashboardActivity(String event, String itemRef);

	/**
	 * Register an EntityType, an object that can assist in providing information 
	 * about a particular entity of this type.
	 * @param entityType
	 */
	public void registerEntityType(EntityType entityType);
	
	/**
	 * Register an EventProcessor whose processEvent() method will be invoked to handle sakai events 
	 * with the identifier returned by the EventProcessor's getEventIdentifer() method.
	 * @param eventProcessor
	 */
	public void registerEventProcessor(EventProcessor eventProcessor);

	/**
	 * Remove the calendar item uniquely identified by the parameter values.
	 * @param entityReference
	 * @param calendarTimeLabelKey
	 * @param sequenceNumber
	 */
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
	
	/**
	 * Change the value of the label key to newLabelKey for all calendar items with specified
	 * entityReference and oldLabelKey. Does nothing if any of the parameters are null.  
	 * @param entityReference
	 * @param oldLabelKey
	 * @param newLabelKey
	 */
	public void reviseCalendarItemsLabelKey(String entityReference, String oldLabelKey, String newLabelKey);
	
	public void reviseContextTitle(String contextId, String newContextTitle);
	
	public void reviseNewsItemLabelKey(String entityReference, String newLabelKey, String newGroupingIdentifier);
	
	public void reviseNewsItemSubtype(String entityReference, String newSubtype, Date newNewsTime, String newLabelKey, String newGroupingIdentifier);

	public void reviseNewsItemTime(String entityReference, Date newTime, String newGroupingIdentifier);
	
	public void reviseNewsItemTitle(String entityReference, String newTitle, Date newNewsTime, String newLabelKey, String newGroupingIdentifier);

	/**
	 * 
	 * @param entityReference
	 * @param oldType
	 * @param newType
	 */
	public void reviseRepeatingCalendarItemsLabelKey(String entityReference,
			String oldType, String newType);
	
	public boolean reviseRepeatingCalendarItemFrequency(String entityReference,
			String frequency);

	/**
	 * 
	 * @param entityReference
	 * @param labelKey
	 * @param newSubtype
	 */
	public void reviseRepeatingCalendarItemSubtype(String entityReference,
			String labelKey, String newSubtype);

	public void reviseRepeatingCalendarItemTime(String entityReference, Date newFirstTime, Date newLastTime);

	public void reviseRepeatingCalendarItemTitle(String entityReference, String newTitle);

	/**
	 * If an entity uses some form of scheduled release, this method is called to 
	 * perform a check at the scheduled-release time to add links for that item. 
	 * 
	 * @param entityReference
	 * @param entityTypeId 
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

	public String getEntityIconUrl(String type, String subtype);

	public List<NewsItem> getMOTD();

	public Date getRepeatingEventHorizon();

	public void reviseCalendarItemTime(String entityReference, String labelKey,
			Integer sequenceNumber, Date newDate);

	public int getLastIndexInSequence(String entityReference,
			String calendarTimeLabelKey);

	public SortedSet<Integer> getFutureSequnceNumbers(String entityReference,
			String calendarTimeLabelKey, Integer firstSequenceNumber);

	public void removeCalendarLinks(String entityReference,
			String calendarTimeLabelKey, int sequenceNumber);

	// todo:
	// add methods to revise news items, calendar items, news links, calendar links, etc.
	
	
	// add methods to delete news items, calendar items, news links, calendar links, etc.
	
}
