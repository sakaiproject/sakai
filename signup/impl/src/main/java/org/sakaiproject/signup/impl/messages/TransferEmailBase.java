/**
 * Copyright (c) 2007-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.signup.impl.messages;

import net.fortuna.ical4j.model.component.VEvent;
import org.sakaiproject.signup.api.SignupCalendarHelper;
import org.sakaiproject.signup.api.messages.SignupTimeslotChanges;
import org.sakaiproject.signup.api.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract base class for email notifications that are sent when an attendee is transferred 
 * between timeslots in a signup event. This includes both moving an attendee to a different 
 * timeslot and swapping attendees between timeslots.
 *
 * <p>Subclasses should implement specific email notification logic for different types of transfers
 * such as moves and swaps.</p>
 *
 * @author Ben Holmes
 * @see MoveAttendeeEmail
 * @see SwapAttendeeEmail
 */
abstract public class TransferEmailBase extends SignupEmailBase implements SignupTimeslotChanges {

    @Override
    public List<VEvent> generateEvents(User user, SignupCalendarHelper calendarHelper) {

        /* The tracking classes don't maintain the transient VEVents we have created previously
           so we need to check and recreate.
           Cancel all of the removed events */
        List<VEvent> events = new ArrayList<>();
        for (SignupTimeslot timeslot : this.getRemoved()) {
            // check and recreate if necessary
            VEvent event = calendarHelper.generateVEventForTimeslot(meeting, timeslot);
            if (event != null) {
                calendarHelper.cancelVEvent(event);
                events.add(event);
            }
        }

        // add all of the new events
        for (SignupTimeslot timeslot : this.getAdded()) {
            // check and recreate if necessary
            VEvent event = calendarHelper.generateVEventForTimeslot(meeting, timeslot);
            if (event != null) {
                events.add(event);
            }
        }
        return events;
    }

}
