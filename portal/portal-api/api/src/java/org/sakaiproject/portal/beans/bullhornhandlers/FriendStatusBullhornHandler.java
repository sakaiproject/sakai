/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.portal.beans.bullhornhandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.portal.api.BullhornData;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfileLinkLogic;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.user.api.User;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FriendStatusBullhornHandler extends AbstractBullhornHandler {

    @Inject
    private ProfileConnectionsLogic profileConnectionsLogic;

    @Inject
    private ProfileLinkLogic profileLinkLogic;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(ProfileConstants.EVENT_STATUS_UPDATE);
    }

    @Override
    public Optional<List<BullhornData>> handleEvent(Event e, Cache<String, Long> countCache) {

        String from = e.getUserId();

        String ref = e.getResource();
        String[] pathParts = ref.split("/");

        List<BullhornData> bhEvents = new ArrayList<>();

        // Get all the posters friends
        List<User> connections = profileConnectionsLogic.getConnectedUsersForUserInsecurely(from);
        for (User connection : connections) {
            String to = connection.getId();
            String url = profileLinkLogic.getInternalDirectUrlToUserProfile(to, from);
            bhEvents.add(new BullhornData(from, to, "", "", url));
            countCache.remove(to);
        }

        return Optional.of(bhEvents);
    }
}
