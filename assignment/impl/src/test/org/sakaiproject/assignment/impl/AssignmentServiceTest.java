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

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeaderEdit;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tasks.api.Priorities;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tasks.api.UserTaskAdapterBean;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Xml;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.github.javafaker.Faker;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AssignmentTestConfiguration.class})
public class AssignmentServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

    private static final Faker faker = new Faker();

    @Autowired private AssignmentEventObserver assignmentEventObserver;
    @Autowired private AssignmentService assignmentService;
    @Resource(name = "org.sakaiproject.assignment.api.AssignmentService")
    private EntityTransferrer assignmentEntityTransferrer;
    @Autowired private AuthzGroupService authzGroupService;
    @Autowired private EntityManager entityManager;
    @Autowired private FormattedText formattedText;
    @Autowired private GradingService gradingService;
    @Resource(name = "org.sakaiproject.announcement.api.AnnouncementService")
    private AnnouncementService announcementService;
    @Resource(name = "org.sakaiproject.calendar.api.CalendarService")
    private CalendarService calendarService;
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private SiteService siteService;
    @Autowired private TaskService taskService;
    @Resource(name = "org.sakaiproject.time.api.UserTimeService")
    private UserTimeService userTimeService;
    @Resource(name = "org.sakaiproject.time.api.TimeService")
    private TimeService timeService;
    @Autowired private UserDirectoryService userDirectoryService;

    private ResourceLoader resourceLoader;

    @Before
    public void setUp() {
        when(serverConfigurationService.getAccessUrl()).thenReturn("http://localhost:8080/access");
        resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getLocale()).thenReturn(Locale.ENGLISH);
        when(resourceLoader.getString("gen.inpro")).thenReturn("In progress");
        when(resourceLoader.getString("gen.dra2")).thenReturn("Draft -");
        when(resourceLoader.getString("gen.subm4")).thenReturn("Submitted");
        when(resourceLoader.getString("gen.nograd")).thenReturn("No Grade");
        when(resourceLoader.getString("ungra")).thenReturn(AssignmentConstants.UNGRADED_GRADE_TYPE_STRING);
        when(resourceLoader.getString("gen.returned")).thenReturn("Returned");
        when(resourceLoader.getString("pass")).thenReturn("Pass");
        when(resourceLoader.getString("fail")).thenReturn("Fail");
        when(resourceLoader.getString("gen.checked")).thenReturn("Checked");
        when(resourceLoader.getString("assignment.copy")).thenReturn("Copy");
        when(resourceLoader.getString("listsub.nosub")).thenReturn("No Submission");
        when(resourceLoader.getString("gen.notsta")).thenReturn("Not Started");
        when(resourceLoader.getString("gen.due")).thenReturn("Due");
        when(resourceLoader.getFormattedMessage(eq("assig6"), any())).thenReturn("Open Assignment");
        when(resourceLoader.getFormattedMessage(eq("opedat"), any(), any())).thenReturn("Open Date Body");
        when(resourceLoader.getFormattedMessage(eq("assign_due_event_desc"), any(), any())).thenReturn("Due Date Body");
        when(formattedText.convertPlaintextToFormattedText(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        AssignmentServiceImpl assignmentServiceImpl = (AssignmentServiceImpl) AopTestUtils.getTargetObject(assignmentService);
        assignmentServiceImpl.setResourceLoader(resourceLoader);
        assignmentServiceImpl.setTimeService(timeService);
        TimeRange dueRange = mock(TimeRange.class);
        when(timeService.newTimeRange(anyLong(), anyLong())).thenReturn(dueRange);
        when(userTimeService.getLocalTimeZone()).thenReturn(TimeZone.getDefault());
        when(securityService.unlockUsers(anyString(), anyString())).thenReturn(Collections.emptyList());
        User currentUser = mock(User.class);
        when(currentUser.getId()).thenReturn("test-user-id");
        when(userDirectoryService.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    public void AssignmentServiceIsValid() {
        Assert.assertNotNull(assignmentService);
    }

    @Test
    public void checkAssignmentToolId() {
        Assert.assertNotNull(assignmentService.getToolId());
    }

    @Test
    public void securityAllowAddAssignment() {
        // normal user security check
        String context1 = UUID.randomUUID().toString();
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().context(context1).reckon().getReference())).thenReturn(true);
        Assert.assertTrue(assignmentService.allowAddAssignment(context1));

        // group security check
        String context2 = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        when(sessionManager.getCurrentSessionUserId()).thenReturn(userId);
        Site site = mock(Site.class);
        Collection<Group> siteGroups = new HashSet<>();
        Set<String> groupARef = new HashSet<>();
        Group groupA = mock(Group.class);
        String groupAId = UUID.randomUUID().toString();
        when(groupA.getId()).thenReturn(groupAId);
        when(groupA.getReference()).thenReturn("/site/" + context2 + "/group/" + groupAId);
        siteGroups.add(groupA);
        groupARef.add(groupA.getReference());
        when(site.getGroups()).thenReturn(siteGroups);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().context(context2).reckon().getReference())).thenReturn(false);
        try {
            when(siteService.getSite(context2)).thenReturn(site);
        } catch (IdUnusedException e) {
            Assert.fail("missing mock site\n" + e.toString());
        }
        when(authzGroupService.getAuthzGroupsIsAllowed(userId, AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT, groupARef)).thenReturn(groupARef);

        Assert.assertTrue(assignmentService.allowAddAssignment(context2));
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
            Assert.fail(e.toString());
        }
        Assignment assignment = null;
        try {
            assignment = assignmentService.getAssignment(assignmentId);
        } catch (IdUnusedException | PermissionException e) {
            Assert.fail(e.toString());
        }
        Assert.assertNotNull(assignment);
        Assert.assertEquals(assignmentId, assignment.getId());
    }

    @Test
    public void getAssignmentsForContextStudentsCantViewFutureAssignments() {
    	String context = UUID.randomUUID().toString();
    	
    	Site site = mock(Site.class);
    	Collection<Group> siteGroups = new HashSet<>();
    	Set<String> groupARef = new HashSet<>();
    	Group groupA = mock(Group.class);
    	String groupAId = UUID.randomUUID().toString();
    	when(groupA.getId()).thenReturn(groupAId);
    	when(groupA.getReference()).thenReturn("/site/" + context + "/group/" + groupAId);
    	siteGroups.add(groupA);
    	groupARef.add(groupA.getReference());
    	when(site.getGroups()).thenReturn(siteGroups);
    	try {
    		when(siteService.getSite(context)).thenReturn(site);
    	} catch (IdUnusedException e) {
    		Assert.fail("missing mock site\n" + e.toString());
    	}
    	
    	createNewFutureAssignment(context);
        
		when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference())).thenReturn(false);
        Collection assignments = assignmentService.getAssignmentsForContext(context);
        Assert.assertNotNull(assignments);
        Assert.assertEquals(0, assignments.size());
    }

    @Test
    public void getAssignmentsForContextInstructorsCanViewFutureAssignments() {
    	String context = UUID.randomUUID().toString();
    	
    	Site site = mock(Site.class);
    	Collection<Group> siteGroups = new HashSet<>();
    	Set<String> groupARef = new HashSet<>();
    	Group groupA = mock(Group.class);
    	String groupAId = UUID.randomUUID().toString();
    	when(groupA.getId()).thenReturn(groupAId);
    	when(groupA.getReference()).thenReturn("/site/" + context + "/group/" + groupAId);
    	siteGroups.add(groupA);
    	groupARef.add(groupA.getReference());
    	when(site.getGroups()).thenReturn(siteGroups);
    	try {
    		when(siteService.getSite(context)).thenReturn(site);
    	} catch (IdUnusedException e) {
    		Assert.fail("missing mock site\n" + e.toString());
    	}
    	
    	createNewFutureAssignment(context);

        Collection assignments = assignmentService.getAssignmentsForContext(context);
        Assert.assertNotNull(assignments);
        Assert.assertEquals(1, assignments.size());
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
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference())).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference())).thenReturn(true);
        try {
            assignmentService.updateAssignment(assignment);
            AssignmentConstants.Status status = assignmentService.getAssignmentCannonicalStatus(assignmentId);
            Assert.assertEquals(AssignmentConstants.Status.DRAFT, status);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void parseEntityReference() {
        String context = UUID.randomUUID().toString();
        String assignmentId = UUID.randomUUID().toString();

        String refA = AssignmentReferenceReckoner.reckoner().context(context).subtype("a").id(assignmentId).reckon().getReference();
        FakeReference reference = new FakeReference(assignmentService, refA);
        Assert.assertTrue(assignmentService.parseEntityReference(refA, reference));
        Assert.assertEquals(AssignmentServiceConstants.SAKAI_ASSIGNMENT, reference.getType());
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
    public void softDeleteAssignment() {
        String context = UUID.randomUUID().toString();
        Assignment assignment = createNewAssignment(context);
        String stringRef = AssignmentReferenceReckoner.reckoner().context(assignment.getContext()).subtype("a").id(assignment.getId()).reckon().getReference();

        when(securityService.unlock(AssignmentServiceConstants.SECURE_REMOVE_ASSIGNMENT, stringRef)).thenReturn(true);
        try {
            assignmentService.softDeleteAssignment(assignment);
            Assignment deleted = assignmentService.getAssignment(assignment.getId());
            Assert.assertNotNull(deleted);
            Assert.assertTrue(assignment.getDeleted());
        } catch (PermissionException | IdUnusedException e) {
            Assert.fail("Assignment soft deleted\n" + e.toString());
        }
    }

    @Test
    public void deleteAssignment() {
        String context = UUID.randomUUID().toString();
        Assignment assignment = createNewAssignment(context);
        String stringRef = AssignmentReferenceReckoner.reckoner().context(assignment.getContext()).subtype("a").id(assignment.getId()).reckon().getReference();
        Assignment deleted = null;
        when(securityService.unlock(AssignmentServiceConstants.SECURE_REMOVE_ASSIGNMENT, stringRef)).thenReturn(true);
        try {
            assignmentService.deleteAssignment(assignment);
            deleted = assignmentService.getAssignment(assignment.getId());
        } catch (PermissionException e) {
            Assert.fail("Assignment not deleted\n" + e.toString());
        } catch (IdUnusedException e) {
            // tests pass if assignment doesn't exist
            Assert.assertNull(deleted);
            return;
        }
        Assert.fail("Should never reach this line");
    }

    @Test
    public void deleteAssignmentPermissionDenied() {
        String context = UUID.randomUUID().toString();
        Assignment assignment = createNewAssignment(context);
        String stringRef = AssignmentReferenceReckoner.reckoner().context(assignment.getContext()).subtype("a").id(assignment.getId()).reckon().getReference();
        when(securityService.unlock(AssignmentServiceConstants.SECURE_REMOVE_ASSIGNMENT, stringRef)).thenReturn(false);
        try {
            assignmentService.deleteAssignment(assignment);
        } catch (PermissionException e) {
            Assignment notDeleted = null;
            try {
                notDeleted = assignmentService.getAssignment(assignment.getId());
            } catch (Exception e1) {
                Assert.fail("Cannot verify if assignment exists\n" + e1.toString());
            }
            Assert.assertNotNull(notDeleted);
            Assert.assertEquals(assignment.getId(), notDeleted.getId());
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
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference())).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference())).thenReturn(true);
        try {
            assignmentService.updateAssignment(assignment);
            updatedAssignment = assignmentService.getAssignment(assignment.getId());
        } catch (Exception e) {
            Assert.fail("Could not update assignment\n" + e.toString());
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
            AssignmentSubmission savedSubmission = createNewSubmission(context, submitterId, null);
            Assert.assertNotNull(savedSubmission);
            Assert.assertNotNull(savedSubmission.getId());

            when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT_SUBMISSION,
                                        AssignmentReferenceReckoner.reckoner().submission(savedSubmission).reckon().getReference())).thenReturn(true);
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
            Assert.assertEquals(submitterId, submitter.getSubmitter());
        } catch (Exception e) {
            Assert.fail("Could not create submission\n" + e.toString());
        }
    }

    @Test
    public void addAndGetGroupSubmission() {
        String context = UUID.randomUUID().toString();
        String groupSubmitter = UUID.randomUUID().toString();
        String submitter1 = UUID.randomUUID().toString();
        String submitter2 = UUID.randomUUID().toString();
        Set<String> submitters = new HashSet<>();
        submitters.add(submitter1);
        submitters.add(submitter2);

        try {
            AssignmentSubmission savedSubmission = createNewGroupSubmission(context, groupSubmitter, submitters);
            Assert.assertNotNull(savedSubmission);
            Assert.assertNotNull(savedSubmission.getId());

            when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT_SUBMISSION,
                                        AssignmentReferenceReckoner.reckoner().submission(savedSubmission).reckon().getReference())).thenReturn(true);
            AssignmentSubmission getSubmission = assignmentService.getSubmission(savedSubmission.getId());
            Assert.assertNotNull(getSubmission);
            Assert.assertNotNull(getSubmission.getId());

            Assignment assignment = getSubmission.getAssignment();
            Assert.assertNotNull(assignment.getId());
            Assert.assertEquals(context, assignment.getContext());

            Set<AssignmentSubmissionSubmitter> submissionSubmitters = getSubmission.getSubmitters();
            Assert.assertEquals(2, submissionSubmitters.size());
            submissionSubmitters.forEach(s -> Assert.assertTrue(submitters.contains(s.getSubmitter())));
            Assert.assertEquals(1, submissionSubmitters.stream().filter(AssignmentSubmissionSubmitter::getSubmittee).collect(Collectors.toList()).size());
            Assert.assertEquals(groupSubmitter, getSubmission.getGroupId());

            getSubmission.setGrade("44");
            Optional<AssignmentSubmissionSubmitter> optAss = submissionSubmitters.stream().filter(ass -> ass.getSubmitter().equals(submitter1)).findAny();
            Assert.assertTrue(optAss.isPresent());
            optAss.get().setGrade("22");
            assignmentService.updateSubmission(getSubmission);
            Assert.assertTrue(assignmentService.isGradeOverridden(getSubmission, submitter1));
        } catch (Exception e) {
            Assert.fail("Could not create submission\n" + e.toString());
        }
    }

    @Test
    public void removeSubmission() {
        String context = UUID.randomUUID().toString();
        String submitterId = UUID.randomUUID().toString();

        AssignmentSubmission submission = null;
        try {
            submission = createNewSubmission(context, submitterId, null);
        } catch (Exception e) {
            Assert.fail("Could not create submission\n" + e.toString());
        }

        String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
        when(securityService.unlock(AssignmentServiceConstants.SECURE_REMOVE_ASSIGNMENT_SUBMISSION, reference)).thenReturn(true);
        String submissionId = submission.getId();

        try {
            assignmentService.removeSubmission(submission);
        } catch (Exception e) {
            Assert.fail("Could not remove submission\n" + e.toString());
        }

        try {
            when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT_SUBMISSION, reference)).thenReturn(true);
            AssignmentSubmission removedSubmmision = assignmentService.getSubmission(submissionId);
            Assert.assertNull(removedSubmmision);
        } catch (Exception e) {
            Assert.fail("Could not get removed submission\n" + e.toString());
        }
    }

    @Test
    public void findSubmissionForUser() {
        String context = UUID.randomUUID().toString();
        String submitterId = UUID.randomUUID().toString();
        AssignmentSubmission submission = null;

        try {
            submission = createNewSubmission(context, submitterId, null);
        } catch (Exception e) {
            Assert.fail("Could not create submission\n" + e.toString());
        }

        Assignment assignment = submission.getAssignment();
        String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT_SUBMISSION, reference)).thenReturn(true);
        try {
            AssignmentSubmission submission1 = assignmentService.getSubmission(assignment.getId(), submitterId);
            Assert.assertEquals(submission.getId(), submission1.getId());
        } catch (Exception e) {
            Assert.fail("Could not fetch submission\n" + e.toString());
        }
    }

    @Test
    public void duplicateSubmissionsViaService() {
        String context = UUID.randomUUID().toString();
        String submitterId = UUID.randomUUID().toString();
        AssignmentSubmission submission = null;

        try {
            submission = createNewSubmission(context, submitterId, null);
        } catch (Exception e) {
            Assert.fail("Could not create submission\n" + e.toString());
        }

        Assignment assignment = submission.getAssignment();
        String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT_SUBMISSION, reference)).thenReturn(true);
        try {
            AssignmentSubmission submission1 = assignmentService.getSubmission(assignment.getId(), submitterId);
            Assert.assertEquals(submission.getId(), submission1.getId());
        } catch (Exception e) {
            Assert.fail("Could not fetch submission\n" + e.toString());
        }

        // Lets test TRY to create a duplicate submission
        AssignmentSubmission dupSubmission = null;
        try {
            // returns the original vs creating duplicate
            dupSubmission = assignmentService.addSubmission(assignment.getId(), submitterId);
            // submission is the same
            Assert.assertEquals(dupSubmission.getId(), submission.getId());
        } catch (Exception e) {
            Assert.fail("Could not create duplicate submission\n" + e.toString());
        }
    }

    @Test
    public void duplicateSubmissionRemoval() {
        String context = UUID.randomUUID().toString();
        String submitterId = UUID.randomUUID().toString();
        AssignmentSubmission submission = null;

        try {
            submission = createNewSubmission(context, submitterId, null);
        } catch (Exception e) {
            Assert.fail("Could not create submission\n" + e.toString());
        }

        Assignment assignment = submission.getAssignment();
        AssignmentSubmission duplicateSubmission = duplicateSubmission(submission);
        assignment.getSubmissions().add(duplicateSubmission);

        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference())).thenReturn(true);
        try {
            assignmentService.updateAssignment(assignment);
        } catch (Exception e) {
            Assert.fail("Could not update assignment with duplicate submission\n" + e.toString());
        }

        when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT_SUBMISSION, AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference())).thenReturn(true);
        try {
            AssignmentSubmission fetchSubmission = assignmentService.getSubmission(assignment.getId(), submitterId);
            Assert.assertEquals(submission.getId(), fetchSubmission.getId());
        } catch (Exception e) {
            Assert.fail("Could not fetch submission\n" + e.toString());
        }

        AssignmentSubmission duplicateSubmission1 = duplicateSubmission(submission);
        submission.setUserSubmission(true);
        assignment.getSubmissions().add(duplicateSubmission1);
        try {
            assignmentService.updateAssignment(assignment);
        } catch (Exception e) {
            Assert.fail("Could not update assignment with duplicate submission\n" + e.toString());
        }

        try {
            AssignmentSubmission fetchSubmission = assignmentService.getSubmission(assignment.getId(), submitterId);
            Assert.assertNotNull(fetchSubmission);
            Assert.assertEquals(submission.getId(), fetchSubmission.getId());
            Assert.assertTrue(fetchSubmission.getUserSubmission());
        } catch (Exception e) {
            Assert.fail("Could not fetch submission\n" + e.toString());
        }

        // create multiple user submissions which can not be deleted
        AssignmentSubmission duplicateSubmission2 = duplicateSubmission(submission);
        assignment.getSubmissions().add(duplicateSubmission2);
        try {
            assignmentService.updateAssignment(assignment);
        } catch (Exception e) {
            Assert.fail("Could not update assignment with duplicate submission\n" + e.toString());
        }

        try {
            AssignmentSubmission finalSubmission = assignmentService.getSubmission(assignment.getId(), submitterId);
            Assert.assertNotNull(finalSubmission);
            Assert.assertEquals(submission.getId(), finalSubmission.getId());
        } catch (Exception e) {
            Assert.fail("No exception should be thrown\n" + e.toString());
        }
    }

    @Test
    public void getDeletedAssignmentsForContext() {
        String context = UUID.randomUUID().toString();
        Assignment assignment = createNewAssignment(context);
        String stringRef = AssignmentReferenceReckoner.reckoner().context(assignment.getContext()).subtype("a").id(assignment.getId()).reckon().getReference();
        when(securityService.unlock(AssignmentServiceConstants.SECURE_REMOVE_ASSIGNMENT, stringRef)).thenReturn(true);
        //The assignment list should contain the newly created element
        Collection<Assignment> assignmentCollection = assignmentService.getAssignmentsForContext(context);
        Assert.assertNotNull(assignmentCollection);
        Assert.assertEquals(1, assignmentCollection.size());
        //Soft delete the assignment
        try{
            assignmentService.softDeleteAssignment(assignment);
        } catch (PermissionException e) {
            Assert.fail("Get Deleted Assignments For context\n" + e.toString());
        }
        //The assignment list should not contain the assignment because it's deleted
        assignmentCollection = assignmentService.getAssignmentsForContext(context);
        Assert.assertNotNull(assignmentCollection);
        Assert.assertEquals(0, assignmentCollection.size());
        //The assignment list should contain the assignment because it's deleted
        Collection<Assignment> deletedAssignmentCollection = assignmentService.getDeletedAssignmentsForContext(context);
        Assert.assertNotNull(deletedAssignmentCollection);
        Assert.assertEquals(1, deletedAssignmentCollection.size());
    }

    @Test
    public void duplicateAssignment() throws IdUnusedException {
        // Setup a new Assignment
        String context = UUID.randomUUID().toString();
        User mockUser = mock(User.class);
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

        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference())).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference())).thenReturn(true);
        try {
            assignmentService.updateAssignment(assignment);
        } catch (PermissionException e) {
            Assert.fail("Updating assignment\n" + e.toString());
        }

        // Duplicate the Assignment
        Assignment duplicateAssignment = null;
        try {
            when(userDirectoryService.getCurrentUser()).thenReturn(mockUser);
            duplicateAssignment = assignmentService.addDuplicateAssignment(context, assignment.getId());
        } catch (IdInvalidException | PermissionException | IdUsedException e) {
            Assert.fail("Duplicating assignment\n" + e.toString());
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

        // Check on AssignmentConstants.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK
        Assert.assertEquals(AssignmentConstants.GRADEBOOK_INTEGRATION_ADD, duplicateAssignment.getProperties().get(AssignmentConstants.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK));
        duplicateAssignment.getProperties().remove(AssignmentConstants.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);

        // Check that the properties are the same
        Assert.assertEquals(
                assignment.getProperties().entrySet().stream()
                        .filter(e -> !AssignmentServiceConstants.PROPERTIES_EXCLUDED_FROM_DUPLICATE_ASSIGNMENTS.contains(e.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                duplicateAssignment.getProperties());
    }

    @Test
    public void submissionCanonicalStatus() {
        String context = UUID.randomUUID().toString();
        String assignmentId = UUID.randomUUID().toString();
        String submissionId = UUID.randomUUID().toString();
        String submitterId = UUID.randomUUID().toString();

        Instant now = Instant.now();
        Instant oneDayAgo = now.minus(1, ChronoUnit.DAYS);
        Instant tenDaysAgo = now.minus(10, ChronoUnit.DAYS);

        // submission not exists = NO_SUBMISSION
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.NO_SUBMISSION, assignmentService.getSubmissionCanonicalStatus(null, true));
        // submission not exists = NOT_STARTED
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.NOT_STARTED, assignmentService.getSubmissionCanonicalStatus(null, false));

        Assignment assignment = new Assignment();
        assignment.setId(assignmentId);
        assignment.setContext(context);
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setId(submissionId);
        submission.setUserSubmission(true);
        submission.setSubmittedText("Some Text");
        AssignmentSubmissionSubmitter submitter = new AssignmentSubmissionSubmitter();
        submitter.setId(1L);
        submitter.setSubmitter(submitterId);
        submitter.setSubmittee(true);

        submitter.setSubmission(submission);
        submission.setAssignment(assignment);
        submission.getSubmitters().add(submitter);
        assignment.getSubmissions().add(submission);

        Map<String, String> submissionProperties = submission.getProperties();

        String submissionReference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT_SUBMISSION, submissionReference)).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT_SUBMISSION, submissionReference)).thenReturn(true);

        // submission is Submitted | DateSubmitted exists | submission is Returned | DateReturned exists | DateReturned before DateSubmitted | submission not Graded | DateSubmitted after DueDate = LATE
        submission.setSubmitted(true);
        submission.setDateSubmitted(now);
        submission.setReturned(true);
        submission.setDateReturned(submission.getDateSubmitted().minus(1, ChronoUnit.DAYS));
        submission.setGraded(false);
        assignment.setDueDate(submission.getDateSubmitted().minus(6, ChronoUnit.HOURS));
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.LATE, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission is Submitted | DateSubmitted exists | submission is Returned | DateReturned exists | DateReturned before DateSubmitted | submission not Graded | DateSubmitted before DueDate = RESUBMITTED
        submission.setSubmitted(true);
        submission.setDateSubmitted(now);
        submission.setReturned(true);
        submission.setDateReturned(submission.getDateSubmitted().minus(1, ChronoUnit.DAYS));
        submission.setGraded(false);
        assignment.setDueDate(submission.getDateSubmitted().plus(3, ChronoUnit.DAYS));
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.RESUBMITTED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission is Submitted | DateSubmitted exists | submission is Returned | DateReturned exists | DateReturned before DateSubmitted | submission is Graded | resubmission is not allowed = RETURNED
        submission.setSubmitted(true);
        submission.setDateSubmitted(now);
        submission.setReturned(true);
        submission.setDateReturned(submission.getDateSubmitted().minus(1, ChronoUnit.DAYS));
        submission.setGraded(true);
        submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, "0");
        assignment.setDueDate(null);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.RETURNED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission is Submitted | DateSubmitted exists | submission is Returned | DateReturned not exists | resubmission is not allowed = RETURNED
        submission.setSubmitted(true);
        submission.setDateSubmitted(now);
        submission.setReturned(true);
        submission.setDateReturned(null);
        submission.setGraded(false);
        submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, "0");
        assignment.setDueDate(null);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.RETURNED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission is Submitted | DateSubmitted exists | submission is Returned | DateReturned after DateSubmitted | resubmission is not allowed = RETURNED
        submission.setSubmitted(true);
        submission.setDateSubmitted(now);
        submission.setReturned(true);
        submission.setDateReturned(submission.getDateSubmitted().plus(1, ChronoUnit.DAYS));
        submission.setGraded(false);
        submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, "0");
        assignment.setDueDate(null);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.RETURNED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission is Submitted | DateSubmitted exists | submission not Returned | submission is Graded | User can Grade | Grade exists = GRADED
        submission.setSubmitted(true);
        submission.setDateSubmitted(now);
        submission.setReturned(false);
        submission.setDateReturned(null);
        submission.setGraded(true);
        submission.setGrade("1000");
        assignment.setDueDate(null);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.GRADED, assignmentService.getSubmissionCanonicalStatus(submission, true));

        // submission is Submitted | DateSubmitted exists | submission not Returned | submission is Graded | User can Grade | Comment exists | Grade not exists = COMMENTED
        submission.setSubmitted(true);
        submission.setDateSubmitted(now);
        submission.setReturned(false);
        submission.setDateReturned(null);
        submission.setGraded(true);
        submission.setGrade(null);
        submission.setFeedbackComment("Graders comment");
        assignment.setDueDate(null);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.COMMENTED, assignmentService.getSubmissionCanonicalStatus(submission, true));
        submission.setFeedbackComment(null);

        // submission is Submitted | DateSubmitted exists | submission not Returned | User can't Grade = SUBMITTED
        submission.setSubmitted(true);
        submission.setDateSubmitted(now);
        submission.setReturned(false);
        submission.setDateReturned(null);
        submission.setGraded(false);
        submission.setGrade(null);
        assignment.setDueDate(null);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.SUBMITTED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission is Submitted | DateSubmitted exists | submission not Returned | submission not Graded | User can Grade = UNGRADED
        submission.setSubmitted(true);
        submission.setDateSubmitted(now);
        submission.setReturned(false);
        submission.setDateReturned(null);
        submission.setGraded(false);
        submission.setGrade(null);
        assignment.setDueDate(null);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.UNGRADED, assignmentService.getSubmissionCanonicalStatus(submission, true));

        // submission is Submitted | DateSubmitted not exists | submission is Returned | resubmission is not allowed = RETURNED
        submission.setSubmitted(true);
        submission.setDateSubmitted(null);
        submission.setReturned(true);
        submission.setDateReturned(null);
        submission.setGraded(false);
        submission.setGrade(null);
        submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, "0");
        assignment.setDueDate(null);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.RETURNED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission is Submitted | DateSubmitted not exists | submission not Returned | submission is Graded | User can Grade | Grade exists = GRADED
        submission.setSubmitted(true);
        submission.setDateSubmitted(null);
        submission.setReturned(false);
        submission.setDateReturned(null);
        submission.setGraded(true);
        submission.setGrade("1000");
        assignment.setDueDate(null);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.GRADED, assignmentService.getSubmissionCanonicalStatus(submission, true));

        // submission is Submitted | DateSubmitted not exists | submission not Returned | submission is Graded | User can Grade | Comment exists | Grade not exists = COMMENTED
        submission.setSubmitted(true);
        submission.setDateSubmitted(null);
        submission.setReturned(false);
        submission.setDateReturned(null);
        submission.setGraded(true);
        submission.setGrade(null);
        submission.setFeedbackComment("Graders comment");
        assignment.setDueDate(null);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.COMMENTED, assignmentService.getSubmissionCanonicalStatus(submission, true));
        submission.setFeedbackComment(null);

        // submission is Submitted | DateSubmitted not exists | submission not Returned | submission not Graded | User can't Grade | Assignment is HonorPledge | Submission is HonorPledge = HONOR_ACCEPTED
        submission.setSubmitted(true);
        submission.setDateSubmitted(null);
        submission.setReturned(false);
        submission.setDateReturned(null);
        submission.setGraded(false);
        submission.setGrade(null);
        submission.setHonorPledge(true);
        assignment.setDueDate(null);
        assignment.setHonorPledge(true);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.HONOR_ACCEPTED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission is Submitted | DateSubmitted not exists | submission not Returned | submission Graded | User can't Grade | Assignment is HonorPledge | Submission is HonorPledge = HONOR_ACCEPTED
        submission.setSubmitted(true);
        submission.setDateSubmitted(null);
        submission.setReturned(false);
        submission.setDateReturned(null);
        submission.setGraded(true);
        submission.setGrade(null);
        submission.setHonorPledge(true);
        assignment.setDueDate(null);
        assignment.setHonorPledge(true);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.HONOR_ACCEPTED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission is Submitted | DateSubmitted not exists | submission not Returned | submission not Graded | User can Grade = NO_SUBMISSION
        submission.setSubmitted(true);
        submission.setDateSubmitted(null);
        submission.setReturned(false);
        submission.setDateReturned(null);
        submission.setGraded(false);
        submission.setGrade(null);
        submission.setHonorPledge(false);
        assignment.setDueDate(null);
        assignment.setHonorPledge(false);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.NO_SUBMISSION, assignmentService.getSubmissionCanonicalStatus(submission, true));

        // submission is Submitted | DateSubmitted not exists | submission not Returned | submission Graded | User can't Grade | Assignment is HonorPledge | Submission not HonorPledge = NOT_STARTED
        submission.setSubmitted(true);
        submission.setDateSubmitted(null);
        submission.setReturned(false);
        submission.setDateReturned(null);
        submission.setGraded(true);
        submission.setGrade(null);
        submission.setHonorPledge(false);
        assignment.setDueDate(null);
        assignment.setHonorPledge(true);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.NOT_STARTED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission not Submitted | submission Graded | submission Returned | DateModified exists | DateReturned exists | DateModified after DateReturned plus 10 seconds | User can't Grade = IN_PROGRESS
        submission.setSubmitted(false);
        submission.setReturned(true);
        submission.setDateReturned(oneDayAgo);
        submission.setGraded(true);
        submission.setGrade(null);
        submission.setDateModified(now.minus(12, ChronoUnit.HOURS));
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.IN_PROGRESS, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission not Submitted | submission Graded | submission Returned | DateModified not exists | resubmission is not allowed = RETURNED
        submission.setSubmitted(false);
        submission.setReturned(true);
        submission.setDateReturned(null);
        submission.setGraded(true);
        submission.setDateModified(null);
        submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, "0");
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.RETURNED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission not Submitted | submission Graded | submission Returned | DateReturned not exists | resubmission is not allowed = RETURNED
        submission.setSubmitted(false);
        submission.setReturned(true);
        submission.setDateReturned(null);
        submission.setGraded(true);
        submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, "0");
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.RETURNED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission not Submitted | submission Graded | submission Returned | DateModified before DateReturned plus 10 seconds | resubmission is not allowed = RETURNED
        submission.setSubmitted(false);
        submission.setReturned(true);
        submission.setDateReturned(now.minus(1, ChronoUnit.DAYS));
        submission.setGraded(true);
        submission.setDateModified(now.minus(2, ChronoUnit.DAYS));
        submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, "0");
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.RETURNED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission not Submitted | submission Graded | submission Returned | User can Grade | resubmission is not allowed = RETURNED
        submission.setSubmitted(false);
        submission.setReturned(true);
        submission.setGraded(true);
        submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, "0");
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.RETURNED, assignmentService.getSubmissionCanonicalStatus(submission, true));

        // submission not Submitted | submission Graded | submission not Returned | User can Grade | Grade exists = GRADED
        submission.setSubmitted(false);
        submission.setReturned(false);
        submission.setGraded(true);
        submission.setGrade("1000");
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.GRADED, assignmentService.getSubmissionCanonicalStatus(submission, true));

        // submission not Submitted | submission Graded | submission not Returned | User can Grade | Comment exists | Grade not exists = COMMENTED
        submission.setSubmitted(false);
        submission.setReturned(false);
        submission.setGraded(true);
        submission.setFeedbackComment("Graders comment");
        submission.setGrade(null);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.COMMENTED, assignmentService.getSubmissionCanonicalStatus(submission, true));
        submission.setFeedbackComment(null);

        // submission not Submitted | submission Graded | submission not Returned | User can't Grade = IN_PROGRESS
        submission.setSubmitted(false);
        submission.setReturned(false);
        submission.setGraded(true);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.IN_PROGRESS, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission not Submitted | submission not Graded | User can Grade = UNGRADED
        submission.setSubmitted(false);
        submission.setReturned(false);
        submission.setGraded(false);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.UNGRADED, assignmentService.getSubmissionCanonicalStatus(submission, true));

        // submission not Submitted | submission not Graded | DateCreated equals DateModified | Assignment is HonorPledge | Submission is HonorPledge = HONOR_ACCEPTED
        submission.setSubmitted(false);
        submission.setGraded(false);
        submission.setHonorPledge(true);
        submission.setDateCreated(tenDaysAgo);
        submission.setDateModified(tenDaysAgo);
        assignment.setHonorPledge(true);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.HONOR_ACCEPTED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission not Submitted | submission not Graded | DateCreated not equals DateModified = IN_PROGRESS
        submission.setSubmitted(false);
        submission.setGraded(false);
        submission.setHonorPledge(true);
        submission.setDateCreated(tenDaysAgo);
        submission.setDateModified(oneDayAgo);
        assignment.setHonorPledge(true);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.IN_PROGRESS, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission not Submitted | submission not Graded | Assignment not HonorPledge = IN_PROGRESS
        submission.setSubmitted(false);
        submission.setGraded(false);
        assignment.setHonorPledge(false);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.IN_PROGRESS, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission not Submitted | submission not Graded | Submission not HonorPledge = IN_PROGRESS
        submission.setSubmitted(false);
        submission.setGraded(false);
        submission.setHonorPledge(false);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.IN_PROGRESS, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission is Submitted | DateSubmitted exists | submission is Returned | DateReturned after DateSubmitted | resubmission is allowed | NO resubmission close time = RESUBMIT_ALLOWED
        submission.setSubmitted(true);
        submission.setDateSubmitted(now);
        submission.setReturned(true);
        submission.setDateReturned(submission.getDateSubmitted().plus(1, ChronoUnit.DAYS));
        submission.setGraded(false);
        submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, "1");
        assignment.setDueDate(null);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.RESUBMIT_ALLOWED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission is Submitted | DateSubmitted exists | submission is Returned | DateReturned exists | DateReturned before DateSubmitted | submission is Graded | resubmission is allowed | NO resubmission close time = RESUBMIT_ALLOWED
        submission.setSubmitted(true);
        submission.setDateSubmitted(now);
        submission.setReturned(true);
        submission.setDateReturned(submission.getDateSubmitted().minus(1, ChronoUnit.DAYS));
        submission.setGraded(true);
        submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, "1");
        assignment.setDueDate(null);
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.RESUBMIT_ALLOWED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission is Submitted | DateSubmitted exists | submission is Returned | DateReturned after DateSubmitted | resubmission are allowed | resubmission close time has past = RETURNED
        submission.setSubmitted(true);
        submission.setDateSubmitted(now.minus(2, ChronoUnit.DAYS));
        submission.setReturned(true);
        submission.setDateReturned(submission.getDateSubmitted().plus(1, ChronoUnit.DAYS));
        submission.setGraded(false);
        assignment.setDueDate(now.minus(1, ChronoUnit.DAYS));
        assignment.setCloseDate(now.minus(12, ChronoUnit.HOURS));
        submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, "1");
        submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, String.valueOf(now.minus(1, ChronoUnit.HOURS).toEpochMilli()));
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.RETURNED, assignmentService.getSubmissionCanonicalStatus(submission, false));

        // submission is Submitted | DateSubmitted exists | submission is Returned | DateReturned after DateSubmitted | resubmission are allowed | resubmission close time has past | assignment close date is after resubmission accept until date = RETURNED
        submission.setSubmitted(true);
        submission.setDateSubmitted(now.minus(2, ChronoUnit.DAYS));
        submission.setReturned(true);
        submission.setDateReturned(submission.getDateSubmitted().plus(1, ChronoUnit.DAYS));
        submission.setGraded(false);
        assignment.setDueDate(now.minus(1, ChronoUnit.DAYS));
        assignment.setCloseDate(now.plus(12, ChronoUnit.HOURS));
        submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, "1");
        submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, String.valueOf(now.minus(1, ChronoUnit.HOURS).toEpochMilli()));
        Assert.assertEquals(AssignmentConstants.SubmissionStatus.RESUBMIT_ALLOWED, assignmentService.getSubmissionCanonicalStatus(submission, false));

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
        // gen.hpsta         = Honor Accepted

        String context = UUID.randomUUID().toString();
        String submitterId = UUID.randomUUID().toString();

        // test for non existent submission
        String status = assignmentService.getSubmissionStatus("SUBMISSION_DOESNT_EXIST", true);
        Assert.assertEquals("Not Started", status);

        try {
            AssignmentSubmission submission = createNewSubmission(context, submitterId, null);
            when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT_SUBMISSION,
                                        AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference())).thenReturn(true);
            status = assignmentService.getSubmissionStatus(submission.getId(), true);
            Assert.assertEquals("In progress", status);
            AssignmentConstants.SubmissionStatus subStatus = assignmentService.getSubmissionCanonicalStatus(submission, false);
            Assert.assertEquals(AssignmentConstants.SubmissionStatus.IN_PROGRESS, subStatus);
            Assert.assertFalse(submission.getSubmitted());

            // A grader should see additional info before about the status
            String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(submission.getAssignment()).reckon().getReference();
            when(securityService.unlock(AssignmentServiceConstants.SECURE_GRADE_ASSIGNMENT_SUBMISSION, assignmentReference)).thenReturn(true);
            status = assignmentService.getSubmissionStatus(submission.getId(), true);
            Assert.assertEquals("Ungraded - In progress", status);

            String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
            when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT_SUBMISSION, reference)).thenReturn(true);
            when(securityService.unlock(AssignmentServiceConstants.SECURE_GRADE_ASSIGNMENT_SUBMISSION, assignmentReference)).thenReturn(false);
            submission.setSubmitted(true);
            submission.setUserSubmission(true);
            submission.setDateSubmitted(Instant.now());
            submission.setSubmittedText("submittedText");
            assignmentService.updateSubmission(submission);
            status = assignmentService.getSubmissionStatus(submission.getId(), true);
            Assert.assertEquals("Submitted " + assignmentService.getUsersLocalDateTimeString(submission.getDateSubmitted()), status);
            String statusWithDate = assignmentService.getSubmissionStatus(submission.getId(), false);
            Assert.assertEquals("Submitted " + submission.getDateSubmitted(), statusWithDate);
            subStatus = assignmentService.getSubmissionCanonicalStatus(submission, false);
            Assert.assertEquals(AssignmentConstants.SubmissionStatus.SUBMITTED, subStatus);

        } catch (Exception e) {
            Assert.fail("Could not create/update submission\n" + e.toString());
        }
    }

    @Test
    public void gradeDisplay() {
        for(Locale locale : Arrays.asList(Locale.ENGLISH, Locale.FRANCE)) {
            Character ds = DecimalFormatSymbols.getInstance(locale).getDecimalSeparator();
            when(formattedText.getDecimalSeparator()).thenReturn(ds.toString());

            Assert.assertEquals("0", assignmentService.getGradeDisplay("0", Assignment.GradeType.SCORE_GRADE_TYPE, null));

            configureScale(10, locale);
            Assert.assertEquals(/*"0.5 or 0,5"*/"0"+ds+"5", assignmentService.getGradeDisplay("5", Assignment.GradeType.SCORE_GRADE_TYPE, 10));
            Assert.assertEquals(/*"10.0 or 10,0"*/"10"+ds+"0", assignmentService.getGradeDisplay("100", Assignment.GradeType.SCORE_GRADE_TYPE, 10));

            configureScale(100, locale);
            Assert.assertEquals(/*"0.05 or 0,05"*/"0"+ds+"05", assignmentService.getGradeDisplay("5", Assignment.GradeType.SCORE_GRADE_TYPE, 100));
            Assert.assertEquals(/*"5.00 or 5,00"*/"5"+ds+"00", assignmentService.getGradeDisplay("500", Assignment.GradeType.SCORE_GRADE_TYPE, 100));
            Assert.assertEquals(/*"100.00 or 100,0"*/"100"+ds+"00", assignmentService.getGradeDisplay("10000", Assignment.GradeType.SCORE_GRADE_TYPE, 100));

            configureScale(1000, locale);
            Assert.assertEquals(/*"0.005 or 0,005"*/"0"+ds+"005", assignmentService.getGradeDisplay("5", Assignment.GradeType.SCORE_GRADE_TYPE, 1000));
            Assert.assertEquals(/*"50.000 or 50,000"*/"50"+ds+"000", assignmentService.getGradeDisplay("50000", Assignment.GradeType.SCORE_GRADE_TYPE, 1000));

            Assert.assertEquals("0" + ds + "00", assignmentService.getGradeDisplay("Pass", Assignment.GradeType.SCORE_GRADE_TYPE, 100));
        }

        Assert.assertEquals("", assignmentService.getGradeDisplay("", Assignment.GradeType.UNGRADED_GRADE_TYPE, null));
        Assert.assertEquals("No Grade", assignmentService.getGradeDisplay("gen.nograd", Assignment.GradeType.UNGRADED_GRADE_TYPE, null));

        Assert.assertEquals("Pass", assignmentService.getGradeDisplay("pass", Assignment.GradeType.PASS_FAIL_GRADE_TYPE, null));
        Assert.assertEquals("Fail", assignmentService.getGradeDisplay("fail", Assignment.GradeType.PASS_FAIL_GRADE_TYPE, null));
        Assert.assertEquals("", assignmentService.getGradeDisplay("any", Assignment.GradeType.PASS_FAIL_GRADE_TYPE, null));

        Assert.assertEquals("", assignmentService.getGradeDisplay("any", Assignment.GradeType.CHECK_GRADE_TYPE, null));
        Assert.assertEquals("Checked", assignmentService.getGradeDisplay("checked", Assignment.GradeType.CHECK_GRADE_TYPE, null));

        Assert.assertEquals("", assignmentService.getGradeDisplay("", Assignment.GradeType.GRADE_TYPE_NONE, null));
        Assert.assertEquals("self", assignmentService.getGradeDisplay("self", Assignment.GradeType.GRADE_TYPE_NONE, null));
    }

    @Test
    public void peerAssignmentDateTests() {

        // Setup a new Assignment
        String context = UUID.randomUUID().toString();
        Assignment assignment = createNewAssignment(context);
        Assert.assertNotNull(assignment);

        // assignment doesnt allow peer assessment
        Assert.assertFalse(assignmentService.isPeerAssessmentOpen(assignment));
        Assert.assertFalse(assignmentService.isPeerAssessmentPending(assignment));
        Assert.assertFalse(assignmentService.isPeerAssessmentClosed(assignment));

        assignment.setAllowPeerAssessment(true);
        Instant now = Instant.now();
        assignment.setCloseDate(now);
        assignment.setPeerAssessmentPeriodDate(now.minus(Duration.ofDays(1)));

        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference())).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference())).thenReturn(true);
        try {
            assignmentService.updateAssignment(assignment);
        } catch (PermissionException e) {
            Assert.fail("Updating assignment\n" + e.toString());
        }
        // assignment allows peer assessment, close date and peer period past
        Assert.assertFalse(assignmentService.isPeerAssessmentOpen(assignment));
        Assert.assertFalse(assignmentService.isPeerAssessmentPending(assignment));
        Assert.assertTrue(assignmentService.isPeerAssessmentClosed(assignment));

        assignment.setCloseDate(now.plus(Duration.ofDays(3)));
        assignment.setPeerAssessmentPeriodDate(now.plus(Duration.ofDays(1)));
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference())).thenReturn(true);
        try {
            assignmentService.updateAssignment(assignment);
        } catch (PermissionException e) {
            Assert.fail("Updating assignment\n" + e.toString());
        }
        // close date and peer period in the future
        Assert.assertFalse(assignmentService.isPeerAssessmentOpen(assignment));
        Assert.assertTrue(assignmentService.isPeerAssessmentPending(assignment));
        Assert.assertFalse(assignmentService.isPeerAssessmentClosed(assignment));

        assignment.setCloseDate(now.minus(Duration.ofDays(2)));
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference())).thenReturn(true);
        try {
            assignmentService.updateAssignment(assignment);
        } catch (PermissionException e) {
            Assert.fail("Updating assignment" + e.toString());
        }
        // close date past and peer period in the future
        Assert.assertTrue(assignmentService.isPeerAssessmentOpen(assignment));
        Assert.assertFalse(assignmentService.isPeerAssessmentPending(assignment));
        Assert.assertFalse(assignmentService.isPeerAssessmentClosed(assignment));

    }

    @Test
    public void allowAddSubmissionCheckGroups() {
        String context = UUID.randomUUID().toString();
        String contextReference = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        String siteReference = "/site/" + context;
        Assignment assignment = createNewAssignment(context);
        // permissions
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT, contextReference)).thenReturn(false);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION, contextReference)).thenReturn(false);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION, "/site/" + context)).thenReturn(true);
        when(siteService.siteReference(context)).thenReturn(siteReference);

        // test with no groups
        Assert.assertTrue(assignmentService.allowAddSubmissionCheckGroups(assignment));

        // test with a groups
        assignment.setTypeOfAccess(Assignment.Access.GROUP);
        String groupA = UUID.randomUUID().toString();
        String groupB = UUID.randomUUID().toString();
        String groupRefA = "/site/" + context + "/group/" + groupA;
        String groupRefB = "/site/" + context + "/group/" + groupB;

        // group A is allowed
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION, groupRefA)).thenReturn(true);
        assignment.getGroups().add(groupRefA);
        Assert.assertTrue(assignmentService.allowAddSubmissionCheckGroups(assignment));

        // group B is not allowed
        assignment.getGroups().clear();
        assignment.getGroups().add(groupRefB);
        Assert.assertFalse(assignmentService.allowAddSubmissionCheckGroups(assignment));

        // give group B asn.all.groups and should be allowed now
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ALL_GROUPS, siteReference)).thenReturn(true);
        Assert.assertTrue(assignmentService.allowAddSubmissionCheckGroups(assignment));
    }

    @Test
    public void countSubmissions() {
        String context = UUID.randomUUID().toString();
        List<String> submitterIds = Collections.nCopies(10,1).stream().map(i -> UUID.randomUUID().toString()).collect(Collectors.toList());
        List<User> submitterUsers = submitterIds.stream().map(id -> {
            User user = mock(User.class);
            when(user.getId()).thenReturn(id);
            return user;
        }).collect(Collectors.toList());
        Assignment assignment = createNewAssignment(context);
        String addSubmissionReference = AssignmentReferenceReckoner.reckoner().context(context).subtype("s").reckon().getReference();
        String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        when(securityService.unlockUsers(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION, assignmentReference)).thenReturn(submitterUsers);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION, addSubmissionReference)).thenReturn(true);

        final Site site = mock(Site.class);
        try {
            when(siteService.getSite(context)).thenReturn(site);
        } catch (Exception e) {
            Assert.fail("Could not get site\n" + e.toString());
        }

        submitterIds.forEach(id -> {
            try {
                when(site.getGroup(id)).thenReturn(mock(Group.class));
                when(site.getMember(id)).thenReturn(mock(Member.class));
                AssignmentSubmission submission = assignmentService.addSubmission(assignment.getId(), id);
                String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
                when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT_SUBMISSION, reference)).thenReturn(true);
                when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT_SUBMISSION, reference)).thenReturn(true);
            } catch (Exception e) {
                Assert.fail("Could not create submission\n" + e.toString());
            }
        });

        Assert.assertThat(assignment.getSubmissions().size(), is(10)); // ensure we have 10 submissions created

        int count = assignmentService.countSubmissions(assignmentReference, false);
        Assert.assertThat(count, is(0)); // currently none of the submissions are submitted should be 0

        // submit 5 submissions
        assignment.getSubmissions().stream().limit(5).forEach(s -> {
            s.setSubmitted(true);
            s.setDateSubmitted(Instant.now());
            s.setUserSubmission(true);
            try {
                assignmentService.updateSubmission(s);
            } catch (Exception e) {
                Assert.fail("Could not update submission\n" + e.toString());
            }
        });

        int countSubmitted = assignmentService.countSubmissions(assignmentReference, false);
        Assert.assertThat(countSubmitted, is(5)); // should have 5 submissions submitted

        // grade 2 submitted submissions
        assignment.getSubmissions().stream().filter(AssignmentSubmission::getSubmitted).limit(2).forEach(s -> {
            s.setGrade("1000");
            s.setGraded(true);
            try {
                assignmentService.updateSubmission(s);
            } catch (Exception e) {
                Assert.fail("Could not update submission\n" + e.toString());
            }
        });

        int countGraded = assignmentService.countSubmissions(assignmentReference, true);
        Assert.assertThat(countGraded, is(2)); // should have 2 submissions graded

        when(securityService.unlockUsers(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION, assignmentReference)).thenReturn(new ArrayList<>());
        int countNoUsers = assignmentService.countSubmissions(assignmentReference, false);
        Assert.assertThat(countNoUsers, is(0)); // should have 0 submissions submitted
    }

    @Test
    public void gradeUpdateFromAssignmentEventObserver() {
        char ds = DecimalFormatSymbols.getInstance(Locale.ENGLISH).getDecimalSeparator();
        when(formattedText.getDecimalSeparator()).thenReturn(Character.toString(ds));
        configureScale(100, Locale.ENGLISH);

        String context = UUID.randomUUID().toString();
        String gradebookId = UUID.randomUUID().toString();
        String submitterId = UUID.randomUUID().toString();
        String instructorId = UUID.randomUUID().toString();
        Long itemId = new Random().nextLong();
        try {
            AssignmentSubmission newSubmission = createNewSubmission(context, submitterId, null);
            Assignment assignment = newSubmission.getAssignment();
            assignment.getProperties().put(AssignmentConstants.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT, itemId.toString());
            assignment.setTypeOfGrade(Assignment.GradeType.SCORE_GRADE_TYPE);
            assignment.setScaleFactor(assignmentService.getScaleFactor());

            String assignmentRef = AssignmentReferenceReckoner.reckoner().submission(newSubmission).reckon().getReference();
            when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT_SUBMISSION, assignmentRef)).thenReturn(true);

            Event event = createMockEvent(context, gradebookId, itemId, submitterId, "25", instructorId);

            org.sakaiproject.grading.api.Assignment gradebookAssignment = mock(org.sakaiproject.grading.api.Assignment.class);
            when(gradebookAssignment.getName()).thenReturn(itemId.toString());
            when(gradingService.getAssignmentByNameOrId(gradebookId, context, itemId.toString())).thenReturn(gradebookAssignment);
            User mockUser = mock(User.class);
            when(mockUser.getId()).thenReturn(submitterId);
            when(userDirectoryService.getUser(submitterId)).thenReturn(mockUser);
            assignmentEventObserver.update(null, event);

            AssignmentSubmission updatedSubmission = assignmentService.getSubmission(newSubmission.getId());

            Assert.assertEquals(Boolean.TRUE, updatedSubmission.getGraded());
            Assert.assertEquals("2500", updatedSubmission.getGrade());
            Assert.assertEquals(instructorId, updatedSubmission.getGradedBy());
        } catch (Exception e) {
            Assert.fail("Could not create submission\n" + e.toString());
        }
    }

    @Test
    public void mergeAssignmentFromXML() {
        String context = UUID.randomUUID().toString();
        String xml = readResourceToString("/importAssignment.xml");
        Document doc = Xml.readDocument(xml);

        if (doc != null) {
            // Mock everything needed to have permission
            Site siteMock = mock(Site.class);
            Collection<Group> groupCollection = new ArrayList<>();
            Group groupMock = mock(Group.class);
            when(groupMock.getReference()).thenReturn("reference");
            groupCollection.add(groupMock);
            when(siteMock.getGroups()).thenReturn(groupCollection);
            Set<String> references = new HashSet<>();
            references.add("reference");
            when(authzGroupService.getAuthzGroupsIsAllowed(anyString(), anyString(), anyCollection())).thenReturn(references);
            try {
                when(siteService.getSite(context)).thenReturn(siteMock);
            } catch (IdUnusedException e) {
                Assert.fail("Site mock failed");
            }

            when(securityService.unlock("asn.new", "/assignment/a/SITE_ID")).thenReturn(true);
            when(securityService.unlock("asn.revise", "/assignment/a/SITE_ID")).thenReturn(true);
            when(securityService.unlock("asn.read", "/assignment/a/SITE_ID")).thenReturn(true);

            // verify the root element
            Element root = doc.getDocumentElement();
            // the children
            NodeList children = root.getChildNodes();
            int length = children.getLength();

            for (int i = 0; i < length; i++) {
                Node child = children.item(i);
                if (child.getNodeType() != Node.ELEMENT_NODE) continue;

                Element element = (Element) child;
                if ("org.sakaiproject.assignment.api.AssignmentService".equals(element.getTagName())) {
                    assignmentService.merge(context, element, null, null, null, null, null);
                    Assert.assertEquals(1, assignmentService.getAssignmentsForContext(context).size());
                }
            }
        }
    }

    @Test
    public void canSubmit() {
        String context = UUID.randomUUID().toString();
        Instant now = Instant.now();
        String siteRef = "/site/" + context;
        String user1 = "user1";

        Assignment assignment = createNewAssignment(context);

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);
        when(siteService.siteReference(context)).thenReturn(siteRef);

        // test if user has permission to submit to assignment
        Assert.assertFalse(assignmentService.canSubmit(assignment));

        when(securityService.unlock(user1, AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION, siteRef)).thenReturn(true);

        // test future assignment no submission
        assignment.setOpenDate(now.plus(Period.ofDays(1)));
        assignment.setCloseDate(now.plus(Period.ofDays(3)));
        Assert.assertFalse(assignmentService.canSubmit(assignment));

        // test assignment is open no submission
        assignment.setOpenDate(now.minus(Period.ofDays(1)));
        Assert.assertTrue(assignmentService.canSubmit(assignment));

        // test assignment is closed no submission
        assignment.setOpenDate(now.minus(Period.ofDays(2)));
        assignment.setCloseDate(now.minus(Period.ofDays(1)));
        Assert.assertFalse(assignmentService.canSubmit(assignment));


        try {
            // test open assignment with submission with no submission security
            assignment.setOpenDate(now.minus(Period.ofDays(1)));
            assignment.setCloseDate(now.plus(Period.ofDays(3)));
            AssignmentSubmission submission = createNewSubmission(context, user1, assignment);
            Assert.assertFalse(assignmentService.canSubmit(assignment));

            // test open assignment with submission with allowed submission security
            String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
            when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION, reference)).thenReturn(true);
            Assert.assertTrue(assignmentService.canSubmit(assignment));

            // test assignment is closed with submission
            assignment.setOpenDate(now.minus(Period.ofDays(3)));
            assignment.setCloseDate(now.minus(Period.ofDays(1)));
            Assert.assertFalse(assignmentService.canSubmit(assignment));

            // test assignment closed, submission is never submitted and extension of 5 days in the future
            submission.setDateSubmitted(null);
            submission.setSubmitted(false);
            submission.getProperties().put(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME, Long.toString(now.plus(Period.ofDays(5)).toEpochMilli()));
            assignmentService.updateSubmission(submission);
            Assert.assertTrue(assignmentService.canSubmit(assignment));

            // test assignment closed, personal resub allowed tomorrow, submission is dummy because prof wrote "no submission"
            assignment.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, Integer.toString(1));
            assignment.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, Long.toString(now.minus(Period.ofDays(1)).toEpochMilli()));
            submission.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, Integer.toString(1));
            submission.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, Long.toString(now.plus(Period.ofDays(1)).toEpochMilli()));
            submission.setFeedbackComment("<p>No Submission</p>");
            submission.setFeedbackText("No Submission");
            submission.setSubmitted(true);
            submission.setDateSubmitted(null);
            submission.setUserSubmission(false);
            assignmentService.updateSubmission(submission);
            Assert.assertTrue(assignmentService.canSubmit(assignment));

            // test assignment closed, resubs allowed in the past, submission is dummy because prof wrote "no submission" and an extension
            assignment.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, Integer.toString(1));
            assignment.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, Long.toString(now.minus(Period.ofDays(1)).toEpochMilli()));
            submission.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, Integer.toString(1));
            submission.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, Long.toString(now.minus(Period.ofDays(1)).toEpochMilli()));
            submission.setFeedbackComment("<p>No Submission</p>");
            submission.setFeedbackText("No Submission");
            submission.setSubmitted(true);
            submission.setDateSubmitted(null);
            submission.setUserSubmission(false);
            submission.getProperties().put(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME, Long.toString(now.plus(Period.ofDays(5)).toEpochMilli()));
            assignmentService.updateSubmission(submission);
            Assert.assertTrue(assignmentService.canSubmit(assignment));

            // test assignment closed, submission is already submitted and extension of 5 days in the future
            submission.setDateSubmitted(now.minus(6, ChronoUnit.HOURS));
            submission.setSubmitted(true);
            submission.setUserSubmission(true);
            submission.getProperties().put(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME, Long.toString(now.plus(Period.ofDays(5)).toEpochMilli()));
            assignmentService.updateSubmission(submission);
            Assert.assertFalse(assignmentService.canSubmit(assignment));

            // test assignment closed, both extension and resubmission in future, but extension is further out
            assignment.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, Integer.toString(1));
            assignment.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, Long.toString(now.plus(Period.ofDays(3)).toEpochMilli()));
            submission.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, Integer.toString(1));
            submission.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, Long.toString(now.plus(Period.ofDays(3)).toEpochMilli()));
            submission.getProperties().put(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME, Long.toString(now.plus(Period.ofDays(5)).toEpochMilli()));
            submission.setSubmitted(true);
            submission.setDateSubmitted(now.minus(1, ChronoUnit.DAYS)); // Has previous submission
            submission.setUserSubmission(true);
            assignmentService.updateSubmission(submission);
            Assert.assertTrue(assignmentService.canSubmit(assignment));

            // test assignment closed, extension in past, resubmission in future, no previous submission
            assignment.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, Integer.toString(1));
            assignment.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, Long.toString(now.plus(Period.ofDays(2)).toEpochMilli()));
            submission.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, Integer.toString(1));
            submission.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, Long.toString(now.plus(Period.ofDays(2)).toEpochMilli()));
            submission.getProperties().put(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME, Long.toString(now.minus(Period.ofDays(1)).toEpochMilli()));
            submission.setSubmitted(false);
            submission.setDateSubmitted(null); // No previous submission
            submission.setUserSubmission(false);
            assignmentService.updateSubmission(submission);
            // No submission so RE-submission is irrelevant
            Assert.assertFalse(assignmentService.canSubmit(assignment));

            // This student needs an extension to tomorrow to be able to submit
            submission.getProperties().put(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME, Long.toString(now.plus(Period.ofDays(1)).toEpochMilli()));
            assignmentService.updateSubmission(submission);
            Assert.assertTrue(assignmentService.canSubmit(assignment));

            // test assignment closed, competing extension and resubmission both in past
            assignment.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, Integer.toString(1));
            assignment.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, Long.toString(now.minus(Period.ofDays(1)).toEpochMilli()));
            submission.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, Integer.toString(1));
            submission.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, Long.toString(now.minus(Period.ofDays(1)).toEpochMilli()));
            submission.getProperties().put(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME, Long.toString(now.minus(Period.ofDays(2)).toEpochMilli()));
            submission.setSubmitted(true);
            submission.setDateSubmitted(now.minus(3, ChronoUnit.DAYS));
            submission.setUserSubmission(true);
            assignmentService.updateSubmission(submission);
            // Should not allow submission since both deadlines passed
            Assert.assertFalse(assignmentService.canSubmit(assignment));

            // test submission that is not submitted, resubmission allowed, no extension
            assignment.setOpenDate(now.minus(Period.ofDays(3)));
            assignment.setCloseDate(now.minus(Period.ofDays(1)));
            submission.setSubmitted(false);
            submission.setDateSubmitted(null);
            submission.setUserSubmission(false);
            submission.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, Integer.toString(1));
            submission.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, Long.toString(now.plus(Period.ofDays(1)).toEpochMilli()));
            submission.getProperties().remove(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME);
            assignmentService.updateSubmission(submission);
            // Should not allow submission since a resubmission is only allowed after the first submission has occurred
            Assert.assertFalse(assignmentService.canSubmit(assignment));

            // test submission that is submitted, resubmission allowed, no extension
            submission.setSubmitted(true);
            submission.setDateSubmitted(now.minus(1, ChronoUnit.DAYS));
            submission.setUserSubmission(true);
            assignmentService.updateSubmission(submission);
            // Should allow submission since both submitted=true and dateSubmitted is not null
            Assert.assertTrue(assignmentService.canSubmit(assignment));

            // test assignment closed, empty submission with both extension and resubmission in past
            assignment.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, Integer.toString(1));
            assignment.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, Long.toString(now.minus(Period.ofDays(1)).toEpochMilli()));
            submission.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, Integer.toString(1));
            submission.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, Long.toString(now.minus(Period.ofDays(1)).toEpochMilli()));
            submission.getProperties().put(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME, Long.toString(now.minus(Period.ofDays(2)).toEpochMilli()));
            submission.setFeedbackComment("");
            submission.setFeedbackText("");
            submission.setSubmitted(false);
            submission.setDateSubmitted(null);
            submission.setUserSubmission(false);
            assignmentService.updateSubmission(submission);
            // Should not allow submission since both deadlines passed
            Assert.assertFalse(assignmentService.canSubmit(assignment));
        } catch (Exception e) {
            Assert.fail("Could not create submission\n" + e);
        }
    }

    @Test
    public void transferCopyEntitiesImportPublishedRecreatesCalendarAndAnnouncement() throws Exception {

        String fromContext = UUID.randomUUID().toString();
        String toContext = UUID.randomUUID().toString();
        allowTransferCopyPermissions(fromContext, toContext);
        when(userTimeService.dateTimeFormat(any(), any(), any())).thenReturn("formatted-date-time");

        Assignment sourceAssignment = createNewAssignment(fromContext);
        sourceAssignment.setTitle("Import Source Assignment");
        sourceAssignment.setOpenDate(Instant.now().plus(Period.ofDays(1)));
        sourceAssignment.setDueDate(Instant.now().plus(Period.ofDays(2)));
        sourceAssignment.setDraft(true);
        sourceAssignment.getProperties().put(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.TRUE.toString());
        sourceAssignment.getProperties().put(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.TRUE.toString());
        sourceAssignment.getProperties().put(AssignmentConstants.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, AssignmentConstants.GRADEBOOK_INTEGRATION_NO);
        alignCurrentUserWithAssignmentAuthor(sourceAssignment);
        allowUpdateAssignment(sourceAssignment);
        assignmentService.updateAssignment(sourceAssignment);

        Calendar toCalendar = mock(Calendar.class);
        CalendarEvent toEvent = mock(CalendarEvent.class);
        String toCalendarId = "/calendar/" + toContext;
        when(calendarService.calendarReference(toContext, SiteService.MAIN_CONTAINER)).thenReturn(toCalendarId);
        when(calendarService.getCalendar(toCalendarId)).thenReturn(toCalendar);
        when(toCalendar.addEvent(any(), anyString(), anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(toEvent);
        when(toEvent.getId()).thenReturn("imported-calendar-event-id");

        AnnouncementChannel toChannel = mock(AnnouncementChannel.class);
        AnnouncementMessageEdit announcementMessage = mock(AnnouncementMessageEdit.class);
        AnnouncementMessageHeaderEdit header = mock(AnnouncementMessageHeaderEdit.class);
        ResourcePropertiesEdit announcementProperties = mock(ResourcePropertiesEdit.class);
        String toChannelId = "/announcement/" + toContext;
        when(announcementService.channelReference(toContext, SiteService.MAIN_CONTAINER)).thenReturn(toChannelId);
        when(announcementService.getAnnouncementChannel(toChannelId)).thenReturn(toChannel);
        when(toChannel.addAnnouncementMessage()).thenReturn(announcementMessage);
        when(announcementMessage.getAnnouncementHeaderEdit()).thenReturn(header);
        when(announcementMessage.getPropertiesEdit()).thenReturn(announcementProperties);
        when(announcementMessage.getId()).thenReturn("imported-announcement-id");

        transferCopyEntities(fromContext, toContext, List.of(EntityTransferrer.PUBLISH_OPTION));

        Collection<Assignment> importedAssignments = assignmentService.getAssignmentsForContext(toContext);
        Assert.assertEquals(1, importedAssignments.size());
        Assignment importedAssignment = importedAssignments.iterator().next();
        Assert.assertFalse(importedAssignment.getDraft());
        Assert.assertEquals(Boolean.TRUE.toString(), importedAssignment.getProperties().get(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
        Assert.assertEquals(Boolean.TRUE.toString(), importedAssignment.getProperties().get(AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED));
        Assert.assertEquals("imported-announcement-id", importedAssignment.getProperties().get(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID));
        Assert.assertEquals(Boolean.TRUE.toString(), importedAssignment.getProperties().get(AssignmentConstants.NEW_ASSIGNMENT_DUE_DATE_SCHEDULED));
        Assert.assertEquals("imported-calendar-event-id", importedAssignment.getProperties().get(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID));

        verify(toCalendar).addEvent(any(), anyString(), anyString(), anyString(), anyString(), any(), any(), any());
        verify(toChannel).commitMessage(eq(announcementMessage), eq(NotificationService.NOTI_NONE), anyString());
    }

    @Test
    public void transferCopyEntitiesImportAsDraftSkipsCalendarAndAnnouncement() throws Exception {

        String fromContext = UUID.randomUUID().toString();
        String toContext = UUID.randomUUID().toString();
        allowTransferCopyPermissions(fromContext, toContext);

        Assignment sourceAssignment = createNewAssignment(fromContext);
        sourceAssignment.setOpenDate(Instant.now().plus(Period.ofDays(1)));
        sourceAssignment.setDueDate(Instant.now().plus(Period.ofDays(2)));
        sourceAssignment.setDraft(true);
        sourceAssignment.getProperties().put(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.TRUE.toString());
        sourceAssignment.getProperties().put(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.TRUE.toString());
        sourceAssignment.getProperties().put(AssignmentConstants.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, AssignmentConstants.GRADEBOOK_INTEGRATION_NO);
        alignCurrentUserWithAssignmentAuthor(sourceAssignment);
        allowUpdateAssignment(sourceAssignment);
        assignmentService.updateAssignment(sourceAssignment);

        Calendar toCalendar = mock(Calendar.class);
        String toCalendarId = "/calendar/" + toContext;
        when(calendarService.calendarReference(toContext, SiteService.MAIN_CONTAINER)).thenReturn(toCalendarId);
        when(calendarService.getCalendar(toCalendarId)).thenReturn(toCalendar);

        AnnouncementChannel toChannel = mock(AnnouncementChannel.class);
        String toChannelId = "/announcement/" + toContext;
        when(announcementService.channelReference(toContext, SiteService.MAIN_CONTAINER)).thenReturn(toChannelId);
        when(announcementService.getAnnouncementChannel(toChannelId)).thenReturn(toChannel);

        transferCopyEntities(fromContext, toContext, Collections.emptyList());

        Collection<Assignment> importedAssignments = assignmentService.getAssignmentsForContext(toContext);
        Assert.assertEquals(1, importedAssignments.size());
        Assignment importedAssignment = importedAssignments.iterator().next();
        Assert.assertTrue(importedAssignment.getDraft());
        Assert.assertEquals(Boolean.TRUE.toString(), importedAssignment.getProperties().get(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
        Assert.assertNull(importedAssignment.getProperties().get(AssignmentConstants.NEW_ASSIGNMENT_DUE_DATE_SCHEDULED));
        Assert.assertNull(importedAssignment.getProperties().get(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID));
        Assert.assertNull(importedAssignment.getProperties().get(AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED));
        Assert.assertNull(importedAssignment.getProperties().get(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID));

        verify(toCalendar, never()).addEvent(any(), anyString(), anyString(), anyString(), anyString(), any(), any(), any());
        verify(toChannel, never()).addAnnouncementMessage();
    }

    @Test
    public void transferCopyEntitiesImportPublishedCopiesSourceDueDateCalendarEvent() throws Exception {

        String fromContext = UUID.randomUUID().toString();
        String toContext = UUID.randomUUID().toString();
        allowTransferCopyPermissions(fromContext, toContext);

        Assignment sourceAssignment = createNewAssignment(fromContext);
        sourceAssignment.setTitle("Import Source Assignment");
        sourceAssignment.setDueDate(Instant.now().plus(Period.ofDays(2)));
        sourceAssignment.setDraft(true);
        sourceAssignment.getProperties().put(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.TRUE.toString());
        sourceAssignment.getProperties().put(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID, "source-calendar-event-id");
        sourceAssignment.getProperties().put(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.FALSE.toString());
        sourceAssignment.getProperties().put(AssignmentConstants.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, AssignmentConstants.GRADEBOOK_INTEGRATION_NO);
        alignCurrentUserWithAssignmentAuthor(sourceAssignment);
        allowUpdateAssignment(sourceAssignment);
        assignmentService.updateAssignment(sourceAssignment);

        Calendar fromCalendar = mock(Calendar.class);
        CalendarEvent fromEvent = mock(CalendarEvent.class);
        TimeRange fromRange = mock(TimeRange.class);
        String fromCalendarId = "/calendar/" + fromContext;
        when(calendarService.calendarReference(fromContext, SiteService.MAIN_CONTAINER)).thenReturn(fromCalendarId);
        when(calendarService.getCalendar(fromCalendarId)).thenReturn(fromCalendar);
        when(fromCalendar.getEvent("source-calendar-event-id")).thenReturn(fromEvent);
        when(fromEvent.getRange()).thenReturn(fromRange);
        when(fromEvent.getDisplayName()).thenReturn("Due Import Source Assignment");
        when(fromEvent.getDescription()).thenReturn("Due date event");
        when(fromEvent.getType()).thenReturn("Deadline");
        when(fromEvent.getLocation()).thenReturn("");
        when(fromEvent.getAccess()).thenReturn(CalendarEvent.EventAccess.SITE);
        when(fromEvent.getGroups()).thenReturn(Collections.emptyList());
        when(fromEvent.getAttachments()).thenReturn(Collections.emptyList());

        Calendar toCalendar = mock(Calendar.class);
        CalendarEvent toEvent = mock(CalendarEvent.class);
        String toCalendarId = "/calendar/" + toContext;
        when(calendarService.calendarReference(toContext, SiteService.MAIN_CONTAINER)).thenReturn(toCalendarId);
        when(calendarService.getCalendar(toCalendarId)).thenReturn(toCalendar);
        when(toCalendar.addEvent(any(), anyString(), anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(toEvent);
        when(toEvent.getId()).thenReturn("copied-calendar-event-id");

        transferCopyEntities(fromContext, toContext, List.of(EntityTransferrer.PUBLISH_OPTION));

        Collection<Assignment> importedAssignments = assignmentService.getAssignmentsForContext(toContext);
        Assert.assertEquals(1, importedAssignments.size());
        Assignment importedAssignment = importedAssignments.iterator().next();
        Assert.assertFalse(importedAssignment.getDraft());
        Assert.assertEquals("copied-calendar-event-id", importedAssignment.getProperties().get(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID));
        Assert.assertEquals(Boolean.TRUE.toString(), importedAssignment.getProperties().get(AssignmentConstants.NEW_ASSIGNMENT_DUE_DATE_SCHEDULED));

        verify(fromCalendar).getEvent("source-calendar-event-id");
        verify(toCalendar).addEvent(eq(fromRange), eq("Due Import Source Assignment"), eq("Due date event"), eq("Deadline"),
            eq(""), eq(CalendarEvent.EventAccess.SITE), any(), any());
    }

    @Test
    public void transferCopyEntitiesImportPublishedSkipsAnnouncementWhenAutoAnnounceDisabled() throws Exception {

        String fromContext = UUID.randomUUID().toString();
        String toContext = UUID.randomUUID().toString();
        allowTransferCopyPermissions(fromContext, toContext);

        Assignment sourceAssignment = createNewAssignment(fromContext);
        sourceAssignment.setTitle("Import Source Assignment");
        sourceAssignment.setOpenDate(Instant.now().plus(Period.ofDays(1)));
        sourceAssignment.setDraft(true);
        sourceAssignment.getProperties().put(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.FALSE.toString());
        sourceAssignment.getProperties().put(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.FALSE.toString());
        sourceAssignment.getProperties().put(AssignmentConstants.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, AssignmentConstants.GRADEBOOK_INTEGRATION_NO);
        alignCurrentUserWithAssignmentAuthor(sourceAssignment);
        allowUpdateAssignment(sourceAssignment);
        assignmentService.updateAssignment(sourceAssignment);

        AnnouncementChannel toChannel = mock(AnnouncementChannel.class);
        String toChannelId = "/announcement/" + toContext;
        when(announcementService.channelReference(toContext, SiteService.MAIN_CONTAINER)).thenReturn(toChannelId);
        when(announcementService.getAnnouncementChannel(toChannelId)).thenReturn(toChannel);

        transferCopyEntities(fromContext, toContext, List.of(EntityTransferrer.PUBLISH_OPTION));

        Collection<Assignment> importedAssignments = assignmentService.getAssignmentsForContext(toContext);
        Assert.assertEquals(1, importedAssignments.size());
        Assignment importedAssignment = importedAssignments.iterator().next();
        Assert.assertFalse(importedAssignment.getDraft());
        Assert.assertEquals(Boolean.FALSE.toString(), importedAssignment.getProperties().get(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
        Assert.assertNull(importedAssignment.getProperties().get(AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED));
        Assert.assertNull(importedAssignment.getProperties().get(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID));

        verify(toChannel, never()).addAnnouncementMessage();
    }

    @Test
    public void transferCopyEntitiesImportPublishedSkipsAnnouncementWhenOpenDateIsMissing() throws Exception {

        String fromContext = UUID.randomUUID().toString();
        String toContext = UUID.randomUUID().toString();
        allowTransferCopyPermissions(fromContext, toContext);

        Assignment sourceAssignment = createNewAssignment(fromContext);
        sourceAssignment.setTitle("Import Source Assignment");
        sourceAssignment.setOpenDate(null);
        sourceAssignment.setDraft(true);
        sourceAssignment.getProperties().put(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.FALSE.toString());
        sourceAssignment.getProperties().put(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.TRUE.toString());
        sourceAssignment.getProperties().put(AssignmentConstants.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, AssignmentConstants.GRADEBOOK_INTEGRATION_NO);
        alignCurrentUserWithAssignmentAuthor(sourceAssignment);
        allowUpdateAssignment(sourceAssignment);
        assignmentService.updateAssignment(sourceAssignment);

        AnnouncementChannel toChannel = mock(AnnouncementChannel.class);
        String toChannelId = "/announcement/" + toContext;
        when(announcementService.channelReference(toContext, SiteService.MAIN_CONTAINER)).thenReturn(toChannelId);
        when(announcementService.getAnnouncementChannel(toChannelId)).thenReturn(toChannel);

        transferCopyEntities(fromContext, toContext, List.of(EntityTransferrer.PUBLISH_OPTION));

        Collection<Assignment> importedAssignments = assignmentService.getAssignmentsForContext(toContext);
        Assert.assertEquals(1, importedAssignments.size());
        Assignment importedAssignment = importedAssignments.iterator().next();
        Assert.assertFalse(importedAssignment.getDraft());
        Assert.assertEquals(Boolean.TRUE.toString(), importedAssignment.getProperties().get(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
        Assert.assertNull(importedAssignment.getProperties().get(AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED));
        Assert.assertNull(importedAssignment.getProperties().get(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID));

        verify(toChannel, never()).addAnnouncementMessage();
    }

    @Test
    public void createsTasksForNewlyAddedUser() {

        String siteId = "xyz";

        Assignment assignment = createNewAssignment(siteId);

        String assignmentRef = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();

        String studentId = "student1";
        String userReference = "/user/" + studentId;
        when(userDirectoryService.idFromReference(userReference)).thenReturn(studentId);

        // Mock up an existing Task
        Long taskId = 232L;
        Task task = mock(Task.class);
        when(task.getId()).thenReturn(taskId);;
        when(taskService.getTask(assignmentRef)).thenReturn(Optional.of(task));

        // Mock up an event, and then "fire" it
        Event event = mock(Event.class);
        when(event.getEvent()).thenReturn(SiteService.EVENT_USER_SITE_MEMBERSHIP_ADD);
        when(event.getContext()).thenReturn(siteId);
        when(event.getResource()).thenReturn(userReference);
        when(event.getModify()).thenReturn(true);
        assignmentEventObserver.update(null, event);

        // Verify that getTask is called on our taskService mock
        verify(taskService).getTask(assignmentRef);

        UserTaskAdapterBean userTaskBean = new UserTaskAdapterBean();
        userTaskBean.setTaskId(task.getId());
        userTaskBean.setUserId(studentId);
        userTaskBean.setPriority(Priorities.HIGH);

        // Verify that createUserTask is called on our taskService mock, with the correct arguments
        verify(taskService).createUserTask(task, userTaskBean);
    }

    private void allowTransferCopyPermissions(String fromContext, String toContext) {
        String fromContextReference = AssignmentReferenceReckoner.reckoner().context(fromContext).reckon().getReference();
        String toContextReference = AssignmentReferenceReckoner.reckoner().context(toContext).reckon().getReference();
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT, fromContextReference)).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, fromContextReference)).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT, toContextReference)).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, toContextReference)).thenReturn(true);
        when(securityService.unlock(eq(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT), anyString())).thenReturn(true);
    }

    private void allowUpdateAssignment(Assignment assignment) {
        String contextReference = AssignmentReferenceReckoner.reckoner().context(assignment.getContext()).reckon().getReference();
        String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, contextReference)).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, assignmentReference)).thenReturn(true);
    }

    private void alignCurrentUserWithAssignmentAuthor(Assignment assignment) {
        User currentUser = mock(User.class);
        when(currentUser.getId()).thenReturn(assignment.getAuthor());
        when(userDirectoryService.getCurrentUser()).thenReturn(currentUser);
        when(sessionManager.getCurrentSessionUserId()).thenReturn(assignment.getAuthor());
    }

    private void transferCopyEntities(String fromContext, String toContext, List<String> transferOptions) {
        try {
            assignmentEntityTransferrer.transferCopyEntities(fromContext, toContext, null, transferOptions);
        } catch (Exception e) {
            Assert.fail("transferCopyEntities failed\n" + e);
        }
    }

    private AssignmentSubmission createNewSubmission(String context, String submitterId, Assignment assignment) throws UserNotDefinedException, IdUnusedException {

        if (assignment == null) {
            assignment = createNewAssignment(context);
        }

        String addSubmissionRef = AssignmentReferenceReckoner.reckoner().context(context).subtype("s").reckon().getReference();
        Site site = mock(Site.class);
        when(site.getGroup(submitterId)).thenReturn(mock(Group.class));
        when(site.getMember(submitterId)).thenReturn(mock(Member.class));
        when(siteService.getSite(context)).thenReturn(site);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION, addSubmissionRef)).thenReturn(true);
        AssignmentSubmission submission = null;
        try {
            submission = assignmentService.addSubmission(assignment.getId(), submitterId);
        } catch (PermissionException e) {
            Assert.fail(e.toString());
        }
        return submission;
    }

    private AssignmentSubmission createNewGroupSubmission(String context, String groupSubmitter, Set<String> submitters) throws IdUnusedException, PermissionException, GroupNotDefinedException {

        // Setup an Assignment for Group Submission
        Assignment assignment = createNewAssignment(context);
        assignment.setTypeOfAccess(Assignment.Access.GROUP);
        assignment.setIsGroup(true);
        assignment.setOpenDate(Instant.now().minus(Period.ofDays(1)));
        String groupRef = "/site/" + context + "/group/" + groupSubmitter;
        assignment.getGroups().add(groupRef);
        String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        when(siteService.siteReference(context)).thenReturn("/site/" + context);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, assignmentReference)).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference())).thenReturn(true);

        // configure mock group objects
        Site site = mock(Site.class);
        Group group = mock(Group.class);
        when(group.getReference()).thenReturn(groupRef);
        when(group.getProperties()).thenReturn(new BaseResourceProperties());
        Collection<Group> groups = new HashSet<>();
        groups.add(group);
        when(site.getGroups()).thenReturn(groups);
        when(site.getGroup(groupRef)).thenReturn(group);
        when(site.getGroup(groupSubmitter)).thenReturn(group);
        Set<Member> members = new HashSet<>();
        submitters.forEach(s -> {
                Member member = mock(Member.class);
                when(member.getUserId()).thenReturn(s);
                Role r = mock(Role.class);
                when(member.getRole()).thenReturn(r);
                when(r.isAllowed(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION)).thenReturn(true);
                when(r.isAllowed(AssignmentServiceConstants.SECURE_GRADE_ASSIGNMENT_SUBMISSION)).thenReturn(false);
                members.add(member);
        });
        when(group.getMembers()).thenReturn(members);
        when(siteService.getSite(context)).thenReturn(site);
        Set<String> groupRefs = groups.stream().map(Group::getReference).collect(Collectors.toSet());

        AuthzGroup authzGroup = mock(AuthzGroup.class);
        when(authzGroupService.getAuthzGroup(groupRef)).thenReturn(authzGroup);
        assignmentService.updateAssignment(assignment);

        // pick a submitter to be the current user
        String currentUser = submitters.stream().findAny().get();
        when(sessionManager.getCurrentSessionUserId()).thenReturn(currentUser);

        // drop security to student permissions
        when(authzGroupService.getAuthzGroupsIsAllowed(currentUser, AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, groupRefs)).thenReturn(groupRefs);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, groupSubmitter)).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference())).thenReturn(false);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION, groupRef)).thenReturn(true);

        try {
            return assignmentService.addSubmission(assignment.getId(), groupSubmitter);
        } catch (PermissionException e) {
            Assert.fail(e.toString());
        }
        return null;
    }

    private Assignment createNewFutureAssignment(String context) {
        String contextReference = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT, contextReference)).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, contextReference)).thenReturn(true);
        when(sessionManager.getCurrentSessionUserId()).thenReturn(UUID.randomUUID().toString());
        Assignment assignment = null;
        try {
            assignment = assignmentService.addAssignment(context);
            assignment.setOpenDate(Instant.now().plusSeconds(300));
            assignment.setVisibleDate(Instant.now().plusSeconds(300));
        } catch (PermissionException e) {
            Assert.fail(e.toString());
        }
        return assignment;
    }

    private Assignment createNewAssignment(String context) {
        String contextReference = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT, contextReference)).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, contextReference)).thenReturn(true);
        when(sessionManager.getCurrentSessionUserId()).thenReturn(UUID.randomUUID().toString());
        Assignment assignment = null;
        try {
            assignment = assignmentService.addAssignment(context);
        } catch (PermissionException e) {
            Assert.fail(e.toString());
        }
        return assignment;
    }

    private void configureScale(int scale, Locale locale) {
        int dec = new Double(Math.log10(scale)).intValue();
        NumberFormat nf = NumberFormat.getInstance(locale);
        nf.setMaximumFractionDigits(dec);
        nf.setMinimumFractionDigits(dec);
        nf.setGroupingUsed(false);
        when(formattedText.getNumberFormat(dec, dec, false)).thenReturn(nf);
    }

    private AssignmentSubmission duplicateSubmission(AssignmentSubmission submission) {
        // lets create some duplicate submissions by side stepping the service, nobody should ever do this its only for testing
        AssignmentSubmission duplicateSubmission = new AssignmentSubmission();
        BeanUtils.copyProperties(submission, duplicateSubmission);
        duplicateSubmission.setId(null);
        duplicateSubmission.setProperties(new HashMap<>());
        duplicateSubmission.setAttachments(new HashSet<>());
        duplicateSubmission.setFeedbackAttachments(new HashSet<>());
        duplicateSubmission.setSubmitters(new HashSet<>());
        submission.getSubmitters().forEach(s -> {
            AssignmentSubmissionSubmitter submitter = new AssignmentSubmissionSubmitter();
            BeanUtils.copyProperties(s, submitter);
            submitter.setId(null);
            submitter.setSubmission(duplicateSubmission);
            duplicateSubmission.getSubmitters().add(submitter);
        });
        duplicateSubmission.setDateCreated(Instant.now().plusSeconds(5));
        return duplicateSubmission;
    }

    private Event createMockEvent(String context, String gradebookId, Long itemId, String studentUid, String grade, String grader) {
        String[] parts = new String[] {
                "/gradebookng",
                gradebookId,
                itemId.toString(),
                studentUid,
                grade,
                "OK",
                "INSTRUCTOR"
        };

        Event event = mock(Event.class);
        when(event.getEvent()).thenReturn("gradebook.updateItemScore");
        when(event.getModify()).thenReturn(true);
        when(event.getResource()).thenReturn(String.join("/", parts));
        when(event.getUserId()).thenReturn(grader);
        when(event.getContext()).thenReturn(context);
        return event;
    }

    private String readResourceToString(String resource) {
        InputStream is = this.getClass().getResourceAsStream(resource);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        return br.lines().collect(Collectors.joining("\n"));
    }
}
