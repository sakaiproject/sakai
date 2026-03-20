/*
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
package org.sakaiproject.conversations.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.conversations.api.ConversationsEvent;
import org.sakaiproject.conversations.api.ConversationsReferenceReckoner;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.Permissions;
import org.sakaiproject.conversations.api.ShowDateContext;
import org.sakaiproject.conversations.api.TopicShowDateMessager;
import org.sakaiproject.conversations.api.TopicType;
import org.sakaiproject.conversations.api.model.ConversationsTopic;
import org.sakaiproject.conversations.api.repository.ConversationsTopicRepository;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.messaging.api.Message;
import org.sakaiproject.messaging.api.MessageMedium;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.ResourceLoader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TopicShowDateMessagerImpl implements TopicShowDateMessager {

    @Autowired private AuthzGroupService authzGroupService;
    @Autowired private ConversationsService conversationsService;
    @Autowired private EventTrackingService eventTrackingService;
    @Autowired private SiteService siteService;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private UserMessagingService userMessagingService;
    @Autowired private ConversationsTopicRepository topicRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Setter
    private ResourceLoader resourceLoader;

    @Override
    public void execute(String context) {

        try {
            ShowDateContext showDateContext = objectMapper.readValue(context, ShowDateContext.class);

            topicRepository.findById(showDateContext.getTopicId()).ifPresent(topic -> {

                message(topic, showDateContext.getWasNew());
            });
        } catch (JsonProcessingException e) {
            log.error("Exception while building json context for topic message sheduler: {}", e.toString());
        }
    }

    public void message(ConversationsTopic topic, boolean isNew) {

        String reference = ConversationsReferenceReckoner.reckoner().siteId(topic.getSiteId()).type("t").id(topic.getId()).reckon().getReference();

        ConversationsEvent event = isNew ? ConversationsEvent.TOPIC_CREATED : ConversationsEvent.TOPIC_UPDATED;
        eventTrackingService.post(eventTrackingService.newEvent(event.label, reference, topic.getSiteId(), true, NotificationService.NOTI_OPTIONAL));

        try {
            Site site = siteService.getSite(topic.getSiteId());

            Set<User> users = null;
            switch (topic.getVisibility()) {
                case SITE:
                    users = new HashSet<>(userDirectoryService.getUsers(site.getUsers()));
                    break;
                case GROUP:
                    Set<String> userIds = new HashSet<>(authzGroupService.getAuthzUsersInGroups(topic.getGroups()));
                    users = new HashSet<>(userDirectoryService.getUsers(userIds));
                    break;
                case INSTRUCTORS:
                    userIds = site.getUsersIsAllowed(Permissions.ROLETYPE_INSTRUCTOR.label);
                    users = new HashSet<>(userDirectoryService.getUsers(userIds));
                    break;
                default:
            }

            Map<String, Object> replacements = new HashMap<>();
            replacements.put("siteTitle", site.getTitle());
            replacements.put("topicTitle", topic.getTitle());
            replacements.put("topicUrl", conversationsService.getTopicPortalUrl(topic.getId()));
            replacements.put("bundle", new ResourceLoader("conversations_notifications"));

            userMessagingService.message(users,
                Message.builder()
                    .siteId(topic.getSiteId())
                    .tool(ConversationsService.TOOL_ID)
                    .type(topic.getType() == TopicType.QUESTION ? "newquestion" : "newdiscussion").build(),
                Arrays.asList(new MessageMedium[] {MessageMedium.EMAIL}), replacements, NotificationService.NOTI_OPTIONAL);
        } catch (IdUnusedException iue) {
            log.error("No group for site reference {}", siteService.siteReference(topic.getSiteId()));
        }
    }
}
