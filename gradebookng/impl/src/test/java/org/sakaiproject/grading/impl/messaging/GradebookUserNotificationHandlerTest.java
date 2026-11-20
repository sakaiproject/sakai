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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.grading.api.GradingPersistenceManager;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

public class GradebookUserNotificationHandlerTest {

    private static final String SITE_ID = "site1";
    private static final String INSTRUCTOR = "instructor";
    private static final String STUDENT = "student";

    @Mock private GradingPersistenceManager gradingPersistenceManager;
    @Mock private ServerConfigurationService serverConfigurationService;
    @Mock private SiteService siteService;
    @InjectMocks private GradebookUserNotificationHandler handler;

    private Gradebook gradebook;
    private GradebookAssignment item;
    private Site site;

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

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);
        handler = spy(handler);
        doReturn(false).when(handler).hasUnviewedNotification(anyString(), anyString());

        gradebook = new Gradebook();
        gradebook.setUid(SITE_ID);
        gradebook.setAssignmentsDisplayed(true);

        item = new GradebookAssignment();
        item.setName("Quiz 1");
        item.setReleased(true);
        item.setGradebook(gradebook);
        when(gradingPersistenceManager.getAssignmentByNameAndGradebook("Quiz 1", SITE_ID)).thenReturn(Optional.of(item));

        site = mock(Site.class);
        when(site.getId()).thenReturn(SITE_ID);
        List<ToolConfiguration> sitePlacement = List.of(placement("gbtool", null));
        when(site.getTools(GradebookUserNotificationHandler.GRADEBOOK_TOOL_ID)).thenReturn(sitePlacement);
        when(siteService.getSite(SITE_ID)).thenReturn(site);
        when(serverConfigurationService.getPortalUrl()).thenReturn("http://localhost/portal");
    }

    private Event event(String ref, String userId) {

        Event e = mock(Event.class);
        when(e.getEvent()).thenReturn(GradebookUserNotificationHandler.EVENT_UPDATE_ITEM_SCORE);
        when(e.getResource()).thenReturn(ref);
        when(e.getUserId()).thenReturn(userId);
        return e;
    }

    private List<UserNotificationData> handle(String ref) {
        return handler.handleEvent(event(ref, INSTRUCTOR)).orElse(List.of());
    }

    @Test
    public void notifiesStudentForReleasedItem() {

        List<UserNotificationData> result = handle("/gradebook/site1/Quiz 1/student/8.0/student");
        assertEquals(1, result.size());
        UserNotificationData und = result.get(0);
        assertEquals(INSTRUCTOR, und.getFrom());
        assertEquals(STUDENT, und.getTo());
        assertEquals(SITE_ID, und.getSiteId());
        assertEquals("Quiz 1", und.getTitle());
        assertEquals("http://localhost/portal/site/site1/tool/gbtool", und.getUrl());
        assertEquals(GradebookUserNotificationHandler.GRADEBOOK_TOOL_ID, und.getCommonToolId());
    }

    @Test
    public void handlesItemNamesContainingSlashes() {

        GradebookAssignment slashed = new GradebookAssignment();
        slashed.setName("Unit 1/Quiz");
        slashed.setReleased(true);
        slashed.setGradebook(gradebook);
        when(gradingPersistenceManager.getAssignmentByNameAndGradebook("Unit 1/Quiz", SITE_ID)).thenReturn(Optional.of(slashed));

        List<UserNotificationData> result = handle("/gradebook/site1/Unit 1/Quiz/student/8.0/student");
        assertEquals(1, result.size());
        assertEquals("Unit 1/Quiz", result.get(0).getTitle());
    }

    @Test
    public void resolvesSiteForGroupGradebook() throws Exception {

        Gradebook groupGradebook = new Gradebook();
        groupGradebook.setUid("group1");
        groupGradebook.setAssignmentsDisplayed(true);
        GradebookAssignment groupItem = new GradebookAssignment();
        groupItem.setName("Quiz 1");
        groupItem.setReleased(true);
        groupItem.setGradebook(groupGradebook);
        when(gradingPersistenceManager.getAssignmentByNameAndGradebook("Quiz 1", "group1")).thenReturn(Optional.of(groupItem));
        when(siteService.getSite("group1")).thenThrow(new IdUnusedException("group1"));
        Group group = mock(Group.class);
        when(group.getContainingSite()).thenReturn(site);
        when(siteService.findGroup("group1")).thenReturn(group);
        // Each group's Gradebook is its own placement; link to group1's, not the first one
        List<ToolConfiguration> groupPlacements = List.of(placement("gbtool0", "group0"), placement("gbtool1", "group1"));
        when(site.getTools(GradebookUserNotificationHandler.GRADEBOOK_TOOL_ID)).thenReturn(groupPlacements);

        List<UserNotificationData> result = handle("/gradebook/group1/Quiz 1/student/8.0/student");
        assertEquals(1, result.size());
        assertEquals(SITE_ID, result.get(0).getSiteId());
        assertEquals("http://localhost/portal/site/site1/tool/gbtool1", result.get(0).getUrl());
    }

    @Test
    public void skipsWhenNoPlacementShowsTheGradebook() {

        List<ToolConfiguration> otherGroupOnly = List.of(placement("gbtool0", "group0"));
        when(site.getTools(GradebookUserNotificationHandler.GRADEBOOK_TOOL_ID)).thenReturn(otherGroupOnly);
        assertTrue(handle("/gradebook/site1/Quiz 1/student/8.0/student").isEmpty());
    }

    @Test
    public void unviewedCheckMatchesOnlyTheSameItem() {

        String prefix = "/gradebook/site1/Quiz 1/student/";
        assertTrue(GradebookUserNotificationHandler.isSameItem("/gradebook/site1/Quiz 1/student/8.0/student", prefix));
        // Item "Quiz 1/student" scored for user "student" shares the prefix of item "Quiz 1"
        assertFalse(GradebookUserNotificationHandler.isSameItem("/gradebook/site1/Quiz 1/student/student/8.0/student", prefix));
    }

    @Test
    public void ignoresGradebookToolEvents() {
        assertTrue(handle("/gradebookng/7/12/student/8/OK/instructor").isEmpty());
    }

    @Test
    public void ignoresClearedScores() {
        assertTrue(handle("/gradebook/site1/Quiz 1/student/null/student").isEmpty());
    }

    @Test
    public void notifiesForScoresPostedInTheStudentsOwnSession() {

        // SCORM pushes the score while the learner's session is current
        List<UserNotificationData> result = handler.handleEvent(event("/gradebook/site1/Quiz 1/student/8.0/student", STUDENT)).orElse(List.of());
        assertEquals(1, result.size());
        assertEquals(STUDENT, result.get(0).getTo());
    }

    @Test
    public void ignoresUnreleasedItems() {

        item.setReleased(false);
        assertTrue(handle("/gradebook/site1/Quiz 1/student/8.0/student").isEmpty());
    }

    @Test
    public void ignoresGradebooksHidingItemsFromStudents() {

        gradebook.setAssignmentsDisplayed(false);
        assertTrue(handle("/gradebook/site1/Quiz 1/student/8.0/student").isEmpty());
    }

    @Test
    public void ignoresItemsMaintainedByAssignments() {

        item.setExternallyMaintained(true);
        item.setExternalAppName(GradebookUserNotificationHandler.ASSIGNMENTS_APP_NAME);
        assertTrue(handle("/gradebook/site1/Quiz 1/student/8.0/student").isEmpty());
    }

    @Test
    public void notifiesForOtherExternalItems() {

        item.setExternallyMaintained(true);
        item.setExternalAppName("IMS-AGS");
        assertEquals(1, handle("/gradebook/site1/Quiz 1/student/8.0/student").size());
    }

    @Test
    public void skipsWhenAnUnviewedNotificationExists() {

        doReturn(true).when(handler).hasUnviewedNotification(STUDENT, "/gradebook/site1/Quiz 1/student/");
        assertTrue(handle("/gradebook/site1/Quiz 1/student/8.0/student").isEmpty());
    }
}
