/**
 * Copyright (c) 2015, The Apereo Foundation
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.internet.InternetAddress;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.emailtemplateservice.model.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.samigo.api.SamigoETSProvider;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.user.api.*;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class SamigoETSProviderImpl implements SamigoETSProvider {
    private         final   Map<String,String>  constantValues                      = new HashMap<>();
    private         final   String              MULTIPART_BOUNDARY                  = "======sakai-multi-part-boundary======";
    private         final   String              BOUNDARY_LINE                       = "\n\n--"+MULTIPART_BOUNDARY+"\n";
    private         final   String              TERMINATION_LINE                    = "\n\n--"+MULTIPART_BOUNDARY+"--\n\n";
    private         final   String              MIME_ADVISORY                       = "This message is for MIME-compliant mail readers.";
    private static  final   String              ADMIN                               = "admin";
    private                 String              fromAddress                         = "";
    private static  final   ResourceLoader      RB                                  = new ResourceLoader("EmailNotificationMessages");
    private static  final   String              CHANGE_SETTINGS_HOW_TO_INSTRUCTOR   = RB.getString("changeSetting_instructor");
    private static  final   String              CHANGE_SETTINGS_HOW_TO_STUDENT      = RB.getString("changeSetting_student");

    public      void                init                                () {
        log.info("init()");

        fromAddress = serverConfigurationService.getString("samigo.fromAddress");
        if(StringUtils.isBlank(fromAddress)){
            String defaultAddress = "no-reply@" + serverConfigurationService.getServerName();
            fromAddress = serverConfigurationService.getString("setup.request", defaultAddress);
        }

        constantValues.put("localSakaiName" , serverConfigurationService.getString("ui.service", "Sakai"));
        constantValues.put("localSakaiUrl"  , serverConfigurationService.getPortalUrl());

        // Register XML file email templates with the service
        ClassLoader cl = SamigoETSProviderImpl.class.getClassLoader();
        emailTemplateService.importTemplateFromXmlFile(cl.getResourceAsStream(SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_SUBMITTED_FILE_NAME), SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_SUBMITTED);
        emailTemplateService.importTemplateFromXmlFile(cl.getResourceAsStream(SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_AUTO_SUBMITTED_FILE_NAME), SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_AUTO_SUBMITTED);
        emailTemplateService.importTemplateFromXmlFile(cl.getResourceAsStream(SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_TIMED_SUBMITTED_FILE_NAME), SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_TIMED_SUBMITTED);
        emailTemplateService.importTemplateFromXmlFile(cl.getResourceAsStream(SamigoConstants.EMAIL_TEMPLATE_AUTO_SUBMIT_ERRORS_FILE_NAME), SamigoConstants.EMAIL_TEMPLATE_AUTO_SUBMIT_ERRORS);
    }

    public      void                notify                              (String eventKey, Map<String, Object> notificationValues, Event event) {
        log.debug("Notify, templateKey: " + eventKey + " event: " + event.toString());
        switch(eventKey){
            case SamigoConstants.EVENT_ASSESSMENT_SUBMITTED:
                handleAssessmentSubmitted(notificationValues, event);
                break;
            case SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_AUTO:
                handleAssessmentAutoSubmitted(notificationValues, event);
                break;
            case SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_TIMER_THREAD:
                handleAssessmentTimedSubmitted(notificationValues, event);
                break;
        }
    }

    public      void                notifyAutoSubmitFailures            (int count) {
        boolean notifyOn = serverConfigurationService.getBoolean(SamigoConstants.SAK_PROP_AUTO_SUBMIT_ERROR_NOTIFICATION_ENABLED, false);
        if (!notifyOn) {
            return;
        }
        String supportAddress = serverConfigurationService.getString(SamigoConstants.SAK_PROP_SUPPORT_EMAIL_ADDRESS, fromAddress);
        String toAddress = serverConfigurationService.getString(SamigoConstants.SAK_PROP_AUTO_SUBMIT_ERROR_NOTIFICATION_TO_ADDRESS, supportAddress);

        Map<String, String> replacementValues = new HashMap<>();
        replacementValues.put("failureCount", Integer.toString(count));

        try {
            RenderedTemplate template = emailTemplateService.getRenderedTemplate(SamigoConstants.EMAIL_TEMPLATE_AUTO_SUBMIT_ERRORS, Locale.getDefault(), replacementValues);
            if (template == null) {
                throw new IllegalStateException("Template is null");
            }
            InternetAddress[] to = { new InternetAddress(toAddress) };
            emailService.sendMail(new InternetAddress(fromAddress), to, template.getRenderedSubject(), template.getRenderedMessage(), null, null, null);
        }
        catch (Exception e) {
            log.error("Unable to send email notification for AutoSubmit failures.", e);
        }
    }

    private     void                handleAssessmentSubmitted           (Map<String, Object> notificationValues, Event event) {
        log.debug("Assessment Submitted");
        assessmentSubmittedHelper(notificationValues, event, 1);

    }

    private     void                handleAssessmentAutoSubmitted       (Map<String, Object> notificationValues, Event event){
        log.debug("Assessment Auto Submitted");
        assessmentSubmittedHelper(notificationValues, event, 2);
    }

    private     void                handleAssessmentTimedSubmitted      (Map<String, Object> notificationValues, Event event){
        log.debug("Assessment Timed Submitted");
        assessmentSubmittedHelper(notificationValues, event, 3);
    }

    /*
     * assessmentSubmittedType is an int.
     * 1 = Normal Submission
     * 2 = Auto Submission
     * 3 = Timer expired Submission
     */
    private     void                assessmentSubmittedHelper           (Map<String, Object> notificationValues, Event event, int assessmentSubmittedType){
        log.debug("assessment Submitted helper, assessmentSubmittedType: " + assessmentSubmittedType);
        String              priStr                  = Integer.toString(event.getPriority());
        Map<String, String> replacementValues       = new HashMap<>(constantValues);
        try {
            User            user                    = userDirectoryService.getUser(notificationValues.get("userID").toString());

            PublishedAssessmentService pubAssServ   = new PublishedAssessmentService();
            PublishedAssessmentFacade  pubAssFac    = pubAssServ.getSettingsOfPublishedAssessment(notificationValues.get("publishedAssessmentID").toString());

            // Format dates
            DateFormat dfIn                         = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            DateFormat dfIn2                        = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            DateFormat dfOut                        = DateFormat.getDateInstance(DateFormat.DEFAULT, getUserLocale(user.getId()));
            String formattedDueDate                 = (pubAssFac.getDueDate() == null) ? "" : dfOut.format(pubAssFac.getDueDate());
            Date submissionDate                     = null;
            String inSubmissionDateStr              = (notificationValues.get("submissionDate") == null) ? "" : notificationValues.get("submissionDate").toString();
            try {
               submissionDate = dfIn.parse(inSubmissionDateStr);
            } catch (java.text.ParseException e) {
               log.debug("Submission date parse exception (first format): " + e.getMessage());
            }
            if (submissionDate == null) {
               try {
                  submissionDate = dfIn2.parse(inSubmissionDateStr);
               } catch (java.text.ParseException e) {
                  log.debug("Submission date parse exception (second format): " + e.getMessage());
               }
            }
            String formattedSubmissionDate          = (submissionDate == null) ? inSubmissionDateStr : dfOut.format(submissionDate);

            String          siteID                  = pubAssFac.getOwnerSiteId();
            /*
             * siteName
             * siteID
             * userName
             * userDisplayID
             * assessmentTitle
             * assessmentDueDate
             * assessmentGradingID
             * submissionDate
             * confirmationNumber
             * releaseToGroups
             */
            replacementValues.put("siteName"            , pubAssFac.getOwnerSite());
            replacementValues.put("siteID"              , siteID);
            replacementValues.put("userName"            , user.getDisplayName());
            replacementValues.put("userDisplayID"       , user.getDisplayId());
            replacementValues.put("assessmentTitle"     , pubAssFac.getTitle());
            replacementValues.put("assessmentDueDate"   , formattedDueDate);
            replacementValues.put("assessmentGradingID" , notificationValues.get("assessmentGradingID").toString());
            replacementValues.put("submissionDate"      , formattedSubmissionDate);
            replacementValues.put("confirmationNumber"  , notificationValues.get("confirmationNumber").toString());
            
            if (notificationValues.get("releaseToGroups") != null){
            	replacementValues.put("releaseToGroups", notificationValues.get("releaseToGroups").toString());
            }            

            notifyStudent(user, priStr, assessmentSubmittedType, replacementValues);
            notifyInstructor(siteID, pubAssFac.getInstructorNotification(), assessmentSubmittedType, user, replacementValues);
        } catch(UserNotDefinedException e){
            log.warn("UserNotDefined: " + notificationValues.get("userID").toString() + " in sending samigo notification.");
        }
    }

    private     void                notifyInstructor                    (String siteID, Integer instructNoti, int assessmentSubmittedType,
                                                                            User submittingUser, Map<String, String> replacementValues){
        log.debug("notifyInstructor");
        replacementValues.put("changeSettingInstructions" , CHANGE_SETTINGS_HOW_TO_INSTRUCTOR);

        List<User>  validUsers                      = new ArrayList<>();
        RenderedTemplate    rt                      = getRenderedTemplateBySubmissionType(assessmentSubmittedType, submittingUser, replacementValues);
        String              message                 = getBody(rt);

        try{
            Site            site                    = siteService.getSite(siteID);
            Set<String>     siteUsersHasRole        = new HashSet<>();
            
            if (replacementValues.get("releaseToGroups") != null){
            	siteUsersHasRole = extractInstructorsFromGroups(site,replacementValues.get("releaseToGroups") );
            }

            if (siteUsersHasRole.isEmpty()) {
            	AuthzGroup azGroup = authzGroupService.getAuthzGroup("/site/" + siteID);
            	siteUsersHasRole = site.getUsersHasRole(azGroup.getMaintainRole());
            }

            for(String userString : siteUsersHasRole){
                try{
                    if(!userString.equals(ADMIN)) {
                        User user = userDirectoryService.getUser(userString);
                        validUsers.add(user);
                    }
                } catch(UserNotDefinedException e){
                    log.warn("Instructor '" + userString +"' not found in samigo notification.");
                }
            }
        } catch(org.sakaiproject.exception.IdUnusedException e){
            //Site not found
            log.warn("Site '{}' not found while sending instructor notifications for samigo submission.", siteID);
            log.debug(e.getMessage(), e);
        } catch(org.sakaiproject.authz.api.GroupNotDefinedException e){
            // Realm not found
            log.warn("AuthzGroup '/site/{}' not found while sending instructor notifications for samigo submission", siteID);
            log.debug(e.getMessage(), e);
        }

        List<User>          immediateUsers          = new ArrayList<>();

        if(validUsers.size() > 0){
            List<String>        headers         = getHeaders(rt, validUsers, constantValues.get("localSakaiName"), fromAddress);

            for(User user : validUsers){
                if(instructNoti == NotificationService.PREF_IMMEDIATE){
                    immediateUsers.add(user);
                } else if(instructNoti == NotificationService.PREF_DIGEST){
                    log.debug("notifyInstructor + sendDigest + User: " + user.getDisplayName() + " rt: " + rt.getKey());
                    digestService.digest(user.getId(), rt.getRenderedSubject(), rt.getRenderedMessage());
                }
            }

            if(instructNoti == NotificationService.PREF_IMMEDIATE && !immediateUsers.isEmpty()) {
                log.debug("notifyInstructor + send one email to Users: " + immediateUsers.toString() +" rt: " + rt.getKey());
                emailService.sendToUsers(immediateUsers, headers, message);
            }
        }
    }
    
    private Set<String> extractInstructorsFromGroups(Site site,String allGroups){
    	
    	Set<String> usersWithRole = new HashSet<String>();
    	
    	List<String> groups = Stream.of(allGroups)
				.map(s -> s.split(";")).flatMap(Arrays::stream)
				.collect(Collectors.toList());
    	
    	for (String groupId : groups){
    		Group group = site.getGroup(groupId);
    		Set <String> groupUsersWithRole = group.getUsersHasRole(group.getMaintainRole());
    		usersWithRole.addAll(groupUsersWithRole);
    	}
    	
    	return usersWithRole;
    }

    private     void                notifyStudent                       (User user, String priStr, int assessmentSubmittedType,
                                                                            Map<String, String> replacementValues){
        log.debug("notifyStudent");
        replacementValues.put( "changeSettingInstructions" , CHANGE_SETTINGS_HOW_TO_STUDENT );

        List<User>          users                   = new ArrayList<>();
        RenderedTemplate    rt                      = getRenderedTemplateBySubmissionType( assessmentSubmittedType, user, replacementValues );
        String              message                 = getBody( rt );
        users.add(user);

        List<String>        headers                 = getHeaders(rt, users, constantValues.get("localSakaiName"), fromAddress);
        int                 uSamEmailPref           = getUserPreferences(user, priStr);

        if(uSamEmailPref == NotificationService.PREF_IMMEDIATE){
            log.debug("notifyStudent + send one email + rt: " + rt.getKey());
            emailService.sendToUsers(users, headers, message);
        } else if (uSamEmailPref == NotificationService.PREF_DIGEST){
            log.debug("notifyStudent + sendDigest + rt: " + rt.getKey());
            digestService.digest(user.getId(), rt.getRenderedSubject(), rt.getRenderedMessage());
        }
    }

    private     int                 getUserPreferences                  (User user, String priStr){
        log.debug("getUserPreferences User: " + user.getDisplayName());
        int                 uSamEmailPref           = SamigoConstants.NOTI_PREF_DEFAULT;

        Preferences         userPrefs               = preferencesService.getPreferences(user.getId());
        ResourceProperties  props                   = userPrefs.getProperties(NotificationService.PREFS_TYPE + SamigoConstants.NOTI_PREFS_TYPE_SAMIGO);

        try{
            uSamEmailPref                           = (int) props.getLongProperty(priStr);
        } catch (EntityPropertyNotDefinedException | EntityPropertyTypeException e){
            //User hasn't changed preference
        }
        log.debug("getUserPreferences: pref=" + uSamEmailPref);
        return uSamEmailPref;
    }

    private     String              getBody                             (RenderedTemplate rt){
        log.debug("getBody");
        StringBuilder       body                = new StringBuilder();
        body.append(MIME_ADVISORY);

        if (rt.getRenderedMessage() != null) {
            body.append(BOUNDARY_LINE);
            body.append("Content-Type: text/plain; charset=UTF-8\n");
            body.append(rt.getRenderedMessage());
        }
        if (rt.getRenderedHtmlMessage() != null) {
            //append the HMTL part
            body.append(BOUNDARY_LINE);
            body.append("Content-Type: text/html; charset=UTF-8\n");
            body.append(rt.getRenderedHtmlMessage());
        }

        body.append(TERMINATION_LINE);

        return body.toString();
    }

    private     RenderedTemplate    getRenderedTemplateBySubmissionType (int assessmentSubmittedType, User user, Map<String, String> replacementValues){
        RenderedTemplate template;
        switch(assessmentSubmittedType){
            case 2:
                template = getRenderedTemplate(SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_AUTO_SUBMITTED, user, replacementValues);
                break;
            case 3:
                template = getRenderedTemplate(SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_TIMED_SUBMITTED, user, replacementValues);
                break;
            default:
                template = getRenderedTemplate(SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_SUBMITTED, user, replacementValues);
                break;
        }

        return template;
    }

    private     RenderedTemplate    getRenderedTemplate                 (String templateName, User user, Map<String,String> replacementValues){
        log.debug("getting template: " + templateName);
        RenderedTemplate    template            = null;

        try {
            template = emailTemplateService.getRenderedTemplateForUser(templateName, user!=null?user.getReference():"", replacementValues);
        }catch (Exception e) {
            log.warn("Samigo Notification email template error. " + this + e.getMessage());
        }

        return template;
    }

    // Based on EmailTemplateService.sendRenderedMessages()
    private     List<String>        getHeaders                          (RenderedTemplate rt, List<User> toAddress, String fromName, String fromEmail){
        log.debug("getHeaders");
        List<String>        headers             = new ArrayList<>();
        //the template may specify a from address
        if (StringUtils.isNotBlank(rt.getFrom())) {
            headers.add("From: \"" + rt.getFrom() );
        } else {
            headers.add("From: \"" + fromName + "\" <" + fromEmail + ">" );
        }
        // Add a To: header of either the recipient (if only 1), or the sender (if multiple)
        String              toName                  = fromName;
        String              toEmail                 = fromEmail;

        if (toAddress.size() == 1) {
            User u = toAddress.get(0);
            toName = u.getDisplayName();
            toEmail = u.getEmail();
        }

        headers.add("To: \"" + toName + "\" <" + toEmail + ">" );

        //SAK-21742 we need the rendered subject
        headers.add("Subject: " + rt.getRenderedSubject());
        headers.add("Content-Type: multipart/alternative; boundary=\"" + MULTIPART_BOUNDARY + "\"");
        headers.add("Mime-Version: 1.0");
        headers.add("Return-Path: <>");
        headers.add("Auto-Submitted: auto-generated");

        return headers;
    }

    private     Locale              getUserLocale                       (final String userId) {
        Locale              locale                  = preferencesService.getLocale(userId);

        if(locale == null) {
            locale                                  = Locale.getDefault();
        }

        return locale;
    }

    @Setter
    private                         ServerConfigurationService  serverConfigurationService;

    @Setter
    private                         UserDirectoryService        userDirectoryService;

    @Setter
    private                         EmailTemplateService        emailTemplateService;

    @Setter
    private                         PreferencesService          preferencesService;

    @Setter
    private                         EmailService                emailService;

    @Setter
    private                         DigestService               digestService;

    @Setter
    private                         SiteService                 siteService;

    @Setter
    private                         AuthzGroupService           authzGroupService;
}
