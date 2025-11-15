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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
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
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutoSubmitAssignmentsJob implements Job {

    public static final String EVENT_AUTO_SUBMIT_JOB = "asn.auto.submit.job";
    public static final String EVENT_AUTO_SUBMIT_JOB_ERROR = "asn.auto.submit.job.error";
    public static final String EVENT_AUTO_SUBMIT_SUBMISSION = "asn.auto.submit.submission";

    @Setter private AssignmentRepository assignmentRepository;
    @Setter private AssignmentService assignmentService;
    @Setter private AuthzGroupService authzGroupService;
    @Setter private EmailService emailService;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SessionManager sessionManager;
    @Setter private UsageSessionService usageSessionService;

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

        loginToSakai("admin");

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

        eventTrackingService.post(eventTrackingService.newEvent(EVENT_AUTO_SUBMIT_JOB, safeEventLength(whoAmI.toString()), true));

        log.info("Start Job: {}", whoAmI);

        int failures = autoSubmitDraftSubmissions();

        if (failures > 0) {
            notifyAutoSubmitFailures(failures);
        }

        log.info("End Job: {} ({} failures)", whoAmI, failures);

        logoutFromSakai();
    }

    /**
     * Process all sites and auto-submit draft submissions that meet the criteria
     * OPTIMIZED: Uses bulk processing instead of nested loops for better performance
     * @return number of failures encountered
     */
    private int autoSubmitDraftSubmissions() {

        int failures = 0;
        int batchSize = serverConfigurationService.getInt("assignment.autoSubmit.batchSize", 1000); // Increased batch size

        try {
            // OPTIMIZATION: Get all auto-submit eligible submissions in one query instead of nested loops
            log.debug("Starting optimized auto-submit process");
            
            List<SimpleSubmissionDraft> allEligibleSubmissions = assignmentService.getAllEligibleDraftSubmissions();
            
            if (allEligibleSubmissions.isEmpty()) {
                log.debug("No eligible draft submissions found for auto-submit");
                return 0;
            }

            log.info("Found {} eligible draft submissions for auto-submit", allEligibleSubmissions.size());

            // OPTIMIZATION: Process submissions in batches to avoid memory issues
            int totalProcessed = 0;
            for (int i = 0; i < allEligibleSubmissions.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, allEligibleSubmissions.size());
                List<SimpleSubmissionDraft> batch = allEligibleSubmissions.subList(i, endIndex);
                
                log.debug("Processing batch {}-{} of {} submissions", i + 1, endIndex, allEligibleSubmissions.size());
                
                for (SimpleSubmissionDraft submission : batch) {
                    try {
                        autoSubmitSubmissionDTO(submission);
                        totalProcessed++;
                    } catch (Exception e) {
                        log.error("Error auto-submitting submission {}: {}", submission.id, e.getMessage(), e);
                        failures++;
                    }
                }
            }

            log.info("Auto-submit completed: {} processed, {} failures", totalProcessed, failures);

        } catch (Exception e) {
            log.error("Error in optimized auto-submit process: {}", e.getMessage(), e);
            failures++;
        }
        
        return failures;
    }



    /**
     * Auto-submit a draft submission (OPTIMIZED VERSION)
     * @param submission the submission to auto-submit
     */
    private void autoSubmitSubmissionDTO(SimpleSubmissionDraft submission) {

        try {
            log.debug("Auto-submitting draft submission {}", submission.id);

            // Get the actual submission entity
            AssignmentSubmission entity = assignmentRepository.findSubmission(submission.id);
            if (entity == null) {
                log.warn("Submission {} not found for auto-submit", submission.id);
                return;
            }

            // Check if submission is already submitted to avoid duplicates
            if (entity.getSubmitted()) {
                log.debug("Submission {} is already submitted, skipping auto-submit", submission.id);
                return;
            }

            // Get assignment info from the entity directly (no need for separate parameter)
            Assignment assignmentEntity = entity.getAssignment();
            if (assignmentEntity == null) {
                log.warn("Assignment not found for submission {}", submission.id);
                return;
            }

            // Get the original session context to restore later
            String originalUserId = sessionManager.getCurrentSessionUserId();
            String originalUserEid = sessionManager.getCurrentSession().getUserEid();

            try {
                // For group submissions, use the first submitter; for individual, use the single submitter
                String submitterToRunAs = submission.submitterIds != null && !submission.submitterIds.isEmpty() ? submission.submitterIds.iterator().next() : null;

                if (submitterToRunAs != null) {
                    log.debug("Auto-submitting as user: {} for submission {}", submitterToRunAs, submission.id);

                    sessionManager.getCurrentSession().setUserId(submitterToRunAs);
                    sessionManager.getCurrentSession().setUserEid(submitterToRunAs);

                    // Mark as submitted and set submission time to the draft's last modified time
                    entity.setSubmitted(true);
                    entity.setUserSubmission(true);
                    entity.setDateSubmitted(submission.dateModified);

                    // Set auto-submit flag before calling updateSubmission to ensure it's persisted
                    Map<String, String> props = entity.getProperties();
                    if (props == null) {
                        props = new HashMap<>();
                        entity.setProperties(props);
                    }
                    props.size(); // This triggers Hibernate to load the collection
                    props.put(AssignmentConstants.PROP_SUBMISSION_AUTO_SUBMITTED, "true");

                    log.debug("Setting auto-submit flag for submission {}: {}", submission.id, props.get(AssignmentConstants.PROP_SUBMISSION_AUTO_SUBMITTED));

                    assignmentService.updateSubmission(entity);

                    // CRITICAL: Handle Turnitin/content review submission - this is what the normal student flow does!
                    if (assignmentEntity.getContentReview() && !entity.getAttachments().isEmpty()) {
                        log.debug("Posting reviewable attachments to content review service for submission {}", submission.id);
                        assignmentService.postReviewableSubmissionAttachments(entity);
                    }

                    String submitterInfo = String.join(", ", submission.submitterIds != null ? submission.submitterIds : List.of("Unknown"));
                    eventTrackingService.post(eventTrackingService.newEvent(EVENT_AUTO_SUBMIT_SUBMISSION, "Assignment: " + assignmentEntity.getTitle() + " Submitter: " + submitterInfo, true));
                } else {
                    log.warn("No submitter found for submission {}", submission.id);
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
     * Send notification email about auto submit failures
     * @param failureCount number of failures
     */
    private void notifyAutoSubmitFailures(int count) {

        // Register XML file email template with the service
        ClassLoader cl = AutoSubmitAssignmentsJob.class.getClassLoader();
        emailTemplateService.importTemplateFromXmlFile(cl.getResourceAsStream("templates/" + AssignmentConstants.EMAIL_TEMPLATE_AUTO_SUBMIT_ERRORS_FILE_NAME), AssignmentConstants.EMAIL_TEMPLATE_AUTO_SUBMIT_ERRORS);

        boolean notifyOn = serverConfigurationService.getBoolean(AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ERROR_NOTIFICATION_ENABLED, AssignmentConstants.SAK_PROP_AUTO_SUBMIT_ERROR_NOTIFICATION_ENABLED_DFLT);
        if (!notifyOn) {
            return;
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
            return;
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

    @Setter private EmailTemplateService emailTemplateService;
}
