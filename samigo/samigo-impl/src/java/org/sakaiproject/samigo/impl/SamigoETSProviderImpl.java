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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.apache.log4j.Logger;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.emailtemplateservice.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.model.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.samigo.api.SamigoETSProvider;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.user.api.*;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.email.api.DigestService;

public class SamigoETSProviderImpl implements SamigoETSProvider {
    private   static                Logger                      log                             = Logger.getLogger(SamigoETSProviderImpl.class);
    private                         Map<String,String>          constantValues                  = new HashMap<String, String>();
    private             final       String                      MULTIPART_BOUNDARY              = "======sakai-multi-part-boundary======";
    private             final       String                      BOUNDARY_LINE                   = "\n\n--"+MULTIPART_BOUNDARY+"\n";
    private             final       String                      TERMINATION_LINE                = "\n\n--"+MULTIPART_BOUNDARY+"--\n\n";
    private             final       String                      MIME_ADVISORY                   = "This message is for MIME-compliant mail readers.";
    private   static    final       String                      ADMIN                           = "admin";
    private                         String                      fromAddress                     = "";

    public      void                init                            () {
        log.info("init()");

        String samigoFromAddress                    = serverConfigurationService.getString("samigo.fromAddress");

        if( samigoFromAddress == null || StringUtils.isBlank(samigoFromAddress)){
            fromAddress                             = serverConfigurationService.getString("setup.request", "no-reply@" + serverConfigurationService.getServerName());
        } else {
            fromAddress                             = samigoFromAddress;
        }

        constantValues.put("localSakaiName" , serverConfigurationService.getString("ui.service", ""));
        constantValues.put("localSakaiUrl"  , serverConfigurationService.getPortalUrl());

        loadTemplate(SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_SUBMITTED_FILE_NAME      , SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_SUBMITTED);
        loadTemplate(SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_AUTO_SUBMITTED_FILE_NAME , SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_AUTO_SUBMITTED);
        loadTemplate(SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_TIMED_SUBMITTED_FILE_NAME, SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_TIMED_SUBMITTED);
    }

    public      void                notify                          (String eventKey, Map<String, Object> notificationValues, Event event) {
        log.debug("Notify, templateKey: " + eventKey + " event: " + event.toString());
        if          (eventKey.equals(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED)){
            handleAssessmentSubmitted(notificationValues, event);
        } else if   (eventKey.equals(SamigoConstants.EVENT_ASSESSMENT_AUTO_SUBMITTED)){
            handleAssessmentAutoSubmitted(notificationValues, event);
        } else if   (eventKey.equals(SamigoConstants.EVENT_ASSESSMENT_TIMED_SUBMITTED)){
            handleAssessmentTimedSubmitted(notificationValues, event);
        }
    }

    private     void                handleAssessmentSubmitted       (Map<String, Object> notificationValues, Event event) {
        log.debug("Assessment Submitted");
        assessmentSubmittedHelper(notificationValues, event, 1);

    }

    private     void handleAssessmentAutoSubmitted(Map<String, Object> notificationValues, Event event){
        log.debug("Assessment Auto Submitted");
        assessmentSubmittedHelper(notificationValues, event, 2);
    }

    private     void handleAssessmentTimedSubmitted(Map<String, Object> notificationValues, Event event){
        log.debug("Assessment Timed Submitted");
        assessmentSubmittedHelper(notificationValues, event, 3);
    }

    /*
     * assessmentSubmittedType is an int.
     * 1 = Normal Submission
     * 2 = Auto Submission
     * 3 = Timer expired Submission
     */
    private     void                assessmentSubmittedHelper       (Map<String, Object> notificationValues, Event event, int assessmentSubmittedType){
        log.debug("assessment Submitted helper, assessmentSubmittedType: " + assessmentSubmittedType);
        String              priStr                  = Integer.toString(event.getPriority());
        Map<String, String> replacementValues       = new HashMap<>(constantValues);
        try {
            User            user                    = userDirectoryService.getUser(notificationValues.get("userID").toString());

            PublishedAssessmentService pubAssServ   = new PublishedAssessmentService();
            PublishedAssessmentFacade  pubAssFac    = pubAssServ.getSettingsOfPublishedAssessment(notificationValues.get("publishedAssessmentID").toString());

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
             */
            replacementValues.put("siteName"            , pubAssFac.getOwnerSite());
            replacementValues.put("siteID"              , siteID);
            replacementValues.put("userName"            , user.getDisplayName());
            replacementValues.put("userDisplayID"       , user.getDisplayId());
            replacementValues.put("assessmentTitle"     , pubAssFac.getTitle());
            replacementValues.put("assessmentDueDate"   , pubAssFac.getDueDate() == null ? "" : pubAssFac.getDueDate().toString());
            replacementValues.put("assessmentGradingID" , notificationValues.get("assessmentGradingID").toString());
            replacementValues.put("submissionDate"      , notificationValues.get("submissionDate").toString());


            RenderedTemplate rt                 = getRenderedTemplate(SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_SUBMITTED , user, replacementValues);;
            // Assume assessmentSubmittedType is 1 to ensure rt is initialized
            if (assessmentSubmittedType == 2) {
                rt = getRenderedTemplate(SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_AUTO_SUBMITTED   , user, replacementValues);
            } else if (assessmentSubmittedType == 3){
                rt = getRenderedTemplate(SamigoConstants.EMAIL_TEMPLATE_ASSESSMENT_TIMED_SUBMITTED  , user, replacementValues);
            }

            String body = getBody(rt);

            notifyStudent(user, rt, body, priStr);
            notifyInstructor(siteID, pubAssFac.getInstructorNotification(), rt, body, priStr);
        } catch(UserNotDefinedException e){
            log.warn("UserNotDefined: " + notificationValues.get("userID").toString() + " in sending samigo notification.");
        }
    }

    private     void                notifyInstructor                (String siteID, Integer instructNoti , RenderedTemplate rt, String message,String priStr){
        log.debug("notifyInstructor");

        Map<User, Integer>  validUsers              = new HashMap<>();

        try{
            Site            site                    = siteService.getSite(siteID);
            AuthzGroup      azGroup                 = authzGroupService.getAuthzGroup("/site/" + siteID);
            Set<String>     siteUsersHasRole        = site.getUsersHasRole(azGroup.getMaintainRole());

            for(String userString : siteUsersHasRole){
                try{
                    if(!userString.equals(ADMIN)) {
                        User user = userDirectoryService.getUser(userString);

                        Integer uPref = getUserPreferences(user, priStr);
                        validUsers.put(user, uPref);
                    }
                } catch(UserNotDefinedException e){
                    log.warn("Instructor '" + userString +"' not found in samigo notification.");
                }
            }
        } catch(org.sakaiproject.exception.IdUnusedException e){
            //Site not found
            log.warn("Site '" + siteID + "' not found while sending instructor notifications for samigo submission.");
            log.debug(e);
        } catch(org.sakaiproject.authz.api.GroupNotDefinedException e){
            // Realm not found
            log.warn("AuthzGroup '/site/" + siteID + "' not found while sending instructor notifications for samigo submission");
            log.debug(e);
        }

        List<User>          users                   = new ArrayList<>();
        List<User>          immediateUsers          = new ArrayList<>();

        if(validUsers.size() > 0){
            users.addAll(validUsers.keySet());

            List<String>        headers         = getHeaders(rt, users, constantValues.get("localSakaiName"), fromAddress);


            for(Map.Entry<User, Integer> entry : validUsers.entrySet()){
                User user = entry.getKey();
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

    private     void                notifyStudent                   (User user, RenderedTemplate rt, String message, String priStr){
        log.debug("notifyStudent");
        List<User>          users                   = new ArrayList<>();
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

    private     int                 getUserPreferences              (User user, String priStr){
        log.debug("getUserPreferences User: " + user.getDisplayName());
        int                 uSamEmailPref           = SamigoConstants.NOTI_PREF_DEFAULT;

        Preferences         userPrefs               = preferencesService.getPreferences(user.getId());
        ResourceProperties  props                   = userPrefs.getProperties(NotificationService.PREFS_TYPE + SamigoConstants.NOTI_PREFS_TYPE_SAMIGO);

        try{
            uSamEmailPref                           = (int) props.getLongProperty(priStr);
        } catch (Exception e){
            //User hasn't changed preference
        }
        log.debug("getUserPreferences: pref=" + uSamEmailPref);
        return uSamEmailPref;
    }

    private     String              getBody                         (RenderedTemplate rt){
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

    private     RenderedTemplate    getRenderedTemplate             (String templateName, User user, Map<String,String> replacementValues){
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
    private     List<String>        getHeaders                      (RenderedTemplate rt, List<User> toAddress, String fromName, String fromEmail){
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

    // loadTemplate from ValidationLogicImpl.java
    /**
     * Load and register one or more email templates (contained in the given
     * .xml file) with the email template service
     *
     * @param templateFileName - the name of the .xml file to load
     * @param templateRegistrationString - the key (name) of the template to be saved to the service
     */
    @SuppressWarnings("unchecked")
    private     void                loadTemplate                    (String templateFileName, String templateRegistrationString) {
        log.info(this + " loading template " + templateFileName);

        SecurityAdvisor yesMan = new SecurityAdvisor() {
            public SecurityAdvice isAllowed( String userId, String function, String reference ) {
                return SecurityAdvice.ALLOWED;
            }
        };

        try {
            // Push the yesMan SA on the stack and perform the necessary actions
            securityService.pushAdvisor(yesMan);

            // Load up the resource as an input stream
            InputStream input = SamigoETSProviderImpl.class.getClassLoader().getResourceAsStream("" + templateFileName);
            if ( input == null ) {
                log.error( "Could not load resource from '" + templateFileName + "'. Skipping ..." );
            } else {
                // Parse the XML, get all the child templates
                Document document = new SAXBuilder().build( input );
                List<?> childTemplates = document.getRootElement().getChildren( "emailTemplate" );
                Iterator<?> iter = childTemplates.iterator();

                // Create and register a template with the service for each one found in the XML file
                while ( iter.hasNext() ) {
                    xmlToTemplate( (Element) iter.next(), templateRegistrationString );
                }
            }
        } catch (Exception e) {
            if( JDOMException.class.isInstance(e) || IOException.class.isInstance(e)) {
                log.error("loadTemplate error: ", e);
            }
        } finally { // Pop the yesMan SA off the stack (remove elevated permissions)
            securityService.popAdvisor(yesMan);
        }
    }

    // xmlToTemplate from ValidationLogicImpl.java
    private     void                xmlToTemplate                   (Element xmlTemplate, String key) {
        String              subject                 = xmlTemplate.getChildText("subject");
        String              body                    = xmlTemplate.getChildText("message");
        String              bodyHtml                = xmlTemplate.getChildText("messagehtml");
        String              locale                  = xmlTemplate.getChildText("locale");
        String              localeLangTag           = xmlTemplate.getChildText("localeLangTag");
        String              versionString           = xmlTemplate.getChildText("version");


        if (emailTemplateService.getEmailTemplate(key, Locale.forLanguageTag(localeLangTag)) == null)
        {
            EmailTemplate template = new EmailTemplate();
            template.setSubject(subject);
            template.setMessage(body);
            if (bodyHtml != null) {
                String decodedHtml;
                try {
                    decodedHtml = URLDecoder.decode(bodyHtml, "utf8");
                } catch (UnsupportedEncodingException e) {
                    decodedHtml = bodyHtml;
                    e.printStackTrace();
                }
                template.setHtmlMessage(decodedHtml);
            }
            template.setLocale(locale);
            template.setKey(key);
            template.setVersion(Integer.valueOf(versionString));//setVersion(versionString != null ? Integer.valueOf(versionString) : Integer.valueOf(0));	// set version
            template.setOwner("admin");
            template.setLastModified(new Date());
            try {
                this.emailTemplateService.saveTemplate(template);
                log.info(this + " user notification template " + key + " added");
            }catch (Exception e){
                log.warn("Samigo notification xmlToTemplate error." + e);
            }
        }
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
    private                         NotificationService         notificationService;

    @Setter
    private                         SiteService                 siteService;

    @Setter
    private                         SessionManager              sessionManager;

    @Setter
    private                         SecurityService             securityService;

    @Setter
    private                         AuthzGroupService           authzGroupService;
}
