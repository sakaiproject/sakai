/**
 * Copyright (c) 2007-2017 The Apereo Foundation
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

package org.sakaiproject.signup.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * This class holds the information for signup time slot. This object is mapped
 * directly to the DB storage by Hibernate
 * </p>
 */
@Getter @Setter
public class SignupTimeslot implements Comparable{

	private Long id;

	@SuppressWarnings("unused")
	private int version;

	private Date startTime;

	private Date endTime;

	private boolean locked;
	
	private String groupId;

	private boolean canceled;

	private int maxNoOfAttendees;

	private boolean displayAttendees;// TODO : this should be moved to meeting class

	private List<SignupAttendee> attendees;

	private List<SignupAttendee> waitingList;
	
	private String startTimeString;

	private String endTimeString;

	/**
	 * ICS VEvent created for this timeslot, not persisted
	 */
	private net.fortuna.ical4j.model.component.VEvent vevent;
	
	/**
	 * For tracking the event so that we can issue updates, persisted, generated once, never updated.
	 */
	@Setter(AccessLevel.PRIVATE)
	private String uuid;
	
	/**
	 * a constants maximum number for attendees
	 */
	public static final int UNLIMITED = Integer.MAX_VALUE;

	/**
	 * constructor
	 * 
	 */
	public SignupTimeslot() {
		attendees = new ArrayList<SignupAttendee>();
		waitingList = new ArrayList<SignupAttendee>();
		
		//set the timeslot UUID only at construction time
		uuid = UUID.randomUUID().toString();
	}

	/**
	 * check if current time slot is available for adding more people
	 * 
	 * @return true if current time slot is available for adding more people
	 */
	public boolean isAvailable() {
		if (attendees == null)
			return true;

		return (attendees.size() < maxNoOfAttendees);
	}

	/**
	 * get the SignupAttendee object according to the attendee's Id
	 * 
	 * @param attendeeId
	 *            a attendee's Id
	 * @return a SignupAttendee object
	 */
	public SignupAttendee getAttendee(String attendeeId) {
		if (attendees == null)
			return null;
		for (SignupAttendee attendee : attendees) {
			if (attendee.getAttendeeUserId().equals(attendeeId))
				return attendee;
		}
		return null;
	}

	/**
	 * get the SignupAttendee object according to the attendee's Id, who is on
	 * the waiting list at the time slot
	 * 
	 * @param attendeeId
	 *            an attendee's Id
	 * @return a SignupAttendee object
	 */
	public SignupAttendee getWaiter(String attendeeId) {
		if (waitingList == null)
			return null;
		for (SignupAttendee waiter : waitingList) {
			if (waiter.getAttendeeUserId().equals(attendeeId))
				return waiter;
		}
		return null;
	}

	/**
	 * check if the time slot allows unlimited attendee to join
	 * 
	 * @return true if the time slot allows unlimited attendee to join
	 */
	public boolean isUnlimitedAttendee() {
		return (maxNoOfAttendees == UNLIMITED);
	}
	
	public int compareTo(Object o) throws ClassCastException{
		if(!(o instanceof SignupTimeslot))
			throw new ClassCastException("SignupTimeslot object expected.");

		int result = this.getStartTime().compareTo(((SignupTimeslot)o).getStartTime());
		return result;
	}

}
