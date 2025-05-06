/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.impl.jobs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobExecutionContext;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

public class AssignmentGroupUnlockingJobTest {

    private AssignmentGroupUnlockingJob job;
    
    @Mock private AssignmentService assignmentService;
    @Mock private AuthzGroupService authzGroupService;
    @Mock private SessionManager sessionManager;
    @Mock private SiteService siteService;
    @Mock private JobExecutionContext jobExecutionContext;
    @Mock private Session session;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        job = new AssignmentGroupUnlockingJob();
        job.setAssignmentService(assignmentService);
        job.setAuthzGroupService(authzGroupService);
        job.setSessionManager(sessionManager);
        job.setSiteService(siteService);
        
        when(sessionManager.getCurrentSession()).thenReturn(session);
    }
    
    @Test
    public void testGroupUnlockingJobForAssignmentWithNoSubmissions() throws Exception {
        // Mock site
        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site-1");
        when(site.isPublished()).thenReturn(true);
        when(site.isSoftlyDeleted()).thenReturn(false);
        
        // Mock assignment
        Assignment assignment = mock(Assignment.class);
        when(assignment.getId()).thenReturn("assignment-1");
        when(assignment.getDraft()).thenReturn(false);
        when(assignment.getTypeOfAccess()).thenReturn(Assignment.Access.GROUP);
        when(assignment.getContext()).thenReturn("site-1");
        
        // Set up a future open date
        Instant now = Instant.now();
        Instant twoHoursFromNow = now.plusSeconds(2 * 60 * 60);
        when(assignment.getOpenDate()).thenReturn(twoHoursFromNow);
        
        // Set up groups for the assignment
        Set<String> groupRefs = new HashSet<>(Arrays.asList("group-1", "group-2"));
        when(assignment.getGroups()).thenReturn(groupRefs);
        
        // Set up collections to return from service calls
        List<Site> sites = Arrays.asList(site);
        when(siteService.getSites(any(), any(), any(), any(), any(), any())).thenReturn(sites);
        
        Collection<Assignment> assignments = Arrays.asList(assignment);
        when(assignmentService.getAssignmentsForContext("site-1")).thenReturn(assignments);
        
        // No submissions for this assignment
        when(assignmentService.getSubmissions(assignment)).thenReturn(Collections.emptySet());
        
        // Set up mocked group objects
        Group group1 = mock(Group.class);
        when(group1.getProperties()).thenReturn(null);
        AuthzGroup authzGroup1 = mock(AuthzGroup.class);
        
        Group group2 = mock(Group.class);
        when(group2.getProperties()).thenReturn(null);
        AuthzGroup authzGroup2 = mock(AuthzGroup.class);
        
        // Set up site to return the groups
        when(site.getGroup("group-1".replaceFirst(site.getReference() + "/group/", ""))).thenReturn(group1);
        when(site.getGroup("group-2".replaceFirst(site.getReference() + "/group/", ""))).thenReturn(group2);
        
        // Set up authz groups
        when(authzGroupService.getAuthzGroup("group-1")).thenReturn(authzGroup1);
        when(authzGroupService.getAuthzGroup("group-2")).thenReturn(authzGroup2);
        
        // Create the actual reference that will be used by the job
        String reference = AssignmentReferenceReckoner.reckoner()
                .context("site-1")
                .id("assignment-1")
                .reckon()
                .getReference();
        
        // Execute the job
        job.execute(jobExecutionContext);
        
        // Verify that the groups were unlocked (set to NONE lock mode)
        verify(authzGroup1).setLockForReference(reference, AuthzGroup.RealmLockMode.NONE);
        verify(authzGroup2).setLockForReference(reference, AuthzGroup.RealmLockMode.NONE);
        verify(authzGroupService, times(2)).save(any(AuthzGroup.class));
    }
    
    @Test
    public void testGroupUnlockingJobForAssignmentWithSubmissions() throws Exception {
        // Mock site
        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site-1");
        when(site.isPublished()).thenReturn(true);
        when(site.isSoftlyDeleted()).thenReturn(false);
        
        // Mock assignment
        Assignment assignment = mock(Assignment.class);
        when(assignment.getId()).thenReturn("assignment-1");
        when(assignment.getDraft()).thenReturn(false);
        when(assignment.getTypeOfAccess()).thenReturn(Assignment.Access.GROUP);
        when(assignment.getContext()).thenReturn("site-1");
        
        // Set up a future open date
        Instant now = Instant.now();
        Instant twoHoursFromNow = now.plusSeconds(2 * 60 * 60);
        when(assignment.getOpenDate()).thenReturn(twoHoursFromNow);
        
        // Set up groups for the assignment
        Set<String> groupRefs = new HashSet<>(Arrays.asList("group-1", "group-2"));
        when(assignment.getGroups()).thenReturn(groupRefs);
        
        // Set up collections to return from service calls
        List<Site> sites = Arrays.asList(site);
        when(siteService.getSites(any(), any(), any(), any(), any(), any())).thenReturn(sites);
        
        Collection<Assignment> assignments = Arrays.asList(assignment);
        when(assignmentService.getAssignmentsForContext("site-1")).thenReturn(assignments);
        
        // Assignment has submissions, so we should NOT unlock the groups
        Set<AssignmentSubmission> submissions = new HashSet<>(Arrays.asList(mock(AssignmentSubmission.class)));
        when(assignmentService.getSubmissions(assignment)).thenReturn(submissions);
        
        // Create the actual reference that will be used by the job
        String reference = AssignmentReferenceReckoner.reckoner()
                .context("site-1")
                .id("assignment-1")
                .reckon()
                .getReference();
        
        // Execute the job
        job.execute(jobExecutionContext);
        
        // Verify that the authzGroupService.save was never called since the assignment has submissions
        verify(authzGroupService, never()).save(any(AuthzGroup.class));
    }
}