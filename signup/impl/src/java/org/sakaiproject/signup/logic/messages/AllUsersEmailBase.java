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
