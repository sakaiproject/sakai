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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.api.RenderedTemplate;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.messaging.api.Message;
import org.sakaiproject.messaging.api.MessageMedium;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.messaging.api.UserNotificationHandler;
import org.sakaiproject.messaging.api.model.PushSubscription;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.messaging.api.repository.PushSubscriptionRepository;
import org.sakaiproject.messaging.api.repository.UserNotificationRepository;
import org.sakaiproject.serialization.MapperFactory;
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
import org.sakaiproject.util.api.FormattedText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.sakaiproject.messaging.api.MessageMedium.DIGEST;
import static org.sakaiproject.messaging.api.MessageMedium.EMAIL;

@Slf4j
public class UserMessagingServiceImpl implements UserMessagingService, Observer {

    public static final Integer DEFAULT_THREAD_POOL_SIZE = 20;

    @Autowired private DigestService digestService;
    @Autowired private EmailService emailService;
    @Autowired private EmailTemplateService emailTemplateService;
    @Autowired private EventTrackingService eventTrackingService;
    @Autowired private PreferencesService preferencesService;
    @Autowired private PushSubscriptionRepository pushSubscriptionRepository;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private SessionManager sessionManager;
    @Autowired private SiteService siteService;
    @Autowired private ToolManager toolManager;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private UserNotificationRepository userNotificationRepository;
    @Qualifier("org.sakaiproject.time.api.UserTimeService")
    @Autowired private UserTimeService userTimeService;
    @Autowired private FormattedText formattedText;

    @Setter private ResourceLoader resourceLoader;
    @Setter private TransactionTemplate transactionTemplate;

    private volatile Map<String, UserNotificationHandler> notificationHandlers;
    private final ObjectMapper objectMapper;

    private ExecutorService executor;
    private boolean pushEnabled = false;
    private PushService pushService;

    public UserMessagingServiceImpl() {
        objectMapper = MapperFactory.createDefaultJsonMapper();
        Map<String, UserNotificationHandler> handlerMap = new HashMap<>();
        // Site publish is handled specially. It should probably be extracted from the logic below,
        // but for now, fake it in the list of handled events to get it into the if branch.
        handlerMap.put(SiteService.EVENT_SITE_PUBLISH, null);
        notificationHandlers = Collections.unmodifiableMap(handlerMap);

        // Web Push Protocol requires Elliptic Curve cryptography for generating VAPID keys
        Security.addProvider(new BouncyCastleProvider());
    }

    public void init() {
        // Initialize the executor with configurable thread pool size
        int threadPoolSize = serverConfigurationService.getInt("messaging.threadpool.size", DEFAULT_THREAD_POOL_SIZE);
        executor = Executors.newFixedThreadPool(threadPoolSize);
        log.info("Initialized messaging thread pool with {} threads", threadPoolSize);

        if(serverConfigurationService.getBoolean("portal.bullhorns.enabled", true)) {
            eventTrackingService.addLocalObserver(this);
            pushEnabled = serverConfigurationService.getBoolean("portal.notifications.push.enabled", true);

            if (pushEnabled) {
                String home = serverConfigurationService.getSakaiHomePath();
                String publicKeyFileName = serverConfigurationService.getString(PUSH_PUBKEY_PROPERTY, "sakai_push.key.pub");
                Path publicKeyPath = Paths.get(home, publicKeyFileName);
                String privateKeyFileName = serverConfigurationService.getString(PUSH_PRIVKEY_PROPERTY, "sakai_push.key");
                Path privateKeyPath = Paths.get(home, privateKeyFileName);

                if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
                    try {
                        String publicKey = String.join("", Files.readAllLines(publicKeyPath));
                        String privateKey = String.join("", Files.readAllLines(privateKeyPath));
                        pushService = new PushService(publicKey, privateKey);
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
                    } catch (Exception e) {
                        log.error("Failed to generate key pair: {}", e.toString());
                    }
                }

                if (pushService != null) {
                    String defaultSubject = serverConfigurationService.getServerUrl();
                    String pushSubject = serverConfigurationService.getString("portal.notifications.push.subject", defaultSubject);
                    pushService.setSubject(pushSubject);
                    log.info("Push service configured with VAPID subject: {}", pushSubject);
                }
            }
        }
    }

    public void destroy() {
        if (executor != null) {
            try {
                // Attempt a graceful shutdown
                executor.shutdown();
                log.info("Messaging thread pool shutdown initiated");
            } catch (Exception e) {
                log.error("Error shutting down messaging thread pool: {}", e.toString());
            }
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
     * @param templateResource the resource stream for the XML file
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
        Map<String, UserNotificationHandler> newMap = new HashMap<>(notificationHandlers);

        // Update the copy with new handlers only if the key doesn't exist
        boolean mapChanged = false;
        for (String eventName : handler.getHandledEvents()) {
            boolean added = newMap.putIfAbsent(eventName, handler) == null;
            if (added) {
                mapChanged = true;
                log.debug("Registered user notification handler {} for event: {}", handler.getClass().getName(), eventName);
            } else {
                log.warn("Handler for event {} already exists, skipping registration for {}", eventName, handler.getClass().getName());
            }
        }
        // Only update the reference if changes were made
        if (mapChanged) {
            // Atomically replace the reference to the map
            notificationHandlers = Collections.unmodifiableMap(newMap);
        }
    }

    @Override
    public void unregisterHandler(UserNotificationHandler handler) {
        Map<String, UserNotificationHandler> newMap = new HashMap<>(notificationHandlers);

        // Remove handlers for each event this handler handles
        boolean mapChanged = false;
        for (String eventName : handler.getHandledEvents()) {
            if (newMap.containsKey(eventName)) {
                newMap.remove(eventName);
                mapChanged = true;
                log.debug("Unregistered bullhorn handler {} for event: {}", handler.getClass().getName(), eventName);
            }
        }

        // Only update the reference if changes were made
        if (mapChanged) {
            // Atomically replace the reference to the map
            notificationHandlers = Collections.unmodifiableMap(newMap);
        }
    }    

    public void update(Observable o, final Object arg) {

        if (arg instanceof Event e) {
            String event = e.getEvent();
            // We add this comparation with UNKNOWN_USER because implementation of BaseEventTrackingService
            // UNKNOWN_USER is an user in a server without session. 
            if (notificationHandlers.containsKey(event) && !EventTrackingService.UNKNOWN_USER.equals(e.getUserId()) ) {
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
                try {
                    UserNotificationHandler handler = notificationHandlers.get(event);
                    if (handler != null) {
                        handler.handleEvent(e).ifPresent(notifications ->
                                notifications.forEach(bd -> {
                                    UserNotification un = doInsert(from,
                                            bd.getTo(),
                                            event,
                                            ref,
                                            bd.getTitle(),
                                            bd.getSiteId(),
                                            e.getEventTime(),
                                            finalDeferred,
                                            bd.getUrl(),
                                            bd.getCommonToolId());
                            if (!finalDeferred && this.pushEnabled) {
                                un.setTool(bd.getCommonToolId());
                                push(decorateNotification(un));
                            }
                        }));
                    } else if (SiteService.EVENT_SITE_PUBLISH.equals(event)) {
                        final String siteId = pathParts[2];

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

    private UserNotification doInsert(String from, String to, String event, String ref, String title,
                                      String siteId, Date eventDate, boolean deferred, String url, String tool) {

        String processedTitle = title;
        if (processedTitle != null) {
            processedTitle = formattedText.processFormattedText(processedTitle, null, null);
        }
        final String finalTitle = processedTitle;

        return transactionTemplate.execute(status -> {
            UserNotification ba = new UserNotification();
            ba.setFromUser(from);
            ba.setToUser(to);
            ba.setEvent(event);
            ba.setRef(ref);

            ba.setTitle(finalTitle);
            ba.setSiteId(siteId);
            ba.setEventDate(eventDate.toInstant());
            ba.setUrl(url);
            ba.setTool(tool);
            ba.setDeferred(deferred);
            return userNotificationRepository.save(ba);
        });
    }

    /**
     * Helper method to get the current user ID and validate it exists
     * 
     * @return the current user ID or null if no user is logged in
     */
    private String getCurrentUserId() {
        String userId = sessionManager.getCurrentSessionUserId();
        if (StringUtils.isBlank(userId)) {
            log.warn("No current user");
            return null;
        }
        return userId;
    }

    @Transactional  
    public boolean clearNotification(long id) {
        String userId = getCurrentUserId();
        if (userId == null) return false;

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
        String userId = getCurrentUserId();
        if (userId == null) return Collections.emptyList();

        return userNotificationRepository.findByToUser(userId)
                .stream().map(this::decorateNotification).collect(Collectors.toList());
    }

    @Transactional
    public boolean clearAllNotifications() {
        String userId = getCurrentUserId();
        if (userId == null) return false;

        userNotificationRepository.deleteByToUserAndDeferred(userId, false);
        return true;
    }

    @Transactional
    public boolean markAllNotificationsViewed(String siteId, String toolId) {
        String userId = getCurrentUserId();
        if (userId == null) return false;

        userNotificationRepository.setAllNotificationsViewed(userId, siteId, toolId);
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

        if (notification.getTitle() != null) {
            notification.setTitle(formattedText.convertFormattedTextToPlaintext(notification.getTitle()));
        }

        return notification;
    }

    @Transactional
    public void subscribeToPush(String endpoint, String auth, String userKey, String browserFingerprint) {
        String userId = getCurrentUserId();
        if (userId == null) return;

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

    public void sendTestNotification() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        UserNotification un = new UserNotification();
        un.setFromUser(userId);
        un.setToUser(userId);
        un.setEvent("test.notification");
        un.setTitle(resourceLoader.getString("test_notification_title"));
        un.setEventDate(Instant.now());

        push(decorateNotification(un));
    }

    /**
     * Send a push notification to a user
     * 
     * @param un the UserNotification to push
     */
    private void push(UserNotification un) {
        if (un == null) {
            log.warn("Cannot push null notification");
            return;
        }

        if (!pushEnabled || pushService == null) {
            log.debug("Push service is not enabled or not initialized");
            return;
        }

        pushSubscriptionRepository.findByUser(un.getToUser()).forEach(pushSubscription -> {
            String pushEndpoint = pushSubscription.getEndpoint();
            String pushUserKey = pushSubscription.getUserKey();
            String pushAuth = pushSubscription.getAuth();

            // We only push if the user has given permission for notifications
            // and successfully set their subscription details
            if (StringUtils.isAnyBlank(pushEndpoint, pushUserKey, pushAuth)) {
                log.debug("Skipping push notification due to missing subscription details for user {}", un.getToUser());
                return;
            }

            Subscription sub = new Subscription(pushEndpoint, new Subscription.Keys(pushUserKey, pushAuth));
            try {
                String notificationJson = objectMapper.writeValueAsString(un);
                HttpResponse pushResponse = pushService.send(new Notification(sub, notificationJson));

                int statusCode = pushResponse.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    log.debug("Successfully sent push notification to {} with status {}", 
                            pushEndpoint, statusCode);
                } else {
                    String reason = pushResponse.getStatusLine().getReasonPhrase();
                    log.warn("Push notification to {} failed with status {} and reason {}", 
                            pushEndpoint, statusCode, reason);
                    
                    // Handle subscription cleanup for permanent failures
                    if (statusCode == 410 || statusCode == 404 || statusCode == 400) {
                        log.info("Removing invalid push subscription for user {} due to status {}", 
                                un.getToUser(), statusCode);
                        // Clear the invalid subscription
                        clearPushSubscription(pushSubscription);
                    } else if (statusCode == 403) {
                        log.warn("Push authentication failed (403) - check VAPID configuration");
                    }
                }
            } catch (Exception e) {
                log.error("Failed to serialize notification for push: {}", e.toString());
                log.debug("Stacktrace", e);
            }
        });
    }

    /**
     * Removes the specific push subscription
     */
    private void clearPushSubscription(PushSubscription subscription) {
        pushSubscriptionRepository.delete(subscription);
        log.info("Removed invalid push subscription {} for user {}", subscription.getEndpoint(), subscription.getUserId());
    }

    /**
     * Helper class for sending formatted emails
     */
    private class EmailSender {

        // Constants for email formatting
        private static final String MULTIPART_BOUNDARY = "======sakai-multi-part-boundary======";
        private static final String BOUNDARY_LINE = "\n\n--" + MULTIPART_BOUNDARY + "\n";
        private static final String TERMINATION_LINE = "\n\n--" + MULTIPART_BOUNDARY + "--\n\n";
        private static final String MIME_ADVISORY = "This message is for MIME-compliant mail readers.";
        private static final String PLAIN_TEXT_HEADERS = "Content-Type: text/plain\n\n";
        private static final String HTML_HEADERS = "Content-Type: text/html; charset=UTF-8\n\n";
        private static final String HTML_END = "\n  </body>\n</html>\n";

        private final User user;
        private final String subject;
        private final String message;

        /**
         * Constructor for EmailSender
         * 
         * @param user the user to send email to
         * @param subject the email subject
         * @param message the email message body (HTML)
         */
        public EmailSender(User user, String subject, String message) {
            this.user = user;
            this.subject = subject;
            this.message = message;
        }

        /**
         * Send the email to the user
         */
        public void send() {
            if (user == null) {
                log.error("Cannot send email to null user");
                return;
            }

            if (StringUtils.isBlank(user.getEmail())) {
                log.error("Cannot send email to user {} with no email address", user.getId());
                return;
            }

            try {
                emailService.sendToUsers(
                    Collections.singleton(user), 
                    getHeaders(user.getEmail(), this.subject),
                    formatMessage(user.getId(), this.subject, this.message)
                );
                log.info("Email sent to user: {}", user.getId());
            } catch (Exception e) {
                log.error("Failed to send email to user {}: {}", user.getId(), e.getMessage(), e);
            }
        }

        /**
         * Format the email message with both plain text and HTML parts
         * 
         * @param userId the user ID
         * @param subject the email subject
         * @param message the email message body (HTML)
         * @return the formatted email message
         */
        private String formatMessage(String userId, String subject, String message) {
            String enhancedMessage = addPreferencesLink(userId, message);

            return new StringBuilder(MIME_ADVISORY)
                .append(BOUNDARY_LINE)
                .append(PLAIN_TEXT_HEADERS)
                .append(StringEscapeUtils.escapeHtml4(enhancedMessage))
                .append(BOUNDARY_LINE)
                .append(HTML_HEADERS)
                .append(htmlPreamble(subject))
                .append(enhancedMessage)
                .append(HTML_END)
                .append(TERMINATION_LINE)
                .toString();
        }

        /**
         * Add a preferences link to the email message if possible
         * 
         * @param userId the user ID
         * @param message the original message
         * @return the message with preferences link added if possible
         */
        private String addPreferencesLink(String userId, String message) {
            try {
                Site userSite = siteService.getSite(siteService.getUserSiteId(userId));
                ToolConfiguration tc = userSite.getToolForCommonId("sakai.preferences");
                if (tc != null) {
                    String url = serverConfigurationService.getPortalUrl() + "/site/" + userSite.getId() + "/tool/" + tc.getId();
                    String prefsLink = "<br /><br />" + resourceLoader.getFormattedMessage("preferences_link_message", url);
                    return message + prefsLink;
                } else {
                    log.debug("No preferences tool on user {}'s site", userId);
                }
            } catch (IdUnusedException iue) {
                log.debug("User {} doesn't have a home site yet. Can't add preferences link.", userId);
            } catch (Exception e) {
                log.warn("Failed to add preferences link to email for user {}: {}", userId, e.getMessage());
            }
            return message;
        }

        /**
         * Create the HTML preamble for the email
         * 
         * @param subject the email subject
         * @return the HTML preamble
         */
        private String htmlPreamble(final String subject) {
            return new StringBuilder("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("  <meta charset=\"UTF-8\">\n")
                .append("  <title>")
                .append(subject)
                .append("</title>\n")
                .append("</head>\n")
                .append("<body>\n")
                .toString();
        }

        /**
         * Get the email headers
         * 
         * @param emailTo the recipient email address
         * @param subject the email subject
         * @return the list of email headers
         */
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

        /**
         * Get the From header for the email
         * 
         * @return the From header
         */
        private String getFrom() {
            return new StringBuilder("From: ")
                .append(serverConfigurationService.getString("ui.service", "Sakai"))
                .append(" <")
                .append(serverConfigurationService.getSmtpFrom())
                .append(">")
                .toString();
        }

        /**
         * Format the subject header for the email
         * 
         * @param subject the email subject
         * @return the formatted Subject header
         */
        private String formatSubject(final String subject) {
            return "Subject: " + subject;
        }
    }
}
