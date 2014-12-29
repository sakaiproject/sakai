package org.sakaiproject.signup.logic.messages;

import net.fortuna.ical4j.model.component.VEvent;
import org.sakaiproject.signup.logic.SignupCalendarHelper;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

import java.util.ArrayList;
import java.util.List;


/**
 * An email that is sent when an attendee is transfered from one event to another
 * @author Ben Holmes
 */
abstract public class TransferEmailBase extends SignupEmailBase implements SignupTimeslotChanges {

    /**
     * {@inheritDoc}
     */
    public List<VEvent> generateEvents(User user, SignupCalendarHelper calendarHelper) {

        //The tracking classes don't maintain the transient VEVents we have created previously
        //so we need to check and recreate.

        //cancel all of the removed events
        List<VEvent> events = new ArrayList<VEvent>();
        for(SignupTimeslot timeslot: this.getRemoved()) {
            //check and recreate if necessary
            VEvent event = calendarHelper.generateVEventForTimeslot(meeting, timeslot);
            if(event != null){
                calendarHelper.cancelVEvent(event);
                events.add(event);
            }
        }

        //add all of the new events
        for(SignupTimeslot timeslot: this.getAdded()) {
            //check and recreate if necessary
            VEvent event = calendarHelper.generateVEventForTimeslot(meeting, timeslot);
            if(event != null){
                events.add(event);
            }
        }
        return events;
    }

}
