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

package org.sakaiproject.signup.tool.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupUser;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.MessageUIBean;
import org.sakaiproject.signup.tool.jsf.SignupMeetingsBean;
import org.sakaiproject.signup.tool.jsf.organizer.UserDefineTimeslotBean;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * This Utility class provides the common used logic by Signup tool.
 * 
 * @author Peter Liu
 * 
 * </P>
 */
@Slf4j
public final class Utilities implements SignupBeanConstants, MeetingTypes {

	/**
	 * Get the resource bundle for messages.properties file
	 */
	public static ResourceLoader rb = new ResourceLoader("messages");
	
	/**
	 * Get the resource bundle for signupConfig.properties file
	 */
	public static ResourceLoader rbConf = new ResourceLoader("signupConfig");

	/**
	 * Defined a constant name for ,essageUIBean
	 */
	public static final String MESSAGE_UIBEAN = "messageUIBean";
	
	/**
	 * Message types
	 */
	private static final int TYPE_ERROR=1;
	private static final int TYPE_INFO=2;

	/**
	 * Add the error message to mssageUIBean for UI purpose.
	 * 
	 * @param errorMsg
	 *            a error message string.
	 */
	public static void addErrorMessage(String errorMsg) {
		addMessage(MESSAGE_UIBEAN, TYPE_ERROR, errorMsg);
	}
	
	/**
	 * Add the info message to messageUIBean for UI purpose.
	 * 
	 * @param infoMsg
	 *            an info message string.
	 */
	public static void addInfoMessage(String infoMsg) {
		addMessage(MESSAGE_UIBEAN, TYPE_INFO, infoMsg);
	}

	private static void addMessage(String key, int type, String msg) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		MessageUIBean msgBean = (MessageUIBean) sessionMap.get(MESSAGE_UIBEAN);
		
		switch(type) {
			case 1: msgBean.setErrorMessage(msg); 	break;
			case 2: msgBean.setInfoMessage(msg);	break;
			default: log.error("Invalid mesage type ("+type +"). No message will be set");	break;
		}
		
		sessionMap.put(key, msgBean);
		context.renderResponse();
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
	 * This method will retrieve the value from UI CommandButton
	 * parameter/attribute name
	 * 
	 * @param attrName
	 *            a string value
	 * @return a string value
	 */
	public static String getActionAttribute(ActionEvent event, String name) {
        return (String) event.getComponent().getAttributes().get(name);
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
		else if (dateType.equals(START_NOW)){
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTime();
		}else {// days{
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
	 * This method will determine whether the recurring events have been previously setup by 'Start_Now'
	 * type.
	 * @param recurringMeetings
	 * 				a list of SignupMeeting objects.
	 * @return true if the events have been set up as 'start_now' type previously
	 */
	public static boolean testSignupBeginStartNowType(List<SignupMeeting> recurringMeetings){
		boolean isStartNowTypeForRecurEvents=false;
		if(recurringMeetings !=null && recurringMeetings.size()>1){
			long signupBeginsTimeSecLastOne = recurringMeetings.get(recurringMeetings.size()-2).getSignupBegins() == null ? new Date().getTime() : recurringMeetings.get(recurringMeetings.size()-2).getSignupBegins()
					.getTime();
			long signupBeginsTimeLastOne = recurringMeetings.get(recurringMeetings.size()-1).getSignupBegins() == null ? new Date().getTime() : recurringMeetings.get(recurringMeetings.size()-1).getSignupBegins()
					.getTime();
			//check for SignupBegin time and it should be the same.
			if(signupBeginsTimeSecLastOne ==signupBeginsTimeLastOne){
				isStartNowTypeForRecurEvents=true;
				for (SignupMeeting sm : recurringMeetings) {
					//double check, it has to be started now for sign-up process
					if (!sm.getSignupBegins().before(new Date())){
						isStartNowTypeForRecurEvents=false;
						break;
					}
				}
			}
		}
		return isStartNowTypeForRecurEvents;
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

	private static boolean postToDatabase = "false".equals(getSignupConfigParamVal("signup.post.eventTracking.info.to.DB", "true")) ? false : true;

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
		boolean isMeetingSpaceAvail = false;
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
					&& !timeslot.isLocked()
					&& !timeslot.isCanceled()
					&& (size < timeslot.getMaxNoOfAttendees() || timeslot
							.getMaxNoOfAttendees() == SignupTimeslot.UNLIMITED)) {
				availableStatus = rb.getString("event.available");
				isMeetingSpaceAvail =true;
			}
		}
		
		if(isMeetingSpaceAvail && meeting.isLocked()){
			availableStatus = rb.getString("event.meeting.locked");
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
	
	//Backward compatibility, no property validation
	public static String getSignupConfigParamVal(String paramName, String defaultValue) {
		return getSignupConfigParamVal(paramName, defaultValue, null);
	}
	
	/**
	 * @param paramName - Name of the parameter
	 * @param defaultValue - Default value
	 * @param acceptValues - Set of acceptable values (optional, null if all values allowed) 
	 * 		If specified and the value isn't allowed returns the default
	 * @return config parameter
	 */
	public static String getSignupConfigParamVal(String paramName, String defaultValue, Set <String> acceptValues) {
		/* first look at sakai.properties and the tool bundle*/
		String myConfigValue=defaultValue;
		if (paramName != null) {
			try {
				myConfigValue = ServerConfigurationService.getString(paramName);
				if(myConfigValue ==null || myConfigValue.trim().length() < 1){
					myConfigValue = rbConf.getString(paramName);
					int index = myConfigValue.indexOf("missing key");/*return value by rb if no key defined-- hard coded!!!*/
					if (index >=0)
						myConfigValue = defaultValue;
				}
			} catch (Exception e) {
				myConfigValue = defaultValue;
			}
		}
		
		//If acceptable values is defined and this isn't acceptable, just return the default anyway
		if (acceptValues != null && !acceptValues.contains(myConfigValue)) {
			myConfigValue = defaultValue;
		}
			
		return myConfigValue;

	}

	/**
	 * Gets a boolean value for a configuration, instead of a string value.
	 * @param paramname     the name of the config parameter
	 * @param defaultValue  the default boolean value to use
	 * @return <code>true</code> if and only if the config parameter is <code>"true"</code>.
	 */
	public static boolean getSignupConfigParamVal(final String paramname, final boolean defaultValue) {
		String stringValue = getSignupConfigParamVal(paramname, String.valueOf(defaultValue));
		return "true".equalsIgnoreCase(stringValue);
	}

	public static boolean isDataIntegritySafe(boolean isUserDefinedTS, String callerBeanType, UserDefineTimeslotBean uBean){
		if(isUserDefinedTS && !callerBeanType.equals(uBean.getPlaceOrderBean())){
			Utilities.addErrorMessage("You may have opened multiple Tabs in your browser, please close them and try again.");// Utilities.rb.getString("event_endtime_auto_adjusted_warning"));
			return false;
		}
		return true;
	}
	
	/**
	 * Turn list of signup users into list of ids for storage into db. 
	 * SIGNUP-216 removed the restriction that an organizer cannot be in this list (unsure why they couldnt since they have the option of being chosen but are then removed?)
	 * @param coordinators
	 * @param organizerId
	 * @return
	 */
	public static String getSelectedCoordinators(List<SignupUser> coordinators, String organizerId){
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (SignupUser co : coordinators) {
			if(co.isChecked()){
				if(isFirst){
					sb.append(co.getInternalUserId());
					isFirst = false;
				}else{
					//safeguard -db column max size, hardly have over 10 coordinators per meeting
					//SS note, this would still blow the DB limit since the check was for up to 1000, and then adding text to that
					//and the limit is 1000 (why was this number chosen and why wasn't this list normalised?)
					//so the limit has been lowered to 950 to be safer.
					if(sb.length() < 950)
						sb.append("|" + co.getInternalUserId());
				}
			}
		}
		return sb.length()<1? null : sb.toString();
	}
}
