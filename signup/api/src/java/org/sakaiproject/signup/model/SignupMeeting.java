/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/api/src/java/org/sakaiproject/signup/model/SignupMeeting.java $
 * $Id: SignupMeeting.java 59241 2009-03-24 15:52:18Z guangzheng.liu@yale.edu $
***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *   
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.signup.logic.Permission;
import org.sakaiproject.signup.logic.SignupMessageTypes;

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
@Getter @Setter
public class SignupMeeting implements MeetingTypes, SignupMessageTypes {

	private Long id;

	private Long recurrenceId;

	@SuppressWarnings("unused")
	private int version;

	private String title;

	private String description;

	private String location;
	
	private String category;

	/* sakai user id */
	private String creatorUserId;
	
	private String coordinatorIds;

	private Date startTime;

	private Date endTime;

	private Date signupBegins;

	private Date signupDeadline;

	private boolean canceled;

	private boolean locked;

	private String meetingType;
	
	/*once,daily,weekdays,weekly,biweekly */
	private String repeatType; 
	
	private boolean allowWaitList;
	
	private boolean allowComment;
	
	private boolean autoReminder;
	
	private boolean eidInputMode;

	private boolean receiveEmailByOwner;
	
	private boolean sendEmailByOwner;

	private List<SignupTimeslot> signupTimeSlots;

	private List<SignupSite> signupSites;
	
	private List<SignupAttachment> signupAttachments;

	private Permission permission;
	
	//private boolean emailAttendeesOnly = false;
	
	private String sendEmailToSelectedPeopleOnly;
	
	private boolean allowAttendance;
	
	private boolean createGroups;
	
	private Integer maxNumOfSlots;
	
	//numbers of occurrences
	private int repeatNum;	

	private boolean applyToAllRecurMeetings;
	
	/* For RESTful case to pass siteId for email*/
	private String currentSiteId;
	
	private Calendar cal = Calendar.getInstance();

	private Date repeatUntil;
	
	private boolean inMultipleCalendarBlocks = false;
	
	
	/**
	 * ICS VEvent created for this meeting
	 */
	private net.fortuna.ical4j.model.component.VEvent vevent;
	
	/**
	 * For tracking the event so that we can issue updates, persisted, generated once, never updated.
	 */
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
	
	
}
