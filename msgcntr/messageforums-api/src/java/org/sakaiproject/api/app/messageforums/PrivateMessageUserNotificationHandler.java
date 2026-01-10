/**
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
package org.sakaiproject.api.app.messageforums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.messaging.api.AbstractUserNotificationHandler;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PrivateMessageUserNotificationHandler extends AbstractUserNotificationHandler{

    @Resource
    private PrivateMessageManager privateMessageManager;
	
    @Resource
    private SiteService siteService;

    @Resource
    private ServerConfigurationService serverConfigurationService;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(DiscussionForumService.EVENT_MESSAGES_READ_RECEIPT);
    }

    @Override
    public Optional<List<UserNotificationData>> handleEvent(Event e) {

        String from = e.getUserId();

        String ref = e.getResource();
        String[] pathParts = ref.split("/");

        String siteId = pathParts[3];
        String pvtMessageId = pathParts[pathParts.length - 2];

        try {
            PrivateMessage pvtMessage = privateMessageManager.getPrivateMessageByDecryptedId(pvtMessageId);
            switch (e.getEvent()) {
                case DiscussionForumService.EVENT_MESSAGES_READ_RECEIPT:
                    return Optional.of(handleAdd(from, siteId, pvtMessage.getCreatedBy(), pvtMessage));
                default:
                    return Optional.empty();
            }
        } catch (Exception ex) {
            log.error("Failed to find the privateMessage: " + pvtMessageId, ex);
        }

        return Optional.empty();
    }

    private List<UserNotificationData> handleAdd(String from, String siteId, String userId, PrivateMessage pvtMessage) {

        List<UserNotificationData> notificationEvents = new ArrayList<>();

        Date openTime = pvtMessage.getCreated();
        if (openTime == null || openTime.before(new Date()) && !pvtMessage.getDraft()) {
            try {
                Site site = siteService.getSite(siteId);
                String title = pvtMessage.getTitle();
                if (!from.equals(userId)) {
                    String toolId = site.getToolForCommonId(DiscussionForumService.MESSAGES_TOOL_ID).getId();
                    String url = serverConfigurationService.getPortalUrl() + "/site/" + siteId
                            + "/tool/" + toolId + "/privateMsg/pvtMsgDirectAccess?current_msg_detail=" + pvtMessage.getId();
                    notificationEvents.add(new UserNotificationData(from, userId, siteId, title, url, DiscussionForumService.MESSAGES_TOOL_ID, false, null));
                }
            } catch (IdUnusedException idEx) {
                log.error("Failed to find the site: " + siteId, idEx);
            }
            
        }

        return notificationEvents;
    }
}
