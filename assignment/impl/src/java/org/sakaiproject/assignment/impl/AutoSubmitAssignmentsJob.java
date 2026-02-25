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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentPeerAssessmentService;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.SimpleSubmissionDraft;
import org.sakaiproject.assignment.api.persistence.AssignmentRepository;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.api.RenderedTemplate;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutoSubmitAssignmentsJob implements Job {

    public static final String EVENT_AUTO_SUBMIT_JOB = "asn.auto.submit.job";
    public static final String EVENT_AUTO_SUBMIT_JOB_ERROR = "asn.auto.submit.job.error";
    public static final String EVENT_AUTO_SUBMIT_SUBMISSION = "asn.auto.submit.submission";

    @Setter private AssignmentRepository assignmentRepository;
    @Setter private AssignmentService assignmentService;
    @Setter private AssignmentPeerAssessmentService assignmentPeerAssessmentService;
    @Setter private AuthzGroupService authzGroupService;
    @Setter private EmailService emailService;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SessionManager sessionManager;
    @Setter private UsageSessionService usageSessionService;
    @Setter private UserDirectoryService userDirectoryService;
    @Setter private EmailTemplateService emailTemplateService;

    private volatile boolean emailTemplateRegistered = false;

    /*
     * Quartz job to check for assignment draft submissions that should be autosubmitted
     *
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext jobInfo) throws JobExecutionException {

        // Check if auto submit is enabled
        if (!serverConfigurationService.getBoolean(AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ENABLED, AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ENABLED_DFLT)) {
            log.debug("Assignment auto submit is disabled");
            return;
        }

        String jobName = jobInfo.getJobDetail().getKey().getName();
        String triggerName = jobInfo.getTrigger().getKey().getName();
        Date requestedFire = jobInfo.getScheduledFireTime();
        Date actualfire = jobInfo.getFireTime();

        StringBuffer whoAmI = new StringBuffer("AutoSubmitAssignmentsJob $");
        whoAmI.append(" Job: ");
        whoAmI.append(jobName);
        whoAmI.append(" Trigger: ");
        whoAmI.append(triggerName);

        if (requestedFire != null) {
            whoAmI.append(" Fire scheduled: ");
            whoAmI.append(requestedFire.toString());
        }

        if (actualfire != null) {
            whoAmI.append(" Fire actual: ");
            whoAmI.append(actualfire.toString());
        }

        log.info("Start Job: {}", whoAmI);

        boolean loggedIn = false;
        try {
            loginToSakai("admin");
            loggedIn = true;

            eventTrackingService.post(eventTrackingService.newEvent(EVENT_AUTO_SUBMIT_JOB, safeEventLength(whoAmI.toString()), true));

            int failures = autoSubmitDraftSubmissions();

            if (failures > 0) {
                notifyAutoSubmitFailures(failures);
            }

            log.info("End Job: {} ({} failures)", whoAmI, failures);
        } finally {
            if (loggedIn) {
                logoutFromSakai();
            }
        }
    }

    /**
     * Process all sites and auto-submit draft submissions that meet the criteria
     * @return number of failures encountered
     */
    private int autoSubmitDraftSubmissions() {

        int failures = 0;
        int batchSize = serverConfigurationService.getInt("assignment.autoSubmit.batchSize", 1000);

        if (batchSize <= 0) {
            log.warn("Invalid batchSize configuration: {}. Using default value of 1000.", batchSize);
            batchSize = 1000;
        }

        Set<String> assignmentsWithAutoSubmits = new HashSet<>();

        try {
            log.debug("Starting auto-submit process");

            int totalProcessed = 0;
            int offset = 0;
            List<SimpleSubmissionDraft> batch;
            int batchNumber = 1;

            do {
                batch = assignmentService.getAllEligibleDraftSubmissions(batchSize, offset);

                if (batch.isEmpty()) {
                    log.debug("No more eligible draft submissions found for auto-submit");
                    break;
                }

                log.debug("Processing batch {} ({} submissions)", batchNumber, batch.size());

                for (SimpleSubmissionDraft submission : batch) {
                    try {
                        boolean success = autoSubmitSubmissionDTO(submission);
                        if (success) {
                            totalProcessed++;
                            // Track which assignment had an auto-submit
                            assignmentsWithAutoSubmits.add(submission.gradableId);
                        }
                    } catch (Exception e) {
                        log.error("Error auto-submitting submission {}: {}", submission.id, e.getMessage(), e);
                        failures++;
                    }
                }

                offset += batch.size();
                batchNumber++;

            } while (batch.size() == batchSize);

            log.info("Auto-submit completed: {} processed, {} failures", totalProcessed, failures);

            // Re-assign peer reviews for assignments that had auto-submits
            if (!assignmentsWithAutoSubmits.isEmpty()) {
                reassignPeerReviewsForAutoSubmittedAssignments(assignmentsWithAutoSubmits);
            }

        } catch (Exception e) {
            log.error("Error in auto-submit process: {}", e.getMessage(), e);
            failures++;
        }

        return failures;
    }

    /**
     * Auto-submit a draft submission
     * @param submission the submission to auto-submit
     * @return true if submission was successfully auto-submitted, false otherwise
     */
    private boolean autoSubmitSubmissionDTO(SimpleSubmissionDraft submission) {

        try {
            log.debug("Auto-submitting draft submission {}", submission.id);

            // Get the actual submission entity
            AssignmentSubmission entity = assignmentRepository.findSubmission(submission.id);
            if (entity == null) {
                log.warn("Submission {} not found for auto-submit", submission.id);
                return false;
            }

            // Check if submission is already submitted to avoid duplicates
            if (entity.getSubmitted()) {
                log.debug("Submission {} is already submitted, skipping auto-submit", submission.id);
                return false;
            }

            Assignment assignmentEntity = entity.getAssignment();
            if (assignmentEntity == null) {
                log.warn("Assignment not found for submission {}", submission.id);
                return false;
            }

            String originalUserId = sessionManager.getCurrentSessionUserId();
            String originalUserEid = sessionManager.getCurrentSession().getUserEid();

            try {
                // For group submissions, use the first submitter; for individual, use the single submitter
                String submitterToRunAs = submission.submitterIds != null && !submission.submitterIds.isEmpty()
                        ? submission.submitterIds.stream().sorted().findFirst().orElse(null)
                        : null;

                if (submitterToRunAs != null) {
                    log.debug("Auto-submitting as user: {} for submission {}", submitterToRunAs, submission.id);

                    sessionManager.getCurrentSession().setUserId(submitterToRunAs);

                    try {
                        User user = userDirectoryService.getUser(submitterToRunAs);
                        String userEid = user.getEid();
                        sessionManager.getCurrentSession().setUserEid(userEid);
                        log.debug("Resolved user {} to EID: {}", submitterToRunAs, userEid);
                    } catch (UserNotDefinedException e) {
                        log.warn("Could not resolve EID for user {} - using userId as fallback: {}", submitterToRunAs, e.getMessage());
                        sessionManager.getCurrentSession().setUserEid(submitterToRunAs);
                    }

                    // Mark as submitted and set submission time to the draft's last modified time
                    entity.setSubmitted(true);
                    entity.setUserSubmission(true);
                    entity.setDateSubmitted(submission.dateModified);

                    // Set auto-submit flag before calling updateSubmission to ensure it's persisted
                    Map<String, String> props = entity.getProperties();
                    if (props == null) {
                        props = new HashMap<>();
                        entity.setProperties(props);
                    } else {
                        Hibernate.initialize(props);
                    }
                    props.put(AssignmentConstants.PROP_SUBMISSION_AUTO_SUBMITTED, "true");

                    log.debug("Setting auto-submit flag for submission {}: {}", submission.id, props.get(AssignmentConstants.PROP_SUBMISSION_AUTO_SUBMITTED));

                    assignmentService.updateSubmission(entity);

                    if (assignmentEntity.getContentReview() && !entity.getAttachments().isEmpty()) {
                        log.debug("Posting reviewable attachments to content review service for submission {}", submission.id);
                        assignmentService.postReviewableSubmissionAttachments(entity);
                    }

                    String submitterInfo = String.join(", ", submission.submitterIds != null ? submission.submitterIds : List.of("Unknown"));
                    String eventDetail = "Assignment: " + assignmentEntity.getTitle() + " Submitter: " + submitterInfo;
                    eventTrackingService.post(eventTrackingService.newEvent(EVENT_AUTO_SUBMIT_SUBMISSION, safeEventLength(eventDetail), true));
                    return true;
                } else {
                    log.warn("No submitter found for submission {}", submission.id);
                    return false;
                }
            } finally {
                sessionManager.getCurrentSession().setUserId(originalUserId);
                sessionManager.getCurrentSession().setUserEid(originalUserEid);
            }
        } catch (Exception e) {
            log.error("Error auto-submitting submission {}: {}", submission.id, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Re-assign peer reviews for assignments that had auto-submitted drafts
     * @param assignmentIds Set of assignment IDs that had auto-submits
     */
    private void reassignPeerReviewsForAutoSubmittedAssignments(Set<String> assignmentIds) {
        if (assignmentPeerAssessmentService == null) {
            log.warn("AssignmentPeerAssessmentService not available, skipping peer review re-assignment");
            return;
        }

        log.info("Checking {} assignments for peer review re-assignment", assignmentIds.size());
        int reassignedCount = 0;

        for (String assignmentId : assignmentIds) {
            try {
                Assignment assignment = assignmentService.getAssignment(assignmentId);
                if (assignment != null && assignment.getAllowPeerAssessment() && !assignment.getDraft()) {
                    log.info("Re-scheduling peer review assignment for assignment: {} ({})", assignment.getTitle(), assignmentId);
                    // Re-schedule peer review to trigger immediate re-assignment
                    assignmentPeerAssessmentService.schedulePeerReview(assignmentId);
                    reassignedCount++;
                }
            } catch (Exception e) {
                log.error("Error re-scheduling peer review for assignment {}: {}", assignmentId, e.getMessage(), e);
            }
        }

        log.info("Peer review re-assignment completed: {} assignments processed", reassignedCount);
    }

    /**
     * Send notification email about auto submit failures
     * @param failureCount number of failures
     */
    private void notifyAutoSubmitFailures(int count) {

        boolean notifyOn = serverConfigurationService.getBoolean(AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ERROR_NOTIFICATION_ENABLED, AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ERROR_NOTIFICATION_ENABLED_DFLT);
        if (!notifyOn) {
            return;
        }

        // Register XML file email template with the service
        if (!emailTemplateRegistered) {
            synchronized (this) {
                if (!emailTemplateRegistered) {
                    try {
                        ClassLoader cl = AutoSubmitAssignmentsJob.class.getClassLoader();
                        java.io.InputStream templateStream = cl.getResourceAsStream("templates/" + AssignmentConstants.EMAIL_TEMPLATE_AUTO_SUBMIT_ERRORS_FILE_NAME);
                        if (templateStream != null) {
                            emailTemplateService.importTemplateFromXmlFile(templateStream, AssignmentConstants.EMAIL_TEMPLATE_AUTO_SUBMIT_ERRORS);
                            emailTemplateRegistered = true;
                            log.debug("Email template registered: {}", AssignmentConstants.EMAIL_TEMPLATE_AUTO_SUBMIT_ERRORS);
                        } else {
                            log.warn("Email template file not found: templates/{}", AssignmentConstants.EMAIL_TEMPLATE_AUTO_SUBMIT_ERRORS_FILE_NAME);
                        }
                    } catch (Exception e) {
                        log.error("Error importing email template: {}", e.getMessage(), e);
                    }
                }
            }
        }

        String fromAddress = serverConfigurationService.getSmtpFrom();
        String supportAddress = serverConfigurationService.getString(AssignmentConstants.SAK_PROP_SUPPORT_EMAIL_ADDRESS, fromAddress);
        String toAddress = serverConfigurationService.getString(AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ERROR_NOTIFICATION_TO_ADDRESS, supportAddress);

        Map<String, Object> replacementValues = new HashMap<>();
        replacementValues.put("failureCount", Integer.toString(count));

        try {
            RenderedTemplate template = emailTemplateService.getRenderedTemplate(AssignmentConstants.EMAIL_TEMPLATE_AUTO_SUBMIT_ERRORS, Locale.getDefault(), replacementValues);
            if (template == null) {
                throw new IllegalStateException("Template is null");
            }
            InternetAddress[] to = { new InternetAddress(toAddress) };
            emailService.sendMail(new InternetAddress(fromAddress), to, template.getRenderedSubject(), template.getRenderedMessage(), to, null, null);
        }
        catch (Exception e) {
            log.error("Unable to send email notification for AutoSubmit failures.", e);
        }
    }

    /**
     * <p>Login to sakai and start a user session. This user is intended
     * to be one of the 'hard wired' users; admin, postmaster, or synchrobot.</p>
     * <p>( this list of users can be extended; add the user via UI, update
     * the sakai_users table so their EID matches ID, add them to the
     * admin realm, restart )</p>
     * @param whoAs - who to log in as
     */
    protected void loginToSakai(String whoAs) {

        String serverName = serverConfigurationService.getServerName();
        log.debug("AutoSubmitAssignmentsJob Logging into Sakai on {} as {}", serverName, whoAs);

        UsageSession session = usageSessionService.startSession(whoAs, serverName, "AutoSubmitAssignmentsJob");
        if (session == null) {
            eventTrackingService.post(eventTrackingService.newEvent(EVENT_AUTO_SUBMIT_JOB_ERROR, whoAs + " unable to log into " + serverName, true));
            throw new RuntimeException("Unable to start session for " + whoAs);
        }

        Session sakaiSession = sessionManager.getCurrentSession();
        sakaiSession.setUserId(whoAs);
        sakaiSession.setUserEid(whoAs);

        // update the user's externally provided realm definitions
        authzGroupService.refreshUser(whoAs);

        // post the login events
        eventTrackingService.post(eventTrackingService.newEvent(UsageSessionService.EVENT_LOGIN, whoAs + " running " + serverName, true));
    }

    protected void logoutFromSakai() {
        log.debug("Logging out of Sakai on {}", serverConfigurationService.getServerName());
        eventTrackingService.post(eventTrackingService.newEvent(UsageSessionService.EVENT_LOGOUT, null, true));
        usageSessionService.logout();
    }

    /**
     * Sometimes when logging to the sakai_events table it's possible to be logging
     * with a string you don't know the size of. (An exception message is a good
     * example)
     *
     * This method is supplied to keep the length of logged messages w/in the limits
     * of the sakai_event.ref column size.
     *
     * The sakai_event.ref column size is currently 256
     *
     * @param target
     * @return
     */
    static final public String safeEventLength(final String target) {

        return StringUtils.abbreviate(target, 255);
    }
}
