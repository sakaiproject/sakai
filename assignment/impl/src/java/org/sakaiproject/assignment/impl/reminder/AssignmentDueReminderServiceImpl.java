/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.reminder.AssignmentDueReminderService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.messaging.api.Message;
import org.sakaiproject.messaging.api.MessageMedium;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

/**
 * Sends reminder emails to all students who have not submitted an assignment
 * which is currently open and due within the set reminder time.
 */
@Slf4j
public class AssignmentDueReminderServiceImpl implements AssignmentDueReminderService {
    @Setter private AssignmentService assignmentService;
    @Setter private EntityManager entityManager;
    @Setter private PreferencesService preferencesService;
    @Setter private ScheduledInvocationManager scheduledInvocationManager;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SessionManager sessionManager;
    @Setter private SiteService siteService;
    @Setter private UserDirectoryService userDirectoryService;
    @Resource private UserMessagingService userMessagingService;
    @Setter private UserTimeService userTimeService;

    public void init() {
        log.debug("AssignmentDueReminderService init()");
    }

    public void destroy() {
        log.debug("AssignmentDueReminderService destroy()");
    }

    public void scheduleDueDateReminder(String assignmentId) {

        // Remove any previously scheduled reminder
        removeScheduledReminder(assignmentId);

        long reminderSeconds = 60L * 60 * serverConfigurationService.getInt("assignment.reminder.hours", 24); // Convert hours to seconds

        try {
            Assignment assignment = assignmentService.getAssignment(assignmentId);
            // Only schedule due date reminders for posted assignments with due dates far enough in the future for a reminder
            if (!assignment.getDraft() && Instant.now().plusSeconds(reminderSeconds).isBefore(assignment.getDueDate())) {
                Instant reminderDate = assignment.getDueDate().minusSeconds(reminderSeconds);
                scheduledInvocationManager.createDelayedInvocation(reminderDate, "org.sakaiproject.assignment.api.reminder.AssignmentDueReminderService", assignmentId);
            }
        } catch (IdUnusedException | PermissionException e) {
            log.error("Error scheduling due date reminder email for assignmentId {}", assignmentId, e);
        }
    }

    public void removeScheduledReminder(String assignmentId) {
        scheduledInvocationManager.deleteDelayedInvocation("org.sakaiproject.assignment.api.reminder.AssignmentDueReminderService", assignmentId);
    }

    public void execute(String assignmentId) {
        log.debug("Assignment email reminder job starting");

        Session sakaiSession = sessionManager.getCurrentSession();
        sakaiSession.setUserId("admin");
        sakaiSession.setUserEid("admin");

        try {
            Assignment assignment = assignmentService.getAssignment(assignmentId);
            Site site = siteService.getSite(assignment.getContext());

            // Do not send reminders if the site is unpublished or softly deleted
            if (site.isPublished() && !site.isSoftlyDeleted()) {
                Set<Member> members = site.getMembers();
                if (Assignment.Access.GROUP.equals(assignment.getTypeOfAccess())) {
                    // Get members from assigned groups only
                    members = new HashSet<>();
                    for (String groupRef : assignment.getGroups()) {
                        String groupId = groupRef.substring(groupRef.lastIndexOf("/") + 1);
                        org.sakaiproject.site.api.Group group = site.getGroup(groupId);
                        if (group != null) {
                            members.addAll(group.getMembers());
                        }
                    }
                }

                for (Member member : members) {
                    if (member.isActive() && assignmentService.canSubmit(assignment, member.getUserId()) && !assignmentService.allowAddAssignment(assignment.getContext(), member.getUserId()) && checkEmailPreference(member)) {
                        sendEmailReminder(site, assignment, member);
                    }
                }
            }
        } catch (IdUnusedException | PermissionException e) {
            log.warn("Assignment email reminder job failed, {}", e.getMessage(), e);
        } finally {
            sakaiSession.invalidate();
        }
    }

    private String getAssignmentUrl(Assignment assignment) {

        String ref = AssignmentReferenceReckoner.reckoner()
                        .id(assignment.getId())
                        .context(assignment.getContext())
                        .subtype("a")
                        .reckon()
                        .getReference();

        Optional<String> url = entityManager.getUrl(ref, Entity.UrlType.PORTAL);

        if (url.isPresent()) {
            return url.get();
        } else {
            log.warn("Failed to get url for assignment {}", assignment.getId());
            return "";
        }
    }


    private void sendEmailReminder(Site site, Assignment assignment, Member submitter) {
        log.debug("SendEmailReminder: '{}' to {}", assignment.getTitle(), submitter.getUserDisplayId());

        Map<String, Object> replacements = new HashMap<>();

        String submitterUserId = submitter.getUserId();
        ResourceLoader rl = new ResourceLoader(submitterUserId, "assignment");

        replacements.put("url", getAssignmentUrl(assignment));
        replacements.put("title", assignment.getTitle());

        replacements.put("siteUrl", site.getUrl());
        replacements.put("siteTitle", site.getTitle());

        DateTimeFormatter dtfForBody = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.LONG)
            .withLocale(rl.getLocale())
            .withZone(userTimeService.getLocalTimeZone(submitterUserId).toZoneId());

        DateTimeFormatter dtfForSubject = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.LONG)
            .withLocale(rl.getLocale())
            .withZone(userTimeService.getLocalTimeZone(submitterUserId).toZoneId());

        Instant dueDate = assignment.getDueDate();
        String localizedDateForSubject = dtfForSubject
            .format(dueDate)
            .replaceFirst(":00 ", " "); // Get rid of the seconds as subject length is limited
        String localizedDateForBody = dtfForBody.format(dueDate);

        User user = null;
        try {
            user = userDirectoryService.getUser(submitterUserId);
        } catch (UserNotDefinedException unde) {
            log.error("No user for id {} : {}", submitterUserId, unde.toString());
            return;
        }

        Set<Member> instructors
            = site.getMembers().stream().filter(m -> StringUtils.equals(m.getRole().getId()
                                          , site.getMaintainRole())).collect(Collectors.toSet());
        String replyToStr = getReplyTo(instructors);
        log.debug("Reply to string: {}", replyToStr);

        replacements.put("subjectDate", localizedDateForSubject);

        replacements.put("firstName", user.getFirstName());
        int totalHours = serverConfigurationService.getInt("assignment.reminder.hours", 24);
        String hoursMod = (totalHours % 24 == 0) ? "." : " " + rl.getFormattedMessage("email.reminder.andhours", (totalHours % 24));
        String timeText = (totalHours < 25) ? rl.getFormattedMessage("email.reminder.hours", totalHours) : rl.getFormattedMessage("email.reminder.days", (totalHours / 24)) + hoursMod;
        replacements.put("timeText", timeText);
        replacements.put("bodyDate", localizedDateForBody);

        replacements.put("bundle", rl);

        replacements.put("replyTo", replyToStr);

        userMessagingService.message(new HashSet<User>(Arrays.asList(new User[] {user})),
            Message.builder().tool(AssignmentConstants.TOOL_ID).type("duereminder").build(),
            Arrays.asList(new MessageMedium[] {MessageMedium.EMAIL}), replacements, NotificationService.NOTI_REQUIRED);
    }

    private boolean checkEmailPreference(Member member) {
        try {
            Preferences memberPreferences = preferencesService.getPreferences(member.getUserId());
            ResourceProperties memberProps = memberPreferences.getProperties(NotificationService.PREFS_TYPE + "sakai:assignment");
            if (memberProps.getProperty("2") != null) {
                return (int) memberProps.getLongProperty("2") == 2;
            }
        } catch (EntityPropertyNotDefinedException | EntityPropertyTypeException e) {
            // Preference not set / defined for user, ignore and send email.
            log.debug(e.getLocalizedMessage(), e);
        }
        // Default to sending email if preference not set
        return true;
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
            log.warn("Cannot get email for id {} : {}", userId, e.toString());
        }
        return email;
    }
    private String getUserDisplayName(String userId) {
        String displayName = null;
        try {
            displayName = userDirectoryService.getUser(userId).getDisplayName();
        } catch (UserNotDefinedException e) {
            log.warn("Cannot get display name for id {} : {}", userId, e.toString());
        }
        return displayName;
    }

}
