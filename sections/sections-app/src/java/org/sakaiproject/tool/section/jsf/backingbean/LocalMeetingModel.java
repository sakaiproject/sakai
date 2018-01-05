/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;
import java.sql.Time;
import java.util.Calendar;
import java.util.GregorianCalendar;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.sakaiproject.section.api.coursemanagement.Meeting;
import org.sakaiproject.tool.section.jsf.JsfUtil;

@Slf4j
public class LocalMeetingModel implements Meeting, Serializable {
	private static final long serialVersionUID = 1L;

	public LocalMeetingModel() {
		startTimeAm = true;
		endTimeAm = true;
		startTimeString = JsfUtil.getLocalizedMessage("section_start_time_default");
		endTimeString = JsfUtil.getLocalizedMessage("section_end_time_default");
	}
	
	public LocalMeetingModel(Meeting meeting) {
		this.location = meeting.getLocation();
		this.monday = meeting.isMonday();
		this.tuesday = meeting.isTuesday();
		this.wednesday = meeting.isWednesday();
		this.thursday = meeting.isThursday();
		this.friday = meeting.isFriday();
		this.saturday = meeting.isSaturday();
		this.sunday = meeting.isSunday();
		this.startTimeString = JsfUtil.convertTimeToString(meeting.getStartTime());
		if(startTimeString == null) {
			startTimeString = JsfUtil.getLocalizedMessage("section_start_time_default");
		}

		this.endTimeString = JsfUtil.convertTimeToString(meeting.getEndTime());
		if(endTimeString == null) {
			endTimeString = JsfUtil.getLocalizedMessage("section_end_time_default");
		}
		
		Calendar cal = new GregorianCalendar();
		if(meeting.getStartTime() == null) {
			this.startTimeAm = true;
		} else {
			cal.setTime(meeting.getStartTime());
			if(log.isDebugEnabled()) log.debug("cal.get(Calendar.AM_PM) = " + cal.get(Calendar.AM_PM));
			this.startTimeAm = (cal.get(Calendar.AM_PM) == Calendar.AM);
		}
		if(meeting.getEndTime() == null) {
			this.endTimeAm = true;
		} else {
			cal.setTime(meeting.getEndTime());
			if(log.isDebugEnabled()) log.debug("cal.get(Calendar.AM_PM) = " + cal.get(Calendar.AM_PM));
			this.endTimeAm = (cal.get(Calendar.AM_PM) == Calendar.AM);
		}
	}
	
	private String location, startTimeString, endTimeString;
    private boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday;
	private boolean startTimeAm, endTimeAm;


	public boolean isEmpty() {
		return !monday && !tuesday && !wednesday && !thursday && !friday && !saturday && !sunday &&
			(StringUtils.trimToNull(startTimeString) == null || isStartTimeDefault()) &&
			(StringUtils.trimToNull(endTimeString) == null || isEndTimeDefault()) &&
			StringUtils.trimToNull(location) == null;
	}

	public Time getStartTime() {
		if(isStartTimeDefault()) {
			return null;
		}
		return JsfUtil.convertStringToTime(startTimeString, startTimeAm);
	}

	public Time getEndTime() {
		if(isEndTimeDefault()) {
			return null;
		}
		return JsfUtil.convertStringToTime(endTimeString, endTimeAm);
	}
			
	public String getEndTimeString() {
		return endTimeString;
	}
	public void setEndTimeString(String endTimeString) {
		this.endTimeString = endTimeString;
	}
	// Must use a string due to http://issues.apache.org/jira/browse/MYFACES-570
	public String getEndTimeAmString() {
		return Boolean.toString(endTimeAm);
	}
	// Must use a string due to http://issues.apache.org/jira/browse/MYFACES-570
	public void setEndTimeAmString(String endTimeAm) {
		this.endTimeAm = Boolean.parseBoolean(endTimeAm);
	}
	public boolean isFriday() {
		return friday;
	}
	public void setFriday(boolean friday) {
		this.friday = friday;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public boolean isMonday() {
		return monday;
	}
	public void setMonday(boolean monday) {
		this.monday = monday;
	}
	public boolean isSaturday() {
		return saturday;
	}
	public void setSaturday(boolean saturday) {
		this.saturday = saturday;
	}
	public String getStartTimeString() {
		return startTimeString;
	}
	public void setStartTimeString(String startTimeString) {
		this.startTimeString = startTimeString;
	}
	// Must use a string due to http://issues.apache.org/jira/browse/MYFACES-570
	public String getStartTimeAmString() {
		return Boolean.toString(startTimeAm);
	}
	// Must use a string due to http://issues.apache.org/jira/browse/MYFACES-570
	public void setStartTimeAmString(String startTimeAm) {
		this.startTimeAm = Boolean.parseBoolean(startTimeAm);
		if(log.isDebugEnabled()) log.debug("String " + startTimeAm + " caused startTimeAm to be set to " + this.startTimeAm);
	}
	
	public boolean isStartTimeAm() {
		return startTimeAm;
	}
	public boolean isEndTimeAm() {
		return endTimeAm;
	}
	
	public boolean isSunday() {
		return sunday;
	}
	public void setSunday(boolean sunday) {
		this.sunday = sunday;
	}
	public boolean isThursday() {
		return thursday;
	}
	public void setThursday(boolean thursday) {
		this.thursday = thursday;
	}
	public boolean isTuesday() {
		return tuesday;
	}
	public void setTuesday(boolean tuesday) {
		this.tuesday = tuesday;
	}
	public boolean isWednesday() {
		return wednesday;
	}
	public void setWednesday(boolean wednesday) {
		this.wednesday = wednesday;
	}

	public String toString() {
		return new ToStringBuilder(this).append(location).append(startTimeString).append(startTimeAm)
			.append(endTimeString).append(endTimeAm).append(monday).append(tuesday).append(wednesday)
			.append(thursday).append(friday).append(saturday).append(sunday).toString();
	}

	public boolean isStartTimeDefault() {
		return JsfUtil.getLocalizedMessage("section_start_time_default").equals(startTimeString);
	}

	public boolean isEndTimeDefault() {
		return JsfUtil.getLocalizedMessage("section_end_time_default").equals(endTimeString);
	}

	public boolean isLocationDefault() {
		return JsfUtil.getLocalizedMessage("section_location_default").equals(location);
	}
}
