/**********************************************************************************
 * $URL$
 * $Id$
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
package org.sakaiproject.signup.tool.jsf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;

import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.util.SignupDateFormat;

/**
 * <p>
 * This class is a wrapper class for SignupTimeslot for UI purpose
 * </P>
 */
public class TimeslotWrapper {

	private final SignupTimeslot timeSlot;

	//private String eids;

	private String currentUserId;

	private List<AttendeeWrapper> attendeeWrappers;

	private List<AttendeeWrapper> waitingList;

	private SignupAttendee newAttendee;

	private int rankingOnWaiting;

	private List<SelectItem> swapDropDownList;

	private List<SelectItem> moveAvailableTimeSlots;

	private int positionInTSlist;

	/**
	 * Constructor
	 * 
	 * @param slot
	 *            a SignupTimeslot object.
	 */
	public TimeslotWrapper(SignupTimeslot slot) {
		this.timeSlot = slot;
	}

	/**
	 * Constructor
	 * 
	 * @param slot
	 *            a SignupTimeslot object.
	 * @param currentUserId
	 *            a sakai unique internal user id.
	 */
	public TimeslotWrapper(SignupTimeslot slot, String currentUserId) {
		this.timeSlot = slot;
		this.currentUserId = currentUserId;
	}

	/**
	 * This is a getter method.
	 * 
	 * @return a string of Eids. /* public String getEids() { return eids; }
	 * 
	 * public void setEids(String eids) { this.eids = eids; }
	 * 
	 * public List<String> eids() { if (eids == null || eids.trim().length() ==
	 * 0) return null; // eids = eids.trim(); /* Split for any whitespace
	 */
	/*
	 * The tokenizer uses the default delimiter set,which is "\t\n\r\f":
	 * thespace character,the tabcharacter, thenewlinecharacter,
	 * thecarriage-returncharacter, andthe form-feedcharacter.
	 * 
	 * StringTokenizer token = new StringTokenizer(eids); List<String>
	 * attendees = new ArrayList<String>(); while (token.hasMoreTokens()) {
	 * attendees.add(token.nextToken().trim()); }
	 * 
	 * return attendees; }
	 */

	/**
	 * This is a getter for UI.
	 * 
	 * @return a SignupTimeslot object.
	 */
	public SignupTimeslot getTimeSlot() {
		return timeSlot;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if the current user has signed up in this time slot.
	 */
	public boolean isCurrentUserSignedUp() {
		return isAttendeeInList(timeSlot.getAttendees());
	}

	private boolean isAttendeeInList(List attendees) {
		if (attendees == null)
			return false;
		for (Iterator iter = attendees.iterator(); iter.hasNext();) {
			SignupAttendee attendee = (SignupAttendee) iter.next();
			if (currentUserId.equals(attendee.getAttendeeUserId()))
				return true;

		}
		return false;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if the current user is on waiting list at this time slot.
	 */
	public boolean isCurrentUserOnWaitingList() {
		return isAttendeeInList(timeSlot.getWaitingList());

	}

	/**
	 * This is a setter.
	 * 
	 * @param currentUserId
	 *            an unique sakai internal user Id.
	 */
	public void setCurrentUserId(String currentUserId) {
		this.currentUserId = currentUserId;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a list of String Objects, which hold attendee's diplay-name.
	 */
	public AttendeeWrapper[] getDisplayAttendees() {
		AttendeeWrapper[] displayAttendees;

		List<AttendeeWrapper> attendees = getAttendeeWrappers();
		if (attendees == null && attendees.isEmpty() || attendees.size() < timeSlot.getMaxNoOfAttendees())// bug
			// for Max_Value
			displayAttendees = new AttendeeWrapper[timeSlot.getMaxNoOfAttendees()];
		else
			displayAttendees = new AttendeeWrapper[attendees.size()];

		if (attendees == null)
			return displayAttendees;

		for (int i = 0; i < attendees.size(); i++) {
			displayAttendees[i] = (AttendeeWrapper) attendees.get(i);
		}

		return displayAttendees;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a list of AttendeeWrapper objects.
	 */
	public List<AttendeeWrapper> getAttendeeWrappers() {
		return attendeeWrappers;
	}

	/**
	 * This is a setter.
	 * 
	 * @param attendeeWrappers
	 *            a list of AttendeeWrapper objects.
	 */
	public void setAttendeeWrappers(List<AttendeeWrapper> attendeeWrappers) {
		this.attendeeWrappers = attendeeWrappers;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a list of AttendeeWrapper object
	 */
	public List<AttendeeWrapper> getWaitingList() {
		return waitingList;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an int value.
	 */
	public int getSizeOfWaitingList() {
		return waitingList != null ? waitingList.size() : 0;
	}

	/**
	 * This is a setter.
	 * 
	 * @param waitingList
	 *            a list of AttendeeWrapper objects.
	 */
	public void setWaitingList(List<AttendeeWrapper> waitingList) {
		this.waitingList = waitingList;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if there is spot avaiable for signing up.
	 */
	public boolean getAvailableForSignup() {
		if (timeSlot.getAttendees() == null)
			return true;

		if (timeSlot.getAttendees().size() < timeSlot.getMaxNoOfAttendees())
			return true;

		return false;
	}

	/**
	 * This is a getter.
	 * 
	 * @return a SignupAttendee object.
	 */
	public SignupAttendee getNewAttendee() {
		return newAttendee;
	}

	/**
	 * This is a setter.
	 * 
	 * @param newAttendee
	 *            a SignupAttendee object.
	 */
	public void setNewAttendee(SignupAttendee newAttendee) {
		this.newAttendee = newAttendee;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an int value.
	 */
	public int getAvailability() {
		if (timeSlot.getAttendees() == null)
			return timeSlot.getMaxNoOfAttendees();
		/*
		 * attendee size can be over the Max#
		 */
		int num = timeSlot.getMaxNoOfAttendees() - timeSlot.getAttendees().size();
		return (num > -1) ? num : 0;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an int value.
	 */
	public int getNumberOnWaitingList() {
		if (timeSlot.getWaitingList() == null)
			return 0;

		return timeSlot.getWaitingList().size();
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an int value.
	 */
	public int getRankingOnWaiting() {
		if (this.rankingOnWaiting == 0) {
			List<SignupAttendee> waiters = timeSlot.getWaitingList();
			if (waiters == null) {
				setRankingOnWaiting(0);
				return rankingOnWaiting;
			}

			for (int i = 0; i < waiters.size(); i++) {
				if (((SignupAttendee) waiters.get(i)).getAttendeeUserId().equals(currentUserId)) {
					setRankingOnWaiting(i + 1);
					break;
				}
				setRankingOnWaiting(0);
			}

		}

		return rankingOnWaiting;
	}

	/**
	 * This is a setter.
	 * 
	 * @param rankingOnWaiting
	 *            an int value.
	 */
	public void setRankingOnWaiting(int rankingOnWaiting) {
		this.rankingOnWaiting = rankingOnWaiting;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a list of SelectItem objects.
	 */
	public List<SelectItem> getSwapDropDownList() {
		return swapDropDownList;
	}

	/**
	 * This is a setter.
	 * 
	 * @param swapDropDownList
	 *            a list of SelectItem objects.
	 */
	public void setSwapDropDownList(List<SelectItem> swapDropDownList) {
		this.swapDropDownList = new ArrayList<SelectItem>(swapDropDownList);
		// TODO remove currently timeslot attendee
		this.swapDropDownList = swapDropDownList;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a list of SelectItem objects.
	 */
	public List<SelectItem> getMoveAvailableTimeSlots() {
		return moveAvailableTimeSlots;
	}

	/**
	 * This is a setter.
	 * 
	 * @param moveAvailableTimeSlots
	 *            a list of SelectItem objects.
	 */
	public void setMoveAvailableTimeSlots(List<SelectItem> moveAvailableTimeSlots) {
		this.moveAvailableTimeSlots = moveAvailableTimeSlots;
	}

	/**
	 * This is a getter method, which gives a formated timeslot period.
	 * 
	 * @return a string value.
	 */
	public String getLabel() {
		return SignupDateFormat.format_h_mm_a(timeSlot.getStartTime()) + " - "
				+ SignupDateFormat.format_h_mm_a(timeSlot.getEndTime());
	}

	/**
	 * This is a getter method for UI (javaScript function needs it).
	 * 
	 * @return an int value.
	 */
	public int getPositionInTSlist() {
		return positionInTSlist;
	}

	/**
	 * This is a setter.
	 * 
	 * @param positionInTSlist
	 *            an int value.
	 */
	public void setPositionInTSlist(int positionInTSlist) {
		this.positionInTSlist = positionInTSlist;
	}

	/**
	 * This method performs adding attendee into current time slot.
	 * 
	 * @param attendee
	 *            a SignupAttendee object.
	 * @param displayName
	 *            a string of user display name.
	 */
	public void addAttendee(SignupAttendee attendee, String displayName) {
		if (attendeeWrappers == null)
			attendeeWrappers = new ArrayList<AttendeeWrapper>();

		timeSlot.getAttendees().add(attendee);
		AttendeeWrapper wrapper = new AttendeeWrapper(attendee, displayName);
		attendeeWrappers.add(wrapper);
		wrapper.setPositionIndex(attendeeWrappers.size() - 1);// index=size-1=
	}

	/**
	 * This method performs removing attendee from current time slot.
	 * 
	 * @param attendeeUserId
	 *            a unique sakai internal user id.
	 */
	public void removeAttendee(String attendeeUserId) {
		if (attendeeWrappers == null)
			return;

		for (Iterator iter = attendeeWrappers.iterator(); iter.hasNext();) {
			AttendeeWrapper attendeeWrapper = (AttendeeWrapper) iter.next();
			if (attendeeWrapper.getSignupAttendee().getAttendeeUserId().equals(attendeeUserId)) {
				iter.remove();
				break;
			}
		}
		List<SignupAttendee> attendees = timeSlot.getAttendees();
		for (Iterator iter = attendees.iterator(); iter.hasNext();) {
			SignupAttendee attendee = (SignupAttendee) iter.next();
			if (attendee.getAttendeeUserId().equals(attendeeUserId)) {
				iter.remove();
				break;
			}
		}

		updatePositionIndex(attendeeWrappers);

	}

	/** Resets all the position index of the attendee wrapperst */
	private void updatePositionIndex(List<AttendeeWrapper> attendeeWrappers) {
		int count = 0;
		for (AttendeeWrapper wrapper : attendeeWrappers) {
			wrapper.setPositionIndex(count++);
		}
	}

}
