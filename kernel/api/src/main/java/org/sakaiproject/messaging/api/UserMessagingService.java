/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.messaging.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.sakaiproject.user.api.User;
import org.sakaiproject.messaging.api.model.UserNotification;

/**
 * The user messaging service is intendened to the the single interface via which tools
 * send messages to users. Multiple media will be supported although at the moment only email
 * and digest are supported. The idea will be to support SMS and SSE events via this same
 * interface. The advantage of this approach is that we can handle user preference filtering here
 * rather than having that logic scattered through tool code.
 */
public interface UserMessagingService {

    public static final String PUSH_PUBKEY_PROPERTY = "portal.notifications.push.publickey";
    public static final String PUSH_PRIVKEY_PROPERTY = "portal.notifications.push.privatekey";

    /**
     * Send a message to a set of users, via 1 to many message media. If a template is involved
     * you can supply a map of token replacements. User preferences are queried by the
     * implementation and don't have to be handled by the caller.
     *
     * @param users A Set of users to message
     * @param media A list of MessageMedium objects to control how messages are sent
     * @param replacements A Map of replacement tokens for any template processing
     * @param priority a notification priority. If you supply NotificationService.NOTI_REQUIRED
     *          then user preferences will not be honoured and the message will go, regardless.
     *
     */
    void message(Set<User> users, Message message, List<MessageMedium> media, Map<String, Object> replacements, int priority);

    /**
     * Method for importing templates into the EmailTemplateService
     *
     * @param templateResource The resource path for your template. The current thread classloader
     *          will be used to attempt to load a stream for this.
     * @param templateRegistrationKey the unique key to identifiy this particular template when
     *          sending messages.
     */
    boolean importTemplateFromResourceXmlFile(String templateResource, String templateRegistrationKey);

    /**
     * @return the list of notifications for the current user
     */
    public List<UserNotificationTransferBean> getNotifications();

    /**
     * Register a handler for broadcast messages. The first registered handler that
     * handles a given event will receive it exclusively.
     *
     * @param handler a broadcast message handler; may handle multiple events
     */
    void registerHandler(UserNotificationHandler handler);

    /**
     * Unregister a handler for broadcast messages from all of the events it handles.
     *
     * @param handler the broadcast message handler to unregister from events
     */
    void unregisterHandler(UserNotificationHandler handler);

    /**
     * @param id The id of the notification to clear
     * @return boolean to indicate success
     */
    public boolean clearNotification(long id);

    /**
     * @param userId The user to clear the notifications for
     * @return boolean to indicate success
     */
    public boolean clearAllNotifications();

    /**
     * @param userId The user whose notifications to mark as viewed
     * @return boolean to indicate success
     */
    public boolean markAllNotificationsViewed(String siteId, String toolId);

    /**
     * Subscribe the current user to the push service. This is related to browser push and the
     * parameters come from the browser vendor's push service via the Sakai client js.
     *
     * @param endpoint The browser push service supplied endpoint
     * @param auth The browser push service supplied auth
     * @param auth The browser push service supplied userKey
     */
    public void subscribeToPush(String endpoint, String auth, String userKey, String browserFingerprint);

    public void sendTestNotification();
}
