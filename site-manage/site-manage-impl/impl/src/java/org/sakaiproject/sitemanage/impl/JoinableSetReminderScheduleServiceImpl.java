/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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
package org.sakaiproject.sitemanage.impl;

import java.time.Instant;
import java.util.Set;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.JoinableSetReminderScheduleService;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JoinableSetReminderScheduleServiceImpl implements JoinableSetReminderScheduleService {

    @Setter
    private SiteService siteService;

    @Setter
    private UserDirectoryService userDirectoryService;

    @Setter
    private ScheduledInvocationManager scheduledInvocationManager;

    @Setter
    private UserNotificationProvider userNotificationProvider;

    private static final String COMPONENT_ID = "org.sakaiproject.sitemanage.api.JoinableSetReminderScheduleService";

    public void scheduleJSetReminder(Instant dateTime, String dataPair) {
        removeScheduledJSetReminder(dataPair);
        scheduledInvocationManager.createDelayedInvocation(dateTime, COMPONENT_ID, dataPair);
    }

    public void removeScheduledJSetReminder(String dataPair) {
        scheduledInvocationManager.deleteDelayedInvocation(COMPONENT_ID, dataPair);
    }

    public void execute(String dataPair) {
        String splitData[] = dataPair.split(",", 2);
        String siteId = splitData[0];
        String joinableSetName = splitData[1];

        try {
            Site site = siteService.getSite(siteId);
            Set<String> userSet = site.getUsers();

            userSet.forEach(userId -> {
                try {
                    User user = userDirectoryService.getUser(userId);
                    userNotificationProvider.notifyJSetDayLeft(site.getTitle(), user, joinableSetName);
                } catch (UserNotDefinedException e) {
                    log.warn("No user with id {}", userId);
                }
            });
            log.info("Scheduled notification attempted");
        } catch (IdUnusedException e) {
            log.warn("No site with id {}", siteId);
        }
    }
}
