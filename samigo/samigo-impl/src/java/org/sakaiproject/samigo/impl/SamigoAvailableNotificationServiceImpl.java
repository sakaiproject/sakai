/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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
package org.sakaiproject.samigo.impl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.api.RenderedTemplate;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.samigo.api.SamigoAvailableNotificationService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class SamigoAvailableNotificationServiceImpl implements SamigoAvailableNotificationService {

    @Setter private EmailService emailService;
    @Setter private EmailTemplateService emailTemplateService;
    @Setter private PreferencesService preferencesService;
    @Setter private ScheduledInvocationManager scheduledInvocationManager;
    @Setter private SecurityService securityService;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SiteService siteService;
    @Setter private UserDirectoryService userDirectoryService;
    @Setter private UserTimeService userTimeService;
    private PublishedAssessmentService publishedAssessmentService;
    private static final ResourceLoader rl = new ResourceLoader("SamigoAvailableNotificationMessages");
    private final List<String> additionalHeaders = new ArrayList<>();

    public void init() {
        log.debug("SamigoAvailableNotificationService init()");

        publishedAssessmentService = new PublishedAssessmentService();

        String sender = "Sender: \"" + getServiceName() + "\" <" + getSetupRequest() + ">";
        additionalHeaders.add(sender);
        additionalHeaders.add("Content-type: text/html; charset=UTF-8");
    }

    public void scheduleAssessmentAvailableNotification(String publishedId) {

        // Remove any previously scheduled notification
        removeScheduledAssessmentNotification(publishedId);

        try {
            PublishedAssessmentFacade publishedAssessment = publishedAssessmentService.getPublishedAssessment(publishedId);
            List<ExtendedTime> extensionContainer = PersistenceService.getInstance().getExtendedTimeFacade().getEntriesForPub(publishedAssessment.getData());
            Date startDate = publishedAssessment.getStartDate();
            // Only schedule a new notification if start date is in the future to prevent duplicates and spam
            if (Instant.now().isBefore(startDate.toInstant())) {    //for main
                scheduledInvocationManager.createDelayedInvocation(startDate.toInstant(), "org.sakaiproject.samigo.api.SamigoAvailableNotificationService", publishedId);
            }
            for (ExtendedTime extension: extensionContainer){  //make separate delayedInvocations for people with exceptions.
                if (Instant.now().isBefore(extension.getStartDate().toInstant())) {
                    scheduledInvocationManager.createDelayedInvocation(extension.getStartDate().toInstant(), "org.sakaiproject.samigo.api.SamigoAvailableNotificationService", publishedId + ',' + extension.getId().toString());
                }
            }
        } catch (IllegalArgumentException e) {
            log.error("Unable to schedule assessment open notification for assessment {}", publishedId);
        }
    }

    public void removeScheduledAssessmentNotification(String publishedId) {
        scheduledInvocationManager.deleteDelayedInvocation("org.sakaiproject.samigo.api.SamigoAvailableNotificationService", publishedId);  //remove main reminder
        PublishedAssessmentFacade publishedAssessment = publishedAssessmentService.getPublishedAssessment(publishedId);
        if (publishedAssessment != null){
            List<ExtendedTime> extensionContainer = PersistenceService.getInstance().getExtendedTimeFacade().getEntriesForPub(publishedAssessment.getData());
            for (ExtendedTime extension: extensionContainer){  //remove all the assessment's extension reminders.
                scheduledInvocationManager.deleteDelayedInvocation("org.sakaiproject.samigo.api.SamigoAvailableNotificationService", publishedId + ',' + extension.getId().toString());
            }
        }
    }

    public void execute(String publishedId) {
        log.debug("Samigo available assessment email notification job starting");

        SecurityAdvisor sa = (userId, function, reference) -> SecurityAdvisor.SecurityAdvice.ALLOWED;
        securityService.pushAdvisor(sa);

        String extensionId = "";
        if(StringUtils.contains(publishedId, ',')){ //check for any other information stuck in the publishedId besides the published assessment's ID
            String masterId = new String(publishedId);
            publishedId = masterId.substring(0, masterId.indexOf(','));
            extensionId = masterId.substring(masterId.indexOf(',') + 1);
        }
        try {
            PublishedAssessmentFacade publishedAssessment = publishedAssessmentService.getPublishedAssessment(publishedId);

            log.debug("Samigo available assessment notification for id: {} title: {}", publishedId, publishedAssessment.getTitle());
            Site site = siteService.getSite(publishedAssessment.getOwnerSiteId());
            log.debug("Site from samigo assessment: {} site: {}", publishedAssessment.getOwnerSiteId(), site.getTitle());
            if (site.isPublished() && !site.isSoftlyDeleted() && publishedAssessment.getStatus()!=AssessmentBaseIfc.DEAD_STATUS && ifSiteHasSamigo(site)) {    // Do not send notification if the site is unpublished or softly deleted, if the assessment is deleted, or if Samigo has been removed from the site.
                log.debug("Samigo assessment site is published and is not softly deleted.");
                log.debug("Release to: {}", publishedAssessment.getAssessmentAccessControl().getReleaseTo());
                log.debug("Execution has reached the inside of the main published/not deleted/not dead/Samigo-exists block.");
                if (!StringUtils.isEmpty(extensionId)){ //first, we need to deal with the possibility that this could be an Exception email.
                    ExtendedTime extension = PersistenceService.getInstance().getExtendedTimeFacade().getEntry(extensionId);
                    if(extension != null){  //make sure the extension exists
                        if (!StringUtils.isEmpty(extension.getUser())){ //when the extension has an individual user
                            sendEmailNotification(site, publishedAssessment, site.getMember(extension.getUser()), extension);
                        }
                        if (!StringUtils.isEmpty(extension.getGroup())){    //extension for a group
                            for(Member member: site.getGroup(extension.getGroup()).getMembers()){
                                sendEmailNotification(site, publishedAssessment, member, extension);
                            }
                        }
                    }
                } else if (StringUtils.equals(publishedAssessment.getAssessmentAccessControl().getReleaseTo(), "Selected Groups")){ //when there is a releaseTo setting that limits the access of the test
                    for (Object groupId : publishedAssessment.getReleaseToGroups().keySet().toArray()){ //loop through applicable group IDs
                        for (Member member : site.getGroup((String) groupId).getMembers()){  //loop through users of applicable group.
                            if (member.isActive() && !isUserInException(publishedAssessment, member.getUserId(), site)) {
                                log.debug("Calling send email notification: {}", member.getUserId());
                                sendEmailNotification(site, publishedAssessment, member, null);
                            }
                        }
                    }
                } else if(StringUtils.equals(publishedAssessment.getAssessmentAccessControl().getReleaseTo(), "Anonymous Users")){
                    //don't do anything when we've released to Anonymous Users.
                } else {    //if there is no special ReleaseTo setting, we can email all site members.
                    for (Member member : site.getMembers()) {
                        log.debug("Samigo assessment open notification checking if member needs an email, member: {}", member.getUserId());
                        log.debug("Member info: active? {}", member.isActive());
                        log.debug("Member info: released to this member? TODO: THIS");
                        if (member.isActive() && !isUserInException(publishedAssessment, member.getUserId(), site)) {   //send only to active members who aren't part of any exceptions
                            log.debug("Calling send email notification: {}", member.getUserId());
                            sendEmailNotification(site, publishedAssessment, member, null);
                        }
                    }
                }
            }
        } catch (IdUnusedException e) {
            log.warn("Samigo assessment available notification job failed, {}", e.getMessage(), e);
        } finally {
            securityService.popAdvisor(sa);
        }
    }

    private void sendEmailNotification(Site site, PublishedAssessmentFacade publishedAssessment, Member member, ExtendedTime extension) {   // Prep and send the email
        log.debug("SendEmailNotification: '{}' to {}", publishedAssessment.getTitle(), member.getUserDisplayId());
        log.debug("Test {}", rl.getString("available_class_at"));
        User userNow = null;
        try {
            userNow = userDirectoryService.getUser(member.getUserId());
        } catch (UserNotDefinedException u){
            log.debug("UserNotDefined exception for user ID {} , no email sent",member.getUserId());
            return;
        }
        String toStr = getUserEmail(member.getUserId());    //begin making the headers
        String headerToStr = getUserDisplayName(member.getUserId()) + " <" + toStr + ">";
        String fromStr = "\"" + site.getTitle() + "\" <" + getSetupRequest() + ">";
        Set<Member> instructors = new HashSet<>();
        for (Member person : site.getMembers()) {
            if (StringUtils.equals(person.getRole().getId(), site.getMaintainRole())) {
                instructors.add(person);
            }
        }
        String replyToStr = getReplyTo(instructors);
        Map<String,Object> replacementValues = new HashMap<>();
        replacementValues.put("assessmentName", publishedAssessment.getTitle());
        replacementValues.put("siteName", site.getTitle());
        if(extension != null){  //when this is for an extension, we need to do the date and times differently.
            replacementValues.put("openDate", prettyDate(extension.getStartDate().toInstant()));
            if(extension.getDueDate() != null){
                replacementValues.put("dueDate", rl.getFormattedMessage("email.reminder.due",prettyDate(extension.getDueDate().toInstant())));
            } else {
                replacementValues.put("dueDate","");
            }
            if (extension.getTimeHours()==0 && extension.getTimeMinutes()==0) {
                replacementValues.put("timeLimit", rl.getString("email.reminder.nolimit"));
            } else {
                replacementValues.put("timeLimit", rl.getFormattedMessage("email.reminder.limit", getTimeLimit(convertToSeconds(extension.getTimeHours(),extension.getTimeMinutes()))));
            }
        } else {    //normal
            replacementValues.put("openDate", prettyDate(publishedAssessment.getStartDate().toInstant()));
            if(publishedAssessment.getDueDate() != null){
                replacementValues.put("dueDate",rl.getFormattedMessage("email.reminder.due", prettyDate(publishedAssessment.getDueDate().toInstant())));
            } else {
                replacementValues.put("dueDate","");
            }
            if (publishedAssessment.getTimeLimit() == 0) {
                replacementValues.put("timeLimit", rl.getString("email.reminder.nolimit"));
            } else {
                replacementValues.put("timeLimit", getTimeLimit(publishedAssessment.getTimeLimit()));
            }
        }
        String times = rl.getString("email.reminder.unlimitedtimes");
        if (!publishedAssessment.getAssessmentAccessControl().getUnlimitedSubmissions()){
            times = publishedAssessment.getAssessmentAccessControl().getSubmissionsAllowed().toString();
        }
        replacementValues.put("numOfAttempts", times);
        String score = rl.getString("email.reminder.highest");
        if (publishedAssessment.getEvaluationModel().getScoringType() == EvaluationModelIfc.AVERAGE_SCORE){
            score = rl.getString("email.reminder.average");
        } else if (publishedAssessment.getEvaluationModel().getScoringType() == EvaluationModelIfc.LAST_SCORE){
            score = rl.getString("email.reminder.last");
        } else if(publishedAssessment.getEvaluationModel().getScoringType() == EvaluationModelIfc.ALL_SCORE){
            score = rl.getString("email.reminder.collective");
        }
        replacementValues.put("scoreType", score);
        String feedback = " ";
        if(publishedAssessment.getAssessmentFeedback().getFeedbackDelivery() == AssessmentFeedbackIfc.NO_FEEDBACK){
            feedback = feedback + rl.getString("email.reminder.nofeedback");
        }
        replacementValues.put("feedbackType",feedback);
        replacementValues.put("siteUrl", site.getUrl());

        emailTemplateServiceSend(SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_AVAILABLE_REMINDER, null, userNow, fromStr, toStr, headerToStr, replyToStr, replacementValues);
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

    private String getUserEmail(String userId) {
        String email = null;
        try {
            email = userDirectoryService.getUser(userId).getEmail();
        } catch (UserNotDefinedException e) {
            log.warn("Cannot get email for id: {} : {} : {}", userId, e.getClass(), e.getMessage());
        }
        return email;
    }

    private String getUserDisplayName(String userId) {
        String userDisplayName = "";
        try {
            userDisplayName = userDirectoryService.getUser(userId).getDisplayName();
        } catch (Exception e) {
            log.debug("Could not get user {}", userId + e);
        }
        return userDisplayName;
    }

    private String getUserFirstName(String userId) {
        String email = "";
        try {
            email = userDirectoryService.getUser(userId).getFirstName();
        } catch (UserNotDefinedException e) {
            log.warn("Cannot get first name for id: {} : {} : {}", userId, e.getClass(), e.getMessage());
        }
        return email;
    }

    private String getSetupRequest() {
        return serverConfigurationService.getSmtpFrom();
    }

    private String getServiceName() {
        return serverConfigurationService.getString("ui.service", "Sakai");
    }

    private String getTimeLimit (Integer seconds){
        StringBuilder output = new StringBuilder();
        int seconds2 = seconds.intValue();
        int hours = 0;
        while (seconds2/60 > 59){
            hours = hours + 1;
            seconds2 = seconds2 - 3600;
        }
        if (hours > 0) {
            output.append(hours + rl.getString("email.reminder.hour"));
        }
        output.append(seconds2/60 + rl.getString("email.reminder.minutes"));
        return output.toString();
    }

    private Integer convertToSeconds(int hours, int minutes){
        return (hours * 3600) + (minutes * 60);
    }

    private boolean ifSiteHasSamigo(Site site){
        log.debug("Execution has arrived at ifSiteHasSamigo.");
        return site.getToolForCommonId("sakai.samigo") != null ? true : false;
    }

    private boolean isUserInException(PublishedAssessmentFacade publishedAssessment, String userId, Site site){
        log.debug("Execution has arrived at isUserInException.");
        for (ExtendedTime extension: PersistenceService.getInstance().getExtendedTimeFacade().getEntriesForPub(publishedAssessment.getData())){
            if(StringUtils.equals(extension.getUser(), userId)){    //check extension's single-user field
                return true;
            }
            if(!StringUtils.isBlank(extension.getGroup())){ //check the extension's group
                for(Member member: site.getGroup(extension.getGroup()).getMembers()){
                    if(StringUtils.equals(member.getUserId(), userId)){
                        return true;
                    }
                }
            }
        }
        return false;   //user was not detected in any exceptions.
    }

    private String prettyDate(Instant date){
        String fullDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(date);
        String time = fullDate.substring(fullDate.indexOf(' ') + 1);
        Integer hour = Integer.parseInt(time.substring(0,2));
        String noon = rl.getString("email.reminder.am");
        if (hour > 11){
            noon = rl.getString("email.reminder.pm");
            if (hour > 12){
                hour = hour - 12;
                if (hour < 10){
                    time = time.replace(time.substring(0,2), '0' + hour.toString());
                } else {
                    time = time.replace(time.substring(0,2), hour.toString());
                }
            }
        }
        time = time + ' ' + noon;
        return fullDate.substring(0, fullDate.indexOf(' ')) + ' ' + time;
    }

    private String emailTemplateServiceSend(String templateName, Locale locale, User user, String from, String to, String headerTo, String replyTo, Map<String, Object> replacementValues) {
        log.debug("getting template: {}", templateName);
        RenderedTemplate template;
        try {
            if (locale == null) {
                // use user's locale
                template = emailTemplateService.getRenderedTemplateForUser(templateName, user!=null?user.getReference():"", replacementValues);
            } else {
                // use local
                template = emailTemplateService.getRenderedTemplate(templateName, locale, replacementValues);
            }
            if (template != null) {
                List<String> headers = new ArrayList<>();
                headers.add("Precedence: bulk");
                String content = template.getRenderedMessage();
                emailService.send(from, to, template.getRenderedSubject(), content, headerTo, replyTo, headers);
                return content;
            }
        } catch (Exception e) {
            log.warn(this + e.getMessage());
            return null;
        }
        return null;
    }
}