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

package org.sakaiproject.signup.api.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import lombok.Data;
import org.sakaiproject.springframework.data.PersistableEntity;

/**
 * <p>
 * This class holds the information for signup time slot. This object is mapped
 * directly to the DB storage by Hibernate
 * </p>
 */
@Data
@Entity
@Table(name = "signup_ts")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SignupTimeslot implements Comparable, PersistableEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "signup_ts_seq")
	@SequenceGenerator(name = "signup_ts_seq", sequenceName = "signup_ts_ID_SEQ")
	private Long id;

	@Version
	@Column(name = "version")
	private int version;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start_time", nullable = false)
	private Date startTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_time", nullable = false)
	private Date endTime;

	@Column(name = "locked")
	private boolean locked;

	@Column(name = "group_id", length = 99)
	private String groupId;

	@Column(name = "canceled")
	private boolean canceled;

	@Column(name = "max_no_of_attendees")
	private int maxNoOfAttendees;

	@Column(name = "display_attendees")
	private boolean displayAttendees;// TODO : this should be moved to meeting class

	@ElementCollection
	@CollectionTable(name = "signup_ts_attendees", joinColumns = @JoinColumn(name = "timeslot_id", nullable = false))
	@OrderColumn(name = "list_index")
	private List<SignupAttendee> attendees;

	@ElementCollection
	@CollectionTable(name = "signup_ts_waitinglist", joinColumns = @JoinColumn(name = "timeslot_id", nullable = false))
	@OrderColumn(name = "list_index")
	private List<SignupAttendee> waitingList;

	@Transient
	private String startTimeString;

	@Transient
	private String endTimeString;

	/**
	 * ICS VEvent created for this timeslot, not persisted
	 */
	@Transient
	private net.fortuna.ical4j.model.component.VEvent vevent;

	/**
	 * For tracking the event so that we can issue updates, persisted, generated once, never updated.
	 */
	@Column(name = "vevent_uuid", length = 36)
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
		attendees = new ArrayList<>();
		waitingList = new ArrayList<>();
		
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
	 *            an attendee's Id
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
	
	/**
	 * This method will obtain number of participants total signed
	 * 
	 * @return a int
	 */
	public int getParticipantsNum() {
		if (attendees == null) {
			return 0;
		}else {
			return attendees.size();
		}
	}
	
	/**
	 * This method will obtain the number of participants total signed in the waiting list
	 * 
	 * @return a int
	 */
	public int getWaitingListNum() {
		if (waitingList == null) {
			return 0;
		}else {
			return waitingList.size();
		}
	}
}
