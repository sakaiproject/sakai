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

import org.sakaiproject.dash.entity.DashboardEntityInfo;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.model.AvailabilityCheck;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;

/**
 * 
 *
 */
public interface DashboardLogic {

	public static final String MOTD_CONTEXT = "!site";
	
	public static final long ONE_DAY = 1000L * 60L * 60L * 24L;
	public static final long ONE_YEAR = ONE_DAY * 365L;
	
	public static final Integer DEFAULT_WEEKS_TO_HORIZON = new Integer(4);
	
	// strings to indicate which dashboard item type
	public static final String TYPE_NEWS = "news";
	public static final String TYPE_CALENDAR = "calendar";
	
	/**
	 * @param repeatingEvent
	 * @param oldHorizon
	 * @param newHorizon
	 */
	public void addCalendarItemsForRepeatingCalendarItem(
			RepeatingCalendarItem repeatingEvent, Date oldHorizon,
			Date newHorizon);

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
	 * Add or remove links to calendar or news items in a context for all users in the context. Links
	 * will be limited to items referencing entities for which the user has 
	 * access permission. This action may be limited by time or number, depending 
	 * on system settings defining policies for removal of calendar links. 
	 * @param contextId
	 * @param type
	 * @param true for adding; false for removing
	 */
	public void modifyLinksByContext(String contextId, String type, boolean addOrRemove);

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
	 * known to DashboardCommonLogic.
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
	 * known to DashboardCommonLogic.
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
	 * Creates and persists a SourceType object with specified identifier. Each registered DashboardEntityInfo must have one (and only one) SourceType 
	 * definition. After creating the SourceType definition, the method returns the complete SourceType object.
	 * @param resourceTypeIdentifier
	 * @return
	 */
	public SourceType createSourceType(String resourceTypeIdentifier);

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

	/**
	 * Returns the CalendarLink object based on the the unique integer identifer assigned to it when it was persisted.
	 * @param id
	 * @return
	 */
	public CalendarLink getCalendarLink(Long id);

	/**
	 * Returns a previously persisted Context object representing a sakai site with the specified contextId. 
	 * @param contextId
	 * @return the Context object, or null if it is not defined.
	 */
	public Context getContext(String contextId);
	
	/**
	 * @param eventIdentifier
	 * @return
	 */
	public DashboardEntityInfo getDashboardEntityInfo(String Identifier);

	/**
	 * @param eventIdentifier
	 * @return
	 */
	public EventProcessor getEventProcessor(String eventIdentifier);

	/**
	 * @param entityReference
	 * @param calendarTimeLabelKey
	 * @param firstSequenceNumber
	 * @return
	 */
	public SortedSet<Integer> getFutureSequnceNumbers(String entityReference,
			String calendarTimeLabelKey, Integer firstSequenceNumber);

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

	/**
	 * Returns a RepeatingCalendarItem object that references the specified sakai entity and has 
	 * the specified label-key.
	 * @param entityReference
	 * @param calendarTimeLabelKey
	 * @return
	 */
	public RepeatingCalendarItem getRepeatingCalendarItem(String entityReference, String calendarTimeLabelKey);

	/**
	 * @return
	 */
	public Date getRepeatingEventHorizon();

	/**
	 * Retrieve the SourceType by its unique string identifier (corresponding to the identifier used when the corresponding DashboardEntityInfo was registered). 
	 * @param identifier 
	 * @return the SourceType object, or null if it is not defined.
	 */
	public SourceType getSourceType(String identifier);

	/**
	 * Check whether an entity is fully available to users with permission to access it 
	 * (i.e. it is not hidden or restricted through some form of conditional release).
	 * @param entityReference
	 * @param entityTypeId
	 * @return
	 */
	public boolean isAvailable(String entityReference, String entityTypeId);

	/**
	 * Register an DashboardEntityInfo, an object that can assist in providing information 
	 * about a particular entity of this type.
	 * @param dashboardEntityInfo
	 */
	public void registerEntityType(DashboardEntityInfo dashboardEntityInfo);

	/**
	 * Register an EventProcessor whose processEvent() method will be invoked to handle sakai events 
	 * with the identifier returned by the EventProcessor's getEventIdentifer() method.
	 * @param eventProcessor
	 */
	public void registerEventProcessor(EventProcessor eventProcessor);

	/**
	 * Remove a scheduled check for availability.
	 * @param entityReference
	 */
	public void removeAllScheduledAvailabilityChecks(String entityReference);

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
	 * @param entityReference
	 * @param calendarTimeLabelKey
	 * @param sequenceNumber
	 */
	public void removeCalendarLinks(String entityReference,
			String calendarTimeLabelKey, int sequenceNumber);

	/**
	 * Remove all news links and the news item referencing a particular entity.
	 * @param entityReference
	 */
	public void removeNewsItem(String entityReference);

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

	/**
	 * Change the value of the label key to newLabelKey for all calendar items with specified
	 * entityReference and oldLabelKey. Does nothing if any of the parameters are null.  
	 * @param entityReference
	 * @param oldLabelKey
	 * @param newLabelKey
	 */
	public void reviseCalendarItemsLabelKey(String entityReference, String oldLabelKey, String newLabelKey);

	/**
	 * @param entityReference
	 * @param newTime
	 */
	public void reviseCalendarItemsTime(String entityReference, Date newTime);

	/**
	 * @param entityReference
	 * @param newTitle
	 */
	public void reviseCalendarItemsTitle(String entityReference, String newTitle);

	/**
	 * @param entityReference
	 * @param labelKey
	 * @param sequenceNumber
	 * @param newDate
	 */
	public void reviseCalendarItemTime(String entityReference, String labelKey,
			Integer sequenceNumber, Date newDate);

	/**
	 * @param entityReference
	 * @param newTime
	 * @param newGroupingIdentifier
	 */
	public void reviseNewsItemTime(String entityReference, Date newTime, String newGroupingIdentifier);

	/**
	 * @param entityReference
	 * @param newTitle
	 * @param newNewsTime
	 * @param newLabelKey
	 * @param newGroupingIdentifier
	 */
	public void reviseNewsItemTitle(String entityReference, String newTitle, Date newNewsTime, String newLabelKey, String newGroupingIdentifier);

	/**
	 * @param entityReference
	 * @param frequency
	 * @return
	 */
	public boolean reviseRepeatingCalendarItemFrequency(String entityReference,
			String frequency);

	/**
	 * 
	 * @param entityReference
	 * @param oldType
	 * @param newType
	 */
	public void reviseRepeatingCalendarItemsLabelKey(String entityReference,
			String oldType, String newType);

	/**
	 * @param entityReference
	 * @param newFirstTime
	 * @param newLastTime
	 */
	public void reviseRepeatingCalendarItemTime(String entityReference, Date newFirstTime, Date newLastTime);

	/**
	 * @param entityReference
	 * @param newTitle
	 */
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
	 * If an entity uses some form of scheduled release, this method is called to 
	 * update the scheduled-release time. 
	 */
	public void updateScheduleAvailabilityCheck(String entityReference, String entityTypeId, Date scheduledTime);
	
	/**
	 * checks if an entity is scheduled for release has entry in the database table or not
	 */
	public boolean isScheduleAvailabilityCheckMade(String entityReference, String entityTypeId, Date scheduledTime);

	/**
	 * @param newHorizon
	 */
	public void setRepeatingEventHorizon(Date newHorizon);

	/**
	 * @param entityReference
	 */
	public void updateCalendarLinks(String entityReference);

	/**
	 * @param entityReference
	 */
	public void updateNewsLinks(String entityReference);

	/**
	 * Determines whether a particular task has been assigned to a server and, if so, whether 
	 * the current server has that assignment.  If the task has not been assigned to a server,
	 * the method attempts to claim the task. 
	 * @param task the unique identifier for the task.
	 * @return true if the task has been assigned to the current server, and false if the task
	 * has not been assigned or has been assigned to a different server.
	 */
	public boolean checkTaskLock(String task);

	public void updateTaskLock(String task);

	public void removeTaskLocks(String task);

}
