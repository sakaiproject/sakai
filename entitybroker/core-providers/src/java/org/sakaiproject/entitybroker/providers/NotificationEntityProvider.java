/**
 * $Id$
 * $URL$
 **************************************************************************
 * Copyright (c) 2014 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.providers;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * NotificationEntityProvider exposes a REST api to the notification service at baseURL/<code>PREFIX</code>/post/{userEId}
 *
 * Added for https://jira.sakaiproject.org/browse/SAK-25733
 * @author Bill Smith (wsmith @ unicon.net)
 * @author Aaron Zeckoski (azeckoski @ gmail.com) (azeckoski @ unicon.net)
 */
@Slf4j
public class NotificationEntityProvider extends AbstractEntityProvider implements ActionsExecutable, Describeable {

    /**
     * Config setting which determines if this is enabled
     */
    public static final String NOTIFY_POST_ENABLED = "notify.post.enabled";
    public static final String PREFIX = "notify";

    private EventTrackingService eventTrackingService;
    private NotificationService notificationService;
    private UserDirectoryService userDirectoryService;

    ExternalEmailNotification emailNotification;

    public String getEntityPrefix() {
        return PREFIX;
    }

    /**
     * Spring INIT method, runs on start
     */
    public void init() {
        log.info("INIT: enabled="+isEnabled());
        if (isEnabled()) {
            // don't bother loading up this stuff unless the notification is enabled
            NotificationEdit edit = notificationService.addTransientNotification();
            edit.setFunction("rest.notify.post");
            emailNotification = new ExternalEmailNotification();
            edit.setAction(emailNotification);
        } else {
            emailNotification = null;
        }
    }

    /**
     * Determine if the request is valid/authorized
     *
     * @return true if the request is valid/authorized.
     * @throws SecurityException if is not authorized
     */
    private boolean isAuthorized() {
        boolean authorized = false;
        if (developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference())) {
            authorized = true;
        } else {
            throw new SecurityException("Only a sakai super user can access this service: user="+developerHelperService.getCurrentUserId());
        }
        return authorized;
    }

    /**
     * Returns the enabled status of this service. This will eventually be used to turn the service on/off.
     *
     * @return true if the service is enabled.
     */
    public boolean isEnabled() {
        boolean notifyPostEnabled = developerHelperService.getConfigurationSetting(NOTIFY_POST_ENABLED, false);
        return notifyPostEnabled;
    }

    /**
     * Handles a request to create a new notification. Expects /notify/post/{userEId}
     *
     * @param view
     * @param params
     * @return
     */
    @EntityCustomAction(action = "post", viewKey = EntityView.VIEW_NEW)
    public void postNotification(EntityView view, Map<String, Object> params) {
        if (log.isDebugEnabled()) {
            log.debug("postNotification params: " + params);
        }
        if (!isEnabled()) {
            throw new IllegalStateException("Service is disabled. Check your configuration ("+NOTIFY_POST_ENABLED+" must be true)!");
        } else {
            isAuthorized(); // does check and throws exception if fails to pass

            String userEId = getUserEId(view);
            User user = getUserByEId(userEId);
            String notification = getNotification(params);

            emailNotification.addMessage(notification);
            Event event = eventTrackingService.newEvent("rest.notify.post", null, user.getId(), false, NotificationService.NOTI_OPTIONAL);
            eventTrackingService.post(event);
        }
    }


    /**
     * Get the notification from the request params
     * @param params request params
     * @return the notification string
     * @throws IllegalArgumentException if not found
     */
    private String getNotification(Map<String, Object> params) {
        String notification = (String) params.get("notification");
        if (notification == null || "".equals(notification)) {
            throw new IllegalArgumentException("Notification cannot be empty");
        }
        return notification;
    }

    /**
     * Attempts to get the user by EID
     *
     * @param userEId
     *         the EID of the user to retrieve
     * @return the specified user
     * @throws UserNotDefinedException
     *         if the user was not found
     */
    private User getUserByEId(String userEId) throws EntityNotFoundException {
        log.debug("Getting user for id: " + userEId);
        User user;
        try {
            user = userDirectoryService.getUserByEid(userEId);
        } catch (UserNotDefinedException e) {
            throw new EntityNotFoundException(e + ": could not find user for specified id. ", userEId);
        }
        return user;
    }

    /**
     * Extract the user eid from the view
     * @param view the entity view
     * @return user eid
     * @throws IllegalArgumentException if the user eid cannot be found
     */
    private String getUserEId(EntityView view) {
        String userEId = view.getPathSegment(2);
        if (userEId == null || "".equals(userEId)) {
            throw new IllegalArgumentException("User id cannot be empty");
        }
        return userEId;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

}
