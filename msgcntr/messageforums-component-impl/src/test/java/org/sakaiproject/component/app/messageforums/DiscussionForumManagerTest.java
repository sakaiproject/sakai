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
package org.sakaiproject.component.app.messageforums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.GradebookItemCreationException;
import org.sakaiproject.api.app.messageforums.ui.GradebookItemCreationRequest;
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.api.common.type.TypeManager;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.ConflictingAssignmentNameException;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MsgcntrTestConfiguration.class})
public class DiscussionForumManagerTest extends AbstractTransactionalJUnit4SpringContextTests {

    private static final String SITE_ID = "site-id";
    private static final String GROUP_GRADEBOOK_ID = "group-id";
    private static final String TYPE_ID = "discussion-type";
    private static final String USER_ID = "test-user";

    @Autowired
    @Qualifier("org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager")
    private DiscussionForumManager discussionForumManager;

    @Autowired
    @Qualifier("org.sakaiproject.grading.api.GradingService")
    private GradingService gradingService;

    @Autowired private AreaManager areaManager;
    @Autowired private IdManager idManager;
    @Autowired private LocaleService localeService;
    @Autowired private SessionManager sessionManager;
    @Autowired private SiteService siteService;
    @Autowired
    @Qualifier("org.sakaiproject.tool.api.ToolManager")
    private ToolManager toolManager;
    @Autowired private TypeManager typeManager;

    @Before
    public void setUp() throws Exception {
        TestUtil.setRunningTests(true);
        reset(gradingService, localeService);

        Type type = mock(Type.class);
        when(typeManager.getType(any(), any(), any())).thenReturn(type);
        when(type.getUuid()).thenReturn(TYPE_ID);
        when(idManager.createUuid()).thenAnswer(invocation -> UUID.randomUUID().toString());

        Placement placement = mock(Placement.class);
        when(placement.getContext()).thenReturn(SITE_ID);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(sessionManager.getCurrentSessionUserId()).thenReturn(USER_ID);

        Site site = mock(Site.class);
        when(site.getRoles()).thenReturn(Collections.emptySet());
        when(siteService.getSite(SITE_ID)).thenReturn(site);

        Area area = areaManager.createArea(TYPE_ID, SITE_ID);
        area.setName("Discussions");
        area.setHidden(false);
        area.setEnabled(true);
        area.setSendEmailOut(false);
        area.setSendToEmail(Area.EMAIL_COPY_NEVER);
        area.setLocked(false);
        area.setModerated(false);
        area.setPostFirst(false);
        area.setAutoMarkThreadsRead(false);
        area.setAvailabilityRestricted(false);
        areaManager.saveArea(area, USER_ID);
    }

    @Test
    public void createsGradebookItemInGroupGradebook() {
        prepareGradebook("10", 10D, 42L);

        List<Long> assignmentIds = discussionForumManager.createGradebookItems(SITE_ID,
                Collections.singletonList(new GradebookItemCreationRequest(GROUP_GRADEBOOK_ID, "Weekly discussion")), "10");

        ArgumentCaptor<Assignment> assignmentCaptor = ArgumentCaptor.forClass(Assignment.class);
        verify(gradingService).addAssignment(eq(GROUP_GRADEBOOK_ID), eq(SITE_ID), assignmentCaptor.capture());
        Assignment assignment = assignmentCaptor.getValue();
        assertEquals(Collections.singletonList(42L), assignmentIds);
        assertEquals("Weekly discussion", assignment.getName());
        assertEquals(Double.valueOf(10D), assignment.getPoints());
        assertTrue(assignment.getReleased());
        assertTrue(assignment.getCounted());
        assertEquals(Boolean.FALSE, assignment.getExternallyMaintained());
    }

    @Test
    public void returnsGradebookItemIdsInRequestOrder() {
        when(localeService.parseDouble("10")).thenReturn(10D);
        when(gradingService.getGradebook(any(), eq(SITE_ID))).thenReturn(gradebook());
        when(gradingService.addAssignment(eq("gradebook-one"), eq(SITE_ID), any(Assignment.class))).thenReturn(21L);
        when(gradingService.addAssignment(eq("gradebook-two"), eq(SITE_ID), any(Assignment.class))).thenReturn(42L);

        List<Long> assignmentIds = discussionForumManager.createGradebookItems(SITE_ID, List.of(
                new GradebookItemCreationRequest("gradebook-one", "First discussion"),
                new GradebookItemCreationRequest("gradebook-two", "Second discussion")), "10");

        assertEquals(List.of(21L, 42L), assignmentIds);
    }

    @Test
    public void createsGradebookItemInSiteGradebook() {
        prepareGradebook("20", 20D, 84L);

        List<Long> assignmentIds = discussionForumManager.createGradebookItems(SITE_ID,
                Collections.singletonList(new GradebookItemCreationRequest(SITE_ID, "Graded topic")), "20");

        assertEquals(Collections.singletonList(84L), assignmentIds);
        verify(gradingService).addAssignment(eq(SITE_ID), eq(SITE_ID), any(Assignment.class));
    }

    @Test
    public void rejectsInvalidPointsBeforeSaving() {
        when(localeService.parseDouble("zero")).thenReturn(null);

        try {
            discussionForumManager.createGradebookItems(SITE_ID,
                    Collections.singletonList(new GradebookItemCreationRequest(SITE_ID, "Invalid points")), "zero");
            fail("Expected invalid points to be rejected");
        } catch (GradebookItemCreationException e) {
            assertEquals(GradebookItemCreationException.Reason.INVALID_POINTS, e.getReason());
        }

        verify(gradingService, never()).addAssignment(any(), any(), any());
    }

    @Test
    public void reportsDuplicateGradebookItemName() {
        Gradebook gradebook = gradebook();
        when(localeService.parseDouble("10")).thenReturn(10D);
        when(gradingService.getGradebook(SITE_ID, SITE_ID)).thenReturn(gradebook);
        when(gradingService.addAssignment(any(), any(), any()))
                .thenReturn(21L)
                .thenThrow(new ConflictingAssignmentNameException("duplicate"));

        try {
            discussionForumManager.createGradebookItems(SITE_ID, List.of(
                    new GradebookItemCreationRequest(SITE_ID, "Created first"),
                    new GradebookItemCreationRequest(SITE_ID, "Duplicate"),
                    new GradebookItemCreationRequest(SITE_ID, "Must not be attempted")), "10");
            fail("Expected the duplicate name to be rejected");
        } catch (GradebookItemCreationException e) {
            assertEquals(GradebookItemCreationException.Reason.DUPLICATE_NAME, e.getReason());
        }

        verify(gradingService, times(2)).addAssignment(eq(SITE_ID), eq(SITE_ID), any(Assignment.class));
    }

    private void prepareGradebook(String pointsText, Double points, Long assignmentId) {
        when(localeService.parseDouble(pointsText)).thenReturn(points);
        when(gradingService.getGradebook(any(), eq(SITE_ID))).thenReturn(gradebook());
        when(gradingService.addAssignment(any(), eq(SITE_ID), any())).thenReturn(assignmentId);
    }

    private Gradebook gradebook() {
        Gradebook gradebook = new Gradebook();
        gradebook.setCategoryType(GradingConstants.CATEGORY_TYPE_NO_CATEGORY);
        return gradebook;
    }
}
