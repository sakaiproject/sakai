/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.messaging.api.UserNotificationTransferBean;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class NotificationsController extends AbstractSakaiApiController {

    @Autowired
    private UserMessagingService userMessagingService;

    @GetMapping(value = "/users/me/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserNotificationTransferBean> getNotifications() {

        checkSakaiSession();

        return userMessagingService.getNotifications();
    }

    @PostMapping(value = "/users/me/notifications/{id}/clear")
    public ResponseEntity clearNotification(@PathVariable Long id) {

        checkSakaiSession();

        try {
            userMessagingService.clearNotification(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to clear notification: {}", e.toString());
        }

        return ResponseEntity.internalServerError().build();
    }

    @PostMapping(value = "/users/me/notifications/clear")
    public ResponseEntity clearAllNotifications() {

        checkSakaiSession();

        try {
            userMessagingService.clearAllNotifications();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to clear all notifications: {}", e.toString());
        }

        return ResponseEntity.internalServerError().build();
    }

    @PostMapping(value = "/users/me/notifications/markViewed")
    public ResponseEntity markAllNotificationsViewed(@RequestParam(required = false) String siteId, @RequestParam(required = false)  String toolId) {

        checkSakaiSession();

        try {
            userMessagingService.markAllNotificationsViewed(siteId, toolId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to mark all notifications as viewed: {}", e.toString());
        }

        return ResponseEntity.internalServerError().build();
    }

    @PostMapping(value = "/users/me/notifications/test")
    public void sendTestNotification() {

        checkSakaiSession();

        userMessagingService.sendTestNotification();
    }

}
