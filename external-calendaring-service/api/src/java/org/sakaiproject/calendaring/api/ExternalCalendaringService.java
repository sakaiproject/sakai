/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.calendaring.api;

import java.util.List;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;

import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.user.api.User;

/**
 * A service to provide integrations with external calendars using the iCalendar standard.
 * <p>
 * Tools and services can leverage this to generate an ICS file for an event, as well as make updates.
 * 
 * Ref: http://www.ietf.org/rfc/rfc2445.txt
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public interface ExternalCalendaringService {

	/**
	 * Creates an iCal VEvent for a Sakai CalendarEvent.
	 * This must then be turned into a Calendar before it can be turned into an ICS file.
	 * 
	 * <br>If the CalendarEvent has the field 'vevent_uuid', that will be used as the UUID of the VEvent preferentially.
	 * <br>If the CalendarEvent has the field 'vevent_sequence', that will be used as the sequence of the VEvent preferentially.
	 * <br>If the CalendarEvent has the field 'vevent_url', that will be added to the URL property of the VEvent.
	 * 
	 * @param event Sakai CalendarEvent
	 * @return the VEvent for the given event or null if there was an error
	 */
	public VEvent createEvent(CalendarEvent event);
	
	/**
	 * Creates an iCal VEvent for a Sakai CalendarEvent with the given attendees.
	 * This must then be turned into a Calendar before it can be turned into an ICS file.
	 * 
	 * <br>If the CalendarEvent has the field 'vevent_uuid', that will be used as the UUID of the VEvent preferentially.
	 * <br>If the CalendarEvent has the field 'vevent_sequence', that will be used as the sequence of the VEvent preferentially.
	 * <br>If the CalendarEvent has the field 'vevent_url', that will be added to the URL property of the VEvent.
	 * 
	 * @param event Sakai CalendarEvent
	 * @param attendees list of Users that have been invited to the event
	 * @return the VEvent for the given event or null if there was an error
	 */
	public VEvent createEvent(CalendarEvent event, List<User> attendees);
	
	/**
	 * Adds a list of attendees to an existing VEvent.
	 * This must then be turned into a Calendar before it can be turned into an ICS file. 
	 * 
	 * @param vevent  The VEvent to add the attendess too
	 * @param attendees list of Users that have been invited to the event
	 * @return the VEvent for the given event or null if there was an error
	 */
	public VEvent addAttendeesToEvent(VEvent vevent, List<User> attendees);
	
	/**
	 * Adds a list of attendees to an existing VEvent with the chair role.
	 * This must then be turned into a Calendar before it can be turned into an ICS file.
	 *
	 * @param vevent  The VEvent to add the attendess too
	 * @param attendees list of Users that will chair the event
	 * @return the VEvent for the given event or null if there was an error
	 */
	public VEvent addChairAttendeesToEvent(VEvent vevent, List<User> attendees);
	
	/**
	 * Set the status of an existing VEvent to cancelled.
	 * This must then be turned into a Calendar before it can be turned into an ICS file.
	 * 
	 * @param vevent The VEvent to cancel
	 * @return the updated VEvent
	 */
	public VEvent cancelEvent(VEvent vevent);
	
	/**
	 * Creates an iCal calendar from a list of VEvents.
	 * 
	 * @param events iCal VEvents
	 * @return the Calendar for the given events or null if there was an error
	 */
	public Calendar createCalendar(List<VEvent> events);
	
	/**
	 * Creates an iCal calendar from a list of VEvents with a specified method.
	 *
	 * @param events iCal VEvents
	 * @param method the ITIP method for the calendar, e.g. "REQUEST"
	 * @return the Calendar for the given events or null if there was an error
	 */
	public Calendar createCalendar(List<VEvent> events, String method);
	
	/**
	 * Write an iCal calendar out to a file in the filesystem and return the path.
	 * @param calendar iCal calendar object
	 * @return the path to the file
	 */
	public String toFile(Calendar calendar);
	
	/**
	 * Is the ICS service enabled? Tools can use this public method for test in their own UIs.
	 * If this is disabled, nothing will be generated.
	 * @return
	 */
	public boolean isIcsEnabled();
	
	
}
