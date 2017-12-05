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

package org.sakaiproject.signup.tool.jsf.organizer.action;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupEmailFacade;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.logic.SignupMessageTypes;
import org.sakaiproject.signup.logic.messages.SignupEventTrackingInfo;
import org.sakaiproject.signup.logic.messages.SignupEventTrackingInfoImpl;
import org.sakaiproject.signup.model.SignupAttachment;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;
import org.sakaiproject.signup.tool.util.Utilities;

/**
 * <p>
 * This is a abstract base class, which will provide most commen members and
 * shared methods for children user action classes.
 * </P>
 */
@Slf4j
public abstract class SignupAction implements SignupBeanConstants{

	protected final String userId;

	protected final String siteId;

	protected final SignupMeetingService signupMeetingService;

	protected SignupEventTrackingInfo signupEventTrackingInfo;

	protected final boolean isOrganizer;

	/**
	 * Constructor
	 * 
	 * @param userId
	 *            an unique sakai internal user id.
	 * @param siteId
	 *            an unique sakai site id.
	 * @param signupMeetingService
	 *            a SignupMeetingService obect.
	 */
	public SignupAction(String userId, String siteId, SignupMeetingService signupMeetingService, boolean isOrganizer) {
		this.userId = userId;
		this.siteId = siteId;
		this.signupMeetingService = signupMeetingService;
		this.isOrganizer = isOrganizer;

		this.signupEventTrackingInfo = new SignupEventTrackingInfoImpl();
	}

	/**
	 * Remove the attendee from the waiting list in an event/meeting.
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param attendee
	 *            a SignupAttendee object.
	 */
	public void removeAttendeeFromWaitingList(SignupMeeting meeting, SignupAttendee attendee) {
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		for (SignupTimeslot timeslot : signupTimeSlots) {
			List<SignupAttendee> waiters = timeslot.getWaitingList();
			SignupAttendee waiter = timeslot.getWaiter(attendee.getAttendeeUserId());
			if (waiter != null)
				waiters.remove(waiter);
		}

	}

	/**
	 * Remove the attendee from the waiting list at a specific time slot.
	 * 
	 * @param timeslot
	 *            a SingupTimeslot object.
	 * @param attendee
	 *            a SignupAttendee object.
	 */
	protected void removeAttendeeFromWaitingList(SignupTimeslot timeslot, SignupAttendee attendee) {
		List<SignupAttendee> waiters = timeslot.getWaitingList();
		SignupAttendee waiter = timeslot.getWaiter(attendee.getAttendeeUserId());
		if (waiter != null)
			waiters.remove(waiter);

	}

	/** remove the promoted attendee from all the waiting list */
	protected void promoteAttendeeFromWaitingList(SignupMeeting meeting, SignupTimeslot timeSlot) {

		List<SignupAttendee> attendees = timeSlot.getAttendees();
		/*
		 * if timeslot has more attendees than the max since orgranizer can add
		 * any number of attendees -> then don't promote
		 */
		if (attendees != null && timeSlot.getMaxNoOfAttendees() <= attendees.size())
			return;

		List<SignupAttendee> waitingList = timeSlot.getWaitingList();
		if (waitingList == null || waitingList.isEmpty())
			return;

		SignupAttendee promotedAttendee = waitingList.get(0);

		SignupAttendee att = new SignupAttendee();
		att.setAttendeeUserId(promotedAttendee.getAttendeeUserId());
		att.setComments(promotedAttendee.getComments());
		// TODO: att.setCalendarEventId(promotedAttendee.getCalendarEventId());
		att.setSignupSiteId(promotedAttendee.getSignupSiteId());
		timeSlot.getAttendees().add(att);
		/**
		 * Removing from waiting list first is important. This will prevent
		 * infinite loop
		 */
		removeAttendeeFromWaitingList(meeting, promotedAttendee);

		signupEventTrackingInfo.addOrUpdateAttendeeAllocationInfo(att, timeSlot,
				SignupEmailFacade.SIGNUP_ATTENDEE_PROMOTE, false);

		removeAttendeeFromAttendeesList(meeting, timeSlot, promotedAttendee);

	}

	/**
	 * when one attendee is removed from the Singup list, the people on waiting
	 * list will be promoted into this spot.
	 */
	protected void removeAttendeeFromAttendeesList(SignupMeeting meeting, SignupTimeslot currentTimeslot,
			SignupAttendee attendee) {
		String attendeeUserId = attendee.getAttendeeUserId();
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		int maxAllowedTimeslotsPerAttn = meeting.getMaxNumOfSlots();
		int currentCountForAttn = 1;
		for (SignupTimeslot upToDateTimeslot : signupTimeSlots) {
			/* prevent from removing this attendee from just promoted spot */
			if (currentTimeslot.getId().equals(upToDateTimeslot.getId()))
				continue;

			List<SignupAttendee> attendees = upToDateTimeslot.getAttendees();
			/*TODO we only remove attn one time (randomly by now) since it allows multiple timeslots*/
			boolean foundAttendee = false; 
			for (Iterator iter = attendees.iterator(); iter.hasNext();) {
				SignupAttendee att = (SignupAttendee) iter.next();
				if (attendeeUserId.equals(att.getAttendeeUserId())) {
					currentCountForAttn++;
					if(currentCountForAttn > maxAllowedTimeslotsPerAttn){
						iter.remove();
						signupEventTrackingInfo.addOrUpdateAttendeeAllocationInfo(att, upToDateTimeslot,
								SignupEmailFacade.SIGNUP_ATTENDEE_CANCEL, false);
	
						promoteAttendeeFromWaitingList(meeting, upToDateTimeslot);
						foundAttendee=true;
						break;
					}
				}
			}
			
			if(foundAttendee){
				break;
			}
		}
	}

	/**
	 * Get the SignupEventTrackingInfo object.
	 * 
	 * @return a SignupEventTrackingInfo object.
	 */
	public SignupEventTrackingInfo getSignupEventTrackingInfo() {
		return signupEventTrackingInfo;
	}

	/**
	 * This is a setter method.
	 * 
	 * @param signupEventTrackingInfo
	 *            a SignupEventTrackingInfo object.
	 */
	public void setSignupEventTrackingInfo(SignupEventTrackingInfo signupEventTrackingInfo) {
		this.signupEventTrackingInfo = signupEventTrackingInfo;
	}
	
	public List<SignupAttachment> getAttendeeAttachments(List<SignupAttachment> sAttachList) {
		return getCorrespondingAttachment(sAttachList, false);
	}


	public List<SignupAttachment> getEventMainAttachments(List<SignupAttachment> sAttachList) {
		return getCorrespondingAttachment(sAttachList, true);
	}
	
	private List<SignupAttachment> getCorrespondingAttachment(List<SignupAttachment> sAttachList, boolean isMainEventAttachs){
		List<SignupAttachment> tmp = new ArrayList<SignupAttachment>();
		if(sAttachList != null){
			for (SignupAttachment attach: sAttachList) {
				if(isMainEventAttachs){
					if( attach.getTimeslotId() ==null)
						tmp.add(attach);
				}else {
					if(attach.getTimeslotId() !=null && ! attach.getViewByAll())
						tmp.add(attach);
				}
					
				
				//TODO other cases: such as attachment for a specific time slot only.
			}
		}
		return tmp;
	}
	
	// Generate a group title based on the input given
	public String generateGroupTitle(String meetingTitle, SignupTimeslot timeslot, int rowNum) {
		
		//Based on the database limitation
		final int TITLE_MAX_LENGTH = 99;
		final char SEPARATOR = '-';

		final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		int titleSize = TITLE_MAX_LENGTH - SakaiFacade.GROUP_PREFIX.length();
		String dateString = df.format(timeslot.getStartTime());
		StringBuilder sb = new StringBuilder(titleSize);
		
		sb.append(" ");
		sb.append(SEPARATOR);
		sb.append(Utilities.rb.getString("group_slot_in_group_titlename"));
		sb.append(" " + rowNum);
		titleSize -= sb.length();
		//take the dateString length away with " _" prefix, it fixed recurring events duplicate groupName issue
		titleSize -= dateString.length() + 2;
		
		if (titleSize > 0)
			sb.insert(0, meetingTitle.substring(0, Math.min(titleSize, meetingTitle.length())));
		
		//fixes recurring meeting with the same tile, which causes the same groupName
		sb.append(" _" + dateString);

		return sb.toString();
	}
	
	public String getFormatTimeslotDateTime(SignupTimeslot timeslot){
		
		final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		final char SEPARATOR = '-';
		StringBuilder sb = new StringBuilder();		
		sb.append(df.format(timeslot.getStartTime()));
		sb.append(SEPARATOR);
		sb.append(df.format(timeslot.getEndTime()));
		
		return sb.toString();		
	}
	
	//generate a group description
	public String generateGroupDescription(String meetingTitle, SignupTimeslot timeslot) {		
		Object[] params = new Object[] { getFormatTimeslotDateTime(timeslot)};
		return MessageFormat.format(Utilities.rb.getString("group_description_default"),params);
	}
	//convert a list of SignupAttendees to a list of userIds
	public List<String> convertAttendeesToUuids(List<SignupAttendee> attendees) {
		
		List<String> uuids = new ArrayList<String>();
		
		for(SignupAttendee a: attendees) {
			uuids.add(a.getAttendeeUserId());
		}
		
		return uuids;
	}

}
