/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.coursemanagement.impl;

import java.io.Serializable;
import java.sql.Time;

import org.sakaiproject.coursemanagement.api.Meeting;
import org.sakaiproject.coursemanagement.api.Section;

public class MeetingCmImpl extends AbstractPersistentCourseManagementObjectCmImpl implements Meeting, Serializable {
	private static final long serialVersionUID = 1L;

	private Section section;
	
	private String location;
	private Time startTime;
	private Time finishTime;
    private boolean monday;
	private boolean tuesday;
	private boolean wednesday;
	private boolean thursday;
	private boolean friday;
	private boolean saturday;
	private boolean sunday;
	private String notes;
	
	public MeetingCmImpl() {}
	
	public MeetingCmImpl(Section section, String location, Time startTime, Time finishTime) {
		this.section = section;
		this.location = location;
		this.startTime = startTime;
		this.finishTime = finishTime;
	}
	public MeetingCmImpl(Section section, String location, Time startTime, Time finishTime, String notes) {
		this(section, location, startTime, finishTime);
		this.notes = notes;
	}
	
	public Section getSection() {
		return section;
	}
	public void setSection(Section section) {
		this.section = section;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public Time getFinishTime() {
		return finishTime;
	}
	public void setFinishTime(Time finishTime) {
		this.finishTime = finishTime;
	}
	public Time getStartTime() {
		return startTime;
	}
	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	public boolean isFriday() {
		return friday;
	}

	public void setFriday(boolean friday) {
		this.friday = friday;
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
}
