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

package org.sakaiproject.signup.tool.jsf;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import javax.faces.model.SelectItem;

import org.sakaiproject.signup.api.SakaiFacade;
import org.sakaiproject.signup.api.model.SignupAttendee;
import org.sakaiproject.signup.api.model.SignupTimeslot;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.time.api.TimeService;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * This class is a wrapper class for SignupTimeslot for UI purpose
 * </P>
 */
public class TimeslotWrapper implements Comparable<TimeslotWrapper> {

	@Getter private final SignupTimeslot timeSlot;
    @Getter @Setter private String currentUserId;
    @Setter @Getter private List<AttendeeWrapper> attendeeWrappers;
    @Setter @Getter private List<AttendeeWrapper> waitingList;
    @Setter @Getter private SignupAttendee newAttendee;
    @Setter private int rankingOnWaiting;
    @Getter private List<SelectItem> swapDropDownList;
    @Setter @Getter private List<SelectItem> moveAvailableTimeSlots;
    @Setter @Getter private int positionInTSlist;
	@Getter @Setter private Boolean deleted = false;
	@Setter private String errorStyle = "";
    @Getter private boolean comment = false;

	/* Mark the original timeslot sequence in the list. should not changes 
	 * regardless of moving ts up and down the time line or deleted. It is useful 
	 * for modifying the recurring events at custom_ts type */
	@Setter @Getter private int tsMarker = Integer.MAX_VALUE;

	public TimeslotWrapper(SignupTimeslot slot) {
		this.timeSlot = slot;
	}

	public TimeslotWrapper(SignupTimeslot slot, String currentUserId) {
		this.timeSlot = slot;
		this.currentUserId = currentUserId;
	}

    /**
	 * This is a getter method for UI.
	 * 
	 * @return true if the current user has signed up in this time slot.
	 */
	public boolean isCurrentUserSignedUp() {
		return isAttendeeInList(timeSlot.getAttendees());
	}

    private boolean isAttendeeInList(List<SignupAttendee> attendees) {
        if (attendees == null) return false;

        return attendees.stream()
                .filter(attendee -> currentUserId.equals(attendee.getAttendeeUserId()))
                .findFirst()
                .map(attendee -> {
                    // Check for comments
                    String comments = attendee.getComments();
                    if (comments != null && !comments.trim().isEmpty()) {
                        this.comment = true;
                    }
                    return true;
                })
                .orElse(false);
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
	 * This is a getter method for UI.
	 * 
	 * @return a list of String Objects, which hold attendee's diplay-name.
	 */
    public AttendeeWrapper[] getDisplayAttendees() {
        List<AttendeeWrapper> attendees = getAttendeeWrappers();
        int arraySize = attendees == null || attendees.size() < timeSlot.getMaxNoOfAttendees()
                ? timeSlot.getMaxNoOfAttendees()
                : attendees.size();

        AttendeeWrapper[] displayAttendees = new AttendeeWrapper[arraySize];

        if (attendees != null) {
            IntStream.range(0, attendees.size())
                    .forEach(i -> displayAttendees[i] = attendees.get(i));
        }

        return displayAttendees;
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
	 * This is a getter method for UI.
	 * 
	 * @return true if there is spot avaiable for signing up.
	 */
    public boolean getAvailableForSignup() {
        return timeSlot.getAttendees() == null || timeSlot.getAttendees().size() < timeSlot.getMaxNoOfAttendees();
	}
    /**
	 * This is a getter method for UI.
	 * 
	 * @return an int value.
	 */
    public int getAvailability() {
        if (timeSlot.getAttendees() == null) {
            return timeSlot.getMaxNoOfAttendees();
        }
        return Math.max(0, timeSlot.getMaxNoOfAttendees() - timeSlot.getAttendees().size());
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an int value.
	 */
	public int getNumberOnWaitingList() {
        return timeSlot.getWaitingList() == null ? 0 : timeSlot.getWaitingList().size();
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

            // Use streams to find the index of the current user in the waiting list
            rankingOnWaiting = waiters.stream()
                    .filter(waiter -> waiter.getAttendeeUserId().equals(currentUserId))
                    .findFirst()
                    .map(waiter -> waiters.indexOf(waiter) + 1)
                    .orElse(0);
        }
        return rankingOnWaiting;
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
	 * This is a getter method, which gives a formated timeslot period.
	 * 
	 * @return a string value.
	 */
	public String getLabel(SakaiFacade sakaiFacade) {
		Locale locale = Utilities.rb.getLocale();
		TimeService ts = sakaiFacade.getTimeService();

		return ts.timeFormat(timeSlot.getStartTime(), locale, DateFormat.SHORT)
				+ ", "
				+ ts.dayOfWeekFormat(timeSlot.getStartTime(), locale, DateFormat.SHORT) 
				+ " - "
				+ ts.timeFormat(timeSlot.getEndTime(), locale, DateFormat.SHORT)
				+ ", "
				+ ts.dayOfWeekFormat(timeSlot.getEndTime(), locale, DateFormat.SHORT)
				+ ", "
				+ ts.dateFormat(timeSlot.getEndTime(), locale, DateFormat.SHORT); 
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
		if (attendeeWrappers == null) attendeeWrappers = new ArrayList<>();

		timeSlot.getAttendees().add(attendee);
		AttendeeWrapper wrapper = new AttendeeWrapper(attendee, displayName);
		attendeeWrappers.add(wrapper);
		wrapper.setPositionIndex(attendeeWrappers.size() - 1);
	}

	/**
	 * This method performs removing attendee from current time slot.
	 * 
	 * @param attendeeUserId
	 *            a unique sakai internal user id.
	 */
    public void removeAttendee(String attendeeUserId) {
        if (attendeeWrappers == null) return;

        // Remove from attendeeWrappers
        attendeeWrappers.removeIf(wrapper ->
                wrapper.getSignupAttendee().getAttendeeUserId().equals(attendeeUserId));

        // Remove from timeSlot's attendees
        timeSlot.getAttendees().removeIf(attendee ->
                attendee.getAttendeeUserId().equals(attendeeUserId));

        updatePositionIndex(attendeeWrappers);
	}
	/**
     *  Resets all the position index of the attendee wrapperst
     */
    private void updatePositionIndex(List<AttendeeWrapper> attendeeWrappers) {
        IntStream.range(0, attendeeWrappers.size()).forEach(i -> attendeeWrappers.get(i).setPositionIndex(i));
	}

	/**
	 * Compare timeslot wrappers by start time, then by end time.
	 * This ordering matches SignupMeetingService.TIMESLOT_COMPARATOR for consistency.
	 */
    public int compareTo(TimeslotWrapper other) {
        if (other == null) return 1; // null values should sort last

        int result = this.getTimeSlot().getStartTime().compareTo(other.getTimeSlot().getStartTime());
        if (result == 0) {
            result = this.getTimeSlot().getEndTime().compareTo(other.getTimeSlot().getEndTime());
        }
        return result;
	}

    /**
	 * to see if this is a newly added by user and is not in DB.
	 */
	public boolean getNewlyAddedTS() {
        return this.tsMarker == Integer.MAX_VALUE;
    }

    public boolean getNewTimeslotBlock(){
        return this.tsMarker == Integer.MAX_VALUE;
	}

	public String getErrorStyle() {
		String style = this.errorStyle;
		this.errorStyle = "";
		return style;
	}

	public String getGroupId() {
		return timeSlot.getGroupId();
	}
	
	public int getParticipants() {
		return this.timeSlot.getParticipantsNum();
	}
	
	public int getWaitingListSize() {
		return this.timeSlot.getWaitingListNum();
	}
}
