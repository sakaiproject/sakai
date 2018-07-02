/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.calendar.api;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.*;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;

import java.util.List;

/**
* <p>CalendarService is the interface for the Calendar service.</p>
* <p>The service manages a set of calendars, each containing a set of events.</p>
*/
public interface CalendarService
	extends EntityProducer
{
	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
	static final String APPLICATION_ID = "sakai:calendar";

	/** This string starts the references to resources in this service. */
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "calendar";

	/** Name for the event of adding a calendar */
	public static final String EVENT_CREATE_CALENDAR = "calendar.create";

	/** Name for the event of adding a calendar event. */
	public static final String EVENT_ADD_CALENDAR = "calendar.new";

	/** Name for the event of removing a calendar event */
	public static final String EVENT_REMOVE_CALENDAR = "calendar.delete";

	/** Name for the event of removing or changing any events in a calendar. */
	public static final String EVENT_MODIFY_CALENDAR = "calendar.revise";
	
	/** Name for the event of changing event title */
	public static final String EVENT_MODIFY_CALENDAR_EVENT_TITLE = "calendar.revise.event.title";
	
	/** Name for the event of changing the start time of a calendar event */
	public static final String EVENT_MODIFY_CALENDAR_EVENT_TIME = "calendar.revise.event.time";
	
	/** Name for the event of changing the start time of a calendar event */
	public static final String EVENT_MODIFY_CALENDAR_EVENT_TYPE = "calendar.revise.event.type";
	
	/** Name for the event of changing event access settings */
	public static final String EVENT_MODIFY_CALENDAR_EVENT_ACCESS = "calendar.revise.event.access";
	
	/** Name for the event of changing event frequency */
	public static final String EVENT_MODIFY_CALENDAR_EVENT_FREQUENCY = "calendar.revise.event.frequency";
	
	/** Name for the event of adding (or removing) an item in the list of exclusions for a recurring event (the entity ref identifies the recurring event and the index of excluded item) */
	public static final String EVENT_MODIFY_CALENDAR_EVENT_EXCLUSIONS = "calendar.revise.event.exclusions";

	/** Name for the event of excluding an item from a recurring event (the entity ref identifies the newly independent event) */
	public static final String EVENT_MODIFY_CALENDAR_EVENT_EXCLUDED = "calendar.revise.event.excluded";

	/** Name for the event of deleting a calendar event or a repeating calendar event */ 
	public static final String EVENT_REMOVE_CALENDAR_EVENT = "calendar.delete.event";
	
   /** Security lock for adding a calendar event */
	public static final String AUTH_ADD_CALENDAR = "calendar.new";

   /** Security lock for removing any calendar event */
	public static final String AUTH_REMOVE_CALENDAR_ANY = "calendar.delete.any";

   /** Security lock for removing user's own calendar event */
	public static final String AUTH_REMOVE_CALENDAR_OWN = "calendar.delete.own";

	/** Security lock for changing any events (or fields) in a calendar. */
	public static final String AUTH_MODIFY_CALENDAR_ANY = "calendar.revise.any";

	/** Security lock for changing user's own events in a calendar. */
	public static final String AUTH_MODIFY_CALENDAR_OWN = "calendar.revise.own";

	/** Security lock for importing events into a calendar. */
	public static final String AUTH_IMPORT_CALENDAR = "calendar.import";

	/** Security lock for subscribing external calendars. */
	public static final String AUTH_SUBSCRIBE_CALENDAR = "calendar.subscribe";

	/** Security lock for adding to a calendar. */
	public static final String AUTH_READ_CALENDAR = "calendar.read";

	/** Security function granted to users who will then have membership in all site groups based on their site membership. */
	public static final String AUTH_ALL_GROUPS_CALENDAR = "calendar.all.groups";
	
	/** Security lock for adding to a calendar. */
	public static final String AUTH_OPTIONS_CALENDAR = "calendar.options";

	/** Security lock for viewing who the event is for. */
	public static final String AUTH_VIEW_AUDIENCE = "calendar.view.audience";

	/** The Reference type for a calendar. */
	public static final String REF_TYPE_CALENDAR = "calendar";

	/** The Reference type for a calendar pdf. */
	public static final String REF_TYPE_CALENDAR_PDF = "calpdf";

	/** The Reference type for a calendar pdf. */
	public static final String REF_TYPE_CALENDAR_ICAL = "ical";

	/** The Reference type for a external calendar subscription. */
	public static final String REF_TYPE_CALENDAR_SUBSCRIPTION = "subscription";

	/** Calendar property to enable ical export */
	//(tbd: move to ResourceProperties) 
	public static final String PROP_ICAL_ENABLE = "ICAL:enable";
	
	/** The Reference type for an event. */
	public static final String REF_TYPE_EVENT = "event";
	
	/** The Reference type for a subscripted event. */
	public static final String REF_TYPE_EVENT_SUBSCRIPTION = "eventsubscripted";

	/** Recurring event modification intention: no intention. */
	public static final int MOD_NA = 0;

	/** Recurring event modification intention: just this one. */
	public static final int MOD_THIS = 1;

	/** Recurring event modification intention: all. */
	public static final int MOD_ALL = 2;

	/** Recurring event modification intention: this and subsequent. */
	public static final int MOD_REST = 3;

	/** Recurring event modification intention: this and prior. */
	public static final int MOD_PRIOR = 4;

	/** Calendar Printing Views. */
	public static final int UNKNOWN_VIEW = -1;
	public static final int DAY_VIEW = 0;
	public static final int WEEK_VIEW = 2;
	public static final int MONTH_VIEW = 3;
	public static final int LIST_VIEW = 5;
	
	/** Security function / event for reading site / event. */
	public static final String SECURE_READ = "read";

	/** Security function / event for adding site / event. */
	public static final String SECURE_ADD = "new";
	
	/** Security function / event for removing event */
	public static final String SECURE_REMOVE = "delete";

	/** Security function / event for updating event. */
	public static final String SECURE_UPDATE = "revise";

	/** Security function giving the user permission to all groups, if granted to at the calendar or calendar level. */
	public static final String SECURE_ALL_GROUPS = "calendar.all.groups";

   /** session attribute for list of all calendars user can reference */   
   public static final String SESSION_CALENDAR_LIST = "calendar.ref.list";

	/** Security lock for subscribing to the implicit calendar. */
	public static final String AUTH_SUBSCRIBE_CALENDAR_THIS = "calendar.subscribe.this";
	
	/** The Reference type for an "Opaque URL" URL. */
	public static final String REF_TYPE_CALENDAR_OPAQUEURL = "opaq";
	
	/** Bean id to retrieve an additional calendar service from the Component Manager */
	public static final String ADDITIONAL_CALENDAR = "org.sakaiproject.additional.calendar";

	/**
	* Add a new calendar.
	* Must commitCalendar() to make official, or cancelCalendar() when done!
	* @param ref The new calendar reference.
	* @return The newly created calendar.
	* @exception IdUsedException if the id is not unique.
	* @exception IdInvalidException if the id is not made up of valid characters.
	* @exception PermissionException if the user does not have permission to add a calendar.
	*/
	public CalendarEdit addCalendar(String ref)
		throws IdUsedException, IdInvalidException, PermissionException;

	/**
	* check permissions for getCalendar().
	* @param ref The calendar reference.
	* @return true if the user is allowed to getCalendar(ref), false if not.
	*/
	public boolean allowGetCalendar(String ref);

	/**
	* Return a specific calendar.
	* @param ref The calendar reference.
	* @return the Calendar that has the specified name.
	* @exception IdUnusedException If this name is not defined for any calendar.
	* @exception PermissionException If the user does not have any permissions to the calendar.
	*/
	public Calendar getCalendar(String ref)
		throws IdUnusedException, PermissionException;

	/**
	* check permissions for importing calendar events
	* @param ref The calendar reference.
	* @return true if the user is allowed to import events, false if not.
	*/
	public boolean allowImportCalendar(String ref);

	/**
	* check permissions for subscribing external calendars
	* @param ref The calendar reference.
	* @return true if the user is allowed to subscribe external calendars, false if not.
	*/
	public boolean allowSubscribeCalendar(String ref);

	/**
	* check permissions for editCalendar() e.g. add/delete fields
	* @param ref The calendar reference.
	* @return true if the user is allowed to edit the calendar, false if not.
	*/
	public boolean allowEditCalendar(String ref);

	/**
	* check permissions for mergeCalendar()
	* @param ref The calendar reference.
	* @return true if the user is allowed to merge the calendar, false if not.
	*/
	public boolean allowMergeCalendar(String ref);

	/**
	* Get a locked calendar object for editing.
	* Must commitCalendar() to make official, or cancelCalendar() or removeCalendar() when done!
	* @param ref The calendar reference.
	* @return A CalendarEdit object for editing.
	* @exception IdUnusedException if not found, or if not an CalendarEdit object
	* @exception PermissionException if the current user does not have permission to mess with this user.
	* @exception InUseException if the Calendar object is locked by someone else.
	*/
	public CalendarEdit editCalendar(String ref)
		throws IdUnusedException, PermissionException, InUseException;

	/**
	* Commit the changes made to a CalendarEdit object, and release the lock.
	* The CalendarEdit is disabled, and not to be used after this call.
	* @param edit The CalendarEdit object to commit.
	*/
	public void commitCalendar(CalendarEdit edit);

	/**
	* Cancel the changes made to a CalendarEdit object, and release the lock.
	* The CalendarEdit is disabled, and not to be used after this call.
	* @param edit The CalendarEdit object to commit.
	*/
	public void cancelCalendar(CalendarEdit edit);

	/**
	* Remove a calendar that is locked for edit.
	* @param edit The calendar to remove.
	* @exception PermissionException if the user does not have permission to remove a calendar.
	*/
	public void removeCalendar(CalendarEdit edit)
		throws PermissionException;

	/**
	* Access the internal reference which can be used to access the calendar from within the system.
	* @param context The context.
	* @param id The calendar id.
	* @return The the internal reference which can be used to access the calendar from within the system.
	*/
	public String calendarReference(String context, String id);

	/**
	* Access the internal reference which can be used to access the calendar-in-pdf format from within the system.
	* @param context The context.
	* @param id The calendar id.
	* @param reverseOrder to order the event list as in ListView
	* @return The the internal reference which can be used to access the calendar-in-pdf format from within the system.
	*/
	public String calendarPdfReference(String context, String id, int scheduleType, String timeRangeString,
			String userName, TimeRange dailyTimeRange, boolean reverseOrder);

	/**
	* Access the internal reference which can be used to access the calendar-in-ical format from within the system.
	* @param ref The calendar reference
	* @return The the internal reference which can be used to access the calendar-in-pdf format from within the system.
	*/
	public String calendarICalReference(Reference ref);

	/**
	* Access the internal reference which can be used to access the external calendar subscription from within the system.
	* @param context The context.
	* @param id The calendar id.
	* @return The the internal reference which can be used to access the external calendar subscription from within the system.
	*/
	public String calendarSubscriptionReference(String context, String id);
	
	/**
	 ** Determine if public ical export for this calendar is enabled
	 ** @param ref the calendar reference
	 ** @return true if allowed, otherwise false
	 **/
 	public boolean getExportEnabled(String ref);
	
	/**
	 ** Enable public ical export for this calendar
	 ** @param ref the calendar reference
	 ** @param enable true to enable, otherwise false
	 **/
 	public void setExportEnabled(String ref, boolean enable);
	
	/**
	 * Access the internal reference which can be used to access the event from within the system.
	 * 
	 * @param context
	 *        The context.
	 * @param calendarlId
	 *        The channel id.
	 * @param id
	 *        The event id.
	 * @return The the internal reference which can be used to access the event from within the system.
	 */
	public String eventReference(String context, String calendarId, String id);
	
	/**
	 * Access the internal reference which can be used to access the subscripted event from within the system.
	 * 
	 * @param context
	 *        The context.
	 * @param calendarlId
	 *        The channel id.
	 * @param id
	 *        The event id.
	 * @return The the internal reference which can be used to access the subscripted event from within the system.
	 */
	public String eventSubscriptionReference(String context, String calendarId, String id);

	/**
	* Takes several calendar References and merges their events from within a given time range.
	* @param references The List of calendar References.
	* @param range The time period to use to select events.
	* @return CalendarEventVector object with the union of all events from the list of calendars in the given time range.
	*/
	public CalendarEventVector getEvents(List references, TimeRange range);
	
	/**
	 * Takes several calendar References and merges their events from within a given time range.
	 * 
	 * @param references The List of calendar References.
	 * @param range The time period to use to select events.
	 * @param reverseOrder CalendarEventVector object will be ordered reverse.       
	 * @return CalendarEventVector object with the union of all events from the list of calendars in the given time range.
	 */
	public CalendarEventVector getEvents(List references, TimeRange range, boolean reverseOrder);

	/**
	 * Construct a new recurrence rule who's frequency description matches the frequency parameter.
	 * @param frequency The frequency description of the desired rule.
	 * @return A new recurrence rule.
	 */
	RecurrenceRule newRecurrence(String frequency);

	/**
	 * Construct a new recurrence rule who's frequency description matches the frequency parameter.
	 * @param frequency The frequency description of the desired rule.
	 * @param interval The recurrence interval.
	 * @return A new recurrence rule.
	 */
	RecurrenceRule newRecurrence(String frequency, int interval);

	/**
	 * Construct a new recurrence rule who's frequency description matches the frequency parameter.
	 * @param frequency The frequency description of the desired rule.
	 * @param interval The recurrence interval.
	 * @param count The number of reecurrences limit.
	 * @return A new recurrence rule.
	 */
	RecurrenceRule newRecurrence(String frequency, int interval, int count);

	/**
	 * Construct a new recurrence rule who's frequency description matches the frequency parameter.
	 * @param frequency The frequency description of the desired rule.
	 * @param interval The recurrence interval.
	 * @param until The time after which recurrences stop.
	 * @return A new recurrence rule.
	 */
	RecurrenceRule newRecurrence(String frequency, int interval, Time until);

 	/**
 	 * check permissions for subscribing to the implicit calendar.
 	 * @param ref The calendar reference.
 	 * @return true if the user is allowed to subscribe to the implicit calendar, false if not.
 	 */
	public boolean allowSubscribeThisCalendar(String ref);
	
	/**
	 * Access the internal reference which can be used to access the calendar in iCal format from within the system, via an opaque URL.
	 * @param ref The calendar reference
	 * @return The the internal reference which can be used to access the calendar-in-pdf format from within the system.
	 */
	public String calendarOpaqueUrlReference(Reference ref);

	/**
	 * Returns all calendar references for a given siteId
	 * @param siteId Id of the site
	 * @return List object is returned with all calendar references
	 */
	public List<String> getCalendarReferences(String siteId);
	/**
	 * Returns the tool id value (i.e. sakai.schedule)
	 * @return
	 */
	public String getToolId();
	
	/**
	 * Checks the calendar has been created.
	 * @return
	 */
	public boolean isCalendarToolInitialized(String siteId);
}	// CalendarService



