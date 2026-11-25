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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.grading.api.GradingPersistenceManager;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

public class GradebookUserNotificationHandlerTest {

    @Mock private GradingPersistenceManager gradingPersistenceManager;
    @Mock private ServerConfigurationService serverConfigurationService;
    @Mock private SiteService siteService;
    @InjectMocks private GradebookUserNotificationHandler handler;

    private Gradebook gradebook;
    private GradebookAssignment item;
    private Site site;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);

        gradebook = new Gradebook();
        gradebook.setAssignmentsDisplayed(true);
        item = new GradebookAssignment();
        item.setName("Quiz 1");
        item.setReleased(true);
        item.setGradebook(gradebook);
        when(gradingPersistenceManager.getAssignmentById(7L)).thenReturn(Optional.of(item));

        List<ToolConfiguration> placements = List.of(placement("gbsite", null), placement("gbgroup", "group1"));
        site = mock(Site.class);
        when(site.getId()).thenReturn("site1");
        when(site.getTools(GradebookUserNotificationHandler.GRADEBOOK_TOOL_ID)).thenReturn(placements);
        when(siteService.getSite("site1")).thenReturn(site);
        when(serverConfigurationService.getPortalUrl()).thenReturn("http://localhost/portal");
    }

    /** A Gradebook placement; groupId is its gb-group property, null for the site's own Gradebook. */
    private ToolConfiguration placement(String toolId, String groupId) {

        Properties config = new Properties();
        if (groupId != null) {
            config.setProperty("gb-group", groupId);
        }
        ToolConfiguration tool = mock(ToolConfiguration.class);
        when(tool.getId()).thenReturn(toolId);
        when(tool.getPlacementConfig()).thenReturn(config);
        return tool;
    }

    private List<UserNotificationData> handle(String gradebookUid) {

        Event e = mock(Event.class);
        when(e.getResource()).thenReturn("/gradebook/" + gradebookUid + "/7/student1");
        when(e.getContext()).thenReturn("site1");
        when(e.getUserId()).thenReturn("instructor1");
        return handler.handleEvent(e).orElse(List.of());
    }

    @Test
    public void notifiesTheStudentWithALinkToTheirGradebook() {

        List<UserNotificationData> result = handle("site1");
        assertEquals(1, result.size());
        UserNotificationData und = result.get(0);
        assertEquals("instructor1", und.getFrom());
        assertEquals("student1", und.getTo());
        assertEquals("site1", und.getSiteId());
        assertEquals("Quiz 1", und.getTitle());
        assertEquals("http://localhost/portal/site/site1/tool/gbsite", und.getUrl());
        assertTrue(und.isSkipIfUnread());
    }

    @Test
    public void linksAGroupGradebookToItsOwnPlacement() {
        assertEquals("http://localhost/portal/site/site1/tool/gbgroup", handle("group1").get(0).getUrl());
    }

    @Test
    public void skipsItemsStudentsCannotSee() {

        item.setReleased(false);
        assertTrue(handle("site1").isEmpty());

        item.setReleased(true);
        gradebook.setAssignmentsDisplayed(false);
        assertTrue(handle("site1").isEmpty());
    }
}
