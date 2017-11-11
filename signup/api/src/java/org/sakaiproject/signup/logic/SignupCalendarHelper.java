/**
 * Copyright (c) 2007-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

package org.sakaiproject.signup.logic;

import java.util.List;
import java.util.Set;

import net.fortuna.ical4j.model.component.VEvent;

import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * Helper to create and modify CalendarEventEdit and VEvent objects from meetings and timeslots.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public interface SignupCalendarHelper {

	/**
	 * Create a simple CalendarEventEdit object for an overall meeting. Does not include any timeslots.
	 * @param m signup meeting.
	 * @return
	 */
	public CalendarEventEdit generateEvent(SignupMeeting m);
	
	/**
	 * Create a simple CalendarEventEdit object for a specific timeslot.
	 * @param m signup meeting. Needed since it stores the main pieces of data liketitle/description/location etc.
	 * @param ts the actual timeslot.
	 * @return
	 */
	public CalendarEventEdit generateEvent(SignupMeeting m, SignupTimeslot ts);

	/**
	 * Create a VEvent for a timeslot. Checks if it exists in the given timeslot already (can be transient)
	 * @param meeting	overall SignupMeeting
	 * @param ts		SignupTimeslot we need VEvent for
	 * @return
	 */
	public VEvent generateVEventForTimeslot(SignupMeeting meeting, SignupTimeslot ts);
	
	/**
	 * Create a VEvent for an overall meeting, no timeslots are included. Checks if it exists in the given meeting already (can be transient)
	 * @param meeting	overall SignupMeeting
	 * @return
	 */
	public VEvent generateVEventForMeeting(SignupMeeting meeting);
	
	/**
	 * Create a calendar for a list of VEvents and return the path to the file
	 * @param vevents	List of VEvents
	 * @return a path to the calendar file
	 */
	public String createCalendarFile(List<VEvent> vevents);
	
	/**
	 * Create a calendar for a list of VEvents and return the path to the file
	 * @param vevents	List of VEvents
	 * @param method	The ITIP method for the calendar, e.g. "REQUEST"
	 * @return a path to the calendar file
	 */
	public String createCalendarFile(List<VEvent> vevents, String method);
	
	/**
	 * Cancel an event
	 * @param vevent VEvent to cancel
	 * @return the updated VEvent
	 */
	public VEvent cancelVEvent(VEvent vevent);
	
	/**
	 * Add the list of Users to the VEvent as attendees
	 * @param vevent	VEvent to modify
	 * @param users		List of Users to add
	 * @return
	 */
	public VEvent addUsersToVEvent(VEvent vevent, Set<User> users);
	
	/**
	 * Add the list of SignupAttendees to the VEvent as attendees
	 * @param vevent	VEvent to modify
	 * @param attendees	List of Attendees to add
	 * @return
	 */
	public VEvent addAttendeesToVEvent(VEvent vevent, Set<SignupAttendee> attendees);
	
	/**
	 * Is ICS calendar generation enabled in the external calendaring service?
	 * @return	true/false
	 */
	public boolean isIcsEnabled();

	
}
