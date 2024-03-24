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
import org.apache.http.HttpResponse;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.api.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.messaging.api.UserNotificationHandler;
import org.sakaiproject.messaging.api.Message;
import org.sakaiproject.messaging.api.MessageMedium;
import org.sakaiproject.messaging.api.model.PushSubscription;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.messaging.api.repository.PushSubscriptionRepository;
import org.sakaiproject.messaging.api.repository.UserNotificationRepository;
import static org.sakaiproject.messaging.api.MessageMedium.*;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserMessagingServiceImpl implements UserMessagingService, Observer {

    private static final Set<String> HANDLED_EVENTS = new HashSet<>();

    @Autowired private DigestService digestService;
    @Autowired private EmailService emailService;
    @Autowired private EmailTemplateService emailTemplateService;
    @Autowired private EntityManager entityManager;
    @Autowired private EventTrackingService eventTrackingService;
    @Autowired private PreferencesService preferencesService;
    @Autowired private PushSubscriptionRepository pushSubscriptionRepository;
    @Autowired private ServerConfigurationService serverConfigurationService;

    @Qualifier("org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    @Autowired private SessionFactory sessionFactory;

    @Autowired private SessionManager sessionManager;
    @Autowired private SiteService siteService;
    @Autowired private ToolManager toolManager;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private UserNotificationRepository userNotificationRepository;

    @Qualifier("org.sakaiproject.time.api.UserTimeService")
    @Autowired private UserTimeService userTimeService;

    @Setter private ResourceLoader resourceLoader;

    @Qualifier("org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    @Autowired private PlatformTransactionManager transactionManager;

    private List<UserNotificationHandler> handlers = new ArrayList<>();
    private Map<String, UserNotificationHandler> handlerMap = new HashMap<>();
    private ExecutorService executor;
    private PushService pushService;
    private String publicKey = "";
    private boolean pushEnabled = false;

    private ObjectMapper objectMapper = new ObjectMapper();

    public void init() {

        pushEnabled = serverConfigurationService.getBoolean("portal.notifications.push.enabled", true);

        if (pushEnabled) {
            // Site publish is handled specially. It should probably be extracted from the logic below, but for now,
            // we fake it in the list of handled events to get it into the if branch.
            HANDLED_EVENTS.add(SiteService.EVENT_SITE_PUBLISH);
            eventTrackingService.addLocalObserver(this);
        }

        objectMapper.registerModule(new JavaTimeModule());

        Security.addProvider(new BouncyCastleProvider());

        executor = Executors.newFixedThreadPool(20);

        if (pushEnabled) {
            String home = serverConfigurationService.getSakaiHomePath();
            String publicKeyFileName = serverConfigurationService.getString(PUSH_PUBKEY_PROPERTY, "sakai_push.key.pub");
            Path publicKeyPath = Paths.get(home, publicKeyFileName);
            String privateKeyFileName = serverConfigurationService.getString(PUSH_PRIVKEY_PROPERTY, "sakai_push.key");
            Path privateKeyPath = Paths.get(home, privateKeyFileName);

            if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
                try {
                    publicKey = String.join("", Files.readAllLines(Paths.get(home, publicKeyFileName)));
                    String privateKey = String.join("", Files.readAllLines(Paths.get(home, privateKeyFileName)));
                    pushService = new PushService(publicKey, privateKey);
                    String pushSubject = serverConfigurationService.getString("portal.notifications.push.subject", "");
                    pushService.setSubject(pushSubject);
                } catch (Exception e) {
                    log.error("Failed to setup push service: {}", e.toString());
                }
            } else {
                ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec("prime256v1");

                try {
                    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
                    keyPairGenerator.initialize(parameterSpec);

                    KeyPair keyPair = keyPairGenerator.generateKeyPair();

                    byte[] publicKey = Utils.encode((ECPublicKey) keyPair.getPublic());
                    String publicKeyBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey);
                    try (FileWriter fw = new FileWriter(publicKeyPath.toFile())) {
                        fw.write(publicKeyBase64);
                    }

                    byte[] privateKey = Utils.encode((ECPrivateKey) keyPair.getPrivate());
                    String privateKeyBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(privateKey);
                    try (FileWriter fw = new FileWriter(privateKeyPath.toFile())) {
                        fw.write(privateKeyBase64);
                    }

                    pushService = new PushService(publicKeyBase64, privateKeyBase64);
                    String pushSubject = serverConfigurationService.getString("portal.notifications.push.subject", "");
                    pushService.setSubject(pushSubject);
                } catch (Exception e) {
                    log.error("Failed to generate key pair: {}", e.toString());
                }
            }
        }
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
                    formatMessage(user.getId(), this.subject, this.message));

            log.info("Email sent to: {}", user.getId());
        }

        /** helper methods for formatting the message */
        private String formatMessage(String userId, String subject, String message) {

            try {
                Site userSite = siteService.getSite(siteService.getUserSiteId(userId));
                ToolConfiguration tc = userSite.getToolForCommonId("sakai.preferences");
                if (tc != null) {
                    String url = serverConfigurationService.getPortalUrl() + "/site/" + userSite.getId() + "/tool/" + tc.getId();
                    String prefsLink = "<br /><br />" + resourceLoader.getFormattedMessage("preferences_link_message", url);
                    message = message + prefsLink;
                } else {
                    log.debug("No preferences tool on user {}'s site", userId);
                }
            } catch (IdUnusedException iue) {
                log.warn("User id {} doesn't have a home site yet. We can't add the preferences link until they've logged in a least once", userId);
            } catch (Exception e) {
                log.error("Failed to add preferences link to email message body: {}", e.toString());
            }

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
                .append(serverConfigurationService.getSmtpFrom())
                .append(">").toString();
        }

        private String formatSubject(final String subject) {

            StringBuilder sb = new StringBuilder();
            return sb.append("Subject: ").append(subject).toString();
        }
    }

    public void message(Set<User> users, Message message, List<MessageMedium> media, Map<String, Object> replacements, int priority) {

        Placement placement = toolManager.getCurrentPlacement();
        String context = placement != null ? placement.getContext() : null;

        if (replacements.containsKey("icon")) {
            replacements.put("iconClass", "si si-" + replacements.get("icon"));
        }

        executor.execute(() -> {

            String tool = message.getTool();
            String type = message.getType();

            users.forEach(user -> {

                Locale locale = preferencesService.getLocale(user.getId());
                Preferences prefs = preferencesService.getPreferences(user.getId());
                ResourceProperties toolProps = prefs.getToolNotificationProperties(tool);
                String noti = toolProps.getProperty("2") == null ? String.valueOf(NotificationService.PREF_IMMEDIATE) : toolProps.getProperty("2");

                String siteId = context != null ? context : message.getSiteId();
                
                String siteOverride = siteId != null ? toolProps.getProperty(siteId) : null;

                media.forEach(m -> {

                    final RenderedTemplate template = (m == EMAIL || m == DIGEST)
                        ? emailTemplateService.getRenderedTemplate(tool + "." + type, locale, replacements)
                            : null;

                    switch (m) {
                        case EMAIL:
                            if (NotificationService.NOTI_REQUIRED == priority) {
                                new EmailSender(user, template.getRenderedSubject(), template.getRenderedHtmlMessage()).send();
                            } else {
                                if (siteOverride != null) {
                                    if (siteOverride.equals(String.valueOf(NotificationService.PREF_IMMEDIATE))) {
                                        new EmailSender(user, template.getRenderedSubject(), template.getRenderedHtmlMessage()).send();
                                    }
                                } else if (noti.equals(String.valueOf(NotificationService.PREF_IMMEDIATE))) {
                                    new EmailSender(user, template.getRenderedSubject(), template.getRenderedHtmlMessage()).send();
                                }
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

    @Override
    public void registerHandler(UserNotificationHandler handler) {

        handler.getHandledEvents().forEach(eventName -> {

            HANDLED_EVENTS.add(eventName);
            handlerMap.put(eventName, handler);
            log.debug("Registered user notification handler {} for event: {}", handler.getClass().getName(), eventName);
        });
    }

    @Override
    public void unregisterHandler(UserNotificationHandler handler) {

        handler.getHandledEvents().forEach(eventName -> {

            UserNotificationHandler current = handlerMap.get(eventName);

            if (handler == current) {
                HANDLED_EVENTS.remove(eventName);
                handlerMap.remove(eventName);
                log.debug("Unregistered bullhorn handler {} for event: {}", handler.getClass().getName(), eventName);
            }
        });
    }

    public void update(Observable o, final Object arg) {

        if (arg instanceof Event) {
            Event e = (Event) arg;
            String event = e.getEvent();
            // We add this comparation with UNKNOWN_USER because implementation of BaseEventTrackingService
            // UNKNOWN_USER is an user in a server without session. 
            if (HANDLED_EVENTS.contains(event) && !EventTrackingService.UNKNOWN_USER.equals(e.getUserId()) ) {
                String ref = e.getResource();
                String context = e.getContext();

                boolean deferred = false;
                try {
                    deferred = !siteService.getSite(context).isPublished();
                } catch (IdUnusedException iue) {
                    log.warn("Failed to find site with id {} while setting deferred to published", context);
                }
                final boolean finalDeferred = deferred;

                String[] pathParts = ref.split("/");
                String from = e.getUserId();
                Optional<String> tool = entityManager.getTool(ref);
                long at = e.getEventTime().getTime();
                try {
                    UserNotificationHandler handler = handlerMap.get(event);
                    if (handler != null) {
                        Optional<List<UserNotificationData>> result = handler.handleEvent(e);
                        if (result.isPresent()) {
                            result.get().forEach(bd -> {
                                UserNotification un = doInsert(from, bd.getTo(), event, ref, bd.getTitle(),
                                                bd.getSiteId(), e.getEventTime(), finalDeferred, bd.getUrl(), tool);
                                if (!finalDeferred && this.pushEnabled) {
                                    un.setTool(tool.orElse(""));
                                    push(decorateNotification(un));
                                }
                            });
                        }
                    } else if (SiteService.EVENT_SITE_PUBLISH.equals(event)) {
                        final String siteId = pathParts[2];

                        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

                        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

                            protected void doInTransactionWithoutResult(TransactionStatus status) {
                                userNotificationRepository.setDeferredBySiteId(siteId, false);
                            }
                        });
                    }
                } catch (Exception ex) {
                    log.error("Caught exception whilst handling events", ex);
                }
            }
        }
    }

    private UserNotification doInsert(String from, String to, String event, String ref
                            , String title, String siteId, Date eventDate, boolean deferred, String url, Optional<String> tool) {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        return transactionTemplate.execute(new TransactionCallback<UserNotification>() {

            public UserNotification doInTransaction(TransactionStatus status) {

                UserNotification ba = new UserNotification();
                ba.setFromUser(from);
                ba.setToUser(to);
                ba.setEvent(event);
                ba.setRef(ref);
                ba.setTitle(title);
                ba.setSiteId(siteId);
                ba.setEventDate(eventDate.toInstant());
                ba.setUrl(url);
                ba.setDeferred(deferred);

                return userNotificationRepository.save(ba);
            }
        });
    }

    @Transactional  
    public boolean clearNotification(long id) {

        String userId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(userId)) {
            log.warn("No current user");
            return false;
        }

        Optional<UserNotification> optUserNotification = userNotificationRepository.findById(id);

        if (optUserNotification.isPresent()) {

            UserNotification un = optUserNotification.get();

            if (un.getToUser().equals(userId)) {
                userNotificationRepository.delete(un);
                return true;
            } else {
                log.warn("{} attempted to delete a notification belonging to {}", userId, un.getToUser());
            }
        }

        return false;
    }

    public List<UserNotification> getNotifications() {

        String userId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(userId)) {
            log.warn("No current user");
            return Collections.<UserNotification>emptyList();
        }

        return userNotificationRepository.findByToUser(userId)
                .stream().map(this::decorateNotification).collect(Collectors.toList());
    }

    @Transactional
    public boolean clearAllNotifications() {

        String userId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(userId)) {
            log.warn("No current user");
            return false;
        }

        userNotificationRepository.deleteByToUserAndDeferred(userId, false);
        return true;
    }

    @Transactional
    public boolean markAllNotificationsViewed() {

        String userId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(userId)) {
            log.warn("No current user");
            return false;
        }

        userNotificationRepository.setAllNotificationsViewed(userId);
        return true;
    }

    private UserNotification decorateNotification(UserNotification notification) {

        try {
            User fromUser = userDirectoryService.getUser(notification.getFromUser());
            notification.setFromDisplayName(fromUser.getDisplayName());
            notification.setFormattedEventDate(userTimeService.dateTimeFormat(notification.getEventDate(), null, null));
            if (StringUtils.isNotBlank(notification.getSiteId())) {
                notification.setSiteTitle(siteService.getSite(notification.getSiteId()).getTitle());
            }
        } catch (UserNotDefinedException unde) {
            notification.setFromDisplayName(notification.getFromUser());
        } catch (IdUnusedException iue) {
            notification.setSiteTitle(notification.getSiteId());
        }

        return notification;
    }

    @Transactional
    public void subscribeToPush(String endpoint, String auth, String userKey, String browserFingerprint) {

        String userId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(userId)) {
            log.warn("No current user");
            return;
        }

        pushSubscriptionRepository.deleteByFingerprint(browserFingerprint);

        PushSubscription ps = new PushSubscription();
        ps.setUserId(userId);
        ps.setEndpoint(endpoint);
        ps.setAuth(auth);
        ps.setUserKey(userKey);
        ps.setCreated(Instant.now());
        ps.setFingerprint(browserFingerprint);

        pushSubscriptionRepository.save(ps);
    }

    private void push(UserNotification un) {

        pushSubscriptionRepository.findByUser(un.getToUser()).forEach(pushSubscription -> {

            String pushEndpoint = pushSubscription.getEndpoint();
            String pushUserKey = pushSubscription.getUserKey();
            String pushAuth = pushSubscription.getAuth();

            // We only push if the user has given permission for  notifications
            // and successfully set their subscription details
            if (!StringUtils.isAnyBlank(pushEndpoint, pushUserKey, pushAuth)) {
                Subscription sub = new Subscription(pushEndpoint, new Subscription.Keys(pushUserKey, pushAuth));
                try {
                    HttpResponse pushResponse = pushService.send(new Notification(sub, objectMapper.writeValueAsString(un)));
                    log.debug("The push response from {} returned code {} and reason {}",
                            pushEndpoint,
                            pushResponse.getStatusLine().getStatusCode(),
                            pushResponse.getStatusLine().getReasonPhrase());
                } catch (Exception e) {
                    log.error("Exception while pushing notification: {}", e.toString());
                }
            }
        });
    }
}
