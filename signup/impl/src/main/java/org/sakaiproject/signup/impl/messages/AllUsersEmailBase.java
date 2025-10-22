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
import org.sakaiproject.user.api.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * An email sent to all users as an announcement
 *
 * @author Ben Holmes
 */
abstract public class AllUsersEmailBase extends SignupEmailBase {

    @Override
    public List<VEvent> generateEvents(User user, SignupCalendarHelper calendarHelper) {
        List<VEvent> events = new ArrayList<>();

        if (this.userIsAnOrganiser(user)) {
            Optional.ofNullable(this.meeting.getVevent()).ifPresent(events::add);
        } else {
            events.addAll(eventsWhichUserIsAttending(user));
        }

        if (this.cancellation) {
            events.forEach(calendarHelper::cancelVEvent);
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
