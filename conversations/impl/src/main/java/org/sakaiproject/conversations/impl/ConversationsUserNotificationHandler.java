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
package org.sakaiproject.conversations.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;

import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.ConversationsEvent;
import org.sakaiproject.conversations.api.ConversationsReferenceReckoner;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.messaging.api.UserNotificationHandler;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConversationsUserNotificationHandler implements UserNotificationHandler{

    @Resource
    private ConversationsService conversationsService;

    @Resource
    private SiteService siteService;

    @Resource
    private UserMessagingService userMessagingService;

    @Resource
    private UserDirectoryService userDirectoryService;

    @Resource(name = "org.sakaiproject.util.ResourceLoader.conversations")
    private ResourceLoader resourceLoader;

    public void init() {
        userMessagingService.registerHandler(this);
    }

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(ConversationsEvent.POST_REPLIED.label, ConversationsEvent.POSTED_TO_TOPIC.label);
    }

    @Override
    public Optional<List<UserNotificationData>> handleEvent(Event e) {

        ConversationsReferenceReckoner.ConversationsReference ref = ConversationsReferenceReckoner.reckoner().reference(e.getResource()).reckon();


        return ConversationsEvent.from(e.getEvent()).map(ce -> {

            return switch (ce) {
                case POST_REPLIED: {
                    try {
                        yield conversationsService.getPost(ref.getId()).map(p -> {

                            if (StringUtils.isBlank(p.parentPost)) {
                                return Collections.<UserNotificationData>emptyList();
                            }

                            try {
                                return conversationsService.getPost(p.parentPost).map(pp -> {

                                    String from = p.creator;
                                    String to = pp.creator;
                                    if (from.equals(to)) {
                                        return Collections.<UserNotificationData>emptyList();
                                    }
                                    String url = conversationsService.getPostPortalUrl(p.topic, p.id).orElse("");
                                    return List.of(new UserNotificationData(from, to, ref.getSiteId(), "REPLY", url, ConversationsService.TOOL_ID, false, null));
                                }).orElseThrow();
                            } catch (Exception e2) {
                                log.warn("Exception thrown while getting post {}", p.parentPost, e2);
                                return Collections.<UserNotificationData>emptyList();
                            }
                        }).orElseThrow();
                    } catch (Exception e1) {
                        log.warn("Exception thrown while getting post {}", ref.getId(), e1);
                    }
                }
                case POSTED_TO_TOPIC: {
                    try {
                        yield conversationsService.getPost(ref.getId()).map(p -> {
                            try {
                                return conversationsService.getTopic(p.topic).map(t -> {
                                    String from = p.creator;
                                    String to = t.creator;
                                    if (from.equals(to)) {
                                        return Collections.<UserNotificationData>emptyList();
                                    }
                                    String url = conversationsService.getPostPortalUrl(p.topic, p.id).orElse("");
                                    return List.of(new UserNotificationData(from, to, ref.getSiteId(), "POST", url, ConversationsService.TOOL_ID, false, null));
                                }).orElseThrow();
                            } catch (Exception e2) {
                                log.warn("Exception thrown while getting topic {}", p.topic, e2);
                                return Collections.<UserNotificationData>emptyList();
                            }
                        }).orElseThrow();
                    } catch (Exception e1) {
                        log.warn("Exception thrown while getting post {}", ref.getId(), e1);
                    }
                }
                default: yield Collections.<UserNotificationData>emptyList();
            };
        });
    }

    @Override
    public String getTitle(String event, String reference) {

        ConversationsReferenceReckoner.ConversationsReference ref = ConversationsReferenceReckoner.reckoner().reference(reference).reckon();

        final Site site;
        try {
            site = siteService.getSite(ref.getSiteId());
        } catch (Exception e) {
            log.warn("Failed to get site for id {}", ref.getSiteId(), e);
            return "";
        }

        return ConversationsEvent.from(event).map(ce -> {

            return switch (ce) {
                case POSTED_TO_TOPIC -> {
                    try {
                        yield conversationsService.getPost(ref.getId()).map(p -> {

                            try {
                                return conversationsService.getTopic(p.topic).map(t -> {
                                    try {
                                        User u = userDirectoryService.getUser(p.creator);
                                        return resourceLoader.getFormattedMessage("topic_posted", u.getDisplayName(), t.title, site.getTitle());
                                    } catch (Exception e) {
                                        log.warn("Exception thrown while formatting topic_posted", e);
                                        return "";
                                    }
                                }).orElseThrow();
                            } catch (Exception e2) {
                                log.warn("Exception thrown while getting topic {}", p.topic, e2);
                                return "";
                            }
                        }).orElseThrow();
                    } catch (Exception e1) {
                        log.warn("Exception thrown while getting post {}", ref.getId(), e1);
                        yield "POSTED";
                    }
                }
                case POST_REPLIED -> {
                    try {
                        yield conversationsService.getPost(ref.getId()).map(p -> {

                            try {
                                User u = userDirectoryService.getUser(p.creator);
                                return resourceLoader.getFormattedMessage("post_replied", u.getDisplayName(), site.getTitle());
                            } catch (Exception e) {
                                log.warn("Exception thrown while formatting post_replied", e);
                                return "";
                            }
                        }).orElseThrow();
                    } catch (Exception e1) {
                        log.warn("Exception thrown while getting post {}", ref.getId(), e1);
                        yield "REPLIED";
                    }
                }
                default -> "UNKNOWN";
            };
        }).orElse("UNRECOGNISED EVENT");
    }
}
