package org.sakaiproject.signup.logic;

import java.util.List;

import net.fortuna.ical4j.model.component.VEvent;

import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * Helper to create and modify CalendarEventEdit and VEvent objects from meetings and timeslots.
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

	/**
	 * Create a VEvent for a timeslot. Checks if it exists in the given timeslot already (can be transient)
	 * @param meeting	overall SignupMeeting
	 * @param ts		SignupTimeslot we need VEvent for
	 * @return
	 */
	public VEvent generateVEventForTimeslot(SignupMeeting meeting, SignupTimeslot ts);
	
	/**
	 * Create a VEvent for an overall meeting, no timeslots are included. Checks if it exists in the given meeting already (can be transient)
	 * @param meeting	overall SignupMeeting
	 * @return
	 */
	public VEvent generateVEventForMeeting(SignupMeeting meeting);
	
	/**
	 * Create a calendar for a list of VEvents and return the path to the file
	 * @param vevents	List of VEvents
	 * @return
	 */
	public String createCalendarFile(List<VEvent> vevents);
	
	/**
	 * Cancel an event
	 * @param vevent VEvent to cancel
	 * @return
	 */
	public VEvent cancelVEvent(VEvent vevent);
	
	/**
	 * Add the list of Users to the VEvent as attendees
	 * @param vevent	VEvent to modify
	 * @param users		List of Users to add
	 * @return
	 */
	public VEvent addAttendeesToVEvent(VEvent vevent, List<User> users);
	
	/**
	 * Is ICS calendar generation enabled in the external calendaring service?
	 * @return	true/false
	 */
	public boolean isIcsEnabled();

	
}
