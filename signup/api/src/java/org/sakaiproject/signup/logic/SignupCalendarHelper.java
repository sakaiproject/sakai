package org.sakaiproject.signup.logic;

import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;

/**
 * Helper to create CalendarEventEdit objects from meetings and timeslots.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public interface SignupCalendarHelper {

	/**
	 * Create a simple CalendarEventEdit object for an overall meeting. Does not include any timeslots.
	 * @param m signup meeting.
	 * @return
	 */
	public CalendarEventEdit generateEvent(SignupMeeting m);
	
	/**
	 * Create a simple CalendarEventEdit object for a specific timeslot.
	 * @param m signup meeting. Needed since it stores the main pieces of data liketitle/description/location etc.
	 * @param ts the actual timeslot.
	 * @return
	 */
	public CalendarEventEdit generateEvent(SignupMeeting m, SignupTimeslot ts);

	
}
