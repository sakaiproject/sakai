package org.sakaiproject.signup.logic.messages;

import net.fortuna.ical4j.model.component.VEvent;
import org.sakaiproject.signup.logic.SignupCalendarHelper;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * An email that is sent to an organizer when they are signed up
 * @author Ben Holmes
 */
abstract public class OrganizerEmailBase extends SignupEmailBase {

    /**
     * {@inheritDoc}
     */
    public List<VEvent> generateEvents(User user, SignupCalendarHelper calendarHelper) {
        List<VEvent> events = new ArrayList<VEvent>();

        VEvent meetingEvent = meeting.getVevent();
        if (meetingEvent == null) {
            return events;
        }

        Set<SignupAttendee> attendees = new HashSet<SignupAttendee>();
        for(SignupTimeslot timeslot: meeting.getSignupTimeSlots()) {
            attendees.addAll(timeslot.getAttendees());
        }

        calendarHelper.addAttendeesToVEvent(meetingEvent, attendees);

        events.add(meetingEvent);

        return events;
    }
}
