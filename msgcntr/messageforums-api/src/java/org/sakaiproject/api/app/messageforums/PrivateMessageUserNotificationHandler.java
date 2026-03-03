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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import org.apache.commons.lang3.StringUtils;

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
        return Arrays.asList(DiscussionForumService.EVENT_MESSAGES_READ_RECEIPT,
                DiscussionForumService.EVENT_MESSAGES_ADD);
    }

    @Override
    public Optional<List<UserNotificationData>> handleEvent(Event e) {

        String from = e.getUserId();

        String ref = e.getResource();
        String[] pathParts = ref.split("/");

        if (pathParts.length < 6) {
            log.warn("Invalid reference for event {}", e.getEvent());
            return Optional.empty();
        }

        String siteId = pathParts[3];
        String pvtMessageId = pathParts[5];

        try {
            PrivateMessage pvtMessage = privateMessageManager.getPrivateMessageByDecryptedId(pvtMessageId);
            switch (e.getEvent()) {
                case DiscussionForumService.EVENT_MESSAGES_READ_RECEIPT:
                    return Optional.of(handleReadReceipt(from, siteId, pvtMessage.getCreatedBy(), pvtMessage));
                case DiscussionForumService.EVENT_MESSAGES_ADD:
                    return Optional.of(handleAdd(from, siteId, pvtMessage));
                default:
                    return Optional.empty();
            }
        } catch (Exception ex) {
            log.error("Failed to find the privateMessage: {}", pvtMessageId, ex);
        }

        return Optional.empty();
    }

    private List<UserNotificationData> handleReadReceipt(String from, String siteId, String to, PrivateMessage pvtMessage) {

        List<UserNotificationData> notificationEvents = new ArrayList<>();

        Date openTime = pvtMessage.getCreated();
        if ((openTime == null || openTime.before(new Date())) && !pvtMessage.getDraft()) {
            try {
                Site site = siteService.getSite(siteId);
                String title = pvtMessage.getTitle();
                if (!StringUtils.equals(from, to)) {
                    String toolId = site.getToolForCommonId(DiscussionForumService.MESSAGES_TOOL_ID).getId();
                    String url = serverConfigurationService.getPortalUrl() + "/site/" + siteId
                            + "/tool/" + toolId + "/privateMsg/pvtMsgDirectAccess?current_msg_detail=" + pvtMessage.getId();
                    notificationEvents.add(new UserNotificationData(from, to, siteId, title, url, DiscussionForumService.MESSAGES_TOOL_ID, false, null));
                }
            } catch (IdUnusedException idEx) {
                log.error("Failed to find the site: {}", siteId, idEx);
            }
            
        }

        return notificationEvents;
    }

    private List<UserNotificationData> handleAdd(String from, String siteId, PrivateMessage pvtMessage) {

        // If the message is not open yet or is draft, return empty.
        Date openTime = pvtMessage.getCreated();
        if ((openTime != null && openTime.after(new Date())) || pvtMessage.getDraft()) {
            return Collections.emptyList();
        }

        try {
            Site site = siteService.getSite(siteId);
            String title = pvtMessage.getTitle();
            String toolId = site.getToolForCommonId(DiscussionForumService.MESSAGES_TOOL_ID).getId();
            String url = serverConfigurationService.getPortalUrl() + "/site/" + siteId
                    + "/tool/" + toolId + "/privateMsg/pvtMsgDirectAccess?current_msg_detail=" + pvtMessage.getId();

            return pvtMessage.getRecipients()
                .stream()
                .filter(r -> !StringUtils.equals(r.getUserId(), from))
                .map(r -> new UserNotificationData(from, r.getUserId(), siteId, title, url, DiscussionForumService.MESSAGES_TOOL_ID, false, null))
                .collect(Collectors.toList());
        } catch (IdUnusedException idEx) {
            log.error("No site for id {}", siteId, idEx);
        }

        return Collections.emptyList();
    }
}
