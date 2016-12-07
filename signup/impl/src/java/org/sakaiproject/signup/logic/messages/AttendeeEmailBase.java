package org.sakaiproject.signup.logic.messages;

import net.fortuna.ical4j.model.component.VEvent;
import org.sakaiproject.signup.logic.SignupCalendarHelper;
import org.sakaiproject.user.api.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * An email that is sent to an attendee when they are signed up
 * @author Ben Holmes
 */
abstract public class AttendeeEmailBase extends SignupEmailBase {

    /**
     * {@inheritDoc}
     */
    public List<VEvent> generateEvents(User user, SignupCalendarHelper calendarHelper) {
        List<VEvent> events = new ArrayList<VEvent>();
        events.addAll(eventsWhichUserIsAttending(user));

        for (VEvent event : events) {
            calendarHelper.addUsersToVEvent(event, Collections.singleton(user));
        }

        return events;
    }
}
