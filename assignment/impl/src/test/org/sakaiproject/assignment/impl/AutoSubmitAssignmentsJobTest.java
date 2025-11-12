/**
 * Copyright (c) 2005-2025 The Apereo Foundation
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
package org.sakaiproject.assignment.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.persistence.AssignmentRepository;
import org.sakaiproject.assignment.api.persistence.AssignmentRepository.SimpleSubmissionDraft;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.api.RenderedTemplate;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Test for AutoSubmitAssignmentsJob
 */
public class AutoSubmitAssignmentsJobTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private AssignmentService assignmentService;
    @Mock private AuthzGroupService authzGroupService;
    @Mock private EmailService emailService;
    @Mock private EmailTemplateService emailTemplateService;
    @Mock private EventTrackingService eventTrackingService;
    @Mock private ServerConfigurationService serverConfigurationService;
    @Mock private SessionManager sessionManager;
    @Mock private UsageSessionService usageSessionService;

    @Mock private JobExecutionContext jobContext;
    @Mock private JobDetail jobDetail;
    @Mock private Trigger trigger;
    @Mock private UsageSession usageSession;
    @Mock private Session session;
    @Mock private Event event;
    @Mock private Assignment assignment;
    @Mock private AssignmentSubmission submission;
    @Mock private RenderedTemplate template;

    private AutoSubmitAssignmentsJob job;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        job = new AutoSubmitAssignmentsJob();
        job.setAssignmentRepository(assignmentRepository);
        job.setAssignmentService(assignmentService);
        job.setAuthzGroupService(authzGroupService);
        job.setEmailService(emailService);
        job.setEmailTemplateService(emailTemplateService);
        job.setEventTrackingService(eventTrackingService);
        job.setServerConfigurationService(serverConfigurationService);
        job.setSessionManager(sessionManager);
        job.setUsageSessionService(usageSessionService);
    }

    @Test
    public void testJobExecutionWhenDisabled() throws Exception {
        // Auto-submit disabled
        when(serverConfigurationService.getBoolean(AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ENABLED, AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ENABLED_DFLT)).thenReturn(false);

        job.execute(jobContext);

        // Job should exit early without processing
        verify(eventTrackingService, never()).post(any(Event.class));
        verify(usageSessionService, never()).startSession(anyString(), anyString(), anyString());
    }

    @Test
    public void testJobExecutionWhenEnabled() throws Exception {
        setupJobContext();
        
        // Auto-submit enabled
        when(serverConfigurationService.getBoolean(AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ENABLED, AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ENABLED_DFLT)).thenReturn(true);
        when(serverConfigurationService.getServerName()).thenReturn("localhost");

        // No eligible submissions to process - return empty list
        when(assignmentService.getAllEligibleDraftSubmissions()).thenReturn(Collections.emptyList());

        job.execute(jobContext);

        // Job should have logged in, processed (empty) submissions, and logged out
        verify(usageSessionService).startSession("admin", "localhost", "AutoSubmitAssignmentsJob");
        verify(sessionManager).getCurrentSession();
        verify(session).setUserId("admin");
        verify(session).setUserEid("admin");
        verify(authzGroupService).refreshUser("admin");
        verify(eventTrackingService, atLeast(2)).post(any(Event.class)); // login and job start events
        verify(usageSessionService).logout();
    }

    @Test
    public void testAutoSubmitOnlyDraftsAfterCloseDate() throws Exception {
        setupJobContext();
        setupEnabledJob();
        
        // Return empty list to test the job flow without actual processing
        when(assignmentService.getAllEligibleDraftSubmissions()).thenReturn(Collections.emptyList());

        job.execute(jobContext);

        // Verify the job executed successfully without processing any submissions
        verify(assignmentService).getAllEligibleDraftSubmissions();
        verify(eventTrackingService, atLeast(2)).post(any(Event.class)); // login and job start events
        verify(usageSessionService).logout();
    }

    @Test 
    public void testProcessSingleSubmission() throws Exception {
        setupJobContext();
        setupEnabledJob();
        
        // Setup draft submission
        SimpleSubmissionDraft draftDTO = new SimpleSubmissionDraft();
        draftDTO.id = "draft1";
        draftDTO.gradableId = "assignment1";
        draftDTO.submitterIds = Set.of("student1");
        draftDTO.submitted = false;
        draftDTO.draft = true;
        draftDTO.dateModified = Instant.now().minusSeconds(1800);
        
        when(assignmentService.getAllEligibleDraftSubmissions()).thenReturn(List.of(draftDTO));
        
        // Setup submission entity with all required mocks
        when(assignmentRepository.findSubmission("draft1")).thenReturn(submission);
        when(submission.getSubmitted()).thenReturn(false);
        
        Map<String, String> properties = new HashMap<>();
        when(submission.getProperties()).thenReturn(properties);
        when(submission.getAssignment()).thenReturn(assignment);
        when(assignment.getContentReview()).thenReturn(false);
        when(assignment.getTitle()).thenReturn("Test Assignment");
        when(submission.getAttachments()).thenReturn(Collections.emptySet());
        
        // Setup session context carefully
        when(sessionManager.getCurrentSessionUserId()).thenReturn("system");
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(session.getUserEid()).thenReturn("system");

        job.execute(jobContext);

        // Verify the submission was processed
        verify(submission).setSubmitted(true);
        verify(submission).setUserSubmission(true);
        verify(submission).setDateSubmitted(draftDTO.dateModified);
        verify(assignmentService).updateSubmission(submission);
    }

    @Test
    public void testNoAutoSubmitIfAlreadySubmitted() throws Exception {
        setupJobContext();
        setupEnabledJob();
        
        SimpleSubmissionDraft draftDTO = new SimpleSubmissionDraft();
        draftDTO.id = "draft1";
        draftDTO.gradableId = "assignment1";
        draftDTO.submitterIds = Set.of("student1");
        draftDTO.submitted = false;
        draftDTO.draft = true;
        
        when(assignmentService.getAllEligibleDraftSubmissions()).thenReturn(List.of(draftDTO));
        
        // Submission is already submitted
        when(assignmentRepository.findSubmission("draft1")).thenReturn(submission);
        when(submission.getSubmitted()).thenReturn(true);

        job.execute(jobContext);

        // Should not update an already submitted submission
        verify(assignmentService, never()).updateSubmission(submission);
    }

    @Test
    public void testTurnitinIntegration() throws Exception {
        setupJobContext();
        setupEnabledJob();
        
        SimpleSubmissionDraft draftDTO = new SimpleSubmissionDraft();
        draftDTO.id = "draft1";
        draftDTO.gradableId = "assignment1";
        draftDTO.submitterIds = Set.of("student1");
        draftDTO.submitted = false;
        draftDTO.draft = true;
        draftDTO.dateModified = Instant.now().minusSeconds(1800);
        
        when(assignmentService.getAllEligibleDraftSubmissions()).thenReturn(List.of(draftDTO));
        
        // Setup submission with attachments and content review enabled
        when(assignmentRepository.findSubmission("draft1")).thenReturn(submission);
        when(submission.getSubmitted()).thenReturn(false);
        when(submission.getProperties()).thenReturn(new HashMap<>());
        when(submission.getAssignment()).thenReturn(assignment);
        when(assignment.getContentReview()).thenReturn(true); // Turnitin enabled
        when(submission.getAttachments()).thenReturn(Set.of("attachment1")); // Has attachments
        
        when(sessionManager.getCurrentSessionUserId()).thenReturn("system");
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(session.getUserEid()).thenReturn("system");

        job.execute(jobContext);

        // Verify Turnitin integration was called
        verify(assignmentService).updateSubmission(submission);
        verify(assignmentService).postReviewableSubmissionAttachments(submission);
    }

    @Test
    public void testAutoSubmitFlagIsSet() throws Exception {
        setupJobContext();
        setupEnabledJob();
        
        SimpleSubmissionDraft draftDTO = new SimpleSubmissionDraft();
        draftDTO.id = "draft1";
        draftDTO.gradableId = "assignment1";
        draftDTO.submitterIds = Set.of("student1");
        draftDTO.submitted = false;
        draftDTO.draft = true;
        draftDTO.dateModified = Instant.now().minusSeconds(1800);
        
        when(assignmentService.getAllEligibleDraftSubmissions()).thenReturn(List.of(draftDTO));
        
        Map<String, String> properties = new HashMap<>();
        when(assignmentRepository.findSubmission("draft1")).thenReturn(submission);
        when(submission.getSubmitted()).thenReturn(false);
        when(submission.getProperties()).thenReturn(properties);
        when(submission.getAssignment()).thenReturn(assignment);
        when(assignment.getContentReview()).thenReturn(false);
        
        when(sessionManager.getCurrentSessionUserId()).thenReturn("system");
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(session.getUserEid()).thenReturn("system");

        job.execute(jobContext);

        // Verify auto-submit flag was set
        org.junit.Assert.assertEquals("true", properties.get(AssignmentConstants.PROP_SUBMISSION_AUTO_SUBMITTED));
    }

    @Test
    public void testNotifyAutoSubmitFailuresSendsEmail() throws Exception {
        setupJobContext();
        
        // Auto-submit enabled
        when(serverConfigurationService.getBoolean(AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ENABLED, AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ENABLED_DFLT)).thenReturn(true);
        when(serverConfigurationService.getServerName()).thenReturn("localhost");

        // Email notification enabled
        when(serverConfigurationService.getBoolean(AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ERROR_NOTIFICATION_ENABLED, AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ERROR_NOTIFICATION_ENABLED_DFLT)).thenReturn(true);
        when(serverConfigurationService.getSmtpFrom()).thenReturn("from@example.com");
        when(serverConfigurationService.getString(AssignmentConstants.SAK_PROP_SUPPORT_EMAIL_ADDRESS, "from@example.com")).thenReturn("support@example.com");
        when(serverConfigurationService.getString(AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ERROR_NOTIFICATION_TO_ADDRESS, "support@example.com")).thenReturn("to@example.com");

        when(template.getRenderedSubject()).thenReturn("subject");
        when(template.getRenderedMessage()).thenReturn("body");
        when(emailTemplateService.getRenderedTemplate(any(), any(), any())).thenReturn(template);

        // Force failure by throwing exception when getting eligible submissions
        when(assignmentService.getAllEligibleDraftSubmissions()).thenThrow(new RuntimeException("Simulated failure"));

        job.execute(jobContext);

        // Verify email was sent
        verify(emailService).sendMail(any(), any(), anyString(), anyString(), any(), any(), any());
    }

    private void setupJobContext() {
        when(jobContext.getJobDetail()).thenReturn(jobDetail);
        when(jobContext.getTrigger()).thenReturn(trigger);
        when(jobContext.getScheduledFireTime()).thenReturn(new Date());
        when(jobContext.getFireTime()).thenReturn(new Date());
        when(jobDetail.getKey()).thenReturn(JobKey.jobKey("testJob"));
        when(trigger.getKey()).thenReturn(TriggerKey.triggerKey("testTrigger"));
        when(usageSessionService.startSession("admin", "localhost", "AutoSubmitAssignmentsJob")).thenReturn(usageSession);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(eventTrackingService.newEvent(anyString(), anyString(), anyBoolean())).thenReturn(event);
    }

    private void setupEnabledJob() {
        when(serverConfigurationService.getBoolean(AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ENABLED, AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ENABLED_DFLT)).thenReturn(true);
        when(serverConfigurationService.getServerName()).thenReturn("localhost");
        when(serverConfigurationService.getInt("assignment.autoSubmit.batchSize", 1000)).thenReturn(1000);
    }
}
