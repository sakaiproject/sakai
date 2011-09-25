/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/api/src/java/org/sakaiproject/signup/logic/messages/SignupEventTrackingInfoImpl.java $
 * $Id: SignupEventTrackingInfoImpl.java 56827 2009-01-13 21:52:18Z guangzheng.liu@yale.edu $
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
package org.sakaiproject.signup.logic.messages;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.signup.logic.SignupMessageTypes;
import org.sakaiproject.signup.logic.SignupTrackingItem;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.util.SignupDateFormat;

/**
 * <P>
 * This class implements the SignupEventTrackingInfo interface, which manage the
 * event tracking/recording and provides the necessary access methods to
 * retrieve all related information caused by an user action inside the Signup
 * tool
 * </P>
 */
public class SignupEventTrackingInfoImpl implements SignupEventTrackingInfo, SignupMessageTypes {

	private List<SignupTrackingItem> attendeeAllocationInfos;

	private SignupMeeting meeting;

	/**
	 * Constructor
	 * 
	 */
	public SignupEventTrackingInfoImpl() {
		this.attendeeAllocationInfos = new ArrayList<SignupTrackingItem>();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SignupTrackingItem> getAttendeeTransferInfos() {

		return this.attendeeAllocationInfos;
	}

	/* find the one if it's already in the list and if not, create a new one */
	private SignupTrackingItem findOrCreateAttendeeTransferInfo(SignupAttendee attendee, String messageType) {
		for (SignupTrackingItem allocInfo : attendeeAllocationInfos) {
			if (allocInfo.getAttendee().getAttendeeUserId().equals(attendee.getAttendeeUserId()))
				return allocInfo;
		}

		SignupTrackingItem stItem = new SignupTrackingItem(attendee, messageType);
		attendeeAllocationInfos.add(stItem);
		return stItem;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addOrUpdateAttendeeAllocationInfo(SignupAttendee attendee, SignupTimeslot timeslot, String messageType,
			boolean isInitiator) {
		SignupTrackingItem allocInfo = findOrCreateAttendeeTransferInfo(attendee, messageType);
		allocInfo.setInitiator(isInitiator);
		if (SIGNUP_ATTENDEE_PROMOTE.equals(messageType) || SIGNUP_ATTENDEE_SIGNUP_MOVE.equals(messageType)
				|| SIGNUP_ATTENDEE_SIGNUP.equals(messageType)) {
			allocInfo.setAddToTimeslot(timeslot);
			allocInfo.setMessageType(messageType);
		} else if (SIGNUP_ATTENDEE_CANCEL.equals(messageType)) {
			allocInfo.addToRemovedTimeslotList(timeslot);
			/*
			 * this is for initializer only and MessageType won't get changed
			 * for other people again.
			 */
			if (isInitiator)
				allocInfo.setMessageType(messageType);

		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void addOrUpdateAttendeeAllocationInfo(SignupAttendee attendee, SignupTimeslot timeslot, String messageType,
			boolean isInitiator, SignupAttendee replacedAttendee) {
		SignupTrackingItem allocInfo = findOrCreateAttendeeTransferInfo(attendee, messageType);
		allocInfo.setInitiator(isInitiator);
		if (SIGNUP_ATTENDEE_PROMOTE.equals(messageType) || SIGNUP_ATTENDEE_SIGNUP_MOVE.equals(messageType)) {
			allocInfo.setAddToTimeslot(timeslot);
			allocInfo.setMessageType(messageType);
		} else if (SIGNUP_ATTENDEE_CANCEL.equals(messageType)) {
			allocInfo.addToRemovedTimeslotList(timeslot);
			/*
			 * this is for initializer only and MessageType won't get changed
			 * for other people again.
			 */
			if (isInitiator)
				allocInfo.setMessageType(messageType);
		} else if (SIGNUP_ATTENDEE_SIGNUP_SWAP.equals(messageType)
				|| SIGNUP_ATTENDEE_SIGNUP_REPLACE.equals(messageType)) {
			allocInfo.setAddToTimeslot(timeslot);
			allocInfo.setMessageType(messageType);
			allocInfo.setReplacedAttendde(replacedAttendee);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public SignupTrackingItem getInitiatorAllocationInfo() {
		for (SignupTrackingItem allocInfo : attendeeAllocationInfos) {
			if (allocInfo.isInitiator())
				return allocInfo;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public SignupMeeting getMeeting() {
		return meeting;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMeeting(SignupMeeting meeting) {
		this.meeting = meeting;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAllAttendeeTransferLogInfo() {
		StringBuilder sb = new StringBuilder();
		List<SignupTrackingItem> trackItemList = this.getAttendeeTransferInfos();
		sb.append(" Acts:");
		for (SignupTrackingItem item : trackItemList) {
			if (item.getMessageType().equals(SIGNUP_ATTENDEE_SIGNUP)) {
				sb.append("-att:" + item.getAttendee().getAttendeeUserId() + " signUpToTs: "
						+ getTimeSlot(item.getAddToTimeslot()));
			} else if (item.getMessageType().equals(SIGNUP_ATTENDEE_CANCEL)) {
				sb.append("-att:" + item.getAttendee().getAttendeeUserId() + " canclFrTs: "
						+ getTimeSlot(item.getRemovedFromTimeslot()));
			} else if (item.getMessageType().equals(SIGNUP_ORGANIZER_REMOVE)) {
				sb.append("-att:" + item.getAttendee().getAttendeeUserId() + " rmvFrTs: "
						+ getTimeSlot(item.getRemovedFromTimeslot()));
			} else if (item.getMessageType().equals(SIGNUP_ATTENDEE_PROMOTE)) {
				sb.append("-att:" + item.getAttendee().getAttendeeUserId() + " promtToTs: "
						+ getTimeSlot(item.getAddToTimeslot()));
			} else if (item.getMessageType().equals(SIGNUP_ATTENDEE_SIGNUP_SWAP)) {
				sb.append("-att:" + item.getAttendee().getAttendeeUserId() + " swapToTs: "
						+ getTimeSlot(item.getAddToTimeslot()));
			} else if (item.getMessageType().equals(SIGNUP_ATTENDEE_SIGNUP_MOVE)) {
				sb.append("-att:" + item.getAttendee().getAttendeeUserId() + " movToTs: "
						+ getTimeSlot(item.getAddToTimeslot()));
			} else if (item.getMessageType().equals(SIGNUP_ATTENDEE_SIGNUP_REPLACE)) {
				sb.append("-att:" + item.getAttendee().getAttendeeUserId() + " add(replace)ToTs: "
						+ getTimeSlot(item.getAddToTimeslot()));
			}

		}
		sb.append(" on " + SignupDateFormat.format_date_mm_dd_yy(meeting.getStartTime()));
		return sb.toString();
	}

	/* display time slot in a string */
	private String getTimeSlot(SignupTimeslot timeslot) {
		String s = " " + SignupDateFormat.format_h_mm_a(timeslot.getStartTime()) + "-"
				+ SignupDateFormat.format_h_mm_a(timeslot.getEndTime()) + " ";
		return s;
	}

	/* display a list of time slots in a formatted string */
	private String getTimeSlot(List<SignupTimeslot> timeslots) {
		if (timeslots == null || timeslots.isEmpty())
			return " ";
		StringBuilder sb = new StringBuilder();
		for (SignupTimeslot timeslot : timeslots) {
			sb.append(" " + SignupDateFormat.format_h_mm_a(timeslot.getStartTime()) + "-"
					+ SignupDateFormat.format_h_mm_a(timeslot.getEndTime()) + " ");
		}
		return sb.toString();
	}

}
