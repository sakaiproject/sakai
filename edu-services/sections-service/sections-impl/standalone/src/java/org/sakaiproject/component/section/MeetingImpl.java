package org.sakaiproject.component.section;

import java.io.Serializable;
import java.sql.Time;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.section.api.coursemanagement.Meeting;

public class MeetingImpl implements Meeting, Serializable {

	private static final long serialVersionUID = 1L;

	private String location;
    private boolean monday;
	private boolean tuesday;
	private boolean wednesday;
	private boolean thursday;
	private boolean friday;
	private boolean saturday;
	private boolean sunday;
	private Time startTime;
	private Time endTime;

	public MeetingImpl() {
	}
	
	public MeetingImpl(String location, Time startTime, Time endTime, boolean monday, boolean tuesday,
			boolean wednesday, boolean thursday, boolean friday, boolean saturday, boolean sunday) {
		this.location = location;
		this.startTime = startTime;
		this.endTime = endTime;
		this.monday = monday;
		this.tuesday = tuesday;
		this.wednesday = wednesday;
		this.thursday = thursday;
		this.friday = friday;
		this.saturday = saturday;
		this.sunday = sunday;
	}

	public MeetingImpl(Meeting meeting) {
		this.location = meeting.getLocation();
		this.startTime = meeting.getStartTime();
		this.endTime = meeting.getEndTime();
		this.monday = meeting.isMonday();
		this.tuesday = meeting.isTuesday();
		this.wednesday = meeting.isWednesday();
		this.thursday = meeting.isThursday();
		this.friday = meeting.isFriday();
		this.saturday = meeting.isSaturday();
		this.sunday = meeting.isSunday();
	}

	public boolean isEmpty() {
		return !monday && !tuesday && !wednesday && !thursday && !friday && !saturday && !sunday &&
			startTime == null && endTime == null && StringUtils.trimToNull(location) == null;
	}
	
	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
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

	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
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
