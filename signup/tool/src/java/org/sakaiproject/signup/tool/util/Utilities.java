/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Yale University
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
 **********************************************************************************/
package org.sakaiproject.signup.tool.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.ErrorMessageUIBean;
import org.sakaiproject.signup.tool.jsf.SignupMeetingsBean;

/**
 * <p>
 * This Utility class provides the common used logic by Signup tool.
 * </P>
 */
public final class Utilities implements SignupBeanConstants {

	/**
	 * Get the resource bundle for messages.properties file
	 */
	public static ResourceBundle rb = ResourceBundle.getBundle("messages");

	/**
	 * Defined a constant name for errorMessageUIBean
	 */
	public static final String ERROR_MESSAGE_UIBEAN = "errorMessageUIBean";

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
		Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		ErrorMessageUIBean errorBean = (ErrorMessageUIBean) sessionMap.get(ERROR_MESSAGE_UIBEAN);
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

		String value = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(
				attrName);

		if (value == null || value.trim().length() == 0) {
			value = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get(attrName);
		}

		return value;
	}

	/**
	 * * Reset the meetings in the SignupMeetingsBean to null so we will fetch
	 * all the updat-to-date meeting data again
	 * 
	 */
	public static void resetMeetingList() {
		SignupMeetingsBean meetingsBean = (SignupMeetingsBean) FacesContext.getCurrentInstance().getExternalContext()
				.getSessionMap().get("SignupMeetingsBean");
		meetingsBean.setSignupMeetings(null);
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
	public static Date reAllocateTimeslots(Date startTime, int timeSlotDuration, int numOfTimeslot,
			List<SignupTimeslot> tsList) {
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

}
