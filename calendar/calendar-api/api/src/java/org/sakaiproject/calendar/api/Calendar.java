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

import java.util.Collection;
import java.util.List;

import org.sakaiproject.calendar.api.CalendarEvent.EventAccess;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.Time;
import org.w3c.dom.Element;

/**
* <p>Calendar is the base interface for Calendar service calendars.</p>
* <p>Calendars contains collections of CalendarEvents.</p>
*/
public interface Calendar
	extends Entity
{
	/**
	* Access the context of the resource.
	* @return The context.
	*/
	public String getContext();

	/**
	 ** check if this calendar enables ical exports
	 ** @return true if the calender allows exports; false if not
	 **/
	public boolean getExportEnabled();

	/**
	 ** set if this calendar enables ical exports
	 **/
	public void setExportEnabled( boolean enable );

	/**
	 ** Get the time of the last modify to this calendar
	 ** @return String representation of current time
	 **/
	public Time getModified();

	/**
	 ** Set the time of the last modify for this calendar to now
	 **/
	public void setModified();

	/**
	* check permissions for getEvents() and getEvent() on a SITE / calendar level.
	* @return true if the user is allowed to get events from the calendar, false if not.
	*/
	public boolean allowGetEvents();
	
	/**
	* check permissions for getEvent() for a particular event.
	* @return true if the user is allowed to get the event from the calendar, false if not.
	*/
	public boolean allowGetEvent(String eventId);

	/**
	* Return a List of all or filtered events in the calendar.
	* The order in which the events will be found in the iteration is by event start date.
	* @param range A time range to limit the iterated events.  May be null; all events will be returned.
	* @param filter A filtering object to accept events into the iterator, or null if no filtering is desired.
	* @return a List of all or filtered CalendarEvents in the calendar (may be empty).
	* @exception PermissionException if the user does not have read permission to the calendar.
	*/
	public List getEvents(TimeRange range, Filter filter)
		throws PermissionException;

	/**
	* Return a specific calendar event, as specified by event name.
	* @param eventId The id of the event to get.
	* @return the CalendarEvent that has the specified id.
	* @exception IdUnusedException If this id is not a defined event in this calendar.
	* @exception PermissionException If the user does not have any permissions to read the calendar.
	*/
	public CalendarEvent getEvent(String eventId)
		throws IdUnusedException, PermissionException;

	/**
	* Return the extra fields kept for each event in this calendar.
	* @return the extra fields kept for each event in this calendar, formatted into a single string. %%%
	*/
	public String getEventFields();

	/**
	* check permissions for addEvent().
	* @return true if the user is allowed to addEvent(...), false if not.
	*/
	public boolean allowAddEvent();

	/**
	 * Check if the user has permission to add a calendar-wide (not grouped) message.
	 * 
	 * @return true if the user has permission to add a calendar-wide (not grouped) message.
	 */
	boolean allowAddCalendarEvent();

	/**
	* Add a new event to this calendar.
	* @param range The event's time range.
	* @param displayName The event's display name (PROP_DISPLAY_NAME) property value.
	* @param description The event's description (PROP_DESCRIPTION) property value.
	* @param type The event's calendar event type (PROP_CALENDAR_TYPE) property value.
	* @param location The event's calendar event location (PROP_CALENDAR_LOCATION) property value.
	* @param access The event's access type site or grouped
	* @param groups The groups which can access this event
	* @param attachments The event attachments, a vector of Reference objects.
	* @return The newly added event.
	* @exception PermissionException If the user does not have permission to modify the calendar.
	*/
	public CalendarEvent addEvent(TimeRange range, String displayName, String description,
									String type, String location, EventAccess access, Collection groups,  List attachments)
		throws PermissionException;

	/**
	* Add a new event to this calendar.
	* @param range The event's time range.
	* @param displayName The event's display name (PROP_DISPLAY_NAME) property value.
	* @param description The event's description (PROP_DESCRIPTION) property value.
	* @param type The event's calendar event type (PROP_CALENDAR_TYPE) property value.
	* @param location The event's calendar event location (PROP_CALENDAR_LOCATION) property value.
	* @param attachments The event attachments, a vector of Reference objects.
	* @return The newly added event.
	* @exception PermissionException If the user does not have permission to modify the calendar.
	*/
	public CalendarEvent addEvent(TimeRange range, String displayName, String description,
									String type, String location, List attachments)
		throws PermissionException;
	
	/**
	* Add a new event to this calendar.
	* Must commitEvent() to make official, or cancelEvent() when done!
	* @return The newly added event, locked for update.
	* @exception PermissionException If the user does not have write permission to the calendar.
	*/
	public CalendarEventEdit addEvent()
		throws PermissionException;

	/**
	* check permissions for editEvent()
	* @param id The event id.
	* @return true if the user is allowed to update the event, false if not.
	*/
	public boolean allowEditEvent(String eventId);

	/**
	* Return a specific calendar event, as specified by event name, locked for update.
	* Must commitEvent() to make official, or cancelEvent(), or removeEvent() when done!
	* @param eventId The id of the event to get.
	* @param editType add, remove or modifying calendar?
	* @return the Event that has the specified id.
	* @exception IdUnusedException If this name is not a defined event in this calendar.
	* @exception PermissionException If the user does not have any permissions to edit the event.
	* @exception InUseException if the event is locked for edit by someone else.
	*/
	public CalendarEventEdit getEditEvent(String eventId, String editType)
		throws IdUnusedException, PermissionException, InUseException;

	/**
	* Commit the changes made to a CalendarEventEdit object, and release the lock.
	* The CalendarEventEdit is disabled, and not to be used after this call.
	* @param edit The CalendarEventEdit object to commit.
	* @param intention The recurring event modification intention,
	* based on values in the GenericCalendarService "MOD_*",
	* used if the event is part of a recurring event sequence to determine how much of the sequence is changed by this commmit.
	*/
	public void commitEvent(CalendarEventEdit edit, int intention);

	/**
	* Commit the changes made to a CalendarEventEdit object, and release the lock.
	* The CalendarEventEdit is disabled, and not to be used after this call.
	* Note: if the event is a recurring event, the entire sequence is modified by this commit (MOD_ALL).
	* @param edit The CalendarEventEdit object to commit.
	*/
	public void commitEvent(CalendarEventEdit edit);

	/**
	* Cancel the changes made to a CalendarEventEdit object, and release the lock.
	* The CalendarEventEdit is disabled, and not to be used after this call.
	* @param edit The CalendarEventEdit object to commit.
	*/
	public void cancelEvent(CalendarEventEdit edit);

	/**
	* Merge in a new event as defined in the xml.
	* @param el The event information in XML in a DOM element.
	* @exception PermissionException If the user does not have write permission to the calendar.
	* @exception IdUsedException if the user id is already used.
	*/
	public CalendarEventEdit mergeEvent(Element el)
		throws PermissionException, IdUsedException;

	/**
	* check permissions for removeEvent().
	* @param event The event from this calendar to remove.
	* @return true if the user is allowed to removeEvent(event), false if not.
	*/
	public boolean allowRemoveEvent(CalendarEvent event);

	/**
	* Remove an event from the calendar, one locked for edit.
	* @param edit The event from this calendar to remove.
	* @param intention The recurring event modification intention,
	* based on values in the GenericCalendarService "MOD_*",
	* used if the event is part of a recurring event sequence to determine how much of the sequence is removed.
	* @throws PermissionException if the end user does not have permission to remove.
	*/
	public void removeEvent(CalendarEventEdit edit, int intention) throws PermissionException;

	/**
	* Remove an event from the calendar, one locked for edit.
	* Note: if the event is a recurring event, the entire sequence is removed by this commit (MOD_ALL).
	* @param edit The event from this calendar to remove.
	* @throws PermissionException if the end user does not have permission to remove.
	*/
	public void removeEvent(CalendarEventEdit edit) throws PermissionException;
	
	/**
	 * Get the collection of Groups defined for the context of this calendar that the end user has add event permissions in.
	 * 
	 * @return The Collection (Group) of groups defined for the context of this calendar that the end user has add event permissions in, empty if none.
	 */
	Collection getGroupsAllowAddEvent();

	/**
	 * Get the collection of Group defined for the context of this calendar that the end user has get event permissions in.
	 * 
	 * @return The Collection (Group) of groups defined for the context of this calendar that the end user has get event permissions in, empty if none.
	 */
	Collection getGroupsAllowGetEvent();

	/**
	 * Get the collection of Group defined for the context of this channel that the end user has remove event permissions in.
    *
    * @param own boolean flag indicating whether user owns event
	 * 
	 * @return The Collection (Group) of groups defined for the context of this channel that the end user has remove event permissions in, empty if none.
	 */
	Collection getGroupsAllowRemoveEvent( boolean own );
	
	/**
	 * Checks if user has permission to modify any event (or fields) in this calendar
	 * @param function
	 * @return
	 */
	public boolean canModifyAnyEvent(String function);


}	// Calendar



