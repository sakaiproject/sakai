/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.messaging.impl;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.api.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.messaging.api.Message;
import org.sakaiproject.messaging.api.MessageMedium;
import static org.sakaiproject.messaging.api.MessageMedium.*;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserMessagingServiceImpl implements UserMessagingService {

    @Resource private DigestService digestService;
    @Resource private EmailService emailService;
    @Resource private EmailTemplateService emailTemplateService;
    @Resource private PreferencesService preferencesService;
    @Resource private UserDirectoryService userDirectoryService;
    @Resource private ServerConfigurationService serverConfigurationService;

    private ExecutorService executor;

    public void init() {
        executor = Executors.newFixedThreadPool(20);
    }

    public void destroy() {
        executor.shutdownNow();
    }

    private class EmailSender {

        public final String MULTIPART_BOUNDARY = "======sakai-multi-part-boundary======";
        public final String BOUNDARY_LINE = "\n\n--" + this.MULTIPART_BOUNDARY + "\n";
        public final String TERMINATION_LINE = "\n\n--" + this.MULTIPART_BOUNDARY + "--\n\n";
        public final String MIME_ADVISORY = "This message is for MIME-compliant mail readers.";
        public final String PLAIN_TEXT_HEADERS = "Content-Type: text/plain\n\n";
        public final String HTML_HEADERS = "Content-Type: text/html; charset=ISO-8859-1\n\n";
        public final String HTML_END = "\n  </body>\n</html>\n";

        private final User user;
        private final String subject;
        private final String message;

        public EmailSender(User user, String subject, String message) {
            this.user = user;
            this.subject = subject;
            this.message = message;
        }

        // do it!
        public void send() {

            if (StringUtils.isBlank(user.getEmail())) {
                log.error("SakaiProxy.sendEmail() failed. No email for userId: {}", user.getId());
                return;
            }

            // do it
            emailService.sendToUsers(Collections.singleton(user), getHeaders(user.getEmail(), this.subject),
                    formatMessage(this.subject, this.message));

            log.info("Email sent to: {}", user.getId());
        }

        /** helper methods for formatting the message */
        private String formatMessage(final String subject, final String message) {

            StringBuilder sb = new StringBuilder();
            return sb.append(MIME_ADVISORY)
                .append(BOUNDARY_LINE)
                .append(PLAIN_TEXT_HEADERS)
                .append(StringEscapeUtils.escapeHtml4(message))
                .append(BOUNDARY_LINE)
                .append(HTML_HEADERS)
                .append(htmlPreamble(subject))
                .append(message)
                .append(HTML_END)
                .append(TERMINATION_LINE).toString();
        }

        private String htmlPreamble(final String subject) {

            StringBuilder sb = new StringBuilder();
            return sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n")
                .append("\"http://www.w3.org/TR/html4/loose.dtd\">\n")
                .append("<html>\n")
                .append("<head><title>")
                .append(subject)
                .append("</title></head>\n")
                .append("<body>\n").toString();
        }

        private List<String> getHeaders(final String emailTo, final String subject) {

            final List<String> headers = new ArrayList<>();
            headers.add("MIME-Version: 1.0");
            headers.add("Content-Type: multipart/alternative; boundary=\"" + MULTIPART_BOUNDARY + "\"");
            headers.add(formatSubject(subject));
            headers.add(getFrom());
            if (StringUtils.isNotBlank(emailTo)) {
                headers.add("To: " + emailTo);
            }
            return headers;
        }

        private String getFrom() {

            StringBuilder sb = new StringBuilder();
            return sb.append("From: ")
                .append(serverConfigurationService.getString("ui.service", "Sakai"))
                .append(" <")
                .append(serverConfigurationService.getString("setup.request", "no-reply@" + serverConfigurationService.getServerName()))
                .append(">").toString();
        }

        private String formatSubject(final String subject) {

            StringBuilder sb = new StringBuilder();
            return sb.append("Subject: ").append(subject).toString();
        }
    }

    public void message(Set<User> users, Message message, List<MessageMedium> media, Map<String, Object> replacements, int priority) {

        executor.execute(() -> {

            String tool = message.getTool();
            String type = message.getType();

            users.forEach(user -> {

                Locale locale = preferencesService.getLocale(user.getId());
                Preferences prefs = preferencesService.getPreferences(user.getId());
                ResourceProperties toolProps = prefs.getToolNotificationProperties(tool);
                String noti = toolProps.getProperty("2");

                media.forEach(m -> {

                    final RenderedTemplate template = (m == EMAIL || m == DIGEST)
                        ? emailTemplateService.getRenderedTemplate(tool + "." + type, locale, replacements)
                            : null;

                    switch (m) {
                        case EMAIL:
                            if (NotificationService.NOTI_REQUIRED == priority || noti.equals(String.valueOf(NotificationService.PREF_IMMEDIATE))) {
                                new EmailSender(user, template.getRenderedSubject(), template.getRenderedHtmlMessage()).send();
                            }
                            break;
                        case DIGEST:
                            if (NotificationService.NOTI_REQUIRED == priority || noti.equals(String.valueOf(NotificationService.PREF_DIGEST))) {
                                digestService.digest(user.getId(), template.getRenderedSubject(), template.getRenderedMessage());
                            }
                            break;
                        default:
                    }
                });
            });
        });
    }

    /**
     * Registers a new template with the service, defined by the given XML file
     *
     * @param templateResourceStream the resource stream for the XML file
     * @param templateRegistrationKey the key (name) to register the template under
     * @return true if the template was registered
     */
    @Override
    public boolean importTemplateFromResourceXmlFile(String templateResource, String templateRegistrationKey) {

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return emailTemplateService.importTemplateFromXmlFile(loader.getResourceAsStream(templateResource), templateRegistrationKey);
    }
}
