/*
  Copyright (c) 2003-2018 The Apereo Foundation

  Licensed under the Educational Community License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

              http://opensource.org/licenses/ecl2

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.sakaiproject.assignment.impl.reminder;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.reminder.AssignmentDueReminderService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sends reminder emails to all students who have not submitted an assignment
 * which is currently open and due within the set reminder time.
 */
@Slf4j
public class AssignmentDueReminderServiceImpl implements AssignmentDueReminderService {
    private List<String> additionalHeaders = new ArrayList<>();
    @Setter
    private EmailService emailService;
    @Setter
    private AssignmentService assignmentService;
    @Setter
    private SiteService siteService;
    @Setter
    private UserDirectoryService userDirectoryService;
    @Setter
    private PreferencesService preferencesService;
    @Setter
    private ScheduledInvocationManager scheduledInvocationManager;
    @Setter
    private ServerConfigurationService serverConfigurationService;

    public void init() {
        log.debug("AssignmentDueReminderService init()");

        String sender = "Sender: \"" + getServiceName() + "\" <" + getSetupRequest() + ">";
        additionalHeaders.add(sender);
        additionalHeaders.add("Content-type: text/html; charset=UTF-8");
    }

    public void destroy() {
        log.debug("AssignmentDueReminderService destroy()");
    }

    public void scheduleDueDateReminder(String assignmentId) {

        // Remove any previously scheduled reminder
        removeScheduledReminder(assignmentId);

        long reminderSeconds = 60 * 60 * serverConfigurationService.getInt("assignment.reminder.hours", 24); // Convert hours to seconds

        try {
            Assignment assignment = assignmentService.getAssignment(assignmentId);
            // Only schedule due date reminders for posted assignments with due dates far enough in the future for a reminder
            if (!assignment.getDraft() && Instant.now().plusSeconds(reminderSeconds).isBefore(assignment.getDueDate())) {
                Instant reminderDate = assignment.getDueDate().minusSeconds(reminderSeconds);
                scheduledInvocationManager.createDelayedInvocation(reminderDate, "org.sakaiproject.assignment.api.reminder.AssignmentDueReminderService", assignmentId);
            }
        } catch (IdUnusedException | PermissionException e) {
            log.error("Error scheduling due date reminder email.", assignmentId, e);
        }
    }

    public void removeScheduledReminder(String assignmentId) {
        scheduledInvocationManager.deleteDelayedInvocation("org.sakaiproject.assignment.api.reminder.AssignmentDueReminderService", assignmentId);
    }

    public void execute(String opaqueContext) {
        log.debug("AssignmentDueReminderService execute");

        try {
            Assignment assignment = assignmentService.getAssignment(opaqueContext);
            Site site = siteService.getSite(assignment.getContext());

            // Do not send reminders if the site is unpublished or softly deleted
            if (site.isPublished() && !site.isSoftlyDeleted()) {
                for (Member member : site.getMembers()) {
                    if (assignmentService.canSubmit(assignment, member.getUserId())) {
                        if (checkEmailPreference(member)) {
                            sendEmailReminder(site, assignment, member);
                        }
                    }
                }
            }
        } catch (IdUnusedException | PermissionException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void sendEmailReminder(Site site, Assignment assignment, Member submitter) {
        log.debug("SendEmailReminder: '" + assignment.getTitle() + "' to " + submitter.getUserDisplayId());

        String assignmentTitle = assignment.getTitle();
        if (assignment.getTitle().length() > 11) {
            assignmentTitle = assignment.getTitle().substring(0, 11) + "[...]";
        }

        String courseName = site.getTitle();

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm aa");
        Instant dueDate = assignment.getDueDate();
        String formattedDateDue = sdf.format(dueDate);

        String toStr = getUserEmail(submitter.getUserId());
        if (StringUtils.isEmpty(toStr)) {
            return;
        }
        String headerToStr = getUserDisplayName(submitter.getUserId()) + " <" + getUserEmail(submitter.getUserId()) + ">";
        String fromStr = "\"" + courseName + "\" <" + getSetupRequest() + ">";

        Set<Member> instructors = new HashSet<>();
        for (Member member : site.getMembers()) {
            if (StringUtils.equals(member.getRole().getId(), site.getMaintainRole())) {
                instructors.add(member);
            }
        }
        String replyToStr = getReplyTo(instructors);
        log.debug("Reply to string: " + replyToStr);

        String subject = "Assignment '" + assignmentTitle + "' Due on " + formattedDateDue;

        StringBuilder body = new StringBuilder();
        body.append("Hello ");
        body.append(getUserFirstName(submitter.getUserId()));
        body.append(",<br />");
        body.append("<br />");
        body.append("Reminder: An assignment of yours is due within 24 hours.");
        body.append("<br />");
        body.append("<ul>");
        body.append("<li> Assignment : ").append(assignment.getTitle()).append("</li>");
        body.append("<li> Due        : ").append(formattedDateDue).append("</li>");
        body.append("<li> Class      : ").append(courseName).append("</li>");
        body.append("</ul>");
        body.append("<br />");
        body.append("Have a nice day!");
        body.append("<br />");
        body.append("- ").append(getServiceName());

        log.debug("Email To: '" + toStr + "' body: " + body.toString());

        emailService.send(fromStr, toStr, subject, body.toString(), headerToStr, replyToStr, additionalHeaders);
    }

    private boolean checkEmailPreference(Member member) {
        try {
            Preferences memberPreferences = preferencesService.getPreferences(member.getUserId());
            ResourceProperties memberProps = memberPreferences.getProperties(NotificationService.PREFS_TYPE + "sakai:assignment");
            return (int) memberProps.getLongProperty("2") == 2;
        } catch (EntityPropertyNotDefinedException | EntityPropertyTypeException e) {
            // Preference not set / defined for user, ignore and send email.
            log.debug(e.getLocalizedMessage(), e);
            return true;
        }
    }

    private String getReplyTo(Set<Member> instructors) {
        StringBuilder replyTo = new StringBuilder();
        for (Member instructor : instructors) {
            String userEmail = getUserEmail(instructor.getUserId());
            String displayName = getUserDisplayName(instructor.getUserId());
            if (StringUtils.isNotEmpty(userEmail)) {
                replyTo.append("'");
                replyTo.append(displayName);
                replyTo.append("' <");
                replyTo.append(userEmail);
                replyTo.append(">, ");
            }
        }
        // Return instructors without trailing comma
        return replyTo.length() > 0 ? replyTo.substring(0, replyTo.length() - 2) : "";
    }

    // These functions should probably be moved to a SakaiProxy Implementation
    private String getUserEmail(String userId) {
        String email = null;
        try {
            email = userDirectoryService.getUser(userId).getEmail();
        } catch (UserNotDefinedException e) {
            log.warn("Cannot get email for id: " + userId + " : " + e.getClass() + " : " + e.getMessage());
        }
        return email;
    }

    private String getUserDisplayName(String userId) {
        String userDisplayName = "";
        try {
            userDisplayName = userDirectoryService.getUser(userId).getDisplayName();
        } catch (Exception e) {
            log.debug("Could not get user " + userId + e);
        }
        return userDisplayName;
    }

    private String getUserFirstName(String userId) {
        String email = "";
        try {
            email = userDirectoryService.getUser(userId).getFirstName();
        } catch (UserNotDefinedException e) {
            log.warn("Cannot get first name for id: " + userId + " : " + e.getClass() + " : " + e.getMessage());
        }
        return email;
    }

    private String getSetupRequest() {
        return serverConfigurationService.getString("setup.request", "no-reply@" + serverConfigurationService.getServerName());
    }

    private String getServiceName() {
        return serverConfigurationService.getString("ui.service", "Sakai");
    }
}
