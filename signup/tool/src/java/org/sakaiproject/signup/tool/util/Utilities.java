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
package org.sakaiproject.signup.tool.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.ErrorMessageUIBean;
import org.sakaiproject.signup.tool.jsf.SignupMeetingsBean;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * This Utility class provides the common used logic by Signup tool.
 * </P>
 */
public final class Utilities implements SignupBeanConstants, MeetingTypes {

	/**
	 * Get the resource bundle for messages.properties file
	 */
	public static ResourceLoader rb = new ResourceLoader("messages");

	/**
	 * Defined a constant name for errorMessageUIBean
	 */
	public static final String ERROR_MESSAGE_UIBEAN = "errorMessageUIBean";

	protected Log logger = LogFactoryImpl.getLog(getClass());

	/**
	 * Add the error message to errorMessageUIBean for UI purpose.
	 * 
	 * @param errorMsg
	 *            a error message string.
	 */
	public static void addErrorMessage(String errorMsg) {
		addMessage(ERROR_MESSAGE_UIBEAN, errorMsg);
	}

	private static void addMessage(String key, String errorMsg) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map sessionMap = FacesContext.getCurrentInstance().getExternalContext()
				.getSessionMap();
		ErrorMessageUIBean errorBean = (ErrorMessageUIBean) sessionMap
				.get(ERROR_MESSAGE_UIBEAN);
		errorBean.setErrorMessages(errorMsg);
		errorBean.setError(true);
		sessionMap.put(key, errorBean);
		context.renderResponse();
	}

	/**
	 * Add the error message to errorMessageUIBean for UI purpose.
	 * 
	 * @param message
	 *            a error message string.
	 */
	public static void addMessage(String message) {
		addMessage(ERROR_MESSAGE_UIBEAN, message);

	}

	/**
	 * This method will retrieve the value from Request object by the Request
	 * parameter/attribute name
	 * 
	 * @param attrName
	 *            a string value
	 * @return a string value
	 */
	public static String getRequestParam(String attrName) {

		String value = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get(attrName);

		if (value == null || value.trim().length() == 0) {
			value = (String) FacesContext.getCurrentInstance()
					.getExternalContext().getRequestMap().get(attrName);
		}

		return value;
	}

	/**
	 * Reset the meetings in the SignupMeetingsBean to null so we will fetch all
	 * the up-to-date meeting data again
	 */
	public static void resetMeetingList() {
		SignupMeetingsBean meetingsBean = (SignupMeetingsBean) FacesContext
				.getCurrentInstance().getExternalContext().getSessionMap().get(
						"SignupMeetingsBean");
		meetingsBean.setSignupMeetings(null);
	}

	/**
	 * Get the SignupMeetingsBean in JSF as a session bean
	 * 
	 * @return a SignupMeetingsBean JSF object
	 */
	public static SignupMeetingsBean getSignupMeetingsBean() {
		return (SignupMeetingsBean) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("SignupMeetingsBean");
	}

	/**
	 * Relocate the timeslots in the event/meeting around according to the new
	 * data.
	 * 
	 * @param startTime
	 *            a Date object.
	 * @param timeSlotDuration
	 *            an int value, which indicate the length of the time slot.
	 * @param numOfTimeslot
	 *            an int value, which indicate how many time slots are there.
	 * @param tsList
	 *            a list of SignupTimeslot objects. This object is a reference
	 *            object and after this call, it will hold the relocated new
	 *            data.
	 * @return a Date object, which holds the ending time of the event/meeting.
	 */
	public static Date reAllocateTimeslots(Date startTime,
			int timeSlotDuration, int numOfTimeslot, List<SignupTimeslot> tsList) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startTime);// new starting time
		if (tsList == null || tsList.isEmpty())
			return startTime;

		for (SignupTimeslot timeslot : tsList) {
			timeslot.setStartTime(calendar.getTime());
			calendar.add(Calendar.MINUTE, timeSlotDuration);
			timeslot.setEndTime(calendar.getTime());
		}

		return calendar.getTime();
	}

	/**
	 * Calculate the time according to the input parameters.
	 * 
	 * @param date
	 *            a Date object.
	 * @param time
	 *            an int value.
	 * @param dateType
	 *            a string value.
	 * @return a converted Date object according to the input parameters.
	 */
	public static Date subTractTimeToDate(Date date, int time, String dateType) {
		if (time == 0)
			return date;

		int type = -1;
		if (dateType.equals(MINUTES))
			type = Calendar.MINUTE;
		else if (dateType.equals(HOURS))
			type = Calendar.HOUR;
		else {// days{
			time = 24 * time; // convert to hours
			type = Calendar.HOUR;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(type, -1 * time);

		return calendar.getTime();
	}

	/**
	 * Get the Signup tool defined time unit type.
	 * 
	 * @param timeLength
	 *            a long value.
	 * @return a time unit value string such as 'hours', 'minutes' and 'days'.
	 */
	public static String getTimeScaleType(long timeLength) {
		String timeUnitType = MINUTES;
		if (timeLength == (((long) (timeLength / DAY_IN_MINUTES)) * DAY_IN_MINUTES))
			timeUnitType = DAYS;
		else if (timeLength == (((long) (timeLength / Hour_In_MINUTES)) * Hour_In_MINUTES))
			timeUnitType = HOURS;

		return timeUnitType;
	}

	/**
	 * Get the relative time value according to the time unit type.
	 * 
	 * @param timeScaleType
	 *            a string value.
	 * @param timeLength
	 *            a long value
	 * @return a int value.
	 */
	public static int getRelativeTimeValue(String timeScaleType, long timeLength) {
		long rValue = timeLength;
		if (DAYS.equals(timeScaleType))
			rValue = timeLength / DAY_IN_MINUTES;
		if (HOURS.equals(timeScaleType))
			rValue = timeLength / Hour_In_MINUTES;

		return (int) rValue;
	}

	/**
	 * It provides a list of meeting type choices for user.
	 * 
	 * @param mSelectedType
	 *            a String value, which indicates that the passed-in meeting
	 *            type will not disabled.
	 * @param disableNotSelectedOnes
	 *            a boolean value which indicate whether to disable other
	 *            meeting types except this one.
	 * @return a list of SelectItem objects.
	 */
	public static List<SelectItem> getMeetingTypeSelectItems(
			String mSelectedType, boolean disableNotSelectedOnes) {
		List<SelectItem> meetingTypeItems = new ArrayList<SelectItem>();
		SelectItem announ = new SelectItem(ANNOUNCEMENT, Utilities.rb
				.getString("label_announcement"), "anouncment");
		SelectItem multiple = new SelectItem(INDIVIDUAL, Utilities.rb
				.getString("label_individaul"), "individaul");
		SelectItem group = new SelectItem(GROUP, Utilities.rb
				.getString("label_group"), "group");

		if (disableNotSelectedOnes) {
			if (!ANNOUNCEMENT.equals(mSelectedType))
				announ.setDisabled(true);
			if (!GROUP.equals(mSelectedType))
				group.setDisabled(true);
			if (!INDIVIDUAL.equals(mSelectedType))
				multiple.setDisabled(true);
		}
		meetingTypeItems.add(announ);
		meetingTypeItems.add(group);
		meetingTypeItems.add(multiple);

		return meetingTypeItems;
	}

	private static boolean postToDatabase = "false".equals(rb
			.getString("post.eventTracking.info.to.DB")) ? false : true;

	/**
	 * This method will post user action event to DB by using
	 * Sakai-event-tracking mechanism. This event tracking can be turned off by
	 * setting value of post.eventTracking.info.to.DB in message.properties file
	 * to <b>false</b>
	 * 
	 * @param mainSignupEventType
	 *            a sign-up event type string
	 * @param eventActionInfo
	 *            a detailed action info string
	 */
	public static void postEventTracking(String mainSignupEventType,
			String eventActionInfo) {
		if (postToDatabase) {
			UsageSession usageSession = UsageSessionService.getSession();
			if (eventActionInfo != null && eventActionInfo.length() >= 256) {
				/* truncate it due to DB field size(255) constraint */
				eventActionInfo = eventActionInfo.substring(0, 252) + "...";
			}
			EventTrackingService.post(EventTrackingService.newEvent(
					mainSignupEventType, eventActionInfo, false), usageSession);
		}

	}

	/**
	 * It will obtain user current sign-up status in an event.
	 * 
	 * @param meeting
	 *            a SignupMeeting object
	 * @param currentUserId
	 *            a unique user internal id.
	 * @param sakaiFacade
	 *            a SakaiFacade object
	 * @return a String object
	 */
	public static String retrieveAvailStatus(SignupMeeting meeting,
			String currentUserId, SakaiFacade sakaiFacade) {
		long curTime = (new Date()).getTime();
		long meetingStartTime = meeting.getStartTime().getTime();
		long meetingEndTime = meeting.getEndTime().getTime();
		long meetingSignupBegin = meeting.getSignupBegins().getTime();
		if (meetingEndTime < curTime)
			return rb.getString("event.closed");
		if (meetingEndTime > curTime && meetingStartTime < curTime)
			return rb.getString("event.inProgress");

		if (meeting.getMeetingType().equals(SignupMeeting.ANNOUNCEMENT)) {
			return rb.getString("event.SignupNotRequire");
		}

		String availableStatus = rb.getString("event.unavailable");
		boolean isSignupBegin = true;
		if (meetingSignupBegin > curTime) {
			isSignupBegin = false;
			availableStatus = rb.getString("event.Signup.not.started.yet")
					+ " "
					+ sakaiFacade.getTimeService().newTime(
							meeting.getSignupBegins().getTime())
							.toStringLocalShortDate();
		}

		boolean isOnWaitingList = false;
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		for (SignupTimeslot timeslot : signupTimeSlots) {
			List<SignupAttendee> attendees = timeslot.getAttendees();
			for (SignupAttendee attendee : attendees) {
				if (attendee.getAttendeeUserId().equals(currentUserId))
					return rb.getString("event.youSignedUp");
			}

			List<SignupAttendee> waiters = timeslot.getWaitingList();
			if (!isOnWaitingList) {
				for (SignupAttendee waiter : waiters) {
					if (waiter.getAttendeeUserId().equals(currentUserId)) {
						availableStatus = rb.getString("event.youOnWaitList");
						isOnWaitingList = true;
						break;
					}
				}
			}

			int size = (attendees == null) ? 0 : attendees.size();
			if (!isOnWaitingList
					&& isSignupBegin
					&& (size < timeslot.getMaxNoOfAttendees() || timeslot
							.getMaxNoOfAttendees() == SignupTimeslot.UNLIMITED)) {
				availableStatus = rb.getString("event.available");
			}
		}

		return availableStatus;
	}

	/**
	 * This method will convert int days to a Date object
	 * 
	 * @param days
	 *            it indicates how many days from current time into the future
	 * @return a Date object
	 */
	public static Date getUserDefinedDate(int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR, 24 * days);
		return cal.getTime();
	}
}
