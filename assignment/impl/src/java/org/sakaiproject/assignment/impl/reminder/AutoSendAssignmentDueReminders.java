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
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.persistence.AssignmentRepository;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * Sends reminder emails to all students who have not submitted an assignment
 * which is currently open and due within 24 hours.
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
@Slf4j
public class AutoSendAssignmentDueReminders {
    private List<String> additionalHeaders = new ArrayList<>();

    public void init() {
        log.debug("AutoSendAssignmentDueReminders init()");

        String sender = "Sender: \"" + getServiceName() + "\" <" + getSetupRequest() + ">";
        additionalHeaders.add(sender);
        additionalHeaders.add("Content-type: text/html; charset=UTF-8");
    }

    public void destroy() {
        log.debug("AutoSendAssignmentDueReminders destroy()");
    }

    public void execute() {
        log.debug("AutoSendRemindersDue execute");

        List<Assignment> assignments = repository.findAssignmentsThatNeedReminding();

        if(assignments == null) {
            return;
        }

        Set<String> contexts = extractUniqueContexts(assignments);

        if(log.isDebugEnabled()) {
            log.debug("Before loop. assignments size " + assignments.size());
            for (Assignment assignment : assignments) {
                log.debug("Assignment dueDate: " + assignment.getDueDate() + " EmailSent? " + assignment.getReminderEmailSent());
                for (AssignmentSubmission submission : assignment.getSubmissions()) {
                    log.debug("Submission: " + submission.getId() + " draft: " + submission.getSubmitted());
                }
            }
        }

        for(String context : contexts) {
            try {
                Site site = siteService.getSite(context);
                if(site.isPublished() && !site.isSoftlyDeleted()) {
                    for (Assignment assignment : assignments) {
                        if (assignment.getContext().equals(context)) {
                            handleEmailRemindersForAssignment(assignment, site, site.getMaintainRole());
                            assignment.setReminderEmailSent(Boolean.TRUE);
                            assignment.setReminderEmailDate(Instant.now());
                            repository.update(assignment);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error trying to find members for site ..?, ", e);
            }
        }


    }

    private void handleEmailRemindersForAssignment(Assignment assignment, Site site, String maintainRole){
        log.debug("handleEmailRemindersForAssignment()");
        Set<Member> membersThatNeedReminding = new HashSet<>();

        if (assignment.getTypeOfAccess() == Assignment.Access.SITE) {
            if (Objects.equals(assignment.getIsGroup(), Boolean.FALSE)) {
                // Individual assignment released to the entire site
                for (Member member : site.getMembers()) {
                    if (!StringUtils.equals(member.getRole().getId(), maintainRole)) {
                        AssignmentSubmission submission = repository.findSubmissionForUser(assignment.getId(), member.getUserId());
                        if (submission != null && !submission.getSubmitted()) {
                            membersThatNeedReminding.add(member);
                        }
                    }
                }
            } else {
                // Group assignment released to the entire site. Ignore this odd case.
                log.warn("A group assignment released to the entire site was skipped for reminder emails. Assignment ID: " + assignment.getId());
            }
        } else {
            // Group access to assignment.
            Set<String> groups = assignment.getGroups();
            for (String group : groups) {
                if (Objects.equals(assignment.getIsGroup(), Boolean.FALSE)) {
                    // Individual assignment released to groups. Send email to all members with no submission.
                    try {
                        Set<Member> groupMembers = authzGroupService.getAuthzGroup(group).getMembers();
                        if (groupMembers != null) {
                            for (Member member : groupMembers) {
                                if (!StringUtils.equals(member.getRole().getId(), maintainRole)) {
                                    AssignmentSubmission submission = repository.findSubmissionForUser(assignment.getId(), member.getUserId());
                                    if (submission != null && !submission.getSubmitted()) {
                                        membersThatNeedReminding.add(member);
                                    }
                                }
                            }
                        }
                    } catch (GroupNotDefinedException e) {
                        log.warn("Failed to get group membership for assignment reminders.", e);
                    }
                } else {
                    // Group assignment released to groups. Send email to all members in groups with no submission.
                    AssignmentSubmission submission = repository.findSubmissionForGroup(assignment.getId(), group);
                    if (submission != null && !submission.getSubmitted()) {
                        try {
                            Set<Member> groupMembers = authzGroupService.getAuthzGroup(group).getMembers();
                            if (groupMembers != null) {
                                for (Member member : groupMembers) {
                                    if (!StringUtils.equals(member.getRole().getId(), maintainRole)) {
                                        membersThatNeedReminding.add(member);
                                    }
                                }
                            }
                        } catch (GroupNotDefinedException e) {
                            log.warn("Failed to get group membership for assignment reminders.", e);
                        }
                    }
                }
            }
        }

        // Send all emails
        for (Member member : membersThatNeedReminding) {
            if (checkEmailPreference(member)) {
                sendEmailReminder(site, assignment, member);
            }
        }

    }

    private void sendEmailReminder(Site site, Assignment assignment, Member submitter) {
        log.debug("SendEmailReminder: '" + assignment.getTitle() + "' to " + submitter.getUserDisplayId());

        String assignmentTitle = assignment.getTitle();
        if(assignment.getTitle().length() > 11){
            assignmentTitle = assignment.getTitle().substring(0, 11) + "[...]";
        }

        String courseName = site.getTitle();

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm aa");
        Instant dueDate = assignment.getDueDate();
        String formattedDateDue = sdf.format(dueDate);

        String toStr = getUserEmail(submitter.getUserId());
        if(StringUtils.isEmpty(toStr)) {
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
        boolean doSendEmail = false;

        String memberID = member.getUserId();
        String memberName = getUserDisplayName(memberID);

        Preferences memberPreferences = preferencesService.getPreferences(memberID);
        ResourceProperties memberProps = memberPreferences.getProperties(NotificationService.PREFS_TYPE + "sakai:assignment");
        int memberEmailPreference = 2;

        try{
            memberEmailPreference = (int) memberProps.getLongProperty("2");
        } catch (EntityPropertyNotDefinedException ignore){
            // Preference not set / defined for user, ignore. default is 2 == send email
            log.debug("Could not retrieve member, "+ memberID + " email preference.", ignore);
        } catch (Exception e) {
            log.warn("Could not retrieve "+ memberName + " email preference.");
        }

        if(memberEmailPreference == 2) {
            doSendEmail = true;
        }

        return doSendEmail;
    }

    private Set<String> extractUniqueContexts(List<Assignment> assignments) {
        Set<String> contexts = new HashSet<>();
        for(Assignment item : assignments){
            contexts.add(item.getContext());
        }

        return contexts;
    }

    private String getReplyTo(Set<Member> instructors) {
        StringBuilder replyTo = new StringBuilder();
        for (Member instructor : instructors) {
            String userEmail = getUserEmail(instructor.getUserId());
            String displayName = getUserDisplayName(instructor.getUserId());
            if(StringUtils.isNotEmpty(userEmail)) {
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


    @Setter
    private EmailService emailService;

    @Setter
    private AssignmentRepository repository;

    @Setter
    private SiteService siteService;

    @Setter
    private UserDirectoryService userDirectoryService;

    @Setter
    private AuthzGroupService authzGroupService;

    @Setter
    private PreferencesService preferencesService;

    @Setter
    private ServerConfigurationService serverConfigurationService;
}
