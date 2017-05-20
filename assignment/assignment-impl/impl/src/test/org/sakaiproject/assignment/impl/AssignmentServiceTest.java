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

package org.sakaiproject.assignment.impl;

import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.javafaker.Faker;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AssignmentTestConfiguration.class})
public class AssignmentServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

    private static final Faker faker = new Faker();

    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private AssignmentService assignmentService;
    @Autowired private EntityManager entityManager;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private SiteService siteService;

    @Before
    public void setUp() {
        when(serverConfigurationService.getAccessUrl()).thenReturn("http://localhost:8080/access");
    }

    @Test
    public void AssignmentServiceIsValid() {
        Assert.assertNotNull(assignmentService);
    }

    @Test
    public void addAndGetAssignment() {
        String userId = UUID.randomUUID().toString();
        String context = UUID.randomUUID().toString();
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference())).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference())).thenReturn(true);
        when(sessionManager.getCurrentSessionUserId()).thenReturn(userId);

        String assignmentId = null;
        try {
            Assignment asn = assignmentService.addAssignment(context);
            assignmentId = asn.getId();
        } catch (PermissionException e) {
            Assert.fail(e.getClass().getCanonicalName() + ": " + e.getMessage());
        }
        Assignment assignment = null;
        try {
            assignment = assignmentService.getAssignment(assignmentId);
        } catch (IdUnusedException | PermissionException e) {
            Assert.fail(e.getClass().getCanonicalName() + ": " + e.getMessage());
        }
        Assert.assertNotNull(assignment);
        Assert.assertEquals(assignmentId, assignment.getId());
    }

    @Test
    public void getAssignmentsForContext() {
        String context = UUID.randomUUID().toString();
        createNewAssignment(context);
        Collection assignments = assignmentService.getAssignmentsForContext(context);
        Assert.assertNotNull(assignments);
        Assert.assertEquals(1, assignments.size());
    }

    @Test
    public void getAssignmentStatus() {
        String context = UUID.randomUUID().toString();
        Assignment assignment = createNewAssignment(context);
        String assignmentId = assignment.getId();
        assignment.setDraft(Boolean.TRUE);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference())).thenReturn(true);
        try {
            assignmentService.updateAssignment(assignment);
            AssignmentConstants.Status status = assignmentService.getAssignmentCannonicalStatus(assignmentId);
            Assert.assertEquals(AssignmentConstants.Status.DRAFT, status);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void parseEntityReference() {
        String context = UUID.randomUUID().toString();
        String assignmentId = UUID.randomUUID().toString();

        String refA = AssignmentReferenceReckoner.reckoner().context(context).subtype("a").id(assignmentId).reckon().getReference();
        FakeReference reference = new FakeReference(assignmentService, refA);
        Assert.assertTrue(assignmentService.parseEntityReference(refA, reference));
        Assert.assertEquals(AssignmentServiceConstants.APPLICATION_ID, reference.getType());
        Assert.assertEquals("a", reference.getSubType());
        Assert.assertEquals(context, reference.getContext());
        Assert.assertEquals(assignmentId, reference.getId());
        Assert.assertEquals(refA, reference.getReference());
    }

    @Test
    public void createAssignmentEntity() {
        String context = UUID.randomUUID().toString();
        Assignment assignment = createNewAssignment(context);
        String stringRef = AssignmentReferenceReckoner.reckoner().context(assignment.getContext()).subtype("a").id(assignment.getId()).reckon().getReference();
        FakeReference reference = new FakeReference(assignmentService, stringRef);
        assignmentService.parseEntityReference(stringRef, reference);
        when(entityManager.newReference(stringRef)).thenReturn(reference);
        Entity entity = assignmentService.createAssignmentEntity(assignment.getId());
        Assert.assertEquals(assignment.getId(), entity.getId());
        Assert.assertEquals(reference.getReference(), entity.getReference());
    }

    @Test
    public void removeAssignment() {
        String context = UUID.randomUUID().toString();
        Assignment assignment = createNewAssignment(context);
        String stringRef = AssignmentReferenceReckoner.reckoner().context(assignment.getContext()).subtype("a").id(assignment.getId()).reckon().getReference();
        Assignment removed = null;
        when(securityService.unlock(AssignmentServiceConstants.SECURE_REMOVE_ASSIGNMENT, stringRef)).thenReturn(true);
        try {
            assignmentService.removeAssignment(assignment);
            removed = assignmentService.getAssignment(assignment.getId());
        } catch (PermissionException e) {
            Assert.fail("Assignment not removed");
        } catch (IdUnusedException e) {
            // tests pass if assignment doesn't exist
            Assert.assertNull(removed);
            return;
        }
        Assert.fail("Should never reach this line");
    }

    @Test
    public void removeAssignmentPermissionDenied() {
        String context = UUID.randomUUID().toString();
        Assignment assignment = createNewAssignment(context);
        String stringRef = AssignmentReferenceReckoner.reckoner().context(assignment.getContext()).subtype("a").id(assignment.getId()).reckon().getReference();
        when(securityService.unlock(AssignmentServiceConstants.SECURE_REMOVE_ASSIGNMENT, stringRef)).thenReturn(false);
        try {
            assignmentService.removeAssignment(assignment);
        } catch (PermissionException e) {
            Assignment notRemoved = null;
            try {
                notRemoved = assignmentService.getAssignment(assignment.getId());
            } catch (Exception e1) {
                Assert.fail("Cannot verify if assignment exists");
            }
            Assert.assertNotNull(notRemoved);
            Assert.assertEquals(assignment.getId(), notRemoved.getId());
            return;
        }
        Assert.fail("Should never reach this line");
    }

    @Test
    public void updateAssignment() {
        final String title = "ASSIGNMENT TITLE";
        String context = UUID.randomUUID().toString();
        Assignment assignment = createNewAssignment(context);
        assignment.setTitle(title);
        Assignment updatedAssignment = null;
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference())).thenReturn(true);
        try {
            assignmentService.updateAssignment(assignment);
            updatedAssignment = assignmentService.getAssignment(assignment.getId());
        } catch (Exception e) {
            Assert.fail("Could not update assignment");
        }
        Assert.assertNotNull(updatedAssignment);
        // TODO check all fields
        Assert.assertEquals(title, updatedAssignment.getTitle());
        Assert.assertEquals(context, updatedAssignment.getContext());
    }

    @Test
    public void addAndGetSubmission() {
        String context = UUID.randomUUID().toString();
        String submitterId = UUID.randomUUID().toString();
        try {
            AssignmentSubmission savedSubmission = createNewSubmission(context, submitterId);
            Assert.assertNotNull(savedSubmission);
            Assert.assertNotNull(savedSubmission.getId());

            AssignmentSubmission getSubmission = assignmentService.getSubmission(savedSubmission.getId());
            Assert.assertNotNull(getSubmission);
            Assert.assertNotNull(getSubmission.getId());

            Assignment assignment = getSubmission.getAssignment();
            Assert.assertNotNull(assignment.getId());
            Assert.assertEquals(context, assignment.getContext());

            Set<AssignmentSubmissionSubmitter> submitters = getSubmission.getSubmitters();
            Assert.assertEquals(1, submitters.size());
            AssignmentSubmissionSubmitter submitter = submitters.stream().findAny().get();
            Assert.assertNotNull(submitter);
            Assert.assertNotNull(submitter.getId());
            Assert.assertEquals(submitterId, submitter.getSubmitter());
        } catch (Exception e) {
            Assert.fail("Could not create submission, " + e.getMessage());
        }
    }

    @Test
    public void removeSubmission() {
        String context = UUID.randomUUID().toString();
        String submitterId = UUID.randomUUID().toString();
        try {
            AssignmentSubmission submission = createNewSubmission(context, submitterId);
            String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
            when(securityService.unlock(AssignmentServiceConstants.SECURE_REMOVE_ASSIGNMENT_SUBMISSION, reference)).thenReturn(true);
            String submissionId = submission.getId();
            assignmentService.removeSubmission(submission);

            AssignmentSubmission removedSubmmision = assignmentService.getSubmission(submissionId);
            Assert.assertNull(removedSubmmision);
        } catch (Exception e) {
            Assert.fail("Could not create submission, " + e.getMessage());
        }
    }

    private AssignmentSubmission createNewSubmission(String context, String submitterId) throws UserNotDefinedException {
        Assignment assignment = createNewAssignment(context);
        User userMock = Mockito.mock(User.class);
        when(userMock.getId()).thenReturn(submitterId);
        when(userDirectoryService.getUser(submitterId)).thenReturn(userMock);
        when(siteService.siteReference(assignment.getContext())).thenReturn("/site/" + assignment.getContext());
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION, "/site/" + assignment.getContext())).thenReturn(true);
        AssignmentSubmission submission = null;
        try {
            submission = assignmentService.addSubmission(assignment.getId(), submitterId);
        } catch (PermissionException e) {
            Assert.fail(e.getMessage());
        }
        return submission;
    }

    private Assignment createNewAssignment(String context) {
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference())).thenReturn(true);
        Assignment assignment = null;
        try {
            assignment = assignmentService.addAssignment(context);
        } catch (PermissionException e) {
            Assert.fail(e.getMessage());
        }
        return assignment;
    }
}
