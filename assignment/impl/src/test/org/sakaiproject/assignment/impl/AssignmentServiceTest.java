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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    @Autowired private FormattedText formattedText;

    private ResourceLoader resourceLoader;

    @Before
    public void setUp() {
        when(serverConfigurationService.getAccessUrl()).thenReturn("http://localhost:8080/access");
        resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getString("gen.inpro")).thenReturn("In progress");
        when(resourceLoader.getString("gen.dra2")).thenReturn("Draft -");
        when(resourceLoader.getString("gen.subm4")).thenReturn("Submitted");
        when(resourceLoader.getString("gen.nograd")).thenReturn("No Grade");
        when(resourceLoader.getString("ungra")).thenReturn("Ungraded");
        when(resourceLoader.getString("pass")).thenReturn("Pass");
        when(resourceLoader.getString("fail")).thenReturn("Fail");
        when(resourceLoader.getString("gen.checked")).thenReturn("Checked");
        when(resourceLoader.getString("assignment.copy")).thenReturn("Copy");
        ((AssignmentServiceImpl) assignmentService).setResourceLoader(resourceLoader);
    }

    @Test
    public void AssignmentServiceIsValid() {
        Assert.assertNotNull(assignmentService);
    }

    @Test
    public void checkAssignmentToolTitle() {
        Assert.assertNotNull(assignmentService.getToolTitle());
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

    @Test
    public void findSubmissionForUser() {
        String context = UUID.randomUUID().toString();
        String submitterId = UUID.randomUUID().toString();
        try {
            AssignmentSubmission submission = createNewSubmission(context, submitterId);
            Assignment assignment = submission.getAssignment();
            String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
            when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT_SUBMISSION, reference)).thenReturn(true);
            AssignmentSubmission submission1 = assignmentService.getSubmission(assignment.getId(), submitterId);
            Assert.assertEquals(submission.getId(), submission1.getId());
        } catch (Exception e) {
            Assert.fail("Could not create submission, " + e.getMessage());
        }
    }

    @Test
    public void duplicateAssignment() throws IdUnusedException {
        // Setup a new Assignment
        String context = UUID.randomUUID().toString();
        Assignment assignment = createNewAssignment(context);
        Assert.assertNotNull(assignment);

        Instant now = Instant.now();
        assignment.setTitle("Assignment Week One");
        assignment.setSection("0001");
        assignment.setOpenDate(now);
        assignment.setDueDate(now.plus(Duration.ofDays(1)));
        assignment.setDropDeadDate(now.plus(Duration.ofDays(2)));
        assignment.setCloseDate(now.plus(Duration.ofDays(3)));
        Map<String, String> properties = assignment.getProperties();
        IntStream.range(0, 10).forEach(i -> properties.put("PROP_NAME_" + i, "PROP_VALUE_" + i));
        AssignmentServiceConstants.PROPERTIES_EXCLUDED_FROM_DUPLICATE_ASSIGNMENTS.forEach(p -> properties.put(p, p + "_VALUE"));
        assignment.setProperties(properties);

        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference())).thenReturn(true);
        try {
            assignmentService.updateAssignment(assignment);
        } catch (PermissionException e) {
            Assert.fail("Updating assignment, " + e.getMessage());
        }

        // Duplicate the Assignment
        Assignment duplicateAssignment = null;
        try {
            duplicateAssignment = assignmentService.addDuplicateAssignment(context, assignment.getId());
        } catch (IdInvalidException | PermissionException | IdUsedException e) {
            Assert.fail("Duplicating assignment, " + e.getMessage());
        }
        Assert.assertNotNull(duplicateAssignment);
        // Compare the 2 assignments
        Assert.assertNotEquals(assignment, duplicateAssignment);
        Assert.assertNotEquals(assignment.getId(), duplicateAssignment.getId());
        Assert.assertEquals(assignment.getContext(), duplicateAssignment.getContext());
        Assert.assertEquals(assignment.getSection(), duplicateAssignment.getSection());
        Assert.assertEquals(assignment.getOpenDate(), duplicateAssignment.getOpenDate());
        Assert.assertEquals(assignment.getDueDate(), duplicateAssignment.getDueDate());
        Assert.assertEquals(assignment.getDropDeadDate(), duplicateAssignment.getDropDeadDate());
        Assert.assertEquals(assignment.getCloseDate(), duplicateAssignment.getCloseDate());
        Assert.assertEquals(assignment.getPosition(), duplicateAssignment.getPosition());
        Assert.assertEquals(
                assignment.getProperties().entrySet().stream()
                        .filter(e -> !AssignmentServiceConstants.PROPERTIES_EXCLUDED_FROM_DUPLICATE_ASSIGNMENTS.contains(e.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                duplicateAssignment.getProperties());
    }

    @Test
    public void submissionStatus() {
        // gen.resub         = Re-submitted
        // gen.late2         = - late
        // gen.subm4         = Submitted
        // gen.returned      = Returned
        // ungra             = Ungraded
        // listsub.nosub     = No Submission
        // gen.notsta        = Not Started
        // gen.dra2          = Draft -
        // gen.inpro         = In progress
        // gen.commented     = Commented
        // grad3             = Graded

        String context = UUID.randomUUID().toString();
        String submitterId = UUID.randomUUID().toString();
        try {
            AssignmentSubmission submission = createNewSubmission(context, submitterId);
            String status = assignmentService.getSubmissionStatus(submission.getId());
            Assert.assertEquals("Draft - In progress", status);

            String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
            when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT_SUBMISSION, reference)).thenReturn(true);
            submission.setSubmitted(true);
            submission.setDateSubmitted(Instant.now());
            assignmentService.updateSubmission(submission);
            status = assignmentService.getSubmissionStatus(submission.getId());
            Assert.assertEquals("Submitted " + submission.getDateSubmitted().toString(), status);
        } catch (Exception e) {
            Assert.fail("Could not create/update submission, " + e.getMessage());
        }
    }

    @Test
    public void gradeDisplay() {
        Character ds = DecimalFormatSymbols.getInstance().getDecimalSeparator();
        when(formattedText.getDecimalSeparator()).thenReturn(ds.toString());

        Assert.assertEquals("0", assignmentService.getGradeDisplay("0", Assignment.GradeType.SCORE_GRADE_TYPE, null));

        configureScale(10);
        Assert.assertEquals("0.5", assignmentService.getGradeDisplay("5", Assignment.GradeType.SCORE_GRADE_TYPE, 10));
        Assert.assertEquals("10.0", assignmentService.getGradeDisplay("100", Assignment.GradeType.SCORE_GRADE_TYPE, 10));

        configureScale(100);
        Assert.assertEquals("0.05", assignmentService.getGradeDisplay("5", Assignment.GradeType.SCORE_GRADE_TYPE, 100));
        Assert.assertEquals("5.00", assignmentService.getGradeDisplay("500", Assignment.GradeType.SCORE_GRADE_TYPE, 100));
        Assert.assertEquals("100.00", assignmentService.getGradeDisplay("10000", Assignment.GradeType.SCORE_GRADE_TYPE, 100));

        configureScale(1000);
        Assert.assertEquals("0.005", assignmentService.getGradeDisplay("5", Assignment.GradeType.SCORE_GRADE_TYPE, 1000));
        Assert.assertEquals("50.000", assignmentService.getGradeDisplay("50000", Assignment.GradeType.SCORE_GRADE_TYPE, 1000));

        Assert.assertEquals("", assignmentService.getGradeDisplay("", Assignment.GradeType.UNGRADED_GRADE_TYPE, null));
        Assert.assertEquals("No Grade", assignmentService.getGradeDisplay("gen.nograd", Assignment.GradeType.UNGRADED_GRADE_TYPE, null));

        Assert.assertEquals("Pass", assignmentService.getGradeDisplay("pass", Assignment.GradeType.PASS_FAIL_GRADE_TYPE, null));
        Assert.assertEquals("Fail", assignmentService.getGradeDisplay("fail", Assignment.GradeType.PASS_FAIL_GRADE_TYPE, null));
        Assert.assertEquals("Ungraded", assignmentService.getGradeDisplay("any", Assignment.GradeType.PASS_FAIL_GRADE_TYPE, null));

        Assert.assertEquals("Ungraded", assignmentService.getGradeDisplay("any", Assignment.GradeType.CHECK_GRADE_TYPE, null));
        Assert.assertEquals("Checked", assignmentService.getGradeDisplay("checked", Assignment.GradeType.CHECK_GRADE_TYPE, null));

        Assert.assertEquals("Ungraded", assignmentService.getGradeDisplay("", Assignment.GradeType.GRADE_TYPE_NONE, null));
        Assert.assertEquals("self", assignmentService.getGradeDisplay("self", Assignment.GradeType.GRADE_TYPE_NONE, null));
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
        when(sessionManager.getCurrentSessionUserId()).thenReturn(UUID.randomUUID().toString());
        Assignment assignment = null;
        try {
            assignment = assignmentService.addAssignment(context);
        } catch (PermissionException e) {
            Assert.fail(e.getMessage());
        }
        return assignment;
    }

    private void configureScale(int scale) {
        int dec = new Double(Math.log10(scale)).intValue();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(dec);
        nf.setMinimumFractionDigits(dec);
        nf.setGroupingUsed(false);
        when(formattedText.getNumberFormat(dec, dec, false)).thenReturn(nf);
    }
}
