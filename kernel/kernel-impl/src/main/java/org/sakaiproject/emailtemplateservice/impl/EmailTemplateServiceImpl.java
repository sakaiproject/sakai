/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2006, 2007 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.emailtemplateservice.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import org.simpleframework.xml.core.Persister;
//import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import org.hibernate.HibernateException;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateLocaleUsers;
import org.sakaiproject.emailtemplateservice.api.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.api.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.api.repository.EmailTemplateRepository;
import org.sakaiproject.emailtemplateservice.util.TextTemplateLogicUtils;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.Setter;

@Slf4j
@Setter
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final String MULTIPART_BOUNDARY = "======sakai-multi-part-boundary======";
    private final String BOUNDARY_LINE = "\n\n--"+MULTIPART_BOUNDARY+"\n";
    private final String TERMINATION_LINE = "\n\n--"+MULTIPART_BOUNDARY+"--\n\n";
    private final String MIME_ADVISORY = "This message is for MIME-compliant mail readers.";

    private EmailService emailService;
    private EmailTemplateRepository repository;
    private PreferencesService preferencesService;
    private ServerConfigurationService serverConfigurationService;
    private SessionManager sessionManager;
    private SecurityService securityService;
    private UserDirectoryService userDirectoryService;

    public EmailTemplate getEmailTemplateById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("id cannot be null or empty");
        }
        return repository.findById(id).orElse(null);
    }

    private Optional<EmailTemplate> getEmailTemplateNoDefault(String key, Locale locale) {

        log.debug("getEmailTemplateNoDefault({}, {})", key, locale);
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("key cannot be null or empty");
        }

        String localeString = locale != null ? locale.toString() : EmailTemplate.DEFAULT_LOCALE;
        return repository.findByKeyAndLocale(key, localeString);
    }

    public EmailTemplate getEmailTemplate(String key, Locale locale) {

        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("key cannot be null or empty");
        }

        Locale finalLocale = locale == null ? Locale.getDefault() : locale;

        log.debug("getEmailTemplate(key={}, locale={}", key, finalLocale);

        Optional<EmailTemplate> optionalET = Optional.empty();

        if (locale != null) {
            optionalET = repository.findByKeyAndLocale(key, finalLocale.toString());
            if (!optionalET.isPresent()) {
                optionalET = repository.findByKeyAndLocale(key, finalLocale.getLanguage());
            }
        }
        if (!optionalET.isPresent()) {
            optionalET = repository.findByKeyAndLocale(key, EmailTemplate.DEFAULT_LOCALE);
        }

        if (!optionalET.isPresent()) {
            log.warn("no template found for: {} in locale {}", key, finalLocale);
        }

        return optionalET.orElse(null);
    }

    public boolean templateExists(String key, Locale locale) {

        String localeString = locale == null ? EmailTemplate.DEFAULT_LOCALE: locale.toString();
        return repository.findByKeyAndLocale(key, localeString).isPresent();
    }

    public List<EmailTemplate> getEmailTemplates(int max, int start) {

        if (max > 0) {
            return repository.findAll(PageRequest.of(start, max)).getContent();
        } else {
            return repository.findAll();
        }
    }

    public RenderedTemplate getRenderedTemplate(String key, Locale locale, Map<String, Object> replacementValues) {

        EmailTemplate temp = getEmailTemplate(key, locale);
        //if no template was found we need to return null to avoid an NPE
        if (temp == null) {
            return null;
        }
 
        RenderedTemplate ret = new RenderedTemplate(temp);

        //get the default current user fields
        log.debug("getting default values");

        Map<String, Object> userVals = getCurrentUserFields();
        replacementValues.putAll(userVals);
        log.debug("got replacement values");

        ret.setRenderedSubject(this.processText(ret.getSubject(), replacementValues, key));
        ret.setRenderedMessage(this.processText(ret.getMessage(), replacementValues, key));

        List<String> headers = new ArrayList<>();
        headers.add("MIME-Version: 1.0");
        headers.add("Content-Type: multipart/alternative; boundary=\"" + EmailService.MULTIPART_BOUNDARY + "\"");
        headers.add(ret.getRenderedSubject());
        headers.add("From: " + "\""
            + serverConfigurationService.getString("ui.service", "Sakai") + "\" <"
            + serverConfigurationService.getString("setup.request", "no-reply@"
            + serverConfigurationService.getServerName()) + ">");
        ret.setHeaders(headers);

        //HTML component is optional, so might be null or empty
        if (StringUtils.isNotBlank(ret.getHtmlMessage())) {
            ret.setRenderedHtmlMessage(this.processText(ret.getHtmlMessage(), replacementValues, key));
        }
        return ret;
    }

    public RenderedTemplate getRenderedTemplateForUser(String key, String userId, Map<String, Object> replacementValues) {

        log.debug("getRenderedTemplateForUser({}, {})", key, userId);
        return getRenderedTemplate(key, preferencesService.getLocale(userId), replacementValues);
    }

    public EmailTemplate saveTemplate(EmailTemplate template) {

        //check that fields are set
        if (template == null) {
            throw new IllegalArgumentException("Template can't be null");
        }

        if (template.getKey() == null) {
           throw new IllegalArgumentException("Template key can't be null");
        }

        if (template.getOwner() == null) {
           throw new IllegalArgumentException("Template owner can't be null");
        }

        if (template.getSubject() == null) {
           throw new IllegalArgumentException("Template subject can't be null");
        }

        if (template.getMessage() == null) {
           throw new IllegalArgumentException("Template message can't be null");
        }

        String locale = template.getLocale();
        if (StringUtils.isBlank(locale)) {
           //For backward compatibility set it to default
           template.setLocale(EmailTemplate.DEFAULT_LOCALE);
        }

        //update the modified date
        template.setLastModified(new Date());
        try {
            return repository.save(template);
        } catch (HibernateException die) {
            throw new IllegalArgumentException("Key: " + template.getKey() + " and locale: " + template.getLocale() + " in use already", die);
        }
        //return null;
    }
 
    public void updateTemplate(EmailTemplate template) {

        template.setLastModified(new Date());
        String locale = template.getLocale();
        if (StringUtils.isBlank(locale)) {
            template.setLocale(EmailTemplate.DEFAULT_LOCALE);
        }
        repository.save(template);
        log.info("updated template: {}", template.getId());
    }

    private String processText(String text, Map<String, Object> values, String templateName) {
        return TextTemplateLogicUtils.processTextTemplate(text, values, templateName);
    }

    private Map<String, Object> getCurrentUserFields() {

        Map<String, Object> rv = new HashMap<>();
        User user = userDirectoryService.getCurrentUser();
        if (user != null) {
            try {
                String email = user.getEmail();
                if (email == null) {
                   email = "";
                }
                String first = user.getFirstName();
                if (first == null) {
                   first = "";
                }
                String last = user.getLastName();
                if (last == null) {
                   last ="";
                }

                rv.put(CURRENT_USER_EMAIL, email);
                rv.put(CURRENT_USER_FIRST_NAME, first);
                rv.put(CURRENT_USER_LAST_NAME, last);
                rv.put(CURRENT_USER_DISPLAY_NAME, user.getDisplayName());
                rv.put(CURRENT_USER_DISPLAY_ID, user.getDisplayId());
                rv.put("currentUserDispalyId", user.getDisplayId());
            } catch (Exception e) {
                log.warn("Failed to get current user replacements: {}", user.getId(), e);
            }
        }

        /*NoN user fields */
        rv.put(LOCAL_SAKAI_NAME, serverConfigurationService.getString("ui.service", "Sakai"));
        rv.put(LOCAL_SAKAI_SUPPORT_MAIL,serverConfigurationService.getString("mail.support", "support@"+ serverConfigurationService.getServerName()));
        rv.put(LOCAL_SAKAI_URL,serverConfigurationService.getServerUrl());

        return rv;
    }

    public Map<EmailTemplateLocaleUsers, RenderedTemplate> getRenderedTemplates(
        String key, List<String> userIds, Map<String, Object> replacementValues) {
 
        List<Locale> foundLocales = new ArrayList<>();
        Map<Locale, EmailTemplateLocaleUsers> mapStore = new HashMap<>();
 
        for (String userId : userIds) {
            Locale loc = preferencesService.getLocale(userId);
            //have we found this locale?
            if (!foundLocales.contains(loc)) {
                //create a new EmailTemplateLocalUser
                EmailTemplateLocaleUsers etlu = new EmailTemplateLocaleUsers();
                log.debug("adding user {} to new object", userId);
                etlu.setLocale(loc);
                etlu.addUser(userId);
                mapStore.put(loc, etlu);
                foundLocales.add(loc);
            } else {
                EmailTemplateLocaleUsers etlu = mapStore.get(loc);
                log.debug("adding user {} to existing object", userId);
                etlu.addUser(userId);
                mapStore.remove(loc);
                mapStore.put(loc, etlu);
            }
        }

        Map<EmailTemplateLocaleUsers, RenderedTemplate> ret = new HashMap<>();

        //now for each locale we need a rendered template
        mapStore.entrySet().forEach(entry -> {

            Locale loc = entry.getKey();
            RenderedTemplate rt = this.getRenderedTemplate(key, loc, replacementValues);
            if (rt != null) {
                ret.put(entry.getValue(), rt);
            } else {
                log.error("No template found for key: {} in locale {}", key, loc);
            }
 
        });
        return ret;
    }

    public void sendRenderedMessages(String key, List<String> userReferences,
            Map<String, Object> replacementValues, String fromEmail, String fromName) {

        getRenderedTemplates(key, userReferences, replacementValues).entrySet().forEach(entry -> {

            RenderedTemplate rt = entry.getValue();
            EmailTemplateLocaleUsers etlu = entry.getKey();
            List<User> toUsers = etlu.getUserIds().stream().map(userRef -> {

                try {
                    return userDirectoryService.getUser(userDirectoryService.idFromReference(userRef));
                } catch (UserNotDefinedException unde) {
                }
                return null;
            }).collect(Collectors.toList());
            log.info("sending template {} for locale {} to {} users", key, etlu.getLocale().toString(), toUsers.size());
            sendEmailToUsers(toUsers, rt, fromEmail, fromName);
        });
    }

    /**
     * method to send email to Users.
     * @param toAddress
     * @param rt
     * @param fromEmail
     * @param fromName
     */
    private void sendEmailToUsers(List<User> toAddress, RenderedTemplate rt, String fromEmail, String fromName) {

        StringBuilder message = new StringBuilder();
        message.append(MIME_ADVISORY);
        if (rt.getRenderedMessage() != null) {
            message.append(BOUNDARY_LINE);
            message.append("Content-Type: text/plain; charset=iso-8859-1\n");
            message.append(rt.getRenderedMessage());
        }
        if (rt.getRenderedHtmlMessage() != null) {
            //append the HMTL part
            message.append(BOUNDARY_LINE);
            message.append("Content-Type: text/html; charset=iso-8859-1\n");
            message.append(rt.getRenderedHtmlMessage());
        }

        message.append(TERMINATION_LINE);

        // we need to manually construct the headers
        List<String> headers = new ArrayList<>();
        //the template may specify a from address
        if (StringUtils.isNotBlank(rt.getFrom())) {
            headers.add("From: \"" + rt.getFrom() );
        } else {
            headers.add("From: \"" + fromName + "\" <" + fromEmail + ">" );
        }
        // Add a To: header of either the recipient (if only 1), or the sender (if multiple)
        String toName = fromName;
        String toEmail = fromEmail;

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

        if (toAddress.size() > 1) {
            headers.add("Precedence: bulk");
        }

        String body = message.toString();
        log.debug("message body {}", body);
        emailService.sendToUsers(toAddress, headers, body);
}

    public void processEmailTemplates(List<String> templatePaths) {

        final String ADMIN = "admin";

        Persister persister = new Persister();
        for (String templatePath : templatePaths) {

            log.debug("Processing template: {}", templatePath);
 
            EmailTemplate template;
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(templatePath)) {
                if (in == null) {
                    log.warn("Could not load resource from '{}'. Skipping ...", templatePath);
                    continue;
                }
                template = persister.read(EmailTemplate.class, in);
            } catch (Exception e) {
                log.warn("Error processing template: '{}', {}:{}. Skipping ...", templatePath, e.getClass(), e.getMessage());
                continue;
            }

            //check if we have an existing template of this key and locale
            //its possible the template has no locale set
            //The locale could also be the Default
            Locale loc = null;
            if (StringUtils.isNotBlank(template.getLocale()) && !EmailTemplate.DEFAULT_LOCALE.equals(template.getLocale())) {
                loc = LocaleUtils.toLocale(template.getLocale());
            } 
 
            Optional<EmailTemplate> optionalExistingTemplate = getEmailTemplateNoDefault(template.getKey(), loc);
            if (!optionalExistingTemplate.isPresent()) {
                //no existing, save this one
                Session sakaiSession = sessionManager.getCurrentSession();
                sakaiSession.setUserId(ADMIN);
                sakaiSession.setUserEid(ADMIN);
                saveTemplate(template);
                sakaiSession.setUserId(null);
                sakaiSession.setUserId(null);
                log.info("Saved email template: " + template.getKey() + " with locale: " + template.getLocale());
                continue; //skip to next
            }

            EmailTemplate existingTemplate = optionalExistingTemplate.get();
 
            //check version, if local one newer than persisted, update it - SAK-17679
            //also update the locale - SAK-20987
            int existingTemplateVersion = existingTemplate.getVersion() != null ? existingTemplate.getVersion() : 0;
            if (template.getVersion() > existingTemplateVersion) {
                existingTemplate.setSubject(template.getSubject());
                existingTemplate.setMessage(template.getMessage());
                existingTemplate.setHtmlMessage(template.getHtmlMessage());
                existingTemplate.setVersion(template.getVersion());
                existingTemplate.setOwner(template.getOwner());
                existingTemplate.setLocale(template.getLocale());

                Session sakaiSession = sessionManager.getCurrentSession();
                sakaiSession.setUserId(ADMIN);
                sakaiSession.setUserEid(ADMIN);
                updateTemplate(existingTemplate);
                sakaiSession.setUserId(null);
                sakaiSession.setUserId(null);
                log.info("Updated email template: " + template.getKey() + " with locale: " + template.getLocale());
            }
        }
    }

    public String exportTemplateAsXml(String key, Locale locale) {
 
        EmailTemplate template = getEmailTemplate(key, locale);
        Persister persister = new Persister();
        File file = null;
        String ret = null;
        try {
            file = File.createTempFile("emailtemplate", "xml");
            persister.write(template, file);
            //read the data
            ret = readFile(file.getAbsolutePath());
        } catch (Exception e) {
            log.warn( "Error creating or writing to file", e );
        }
        finally {
            if (file != null) {
                if (!file.delete()) {
                    log.warn("error deleting tmp file");
                }
            }
        }
 
        return ret;
    }

    private static String readFile(String path) throws IOException {

        try (FileInputStream stream = new FileInputStream(new File(path))) {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            return Charset.defaultCharset().decode(bb).toString();
        }
    }
 
    /**
     * Delete all templates in the Database
     * Only used in unit tests so not in API
     * TODO rewrite for efficiency
     */
    public void deleteAllTemplates() {

        log.debug("deleteAllTemplates");
        for (EmailTemplate template : repository.findAll()) {
            log.debug("deleting template: {}", template.getId());
            repository.delete(template);
        }
    }

    public void sendMessage(List<String> userIds, List<String> emailAddresses, RenderedTemplate renderedTemplate, String from, String fromName) {

        //if user has changed the email address
        if (emailAddresses.size() == 1){
            String toEmail = emailAddresses.get(0);
            List<String> additionalHeaders = new ArrayList<>();
            additionalHeaders.add("Content-Type: text/html; charset=UTF-8");
            emailService.send(from, toEmail, renderedTemplate.getRenderedSubject(), renderedTemplate.getRenderedHtmlMessage(), toEmail, from, additionalHeaders );
            return;
        }

        List<User> toUsers = userIds.stream().map(uid -> {

            try {
                return userDirectoryService.getUser(uid);
            } catch (UserNotDefinedException unde) {
            }
            return null;
        }).collect(Collectors.toList());
        sendEmailToUsers(toUsers, renderedTemplate, from, fromName);
    }

    /**
    * Registers a new template with the service, defined by the given XML file
    * @param templateResourceStream the resource stream for the XML file
    * @param templateRegistrationKey the key (name) to register the template under
    * @return true if the template was registered
    */
    @Override
    public boolean importTemplateFromXmlFile(InputStream templateResourceStream, String templateRegistrationKey) {
 
        if (templateResourceStream == null) {
            log.error(String.format("Unable to register template under key '%s': Could not load resource, input stream is null.", templateRegistrationKey));
            return false;
        }

        SecurityAdvisor yesMan = (String userId, String function, String reference) -> SecurityAdvice.ALLOWED;
 
        try {
            securityService.pushAdvisor(yesMan);
 
            // Parse the XML, get all the child templates
            Document document = new SAXBuilder().build(templateResourceStream);
            List<Element> childTemplates = document.getRootElement().getChildren("emailTemplate");

            // Create and register a template with the service for each one found in the XML file
            childTemplates.stream().forEach( (element) -> { xmlToTemplate(element, templateRegistrationKey); } );
        } catch (JDOMException | IOException e) {
            log.error(String.format("Error registering template under key '%s': ", templateRegistrationKey), e);
            return false;
        } finally {
            securityService.popAdvisor(yesMan);
        }
 
        return true;
    }
 
    /**
     * Extracts the email template fields from the given XML element. Checks
     * if the email template already exists; if it does not, it will save
     * the template to the service. If it does exist it will update the template if
     * and only if the existing version is less than the new version.
     * @param xmlTemplate - the XML element containing the email template data
     * @param key - the key (name) of the template to be saved to the service
     */
    private void xmlToTemplate(Element xmlTemplate, String key) {

        String subject = xmlTemplate.getChildText("subject");
        String body = xmlTemplate.getChildText("message");
        String bodyHtml = StringUtils.trimToEmpty(xmlTemplate.getChildText("messagehtml"));
        String locale = xmlTemplate.getChildText("locale");
        String localeLangTag = xmlTemplate.getChildText("localeLangTag");
        int version = NumberUtils.toInt(xmlTemplate.getChildText("version"), 1);

        Locale loc;
        if (StringUtils.isBlank(localeLangTag)) {
            loc = new Locale(locale);
        } else {
            loc = Locale.forLanguageTag(localeLangTag);
        }

        // Determine if template already exists
        EmailTemplate template = getEmailTemplate(key, loc);
        boolean update = true;
        if (template == null) {
            update = false;
            template = new EmailTemplate();
        }

        // If the template does not already exist, or the new version is greater than the existing version, proceed...
        if (!update || template.getVersion() == null || version > template.getVersion()) {
            // Dump the values from XML into the object
            template.setSubject(subject);
            template.setMessage(body);
            template.setLocale(locale);
            template.setKey(key);
            template.setVersion(version);
            template.setOwner("admin");
            try {
                template.setHtmlMessage(URLDecoder.decode(bodyHtml, "utf8"));
            } catch (UnsupportedEncodingException e) {
                template.setHtmlMessage(bodyHtml);
                log.warn(String.format("Unable to decode body HTML for template %s, reverting to original value.", key), e);
            }

            // Update or save the template
            try {
                if (update) {
                    updateTemplate(template);
                } else {
                    saveTemplate( template );
                }

                log.info((update ? "Updated " : "Added ") + key + (update ? " in" : " to") + " the email template service.");
            } catch (Exception e) {
                log.error("Error "+ (update ? "updating" : "saving") + " template: " + key, e);
            }
        }
    }
}
