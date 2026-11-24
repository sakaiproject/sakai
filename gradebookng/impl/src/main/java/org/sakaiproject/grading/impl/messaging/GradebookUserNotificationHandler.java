/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.grading.impl.messaging;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.GradingPersistenceManager;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.messaging.api.AbstractUserNotificationHandler;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

/**
 * Notifies a student when a grade is posted for a released gradebook item.
 */
@Slf4j
public class GradebookUserNotificationHandler extends AbstractUserNotificationHandler {

    public static final String GRADEBOOK_TOOL_ID = "sakai.gradebookng";
    // Set on each Gradebook placement when the site has a gradebook per group
    private static final String GB_GROUP_TOOL_PROPERTY = "gb-group";

    @Autowired private GradingPersistenceManager gradingPersistenceManager;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private SiteService siteService;

    @Override
    public List<String> getHandledEvents() {
        return List.of(GradingConstants.EVENT_GRADE_POSTED);
    }

    @Override
    public Optional<List<UserNotificationData>> handleEvent(Event e) {

        // /gradebook/{gradebookUid}/{itemId}/{studentId}
        String[] parts = e.getResource().split("/");
        if (parts.length != 5) {
            return Optional.empty();
        }
        String gradebookUid = parts[2];
        String studentId = parts[4];

        try {
            GradebookAssignment item = gradingPersistenceManager.getAssignmentById(Long.valueOf(parts[3])).orElse(null);
            if (item == null
                    || BooleanUtils.isTrue(item.getRemoved())
                    || !BooleanUtils.isTrue(item.getReleased())
                    || !BooleanUtils.isTrue(item.getGradebook().getAssignmentsDisplayed())) {
                return Optional.empty();
            }

            Site site = siteService.getSite(e.getContext());
            Optional<ToolConfiguration> tool = site.getTools(GRADEBOOK_TOOL_ID).stream()
                    .filter(t -> gradebookUid.equals(t.getPlacementConfig().getProperty(GB_GROUP_TOOL_PROPERTY, site.getId())))
                    .findFirst();
            if (tool.isEmpty()) {
                return Optional.empty();
            }

            String url = serverConfigurationService.getPortalUrl() + "/site/" + site.getId() + "/tool/" + tool.get().getId();
            return Optional.of(List.of(new UserNotificationData(e.getUserId(), studentId, site.getId(), item.getName(),
                    url, GRADEBOOK_TOOL_ID, false, null)));
        } catch (Exception ex) {
            log.warn("Could not create a grade notification for {}: {}", e.getResource(), ex.toString());
        }
        return Optional.empty();
    }
}
