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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

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

        ToolConfiguration tool = mock(ToolConfiguration.class);
        when(tool.getId()).thenReturn("gbtool");
        Site site = mock(Site.class);
        when(site.getId()).thenReturn(SITE_ID);
        when(site.getToolForCommonId(GradebookUserNotificationHandler.GRADEBOOK_TOOL_ID)).thenReturn(tool);
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
        Site site = siteService.getSite(SITE_ID);
        when(group.getContainingSite()).thenReturn(site);
        when(siteService.findGroup("group1")).thenReturn(group);

        List<UserNotificationData> result = handle("/gradebook/group1/Quiz 1/student/8.0/student");
        assertEquals(1, result.size());
        assertEquals(SITE_ID, result.get(0).getSiteId());
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
