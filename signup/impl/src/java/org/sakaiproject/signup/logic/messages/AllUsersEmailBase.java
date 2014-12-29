package org.sakaiproject.signup.logic.messages;

import net.fortuna.ical4j.model.component.VEvent;
import org.sakaiproject.signup.logic.SignupCalendarHelper;
import org.sakaiproject.user.api.User;

import java.util.ArrayList;
import java.util.List;


/**
 * An email that is sent to all users as an announcement
 * @author Ben Holmes
 */
abstract public class AllUsersEmailBase extends SignupEmailBase {

    /**
     * {@inheritDoc}
     */
    public List<VEvent> generateEvents(User user, SignupCalendarHelper calendarHelper) {

        List<VEvent> events = new ArrayList<VEvent>();

        if (this.userIsAnOrganiser(user)) {

            final VEvent meetingEvent = this.meeting.getVevent();
            if (meetingEvent != null) {
                events.add(meetingEvent);
            }

        } else {
            events.addAll(eventsWhichUserIsAttending(user));
        }

        if (this.cancellation) {
            for (VEvent event : events) {
                calendarHelper.cancelVEvent(event);
            }
        }

        return events;
    }

    private List<String> meetingCreatorAndOrganisers() {
        String creatorUserId = this.meeting.getCreatorUserId();
        List<String> coordinatorIds = this.meeting.getCoordinatorIdsList();
        coordinatorIds.add(creatorUserId);
        return coordinatorIds;
    }

    private boolean userIsAnOrganiser(User user) {
        return this.meetingCreatorAndOrganisers().contains(user.getId());
    }

}
