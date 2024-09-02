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
import org.sakaiproject.section.api.SectionAwareness;
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
import java.time.format.FormatStyle;
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

    @Override
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

    @Override
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

    @Override
    public void execute(String publishedId) {
        log.debug("Samigo available assessment email notification job starting");

        SecurityAdvisor sa = (userId, function, reference) -> SecurityAdvisor.SecurityAdvice.ALLOWED;
        securityService.pushAdvisor(sa);

        String extensionId = "";
        if(StringUtils.contains(publishedId, ',')){ //check for any other information stuck in the publishedId besides the published assessment's ID
            String masterId = publishedId;
            publishedId = masterId.substring(0, masterId.indexOf(','));
            extensionId = masterId.substring(masterId.indexOf(',') + 1);
        }
        try {
            PublishedAssessmentFacade publishedAssessment = publishedAssessmentService.getPublishedAssessment(publishedId);

            log.debug("Samigo available assessment notification for id: {} title: {}", publishedId, publishedAssessment.getTitle());
            Site site = siteService.getSite(publishedAssessment.getOwnerSiteId());
            log.debug("Site from samigo assessment: {} site: {}", publishedAssessment.getOwnerSiteId(), site.getTitle());
            if (site.isPublished() && !site.isSoftlyDeleted() && !Objects.equals(publishedAssessment.getStatus(), AssessmentBaseIfc.DEAD_STATUS) && ifSiteHasSamigo(site)) {    // Do not send notification if the site is unpublished or softly deleted, if the assessment is deleted, or if Samigo has been removed from the site.
                log.debug("Samigo assessment site is published and is not softly deleted.");
                log.debug("Release to: {}", publishedAssessment.getAssessmentAccessControl().getReleaseTo());
                log.debug("Execution has reached the inside of the main published/not deleted/not dead/Samigo-exists block.");
                if (StringUtils.isNotBlank(extensionId)) { //first, we need to deal with the possibility that this could be an Exception email.
                    ExtendedTime extension = PersistenceService.getInstance().getExtendedTimeFacade().getEntry(extensionId);
                    if(extension != null){  //make sure the extension exists
                        if (StringUtils.isNotBlank(extension.getUser())){ //when the extension has an individual user
                            User user = userDirectoryService.getUser(extension.getUser());
                            sendEmailNotification(site, publishedAssessment, user, extension);
                        }
                        if (StringUtils.isNotBlank(extension.getGroup())){    //extension for a group
                            Set<String> groupUserUids = site.getGroup(extension.getGroup()).getUsers();
                            for(User groupUser: userDirectoryService.getUsers(groupUserUids)) {
                                sendEmailNotification(site, publishedAssessment, groupUser, extension);
                            }
                        }
                    }
                } else if (StringUtils.equals(publishedAssessment.getAssessmentAccessControl().getReleaseTo(), "Selected Groups")){ //when there is a releaseTo setting that limits the access of the test
                    for (Object groupId : publishedAssessment.getReleaseToGroups().keySet().toArray()){ //loop through applicable group IDs
                        Set<String> groupUserUids = site.getGroup((String) groupId).getUsersIsAllowed(SamigoConstants.AUTHZ_TAKE_ASSESSMENT);
                        for (User groupUser : userDirectoryService.getUsers(groupUserUids)) {
                            if (!isUserInException(publishedAssessment, groupUser.getId(), site)) {
                                log.debug("Calling send email notification: {}", groupUser.getId());
                                sendEmailNotification(site, publishedAssessment, groupUser, null);
                            }
                        }
                    }
                } else if(StringUtils.equals(publishedAssessment.getAssessmentAccessControl().getReleaseTo(), "Anonymous Users")){
                    //don't do anything when we've released to Anonymous Users.
                } else {    //if there is no special ReleaseTo setting, we can email all site members.
                    Set<String> studentUserUids = site.getUsersIsAllowed(SamigoConstants.AUTHZ_TAKE_ASSESSMENT);
                    for (User u : userDirectoryService.getUsers(studentUserUids)) {
                        log.debug("Samigo assessment open notification checking if member needs an email, member: {}", u.getId());
                        if (!isUserInException(publishedAssessment, u.getId(), site)) {   //send only to active members who aren't part of any exceptions
                            log.debug("Calling send email notification for a regular site member: {}", u.getId());
                            sendEmailNotification(site, publishedAssessment, u, null);
                        }
                    }
                }
            }
        } catch (IdUnusedException e) {
            log.warn("Samigo assessment available notification job failed, {}", e.getMessage(), e);
        } catch (UserNotDefinedException e) {
            log.warn("User not defined so catching exception and not sending email out for user", e);
        } finally {
            securityService.popAdvisor(sa);
        }
    }

    private void sendEmailNotification(Site site, PublishedAssessmentFacade publishedAssessment, User userNow, ExtendedTime extension) {   // Prep and send the email
        log.debug("SendEmailNotification: '{}' to {}", publishedAssessment.getTitle(), userNow.getDisplayId());

        String userEmail = userNow.getEmail();
        if (StringUtils.isBlank(userEmail)) {
            log.warn("Skipping notification to {} because of missing email", userNow.getEid());
            return;
        }

        // Lookup the user preferences to see if the user has disabled this specific email
        Preferences userPrefs = preferencesService.getPreferences(userNow.getId());
        ResourceProperties props = userPrefs.getProperties(NotificationService.PREFS_TYPE + SamigoConstants.NOTI_PREFS_TYPE_SAMIGO_OPEN);
        String notiStr = props != null && props.getProperty("2") != null ? props.getProperty("2") : String.valueOf(NotificationService.PREF_IMMEDIATE);
        int noti = Integer.parseInt(notiStr);
        if (noti == NotificationService.PREF_IGNORE) {
            log.debug("Skipping notification to user because of user preference override: {}", userNow.getEid());
            return;
        }

        String headerToStr = userNow.getDisplayName() + " <" + userEmail + ">";
        String fromStr = "\"" + site.getTitle() + "\" <" + getSetupRequest() + ">";

        Map<String,Object> replacementValues = new HashMap<>();
        replacementValues.put("assessmentName", publishedAssessment.getTitle());
        replacementValues.put("siteName", site.getTitle());
        if(extension != null){  //when this is for an extension, we need to do the date and times differently.
            replacementValues.put("openDate", userTimeService.dateTimeFormat(extension.getStartDate().toInstant(), null, null));
            if(extension.getDueDate() != null){
                replacementValues.put("dueDate", rl.getFormattedMessage("email.reminder.due",userTimeService.dateTimeFormat(extension.getDueDate().toInstant(), null, FormatStyle.FULL)));
            } else {
                replacementValues.put("dueDate","");
            }
            if (extension.getTimeHours()==0 && extension.getTimeMinutes()==0) {
                replacementValues.put("timeLimit", rl.getString("email.reminder.nolimit"));
            } else {
                replacementValues.put("timeLimit", rl.getFormattedMessage("email.reminder.limit", getTimeLimit(convertToSeconds(extension.getTimeHours(),extension.getTimeMinutes()))));
            }
        } else {    //normal
            replacementValues.put("openDate", userTimeService.dateTimeFormat(publishedAssessment.getStartDate().toInstant(), null, FormatStyle.FULL));
            if(publishedAssessment.getDueDate() != null){
                replacementValues.put("dueDate",rl.getFormattedMessage("email.reminder.due", userTimeService.dateTimeFormat(publishedAssessment.getDueDate().toInstant(), null, FormatStyle.FULL)));
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
        if (Objects.equals(publishedAssessment.getEvaluationModel().getScoringType(), EvaluationModelIfc.AVERAGE_SCORE)){
            score = rl.getString("email.reminder.average");
        } else if (Objects.equals(publishedAssessment.getEvaluationModel().getScoringType(), EvaluationModelIfc.LAST_SCORE)){
            score = rl.getString("email.reminder.last");
        } else if(Objects.equals(publishedAssessment.getEvaluationModel().getScoringType(), EvaluationModelIfc.ALL_SCORE)){
            score = rl.getString("email.reminder.collective");
        }
        replacementValues.put("scoreType", score);
        String feedback = " ";
        if(Objects.equals(publishedAssessment.getAssessmentFeedback().getFeedbackDelivery(), AssessmentFeedbackIfc.NO_FEEDBACK)){
            feedback = feedback + rl.getString("email.reminder.nofeedback");
        }
        replacementValues.put("feedbackType",feedback);
        replacementValues.put("siteUrl", site.getUrl());

        emailTemplateServiceSend(SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_AVAILABLE_REMINDER, null, userNow, fromStr, userEmail, headerToStr, null, replacementValues);
    }

    private String getSetupRequest() {
        return serverConfigurationService.getSmtpFrom();
    }

    private String getServiceName() {
        return serverConfigurationService.getString("ui.service", "Sakai");
    }

    private String getTimeLimit (Integer seconds){
        StringBuilder output = new StringBuilder();
        int seconds2 = seconds;
        int hours = 0;
        while (seconds2/60 > 59){
            hours = hours + 1;
            seconds2 = seconds2 - 3600;
        }
        if (hours > 0) {
            output.append(hours).append(rl.getString("email.reminder.hour"));
        }
        output.append(seconds2 / 60).append(rl.getString("email.reminder.minutes"));
        return output.toString();
    }

    private Integer convertToSeconds(int hours, int minutes){
        return (hours * 3600) + (minutes * 60);
    }

    private boolean ifSiteHasSamigo(Site site){
        log.debug("Execution has arrived at ifSiteHasSamigo.");
        return site.getToolForCommonId("sakai.samigo") != null;
    }

    private boolean isUserInException(PublishedAssessmentFacade publishedAssessment, String userId, Site site){
        log.debug("Execution has arrived at isUserInException.");
        for (ExtendedTime extension: PersistenceService.getInstance().getExtendedTimeFacade().getEntriesForPub(publishedAssessment.getData())){
            if(StringUtils.equals(extension.getUser(), userId)){    //check extension's single-user field
                return true;
            }
            if(StringUtils.isNotBlank(extension.getGroup())){ //check the extension's group
                for(Member member: site.getGroup(extension.getGroup()).getMembers()){
                    if(StringUtils.equals(member.getUserId(), userId)){
                        return true;
                    }
                }
            }
        }
        return false;   //user was not detected in any exceptions.
    }

    private String emailTemplateServiceSend(String templateName, Locale locale, User user, String from, String to, String headerTo, String replyTo, Map<String, Object> replacementValues) {
        log.debug("getting template: {}", templateName);
        RenderedTemplate template;
        try {
            if (locale == null) {
                // use user's locale
                template = emailTemplateService.getRenderedTemplateForUser(templateName, user.getReference(), replacementValues);
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
