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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import java.time.Instant;

import javax.inject.Inject;

import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.portal.api.BullhornData;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AddAssignmentBullhornHandler extends AbstractBullhornHandler {

    @Inject
    private AssignmentService assignmentService;

    @Inject
    private AuthzGroupService authzGroupService;

    @Inject
    private SiteService siteService;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(AssignmentConstants.EVENT_ADD_ASSIGNMENT);
    }

    @Override
    public Optional<List<BullhornData>> handleEvent(Event e, Cache<String, Long> countCache) {

        String from = e.getUserId();

        String ref = e.getResource();
        String[] pathParts = ref.split("/");

        String siteId = pathParts[3];
        String assignmentId = pathParts[pathParts.length - 1];
        try {
            Assignment assignment = assignmentService.getAssignment(assignmentId);
            Instant openTime = assignment.getOpenDate();
            if (openTime == null || openTime.isBefore(Instant.now())) {
                Site site = siteService.getSite(siteId);
                String title = assignment.getTitle();
                Set<String> groupIds = assignment.getGroups();
                Collection<String> groupsUsers = authzGroupService.getAuthzUsersInGroups(groupIds);

                List<BullhornData> bhEvents = new ArrayList<>();

                // Get all the members of the site with read ability
                for (String to : site.getUsersIsAllowed(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT)) {
                    //  If this is a grouped assignment, is 'to' in one of the groups?
                    if (groupIds.size() == 0 || groupsUsers.contains(to)) {
                        if (!from.equals(to) && !securityService.isSuperUser(to)) {
                            String url = assignmentService.getDeepLink(siteId, assignmentId, to);
                            bhEvents.add(new BullhornData(from, to, siteId, title, url));
                            countCache.remove(to);
                        }
                    }
                }

                return Optional.of(bhEvents);
            }
        } catch (Exception ex) {
            log.error("Failed to find either the assignment or the site", ex);
        }

        return Optional.empty();
    }
}
