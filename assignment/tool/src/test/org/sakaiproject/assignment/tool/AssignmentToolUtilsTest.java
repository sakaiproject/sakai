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
package org.sakaiproject.assignment.tool;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

@RunWith(MockitoJUnitRunner.class)
public class AssignmentToolUtilsTest {

    @Mock private AssignmentService assignmentService;
    @Mock private FormattedText formattedText;
    @Mock private GradingService gradingService;
    @Mock private ResourceLoader resourceLoader;
    @Mock private RubricsService rubricsService;
    @Mock private SiteService siteService;
    @Mock private TimeService timeService;
    @Mock private ToolManager toolManager;
    @Mock private UserDirectoryService userDirectoryService;

    private AssignmentToolUtils assignmentToolUtils;

    @Before
    public void setUp() {
        assignmentToolUtils = new AssignmentToolUtils(resourceLoader);
        assignmentToolUtils.setAssignmentService(assignmentService);
        assignmentToolUtils.setFormattedText(formattedText);
        assignmentToolUtils.setGradingService(gradingService);
        assignmentToolUtils.setRubricsService(rubricsService);
        assignmentToolUtils.setSiteService(siteService);
        assignmentToolUtils.setTimeService(timeService);
        assignmentToolUtils.setToolManager(toolManager);
        assignmentToolUtils.setUserDirectoryService(userDirectoryService);
    }

    @Test
    public void resolveGradebookTargetsUsesSiteGradebookWhenGroupInstancesDisabled() {
        String siteId = "site";
        Assignment assignment = mock(Assignment.class);

        when(gradingService.isGradebookGroupEnabled(siteId)).thenReturn(false);

        List<AssignmentToolUtils.GradebookTarget> targets =
                assignmentToolUtils.resolveGradebookTargets(siteId, assignment, "external-assignment", null);

        Assert.assertEquals(1, targets.size());
        Assert.assertEquals(siteId, targets.get(0).getGradebookUid());
        Assert.assertEquals("external-assignment", targets.get(0).getGradebookItem());
    }

    @Test
    public void resolveGradebookTargetsPrefersSubmissionGroupOwnerForGroupAssignments() throws Exception {
        String siteId = "site";
        String team1Ref = "/site/site/group/team1";
        String team4Ref = "/site/site/group/team4";

        Assignment assignment = mock(Assignment.class);
        when(assignment.getIsGroup()).thenReturn(true);
        when(assignment.getGroups()).thenReturn(new LinkedHashSet<>(Arrays.asList(team1Ref, team4Ref)));

        AssignmentSubmission submission = mock(AssignmentSubmission.class);
        when(submission.getGroupId()).thenReturn("team4");

        Site site = mock(Site.class);
        Group team1 = mockGroup("team1", Collections.singleton("shared-user"));
        Group team4 = mockGroup("team4", new LinkedHashSet<>(Arrays.asList("shared-user", "team4-user")));

        when(gradingService.isGradebookGroupEnabled(siteId)).thenReturn(true);
        when(siteService.getSite(siteId)).thenReturn(site);
        when(site.getGroup(team1Ref)).thenReturn(team1);
        when(site.getGroup(team4Ref)).thenReturn(team4);

        when(gradingService.isExternalAssignmentDefined("team4", "external-team1")).thenReturn(false);
        when(gradingService.isExternalAssignmentDefined("team4", "1002")).thenReturn(false);
        when(gradingService.getGradebookAssigment(siteId, 1002L)).thenReturn(mockGradebookAssignment("team4"));

        List<AssignmentToolUtils.GradebookTarget> targets =
                assignmentToolUtils.resolveGradebookTargets(siteId, assignment, "external-team1,1002", submission);

        Assert.assertEquals(1, targets.size());
        Assert.assertEquals("team4", targets.get(0).getGradebookUid());
        Assert.assertEquals("1002", targets.get(0).getGradebookItem());
    }

    @Test
    public void resolveGradebookTargetsFallsBackToSubmitterMembershipWithoutGroupOwner() throws Exception {
        String siteId = "site";
        String team1Ref = "/site/site/group/team1";
        String team4Ref = "/site/site/group/team4";

        Assignment assignment = mock(Assignment.class);
        when(assignment.getIsGroup()).thenReturn(false);
        when(assignment.getGroups()).thenReturn(new LinkedHashSet<>(Arrays.asList(team1Ref, team4Ref)));

        AssignmentSubmission submission = mock(AssignmentSubmission.class);

        AssignmentSubmissionSubmitter submitter = mock(AssignmentSubmissionSubmitter.class);
        when(submitter.getSubmitter()).thenReturn("team1-user");
        when(submission.getSubmitters()).thenReturn(Collections.singleton(submitter));

        Site site = mock(Site.class);
        Group team1 = mockGroup("team1", Collections.singleton("team1-user"));
        Group team4 = mockGroup("team4", Collections.singleton("team4-user"));

        when(gradingService.isGradebookGroupEnabled(siteId)).thenReturn(true);
        when(siteService.getSite(siteId)).thenReturn(site);
        when(site.getGroup(team1Ref)).thenReturn(team1);
        when(site.getGroup(team4Ref)).thenReturn(team4);

        when(gradingService.isExternalAssignmentDefined("team1", "external-team1")).thenReturn(true);
        when(gradingService.isExternalAssignmentDefined("team1", "1002")).thenReturn(false);

        List<AssignmentToolUtils.GradebookTarget> targets =
                assignmentToolUtils.resolveGradebookTargets(siteId, assignment, "external-team1,1002", submission);

        Assert.assertEquals(1, targets.size());
        Assert.assertEquals("team1", targets.get(0).getGradebookUid());
        Assert.assertEquals("external-team1", targets.get(0).getGradebookItem());
    }

    @Test
    public void resolveGradebookTargetsUsesAssignmentReferenceWhenAssociationMissing() throws Exception {
        String siteId = "site";
        String team1Ref = "/site/site/group/team1";

        Assignment assignment = mock(Assignment.class);
        when(assignment.getContext()).thenReturn(siteId);
        when(assignment.getId()).thenReturn("assignment-1");
        when(assignment.getGroups()).thenReturn(new LinkedHashSet<>(Collections.singleton(team1Ref)));

        String assignmentRef = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();

        Site site = mock(Site.class);
        Group team1 = mockGroup("team1", Collections.singleton("team1-user"));

        when(gradingService.isGradebookGroupEnabled(siteId)).thenReturn(true);
        when(siteService.getSite(siteId)).thenReturn(site);
        when(site.getGroup(team1Ref)).thenReturn(team1);
        when(gradingService.isExternalAssignmentDefined("team1", assignmentRef)).thenReturn(true);

        List<AssignmentToolUtils.GradebookTarget> targets =
                assignmentToolUtils.resolveGradebookTargets(siteId, assignment, null, null);

        Assert.assertEquals(1, targets.size());
        Assert.assertEquals("team1", targets.get(0).getGradebookUid());
        Assert.assertEquals(assignmentRef, targets.get(0).getGradebookItem());
    }

    @Test
    public void resolveGradebookTargetsIncludesAllAssignedGroupsForBulkUpdates() throws Exception {
        String siteId = "site";
        String team1Ref = "/site/site/group/team1";
        String team4Ref = "/site/site/group/team4";

        Assignment assignment = mock(Assignment.class);
        when(assignment.getGroups()).thenReturn(new LinkedHashSet<>(Arrays.asList(team1Ref, team4Ref)));

        Site site = mock(Site.class);
        Group team1 = mockGroup("team1", Collections.singleton("team1-user"));
        Group team4 = mockGroup("team4", Collections.singleton("team4-user"));

        when(gradingService.isGradebookGroupEnabled(siteId)).thenReturn(true);
        when(siteService.getSite(siteId)).thenReturn(site);
        when(site.getGroup(team1Ref)).thenReturn(team1);
        when(site.getGroup(team4Ref)).thenReturn(team4);

        when(gradingService.isExternalAssignmentDefined("team1", "external-team1")).thenReturn(true);
        when(gradingService.isExternalAssignmentDefined("team1", "1002")).thenReturn(false);
        when(gradingService.isExternalAssignmentDefined("team4", "external-team1")).thenReturn(false);
        when(gradingService.isExternalAssignmentDefined("team4", "1002")).thenReturn(false);
        when(gradingService.getGradebookAssigment(siteId, 1002L)).thenReturn(mockGradebookAssignment("team4"));

        List<AssignmentToolUtils.GradebookTarget> targets =
                assignmentToolUtils.resolveGradebookTargets(siteId, assignment, "external-team1,1002", null);

        Assert.assertEquals(2, targets.size());
        Set<String> targetPairs = targets.stream()
                .map(target -> target.getGradebookUid() + "|" + target.getGradebookItem())
                .collect(Collectors.toSet());
        Assert.assertEquals(Set.of("team1|external-team1", "team4|1002"), targetPairs);
    }

    @Test
    public void resolveGradebookTargetsIgnoresStaleInternalGradebookItems() throws Exception {
        String siteId = "site";
        String team1Ref = "/site/site/group/team1";

        Assignment assignment = mock(Assignment.class);
        when(assignment.getGroups()).thenReturn(new LinkedHashSet<>(Collections.singleton(team1Ref)));

        Site site = mock(Site.class);
        Group team1 = mockGroup("team1", Collections.singleton("team1-user"));

        when(gradingService.isGradebookGroupEnabled(siteId)).thenReturn(true);
        when(siteService.getSite(siteId)).thenReturn(site);
        when(site.getGroup(team1Ref)).thenReturn(team1);

        when(gradingService.isExternalAssignmentDefined("team1", "external-team1")).thenReturn(true);
        when(gradingService.isExternalAssignmentDefined("team1", "1002")).thenReturn(false);
        when(gradingService.getGradebookAssigment(siteId, 1002L))
                .thenThrow(new AssessmentNotFoundException("stale item"));

        List<AssignmentToolUtils.GradebookTarget> targets =
                assignmentToolUtils.resolveGradebookTargets(siteId, assignment, "external-team1,1002", null);

        Assert.assertEquals(1, targets.size());
        Assert.assertEquals("team1", targets.get(0).getGradebookUid());
        Assert.assertEquals("external-team1", targets.get(0).getGradebookItem());
    }

    private Group mockGroup(String groupId, Set<String> users) {
        Group group = mock(Group.class);
        when(group.getId()).thenReturn(groupId);
        when(group.getUsers()).thenReturn(users);
        return group;
    }

    private GradebookAssignment mockGradebookAssignment(String gradebookUid) {
        GradebookAssignment gradebookAssignment = new GradebookAssignment();
        Gradebook gradebook = new Gradebook();
        gradebook.setUid(gradebookUid);
        gradebookAssignment.setGradebook(gradebook);
        return gradebookAssignment;
    }
}
