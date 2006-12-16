package org.sakaiproject.section.api.coursemanagement;

import java.sql.Time;

public interface Meeting {
	/**
	 * Gets the location where this CourseSection meets.
	 * @return
	 */
    public String getLocation();

	/**
	 * Whether the CourseSection meets on Mondays.
	 * 
	 * @return
	 */
    public boolean isMonday();

	/**
	 * Whether the CourseSection meets on Tuesdays.
	 * 
	 * @return
	 */
    public boolean isTuesday();
    
	/**
	 * Whether the CourseSection meets on Wednesdays.
	 * 
	 * @return
	 */
	public boolean isWednesday();

	/**
	 * Whether the CourseSection meets on Thursdays.
	 * 
	 * @return
	 */
	public boolean isThursday();

	/**
	 * Whether the CourseSection meets on Fridays.
	 * 
	 * @return
	 */
	public boolean isFriday();

	/**
	 * Whether the CourseSection meets on Saturdays.
	 * 
	 * @return
	 */
	public boolean isSaturday();

	/**
	 * Whether the CourseSection meets on Sundays.
	 * 
	 * @return
	 */
	public boolean isSunday();
	
	/**
	 * Gets the time of day that this CourseSection's meeting(s) start.
	 * 
	 * @return
	 */
	public Time getStartTime();

	/**
	 * Gets the time of day that this CourseSection's meeting(s) end.
	 * 
	 * @return
	 */
	public Time getEndTime();

	/**
	 * Indicates whether this meeting has no information.  Should return true if there are
	 * no meeting times and a null location.
	 * 
	 * @return
	 */
	public boolean isEmpty();
}
