/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.sakaiproject.signup.logic.Permission;
import org.sakaiproject.signup.logic.SignupMessageTypes;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * This class holds the information for signup meeting/event. This object is
 * mapped directly to the DB storage by Hibernate
 * </p>
 */
/**
 * @author gl256
 *
 */
@Entity
@Table(name = "signup_meetings")
@Getter @Setter
public class SignupMeeting implements MeetingTypes, SignupMessageTypes, PersistableEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "signup_meeting_seq")
	@SequenceGenerator(name = "signup_meeting_seq", sequenceName = "signup_meeting_ID_SEQ")
	@Column(name = "id")
	private Long id;

	@Column(name = "recurrence_id")
	private Long recurrenceId;

	@Version
	@Column(name = "version")
	@SuppressWarnings("unused")
	private int version;

	@Column(name = "title", length = 255, nullable = false)
	private String title;

	@Lob
	@Column(name = "description")
	private String description;

	@Column(name = "location", length = 255, nullable = false)
	private String location;

	@Column(name = "category", length = 255)
	private String category;

	/* sakai user id */
	@Column(name = "creator_user_id", length = 99, nullable = false)
	private String creatorUserId;

	@Column(name = "coordinators_user_Ids", length = 1000)
	private String coordinatorIds;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start_time", nullable = false)
	private Date startTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_time", nullable = false)
	private Date endTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "signup_begins")
	private Date signupBegins;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "signup_deadline")
	private Date signupDeadline;

	@Column(name = "canceled")
	private boolean canceled;

	@Column(name = "locked")
	private boolean locked;

	@Column(name = "meeting_type", length = 50, nullable = false)
	private String meetingType;

	/*once,daily,weekdays,weekly,biweekly */
	@Column(name = "repeat_type", length = 20)
	private String repeatType;

	@Column(name = "allow_waitList")
	private boolean allowWaitList;

	@Column(name = "allow_comment")
	private boolean allowComment;

	@Column(name = "auto_reminder")
	private boolean autoReminder;

	@Column(name = "eid_input_mode")
	private boolean eidInputMode;

	@Column(name = "receive_email_owner")
	private boolean receiveEmailByOwner;

	@Column(name = "default_send_email_by_owner")
	private boolean sendEmailByOwner;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.ALL)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@BatchSize(size = 50)
	@org.hibernate.annotations.ForeignKey(name = "none")
	@OrderColumn(name = "list_index")
	@javax.persistence.JoinColumn(name = "meeting_id", nullable = false)
	private List<SignupTimeslot> signupTimeSlots;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.ALL)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@BatchSize(size = 50)
	@org.hibernate.annotations.ForeignKey(name = "none")
	@OrderColumn(name = "list_index")
	@javax.persistence.JoinColumn(name = "meeting_id", nullable = false)
	private List<SignupSite> signupSites;

	@org.hibernate.annotations.CollectionOfElements(fetch = FetchType.EAGER)
	@org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.ALL)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@BatchSize(size = 50)
	@OrderColumn(name = "list_index")
	@javax.persistence.JoinTable(name = "signup_attachments", joinColumns = @javax.persistence.JoinColumn(name = "meeting_id", nullable = false))
	private List<SignupAttachment> signupAttachments;

	@Transient
	private Permission permission;

	//private boolean emailAttendeesOnly = false;

	@Transient
	private String sendEmailToSelectedPeopleOnly;

	@Column(name = "allow_attendance")
	private boolean allowAttendance;

	@Column(name = "create_groups")
	private boolean createGroups;

	@Column(name = "maxnumof_slot")
	private Integer maxNumOfSlots;

	//numbers of occurrences
	@Transient
	private int repeatNum;

	@Transient
	private boolean applyToAllRecurMeetings;

	/* For RESTful case to pass siteId for email*/
	@Transient
	private String currentSiteId;

	@Transient
	private Calendar cal = Calendar.getInstance();

	@Transient
	private Date repeatUntil;

	@Transient
	private boolean inMultipleCalendarBlocks = false;


	/**
	 * ICS VEvent created for this meeting
	 */
	@Transient
	private net.fortuna.ical4j.model.component.VEvent vevent;

	/**
	 * For tracking the event so that we can issue updates, persisted, generated once, never updated.
	 */
	@Column(name = "vevent_uuid", length = 36)
	@Setter(AccessLevel.PRIVATE)
	private String uuid;
	
	
	public SignupMeeting() {
		//set the meeting UUID only at construction time
		uuid = UUID.randomUUID().toString();
	}
	

	/**
	 * get how many slots allowed for one user to sign in in this meeting
	 * 
	 * @return Integer.
	 */
	public Integer getMaxNumOfSlots() {
		if(maxNumOfSlots == null){
			maxNumOfSlots= new Integer(1);//default
		}
		return maxNumOfSlots;
	}

	

	/**
	 * special setter.
	 * 
	 * @param endTime
	 *            the end time of the event/meeting
	 */
	public void setEndTime(Date endTime) {
		this.endTime = truncateSeconds(endTime);
	}

	/**
	 * This method obtains the number of time slots in the event/meeting
	 * 
	 * @return the number of time slots
	 */
	public int getNoOfTimeSlots() {
		return (signupTimeSlots == null) ? 0 : signupTimeSlots.size();
	}

	
	/**
	 * special setter
	 * 
	 * @param signupBegins
	 *            a time when the signup process starts
	 */
	public void setSignupBegins(Date signupBegins) {
		this.signupBegins = truncateSeconds(signupBegins);
	}


	/**
	 * special setter
	 * 
	 * @param signupDeadLine
	 *            the time when signup process stops
	 */
	public void setSignupDeadline(Date signupDeadLine) {
		this.signupDeadline = truncateSeconds(signupDeadLine);
	}


	/**
	 * special setter
	 * 
	 * @param startTime
	 *            the time when the event/meeting starts
	 */
	public void setStartTime(Date startTime) {
		this.startTime = truncateSeconds(startTime);
	}

	
	
	/**
	 * get the maximum nubmer of the attendees, which is allowed in one time
	 * slot
	 * 
	 * @return the maximum nubmer of the attendees
	 */
	public int getMaxNumberOfAttendees() {
		if (signupTimeSlots == null || signupTimeSlots.isEmpty()) {
			return 0;
		}

		return signupTimeSlots.get(0).getMaxNoOfAttendees();
	}

	/**
	 * test if two event/meeting objects are equal
	 */
	public boolean equals(Object object) {
		if (object == null || !(object instanceof SignupMeeting)) {
			return false;
		}
		SignupMeeting other = (SignupMeeting) object;

		if (id == null) {
			return false;
		}

		return id.equals(other.getId());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}	

	/**
	 * This method will obtain the SignupTimeslot object according to the
	 * timeslot Id
	 * 
	 * @param timeslotId
	 *            a timeslot Id
	 * @return a SignupTimeslot object
	 */
	public SignupTimeslot getTimeslot(Long timeslotId) {
		if (signupTimeSlots == null) {
			return null;
		}

		for (SignupTimeslot timeslot : signupTimeSlots) {
			if (timeslot.getId().equals(timeslotId)) {
				return timeslot;
			}
		}

		return null;
	}

	/**
	 * This method will obtain the number of participants signed
	 * 
	 * @return a int
	 */
	public int getParticipantsNum() {
	    return signupTimeSlots == null ? 0 : signupTimeSlots.stream().map(t -> t.getAttendees().size()).reduce(Integer::sum).orElse(0);
	}
	
	/**
	 * This method will check if the event/meeting is already expired
	 * 
	 * @return true if the event/meeting is expired
	 */
	public boolean isMeetingExpired() {
		Date today = new Date();
		// pastMeeting => today>endDate => value>0
		int value = today.compareTo(endTime);
		return value > 0;
	}

	/**
	 * This method will check if the current time has already passed the signup
	 * deadline
	 * 
	 * @return true if the current time has already passed the signup deadline
	 */
	public boolean isPassedDeadline() {
		Date today = new Date();
		int value = today.compareTo(signupDeadline);
		return value > 0;
	}


	/**
	 * This will test if the event/meeting is cross days
	 * 
	 * @return true if the event/meeting is cross days
	 */
	public boolean isMeetingCrossDays() {
		cal.setTime(getStartTime());
		int startingDay = cal.get(Calendar.DAY_OF_YEAR);
		cal.setTime(getEndTime());
		int endingDay = cal.get(Calendar.DAY_OF_YEAR);
		return (startingDay != endingDay);
	}

	/**
	 * This will test if the event/meeting is started to sign up
	 * 
	 * @return true if the sign-up begin time is before current time.
	 */
	public boolean isStartToSignUp() {
		return signupBegins.before(new Date());
	}

	/**
	 * Set the second value to zero. it only need to accurate to minutes level.
	 * Otherwise it may cause one minute shorter display confusion
	 * 
	 * @param time
	 *            a Date object
	 * @return a Date object
	 */
	private Date truncateSeconds(Date time) {
		/* set second to zero */
		if (time == null) {
			return null;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public boolean isRecurredMeeting() {
		if (recurrenceId != null || DAILY.equals(getRepeatType()) || WEEKLY.equals(getRepeatType())
				|| BIWEEKLY.equals(getRepeatType()) || WEEKDAYS.equals(getRepeatType()))  {
			return true;
		} else {
			return false;
		}
	}

	
	public boolean hasSignupAttachments(){
		if(this.signupAttachments ==null || this.signupAttachments.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Breaks up the coordinatorIds string into a list object
	 * @return a list of coordinator id strings
	 */
	public List<String> getCoordinatorIdsList(){

		List<String> coUsers = new ArrayList<String>();

		if(coordinatorIds != null && coordinatorIds.trim().length()>0){
			StringTokenizer userIdTokens = new StringTokenizer(coordinatorIds,"|");
			while(userIdTokens.hasMoreTokens()){
				String uId = userIdTokens.nextToken();
				coUsers.add(uId);
			}
		}

		return coUsers;
	}
}
