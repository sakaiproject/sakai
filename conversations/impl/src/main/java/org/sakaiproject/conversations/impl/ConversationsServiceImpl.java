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

import java.time.Instant;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.conversations.api.ConversationsPermissionsException;
import org.sakaiproject.conversations.api.ConversationsReferenceReckoner;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.ConversationsStat;
import org.sakaiproject.conversations.api.ConversationsEvents;
import org.sakaiproject.conversations.api.Permissions;
import org.sakaiproject.conversations.api.PostSort;
import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.conversations.api.TopicType;
import org.sakaiproject.conversations.api.TopicVisibility;
import org.sakaiproject.conversations.api.beans.CommentTransferBean;
import org.sakaiproject.conversations.api.beans.TopicTransferBean;
import org.sakaiproject.conversations.api.beans.PostTransferBean;
import org.sakaiproject.conversations.api.model.ConversationsComment;
import org.sakaiproject.conversations.api.model.ConvStatus;
import org.sakaiproject.conversations.api.model.ConversationsPost;
import org.sakaiproject.conversations.api.model.PostReaction;
import org.sakaiproject.conversations.api.model.PostReactionTotal;
import org.sakaiproject.conversations.api.model.PostStatus;
import org.sakaiproject.conversations.api.model.Settings;
import org.sakaiproject.conversations.api.model.Tag;
import org.sakaiproject.conversations.api.model.ConversationsTopic;
import org.sakaiproject.conversations.api.model.TopicReaction;
import org.sakaiproject.conversations.api.model.TopicReactionTotal;
import org.sakaiproject.conversations.api.model.TopicStatus;
import org.sakaiproject.conversations.api.repository.ConversationsCommentRepository;
import org.sakaiproject.conversations.api.repository.ConversationsTopicRepository;
import org.sakaiproject.conversations.api.repository.ConvStatusRepository;
import org.sakaiproject.conversations.api.repository.ConversationsPostRepository;
import org.sakaiproject.conversations.api.repository.PostReactionRepository;
import org.sakaiproject.conversations.api.repository.PostReactionTotalRepository;
import org.sakaiproject.conversations.api.repository.PostStatusRepository;
import org.sakaiproject.conversations.api.repository.SettingsRepository;
import org.sakaiproject.conversations.api.repository.TagRepository;
import org.sakaiproject.conversations.api.repository.TopicReactionRepository;
import org.sakaiproject.conversations.api.repository.TopicReactionTotalRepository;
import org.sakaiproject.conversations.api.repository.TopicStatusRepository;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.messaging.api.Message;
import org.sakaiproject.messaging.api.MessageMedium;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import lombok.Setter;

@Slf4j
@Setter
@Transactional
public class ConversationsServiceImpl implements ConversationsService, Observer {

    private AuthzGroupService authzGroupService;

    private FunctionManager functionManager;

    private ConversationsCommentRepository commentRepository;

    private ConvStatusRepository convStatusRepository;

    private EventTrackingService eventTrackingService;

    private MemoryService memoryService;

    private ConversationsPostRepository postRepository;

    private PostReactionRepository postReactionRepository;

    private PostReactionTotalRepository postReactionTotalRepository;

    private PostStatusRepository postStatusRepository;

    private SecurityService securityService;

    private ServerConfigurationService serverConfigurationService;

    private SessionManager sessionManager;

    private SettingsRepository settingsRepository;

    private SiteService siteService;

    private StatsManager statsManager;

    private TagRepository tagRepository;

    private TopicReactionRepository topicReactionRepository;

    private TopicReactionTotalRepository topicReactionTotalRepository;

    private ConversationsTopicRepository topicRepository;

    private TopicStatusRepository topicStatusRepository;

    private UserDirectoryService userDirectoryService;

    private UserMessagingService userMessagingService;

    private UserTimeService userTimeService;

    private ResourceLoader resourceLoader;

    private Cache<String, List<ConversationsStat>> sortedStatsCache;
    private Cache<String, Map<String, Map<String, Object>>> postsCache;

    public void init() {

        Permissions.stream().forEach(p -> functionManager.registerFunction(p.label, true));
        this.sortedStatsCache = memoryService.<String, List<ConversationsStat>>getCache(STATS_CACHE_NAME);
        this.postsCache = memoryService.<String, Map<String, Map<String, Object>>>getCache(POSTS_CACHE_NAME);
        eventTrackingService.addObserver(this);

        userMessagingService.importTemplateFromResourceXmlFile("emailtemplates/new_question.xml", TOOL_ID + ".newquestion");
        userMessagingService.importTemplateFromResourceXmlFile("emailtemplates/new_discussion.xml", TOOL_ID + ".newdiscussion");
        userMessagingService.importTemplateFromResourceXmlFile("emailtemplates/instructor_answer.xml", TOOL_ID + ".instructoranswer");
        userMessagingService.importTemplateFromResourceXmlFile("emailtemplates/instructor_reply.xml", TOOL_ID + ".instructorreply");
        userMessagingService.importTemplateFromResourceXmlFile("emailtemplates/reply.xml", TOOL_ID + ".reply");
    }

    public void update(Observable observable, Object arg) {

        if (arg instanceof Event) {
            Event e = (Event) arg;
            String event = e.getEvent();
            if (event.equals(AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP)) {
                String baseCacheKey = e.getContext() + "/conversations/";
                this.sortedStatsCache.remove(baseCacheKey + SORT_NAME_ASCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_NAME_DESCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_TOPICS_CREATED_ASCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_TOPICS_CREATED_DESCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_TOPICS_VIEWED_ASCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_TOPICS_VIEWED_DESCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_POSTS_CREATED_ASCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_POSTS_CREATED_DESCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_REACTIONS_MADE_ASCENDING);
            }
        }
    }

    public String getEntityPrefix() {
        return Entity.SEPARATOR + "conversations";
    }

    public TopicTransferBean getBlankTopic(String siteId) throws ConversationsPermissionsException {

        String siteRef = siteService.siteReference(siteId);

        if (StringUtils.isBlank(siteRef)) {
            throw new IllegalArgumentException("Failed to get siteRef for siteId: " + siteId);
        }

        if (!securityService.unlock(Permissions.TOPIC_CREATE.label, siteRef)) {
            throw new ConversationsPermissionsException("Can't create a blank topic");
        }

        String currentUserId = sessionManager.getCurrentSessionUserId();

        TopicTransferBean blankTopic = new TopicTransferBean();

        blankTopic.id = "";
        blankTopic.creator = currentUserId;
        blankTopic.siteId = siteId;
        blankTopic.title = "";
        blankTopic.message = "";
        blankTopic.type = TopicType.QUESTION.name();
        blankTopic.availability = "AVAILABILITY_NOW";
        blankTopic.pinned = false;
        blankTopic.aboutReference = siteRef;

        return decorateTopicBean(blankTopic, null, currentUserId, getSettingsForSite(siteId));
    }

    public Optional<TopicTransferBean> getTopic(String topicId) throws ConversationsPermissionsException {
        return topicRepository.findById(topicId).map(TopicTransferBean::of);
    }

    public boolean currentUserCanViewTopic(ConversationsTopic topic) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(currentUserId)) return false;

        final String siteRef = siteService.siteReference(topic.getSiteId());

        if (!securityService.unlock(SiteService.SITE_VISIT, siteRef)) return false;

        if (!topic.getDraft() && securityService.isSuperUser()) return true;

        Instant now = Instant.now();

        if (topic.getHidden() || (topic.getHideDate() != null && topic.getHideDate().isBefore(now))) {
            if (securityService.unlock(Permissions.MODERATE.label, siteRef)) return true;
            else return false;
        }

        if (topic.getMetadata().getCreator().equals(currentUserId)) return true;

        if (!topic.getDraft() && topic.getVisibility() == TopicVisibility.SITE) return true;

        if (!topic.getDraft() && topic.getVisibility() == TopicVisibility.INSTRUCTORS
                    && securityService.unlock(Permissions.ROLETYPE_INSTRUCTOR.label, siteRef)) {
            return true;
        }

        if (topic.getVisibility() == TopicVisibility.GROUP) {
            if (securityService.unlock(Permissions.VIEW_GROUP_TOPICS.label, siteRef)) return true;

            ArrayList<String> groups = new ArrayList<>(topic.getGroups());
            if (authzGroupService.getAuthzUserGroupIds(groups, currentUserId).stream().findAny().isPresent()) {
                return true;
            }
        }

        return false;
    }

    public List<TopicTransferBean> getTopicsForSite(String siteId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        final String reference = "/site/" + siteId;
        if (!securityService.unlock(SiteService.SITE_VISIT, reference)) {
            throw new ConversationsPermissionsException("Current user cannot view topics.");
        }

        Settings settings = getSettingsForSite(siteId);

        List<ConversationsTopic> topics = topicRepository.findBySiteId(siteId).stream()
            //.map(this::setupDateState)
            .map(this::showIfAfterShowDate)
            .map(this::lockIfAfterLockDate)
            .map(this::hideIfAfterHideDate)
            .filter(this::currentUserCanViewTopic)
            .collect(Collectors.toList());

        return decorateTopics(topics, currentUserId, settings);
    }

    public Optional<String> getTopicPortalUrl(String topicId) {

        return topicRepository.findById(topicId).map(t -> {

            try {
                Site site = siteService.getSite(t.getSiteId());

                return new StringBuilder(serverConfigurationService.getPortalUrl())
                    .append("/site/")
                    .append(t.getSiteId())
                    .append("/tool/")
                    .append(site.getToolForCommonId(TOOL_ID).getId())
                    .append("/topics/")
                    .append(topicId).toString();
            } catch (IdUnusedException iue) {
                log.error("No site for id {}", t.getSiteId());
                return null;
            }
        });
    }

    public Optional<String> getPostPortalUrl(String topicId, String postId) {

        if (StringUtils.isBlank(topicId)) {
            topicId = postRepository.findById(postId).map(p -> p.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("No post for id: " + postId));
        }

        return topicRepository.findById(topicId).map(t -> {

            try {
                Site site = siteService.getSite(t.getSiteId());

                return new StringBuilder(serverConfigurationService.getPortalUrl())
                    .append("/site/")
                    .append(t.getSiteId())
                    .append("/tool/")
                    .append(site.getToolForCommonId(TOOL_ID).getId())
                    .append("/topics/")
                    .append(t.getId())
                    .append("/posts/")
                    .append(postId).toString();
            } catch (IdUnusedException iue) {
                log.error("No site for id {}", t.getSiteId());
                return null;
            }
        });
    }

    public Optional<String> getCommentPortalUrl(String commentId) {

        String postId = commentRepository.findById(commentId).map(c -> c.getPostId())
            .orElseThrow(() -> new IllegalArgumentException("No comment for id: " + commentId));

        String topicId = postRepository.findById(postId).map(p -> p.getTopicId())
            .orElseThrow(() -> new IllegalArgumentException("No post for id: " + postId));

        return topicRepository.findById(topicId).map(t -> {

            try {
                Site site = siteService.getSite(t.getSiteId());

                return new StringBuilder(serverConfigurationService.getPortalUrl())
                    .append("/site/")
                    .append(t.getSiteId())
                    .append("/tool/")
                    .append(site.getToolForCommonId(TOOL_ID).getId())
                    .append("/topics/")
                    .append(topicId)
                    .append("/posts/")
                    .append(postId)
                    .append("/comments/")
                    .append(commentId).toString();
            } catch (IdUnusedException iue) {
                log.error("No site for id {}", t.getSiteId());
                return null;
            }
        });
    }

    public TopicTransferBean saveTopic(final TopicTransferBean topicBean, boolean sendMessage) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        String siteRef = siteService.siteReference(topicBean.siteId);

        Settings settings = getSettingsForSite(topicBean.siteId);

        boolean isModerator = securityService.unlock(Permissions.MODERATE.label, siteRef);

        if (settings.getSiteLocked() && !isModerator) {
            throw new ConversationsPermissionsException("Current user cannot create topic.");
        }

        boolean isNew = StringUtils.isBlank(topicBean.id);
        boolean isMine = !isNew && topicBean.creator.equals(currentUserId);

        if (isNew) {
            if (!securityService.unlock(Permissions.TOPIC_CREATE.label, siteRef)) {
                throw new ConversationsPermissionsException("Current user cannot create topic.");
            }
        } else if (!securityService.unlock(Permissions.TOPIC_UPDATE_ANY.label, siteRef)
                && (isMine && !securityService.unlock(Permissions.TOPIC_UPDATE_OWN.label, siteRef))) {
            throw new ConversationsPermissionsException("Current user cannot update topic.");
        }

        boolean wasDraft = false;

        Instant now = Instant.now();
        if (isNew) {
            topicBean.setCreator(currentUserId);
            topicBean.setCreated(now);
            if (!isModerator) {
                // Only moderators can lock or hide topics
                topicBean.showDate = null;
                topicBean.hideDate = null;
                topicBean.lockDate = null;
            }
        } else {
            ConversationsTopic topic = topicRepository.findById(topicBean.id)
                .orElseThrow(() -> new IllegalArgumentException("No existing topic for " + topicBean.id));

            wasDraft = topic.getDraft() && !topicBean.draft;

            if (topic.getLocked() && !isModerator) {
                throw new ConversationsPermissionsException("Current user cannot update topic.");
            }

            if (topicBean.showDate == null && topic.getShowDate() != null) {
                // show date has been removed
                if (topicBean.hideDate == null || topicBean.hideDate.isAfter(now)) {
                    topicBean.hidden = false;
                }
            }

            if (topicBean.lockDate == null && topic.getLockDate() != null) {
                // lock date has been removed
                topicBean.locked = false;
            }

            // Only moderators can set a show or lock date
            if ((!Objects.equals(topic.getShowDate(), topicBean.showDate)
                || !Objects.equals(topic.getHideDate(), topicBean.hideDate)
                || !Objects.equals(topic.getLockDate(), topicBean.lockDate)) && !isModerator) {
                throw new ConversationsPermissionsException("Current user cannot update show, hide or lock dates.");
            }

            // We remove the cache of posts for this topic. This clears the cache out for every user
            // but seems the safest way of catching changes like due and availability dates. Maybe
            // we will need to be more precise about when we need to do this, like when the due date
            // has been updated, or whatever.
            postsCache.remove(topicBean.id);
        }
        topicBean.setModifier(currentUserId);
        topicBean.setModified(now);

        if (topicBean.showDate != null && topicBean.showDate.isAfter(now)) {
            topicBean.hidden = true;
        }

        ConversationsTopic topic = topicRepository.save(topicBean.asTopic());

        TopicTransferBean outTopicBean = TopicTransferBean.of(topic);

        outTopicBean.tags = topic.getTagIds().stream().map(tagId -> {

            Optional<Tag> optTag = tagRepository.findById(tagId);
            if (optTag.isPresent()) {
                return optTag.get();
            } else {
                return null;
            }
        }).collect(Collectors.toList());

        TopicTransferBean decoratedBean = decorateTopicBean(outTopicBean, topic, currentUserId, settings);

        final boolean finalWasDraft = wasDraft;

        this.afterCommit(() -> {

            ConversationsEvents event = isNew ? ConversationsEvents.TOPIC_CREATED : ConversationsEvents.TOPIC_UPDATED;
            eventTrackingService.post(eventTrackingService.newEvent(event.label, decoratedBean.reference, decoratedBean.siteId, true, NotificationService.NOTI_OPTIONAL));

            if (sendMessage && (isNew || finalWasDraft) && !topic.getDraft()) {
                try {
                    Site site = siteService.getSite(decoratedBean.siteId);

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
                    replacements.put("topicTitle", decoratedBean.title);
                    replacements.put("topicUrl", decoratedBean.portalUrl);
                    replacements.put("bundle", new ResourceLoader("conversations_notifications"));

                    userMessagingService.message(users,
                        Message.builder()
                            .siteId(decoratedBean.siteId)
                            .tool(TOOL_ID)
                            .type(topic.getType() == TopicType.QUESTION ? "newquestion" : "newdiscussion").build(),
                        Arrays.asList(new MessageMedium[] {MessageMedium.EMAIL}), replacements, NotificationService.NOTI_OPTIONAL);
                } catch (IdUnusedException iue) {
                    log.error("No group for site reference {}", siteRef);
                }
            }
        });
        
        return decoratedBean;
    }

    public void pinTopic(String topicId, boolean pinned) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        ConversationsTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        if (!securityService.unlock(Permissions.TOPIC_PIN.label, "/site/" + topic.getSiteId())) {
            throw new ConversationsPermissionsException("Current user cannot pin topics.");
        }

        topic.setPinned(pinned);
        topicRepository.save(topic);
    }

    public TopicTransferBean lockTopic(String topicId, boolean locked, boolean needsModerator) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        ConversationsTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        if (needsModerator && !securityService.unlock(Permissions.MODERATE.label, "/site/" + topic.getSiteId())) {
            throw new ConversationsPermissionsException("Current user cannot lock/unlock topics.");
        }

        topic.setLocked(locked);

        if (!locked) {
            topic.setLockDate(null);
        }

        postRepository.lockByTopicId(locked, topicId);
        postRepository.findByTopicId(topicId).forEach(p -> recursivelyLockPosts(p, locked));

        Settings settings = getSettingsForSite(topic.getSiteId());
        topic = topicRepository.save(topic);
        TopicTransferBean bean = decorateTopicBean(TopicTransferBean.of(topic), topic, currentUserId, settings);
        postsCache.remove(topicId);
        return bean;
    }

    public void recursivelyLockPosts(ConversationsPost post, Boolean locked) {

        postRepository.lockByParentPostId(locked, post.getId());
        commentRepository.lockByPostId(post.getId(), locked);
        postRepository.findByParentPostId(post.getId()).forEach(p -> recursivelyLockPosts(p, locked));
    }

    public ConversationsTopic hideTopic(String topicId, boolean hidden, boolean needsModerator) throws ConversationsPermissionsException {

        getCheckedCurrentUserId();

        ConversationsTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        if (needsModerator && !securityService.unlock(Permissions.MODERATE.label, "/site/" + topic.getSiteId())) {
            throw new ConversationsPermissionsException("Current user cannot lock/unlock topics.");
        }

        topic.setHidden(hidden);

        if (!hidden) {
            topic.setHideDate(null);
            topic.setShowDate(null);
        }

        return topicRepository.save(topic);
    }

    public void bookmarkTopic(String topicId, boolean bookmarked) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        topicRepository.findById(topicId).ifPresent(topic -> {

            TopicStatus topicStatus = topicStatusRepository.findByTopicIdAndUserId(topicId, currentUserId)
                .orElseGet(() -> new TopicStatus(topic.getSiteId(), topicId, currentUserId));
            topicStatus.setBookmarked(bookmarked);
            topicStatusRepository.save(topicStatus);
        });
    }

    public void deleteTopic(String topicId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        ConversationsTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        String siteRef = "/site/" + topic.getSiteId();

        boolean isMine = topic.getMetadata().getCreator().equals(currentUserId);
        if (!securityService.unlock(Permissions.TOPIC_DELETE_ANY.label, siteRef)
            && (isMine && !securityService.unlock(Permissions.TOPIC_DELETE_OWN.label, siteRef))) {
            throw new ConversationsPermissionsException("Current user is not allowed to delete topic.");
        }

        commentRepository.deleteByTopicId(topicId);

        postRepository.findByTopicId(topicId).forEach(p -> {

            postReactionRepository.deleteByPostId(p.getId());
            postReactionTotalRepository.deleteByPostId(p.getId());
            postStatusRepository.deleteByPostId(p.getId());
            postRepository.deleteById(p.getId());
        });
        topicStatusRepository.deleteByTopicId(topicId);
        topicReactionRepository.deleteByTopicId(topicId);
        topicReactionTotalRepository.deleteByTopicId(topicId);
        topicRepository.delete(topic);

        afterCommit(() -> {
            String ref = ConversationsReferenceReckoner.reckoner()
                .siteId(topic.getSiteId())
                .type("t").id(topicId).reckon().getReference();
            eventTrackingService.post(eventTrackingService.newEvent(ConversationsEvents.TOPIC_DELETED.label, ref, topic.getSiteId(), true, NotificationService.NOTI_OPTIONAL));
        });
    }

    public Map<Reaction, Integer> saveTopicReactions(String topicId, Map<Reaction, Boolean> reactions) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        ConversationsTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        if (topic.getMetadata().getCreator().equals(currentUserId)) {
            throw new ConversationsPermissionsException("You can't react to your own topics");
        }

        List<TopicReaction> current = topicReactionRepository.findByTopicIdAndUserId(topicId, currentUserId);

        reactions.entrySet().forEach(es -> {

            TopicReactionTotal total
                = topicReactionTotalRepository.findByTopicIdAndReaction(topicId, es.getKey())
                    .orElseGet(() -> {
                        TopicReactionTotal t = new TopicReactionTotal();
                        t.setTopicId(topicId);
                        t.setReaction(es.getKey());
                        t.setTotal(0);
                        return t;
                    });

            String ref = ConversationsReferenceReckoner.reckoner()
                .siteId(topic.getSiteId())
                .type("t").id(topicId).reckon().getReference();
            boolean postReactedEvent = true;
            Optional<TopicReaction> optExistingReaction = current.stream().filter(tr -> tr.getReaction() == es.getKey()).findAny();
            if (optExistingReaction.isPresent()) {
                TopicReaction existingReaction = optExistingReaction.get();
                if (!existingReaction.getState() && es.getValue()) {
                    // This reaction is being turned on. Increment the total.
                    total.setTotal(total.getTotal() + 1);
                } else if (existingReaction.getState() && !es.getValue()) {
                    // This reaction is being turned off. Decrement the total.
                    total.setTotal(total.getTotal() - 1);
                    postReactedEvent = false;
                    afterCommit(() -> {
                        eventTrackingService.post(eventTrackingService.newEvent(ConversationsEvents.UNREACTED_TO_TOPIC.label, ref, topic.getSiteId(), false, NotificationService.NOTI_OPTIONAL));
                    });
                }
                existingReaction.setState(es.getValue());
                topicReactionRepository.save(existingReaction);
            } else {
                TopicReaction newReaction = new TopicReaction();
                newReaction.setTopicId(topic.getId());
                newReaction.setUserId(currentUserId);
                newReaction.setReaction(es.getKey());
                newReaction.setState(es.getValue());
                topicReactionRepository.save(newReaction);
                if (es.getValue()) {
                    total.setTotal(total.getTotal() + 1);
                }
            }

            if (postReactedEvent) {
                afterCommit(() -> {
                    eventTrackingService.post(eventTrackingService.newEvent(ConversationsEvents.REACTED_TO_TOPIC.label, ref, topic.getSiteId(), false, NotificationService.NOTI_OPTIONAL));
                });
            }
            topicReactionTotalRepository.save(total);
        });

        return topicReactionTotalRepository.findByTopicId(topic.getId())
                .stream().collect(Collectors.toMap(rt -> rt.getReaction(), rt -> rt.getTotal()));
    }

    public Optional<PostTransferBean> getPost(String postId) throws ConversationsPermissionsException {
        return postRepository.findById(postId).map(PostTransferBean::of);
    }

    public PostTransferBean savePost(PostTransferBean postBean, boolean sendMessage) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        String siteRef = siteService.siteReference(postBean.siteId);

        Settings settings = getSettingsForSite(postBean.siteId);

        if (settings.getSiteLocked() && !securityService.unlock(Permissions.MODERATE.label, siteRef)) {
            throw new ConversationsPermissionsException("Current user cannot save posts.");
        }

        boolean isNew = StringUtils.isBlank(postBean.id);
        boolean isMine = !isNew && postBean.creator.equals(currentUserId);

        if (isNew) {
            if (!securityService.unlock(Permissions.POST_CREATE.label, siteRef)) {
                throw new ConversationsPermissionsException("Current user cannot create posts.");
            }
        } else if (!securityService.unlock(Permissions.POST_UPDATE_ANY.label, siteRef)
                && (isMine && !securityService.unlock(Permissions.POST_UPDATE_OWN.label, siteRef))) {
            throw new ConversationsPermissionsException("Current user cannot update posts.");
        }

        final ConversationsTopic topic = topicRepository.findById(postBean.topic)
            .orElseThrow(() -> new IllegalArgumentException("No topic for id " + postBean.topic));

        boolean wasDraft = false;

        // We're creating a new topic, so set the initial dates of creation and modification
        Instant now = Instant.now();
        if (isNew) {
            postBean.setCreator(currentUserId);
            postBean.setCreated(now);
        } else {
            wasDraft = postRepository.findById(postBean.id).map(p -> p.getDraft() && !postBean.draft).orElse(false);
        }
        postBean.setModifier(currentUserId);
        postBean.setModified(now);

        ConversationsPost post = postBean.asPost();

        if ((topic.getLocked() || (topic.getLockDate() != null && now.isAfter(topic.getLockDate()))) && !securityService.unlock(Permissions.MODERATE.label, siteRef)) {
                throw new ConversationsPermissionsException("Current user cannot update posts on locked topics.");
            }

        Optional<ConversationsPost> parent = Optional.empty();
        if (StringUtils.isNotBlank(postBean.parentPost)) {
            parent = postRepository.findById(postBean.parentPost);

            if (parent.isPresent()) {
                post.setDepth(parent.get().getDepth() + 1);
            } else {
                throw new IllegalArgumentException("No post for id " + postBean.parentPost);
            }
        } else {
            post.setDepth(1);
        }

        post.setTopicId(postBean.topic);
        post.setLocked(topic.getLocked());
        post = postRepository.save(post);

        postsCache.remove(postBean.topic);

        if (StringUtils.isNotBlank(postBean.parentThread)) {
            postRepository.findById(postBean.parentThread).ifPresent(thread -> {

                thread.setNumberOfThreadReplies(thread.getNumberOfThreadReplies() + 1);
                postRepository.save(thread);
                updateThreadHowActiveScore(thread);
            });
        }
        this.markPostViewed(postBean.topic, post.getId(), currentUserId);

        if (!post.getDraft() && !post.getPrivatePost()
            && securityService.unlock(postBean.creator, Permissions.ROLETYPE_INSTRUCTOR.label, siteRef)) {

            topic.setResolved(true);
            topicRepository.save(topic);
        }

        TopicStatus topicStatus = topicStatusRepository.findByTopicIdAndUserId(topic.getId(), currentUserId)
            .orElse(new TopicStatus(postBean.siteId, postBean.topic, currentUserId));
        topicStatus.setPosted(true);
        topicStatusRepository.save(topicStatus);

        topicStatusRepository.setViewedByTopicId(topic.getId(), false);

        PostTransferBean decoratedBean = decoratePostBean(PostTransferBean.of(post), postBean.siteId, topic, currentUserId, settings, null);

        // We have to do this to satisfy the lambda requirements
        Optional<ConversationsPost> optParent = parent;

        final boolean finalWasDraft = wasDraft;

        this.afterCommit(() -> {

            ConversationsEvents event = isNew ? ConversationsEvents.POST_CREATED : ConversationsEvents.POST_UPDATED;
            eventTrackingService.post(eventTrackingService.newEvent(event.label, decoratedBean.reference, postBean.siteId, true, NotificationService.NOTI_OPTIONAL));

            if (sendMessage && (isNew || finalWasDraft) && !postBean.draft) {
                try {
                    Site site = siteService.getSite(decoratedBean.siteId);

                    Map<String, Object> replacements = new HashMap<>();
                    replacements.put("siteTitle", site.getTitle());
                    replacements.put("topicTitle", topic.getTitle());
                    replacements.put("postUrl", decoratedBean.portalUrl);
                    replacements.put("creatorDisplayName", decoratedBean.creatorDisplayName);
                    replacements.put("bundle", new ResourceLoader("conversations_notifications"));

                    if (topic.getType() == TopicType.QUESTION && decoratedBean.isInstructor) {
                        Set<User> siteUsers = new HashSet<>(userDirectoryService.getUsers(site.getUsers()));
                        String topicCreator = topic.getMetadata().getCreator();
                        Set<User> questionCreator = Collections.singleton(userDirectoryService.getUser(topicCreator));
                        siteUsers.removeAll(questionCreator);
                        sendMessage(siteUsers, decoratedBean.siteId, replacements, "instructoranswer");

                        // Send a specific message to the question poster
                        sendMessage(questionCreator, decoratedBean.siteId, replacements, "instructorreply");
                    } else if (topic.getType() == TopicType.DISCUSSION && optParent.isPresent()) {
                        String parentCreator = optParent.get().getMetadata().getCreator();
                        if (!parentCreator.equals(currentUserId)) {
                            Set<User> users = Collections.singleton(userDirectoryService.getUser(parentCreator));
                            sendMessage(users, decoratedBean.siteId, replacements, "reply");
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to send notifications: {}", e.toString());
                }
            }
        });

        return decoratedBean;
    }

    public boolean currentUserCanViewPost(ConversationsPost post) {

        String currentUserId = sessionManager.getCurrentSessionUserId();
        if (StringUtils.isBlank(currentUserId)) return false;

        return canUserViewPost(post, currentUserId);
    }

    public boolean currentUserCanViewComment(ConversationsComment comment) {

        String currentUserId = sessionManager.getCurrentSessionUserId();
        if (StringUtils.isBlank(currentUserId)) return false;

        ConversationsPost post = postRepository.findById(comment.getPostId())
            .orElseThrow(() -> new IllegalArgumentException("No post for id " + comment.getPostId()));
        return canUserViewPost(post, currentUserId);
    }

    private ConversationsTopic lockIfAfterLockDate(ConversationsTopic topic) {

        Instant now = Instant.now();
        if (!topic.getLocked() && (topic.getLockDate() != null && topic.getLockDate().isBefore(now))) {
            try {
                return this.lockTopic(topic.getId(), true, false).asTopic();
            } catch (ConversationsPermissionsException cpe) {
                return topic;
            }
        } else {
            return topic;
        }
    }

    /*
    private Topic setupDateState(Topic topic) {

        Instant now = Instant.now();

        Instant showDate = topic.getShowDate();
        Instant hideDate = topic.getHideDate();
        Instant lockDate = topic.getLockDate();
        Instant acceptUntilDate = topic.getAcceptUntilDate();

        try {

            if (!topic.getHidden()) {
                if (showDate != null && showDate.isAfter(now)) {
                    topic = this.hideTopic(topic.getId(), true);
                }
                if (hideDate != null  && hideDate.isBefore(now)) {
                    topic = this.hideTopic(topic.getId(), true);
                }
                if (showDate != null && hideDate != null && hideDate.isAfter(showDate)) {
                    topic = this.hideTopic(topic.getId(), true);
                }
            } else if ((showDate == null || showDate.isBefore(now)))
                && ((hideDate == null || hideDate.isAfter(now)) {
                    topic = this.hideTopic(topic.getId(), false);
                }
            }

            if (!topic.getLocked() && (lockDate != null && lockDate.isBefore(now))
                    || (acceptUntilDate != null && acceptUntilDate.isBefore(now))) {
                topic = this.lockTopic(topic.getId(), true, false).asTopic();
            }
        } catch (ConversationsPermissionsException e) {
            log.error("Failed to setup date state for topic {}: {}", topic.getId(), e.toString());
        }

        return topic;
    }
    */

    private ConversationsTopic showIfAfterShowDate(ConversationsTopic topic) {

        if (topic.getShowDate() != null && topic.getHidden() && topic.getShowDate().isBefore(Instant.now())) {
            try {
                return hideTopic(topic.getId(), false, false);
            } catch (ConversationsPermissionsException cpe) {
                return topic;
            }
        } else {
            return topic;
        }
    }

    private ConversationsTopic hideIfAfterHideDate(ConversationsTopic topic) {

        if (topic.getHideDate() != null && !topic.getHidden() && topic.getHideDate().isBefore(Instant.now())) {
            try {
                return this.hideTopic(topic.getId(), true, false);
            } catch (ConversationsPermissionsException cpe) {
                return topic;
            }
        } else {
            return topic;
        }
    }

    private void sendMessage(Set<User> users, String siteId, Map<String, Object> replacements, String type) {

        userMessagingService.message(users,
                Message.builder()
                    .siteId(siteId)
                    .tool(TOOL_ID)
                    .type(type).build(),
                Arrays.asList(new MessageMedium[] {MessageMedium.EMAIL}), replacements, NotificationService.NOTI_OPTIONAL);
    }

    private void updateThreadHowActiveScore(ConversationsPost thread) {

        int numberOfReplies = thread.getNumberOfThreadReplies();
        int numberOfReactions = thread.getNumberOfThreadReactions();

        int active = numberOfReplies + numberOfReactions;

        thread.setHowActive(active);
        postRepository.save(thread);
    }

    private boolean canUserViewPost(ConversationsPost post, String currentUserId) {

        if (!post.getDraft() && securityService.isSuperUser()) return true;

        if (post.getMetadata().getCreator().equals(currentUserId)) return true;

        if (post.getPrivatePost()) {
            String parentPostId = post.getParentPostId();
            Optional<ConversationsPost> optParentPost = Optional.empty();
            if (parentPostId != null) {
                optParentPost = postRepository.findById(parentPostId);
            }
            ConversationsPost parentPost = optParentPost.orElse(null);
            ConversationsTopic topic = topicRepository.findById(post.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("No topic for id " + post.getTopicId()));
            String parentCreator = parentPost != null
                ? parentPost.getMetadata().getCreator() : topic.getMetadata().getCreator();
            if (parentCreator.equals(currentUserId)) return true;
        }

        if (!post.getPrivatePost() && !post.getDraft()) return true;

        return false;
    }

    @Transactional(readOnly = true)
    public int getNumberOfThreadPages(String siteId, String topicId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        if (!securityService.unlock(SiteService.SITE_VISIT, "/site/" + siteId)) {
            throw new ConversationsPermissionsException("Current user cannot view posts.");
        }

        List<ConversationsPost> threads = postRepository.findByTopicIdAndParentPostIdIsNull(topicId)
            .stream().filter(p -> canUserViewPost(p, currentUserId)).collect(Collectors.toList());
        int pageSize = serverConfigurationService.getInt(ConversationsService.PROP_THREADS_PAGE_SIZE, 10);
        return (int) Math.ceil(threads.size() / pageSize);
    }

    @Transactional(readOnly = true)
    public Collection<PostTransferBean> getPostsByTopicId(String siteId, String topicId, Integer page, PostSort postSort, String requestedPostId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        if (!securityService.unlock(SiteService.SITE_VISIT, "/site/" + siteId)) {
            throw new ConversationsPermissionsException("Current user cannot view posts.");
        }

        ConversationsTopic topic = topicRepository.findById(topicId).orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        if (topic.getMustPostBeforeViewing() && !securityService.unlock(Permissions.ROLETYPE_INSTRUCTOR.label, "/site/" + siteId)) {
            ConversationsPermissionsException cpe
                = new ConversationsPermissionsException("Current user cannot view posts. They need to post something first.");
            TopicStatus topicStatus = topicStatusRepository.findByTopicIdAndUserId(topic.getId(), currentUserId).orElseThrow(() -> cpe);
            if (!topicStatus.getPosted()) {
                throw cpe;
            }
        }

        List<PostTransferBean> fullList = null;
        PostSort previousSort = null;
        Map<String, Map<String, Object>> topicCache = postsCache.get(topicId);
        if (topicCache == null) {
            topicCache = new HashMap<>();
            postsCache.put(topicId, topicCache);
        } else {
            Map<String, Object> userMap = topicCache.get(currentUserId);
            fullList = userMap != null ? (List<PostTransferBean>) userMap.get("posts") : null;
            previousSort = userMap != null ? (PostSort) userMap.get("sort") : null;
        }

        String requestedThreadId = null;

        if (fullList == null || (previousSort != null && previousSort != postSort) || StringUtils.isNotBlank(requestedPostId)) {
            log.debug("Cache miss on {} or post {} requested", topicId, requestedPostId);

            List<ConversationsPost> posts = new ArrayList<>();

            List<ConversationsPost> threads = postRepository.findByTopicIdAndParentPostIdIsNull(topicId)
                .stream().filter(p -> canUserViewPost(p, currentUserId)).collect(Collectors.toList());

            posts.addAll(threads);

            if (topic.getType() == TopicType.DISCUSSION) {
                for (ConversationsPost t : threads) {
                    List<ConversationsPost> threadPosts = postRepository.findByParentThreadId(t.getId())
                        .stream().filter(p -> canUserViewPost(p, currentUserId)).collect(Collectors.toList());

                    if (requestedPostId != null && threadPosts.stream().anyMatch(p -> p.getId().equals(requestedPostId))) {
                        requestedThreadId = t.getId();
                    }

                    posts.addAll(threadPosts);
                }
            } else {
                requestedThreadId = requestedPostId;
            }

            // Grab all the stati for this user and post, in one.
            Map<String, PostStatus> postStati = postStatusRepository.findByUserId(currentUserId)
                .stream().collect(Collectors.toMap(s -> s.getPostId(), s -> s));

            Settings settings = getSettingsForSite(siteId);

            List<PostTransferBean> postBeans
                = posts.stream().map(p -> decoratePostBean(PostTransferBean.of(p), siteId, topic, currentUserId, settings, postStati))
                    .collect(Collectors.toList());

            Map<String, PostTransferBean> postBeanMap = postBeans.stream().collect(Collectors.toMap(pb -> pb.id, pb -> pb));

            if (topic.getType() == TopicType.DISCUSSION) {
                postBeans.forEach(pb -> {

                    if (StringUtils.isNotBlank(pb.parentPost)) {
                        PostTransferBean parent = postBeanMap.get(pb.parentPost);
                        if (parent != null) {
                            parent.posts.add(pb);
                        } else {
                            log.error("No post for parent post id {}", pb.parentPost);
                        }
                    }
                });
                // Only leave the top level threads in the map
                postBeanMap.entrySet().removeIf(e -> StringUtils.isNotBlank(e.getValue().parentPost));
            }

            // Make sure we return the posts in the order the db returned them in. Maps don't order.
            //List<PostTransferBean> fullList = threads.stream().map(t -> postBeanMap.get(t.getId())).collect(Collectors.toList());
            fullList = topic.getType() == TopicType.DISCUSSION ?
                threads.stream().map(t -> postBeanMap.get(t.getId())).collect(Collectors.toList())
                : postBeans;

            final PostSort pSort = postSort != null ? postSort : PostSort.NEWEST;

            if (pSort != PostSort.OLDEST) {
                // DB sorts by oldest already
 
                fullList.sort((t1, t2) -> {

                    switch (pSort) {
                        case OLDEST:
                            if (t1.created.isBefore(t2.created)) return 1;
                            if (t1.created.isAfter(t2.created)) return -1;
                            break;
                        case NEWEST:
                            if (t1.created.isBefore(t2.created)) return 1;
                            if (t1.created.isAfter(t2.created)) return -1;
                            break;
                        case ASC_CREATOR:
                            return t1.creatorDisplayName.compareTo(t2.creatorDisplayName);
                        case DESC_CREATOR:
                            return -1 * t1.creatorDisplayName.compareTo(t2.creatorDisplayName);
                        case MOST_ACTIVE:
                            if (t1.howActive > t2.howActive) return -1;
                            if (t1.howActive < t2.howActive) return 1;
                        case LEAST_ACTIVE:
                            if (t1.howActive < t2.howActive) return -1;
                            if (t1.howActive > t2.howActive) return 1;
                        default:
                    }
                    return 0;
                });
            }
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("posts", fullList);
            userMap.put("sort", pSort); 
            topicCache.put(currentUserId, userMap);
        }

        int pageSize = serverConfigurationService.getInt(ConversationsService.PROP_THREADS_PAGE_SIZE, 10);

        if (fullList.size() < pageSize) {
            return fullList;
        } else if (requestedThreadId != null) {
            String testId = requestedThreadId;
            int numberOfPages = (int) Math.ceil((double) fullList.size() / (double) pageSize);
            for (int i = 0; i < numberOfPages; i++) {
                int start = i * pageSize;
                int end = start + pageSize;
                if (end > fullList.size()) end = fullList.size();
                List<PostTransferBean> pageOfThreads = fullList.subList(start, end);
                if (pageOfThreads.stream().anyMatch(t -> t.id.equals(testId))) {
                    return pageOfThreads;
                }
            }
            return Collections.<PostTransferBean>emptyList();
        } else {
            int start = page * pageSize;
            int end = start + pageSize;
            if (end > fullList.size()) end = fullList.size();
            return fullList.subList(start, end);
        }
    }

    public void deletePost(String siteId, String topicId, String postId, boolean setTopicResolved) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        ConversationsPost post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("No post for id " + postId));

        String siteRef = "/site/" + siteId;

        boolean isMine = post.getMetadata().getCreator().equals(currentUserId);

        if (!securityService.unlock(Permissions.POST_DELETE_ANY.label, siteRef)
            && !(isMine && securityService.unlock(Permissions.POST_DELETE_OWN.label, siteRef))) {
            throw new ConversationsPermissionsException("Current user is not allowed to delete post.");
        }

        if (setTopicResolved && securityService.unlock(post.getMetadata().getCreator(), Permissions.ROLETYPE_INSTRUCTOR.label, siteRef)) {
            topicRepository.findById(post.getTopicId()).ifPresent(t -> {
                setTopicResolved(t);
                topicRepository.save(t);
            });
        }

        if (postRepository.countByParentPostId(postId) > 0) {
            throw new IllegalArgumentException("Post " + postId + " has children. It cannot be deleted, only hidden");
        }

        // This post does not have childen. Just delete it.
        commentRepository.deleteByPostId(postId);
        postStatusRepository.deleteByPostId(postId);
        postReactionTotalRepository.deleteByPostId(postId);
        postReactionRepository.deleteByPostId(postId);
        if (StringUtils.isNotBlank(post.getParentThreadId())) {
            postRepository.findById(post.getParentThreadId()).ifPresent(thread -> {

                thread.setNumberOfThreadReplies(thread.getNumberOfThreadReplies() - 1);
                postRepository.save(thread);
            });
        }
        postRepository.delete(post);

        postsCache.remove(topicId);

        this.afterCommit(() -> {
            String reference = ConversationsReferenceReckoner.reckoner().siteId(siteId).type("p").id(postId).reckon().getReference();
            eventTrackingService.post(eventTrackingService.newEvent(ConversationsEvents.POST_DELETED.label, reference, siteId, true, NotificationService.NOTI_OPTIONAL));
        });
    }

    public PostTransferBean lockPost(String siteId, String topicId, String postId, boolean locked) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        ConversationsTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        ConversationsPost post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("No post for id " + postId));

        if (!securityService.unlock(Permissions.MODERATE.label, "/site/" + siteId)) {
            throw new ConversationsPermissionsException("Current user cannot lock/unlock posts.");
        }

        post.setLocked(locked);
        recursivelyLockPosts(post, locked);
        Settings settings = getSettingsForSite(siteId);
        PostTransferBean postBean = decoratePostBean(PostTransferBean.of(postRepository.save(post)), siteId, topic, currentUserId, settings, null);
        addDecoratedChildren(postBean, siteId, topic, currentUserId, settings);
        postsCache.remove(topicId);
        return postBean;
    }

    private void addDecoratedChildren(PostTransferBean postBean, String siteId, ConversationsTopic topic, String currentUserId, Settings settings) {

        List<PostTransferBean> children = new ArrayList<>();
        postRepository.findByParentPostId(postBean.id).forEach(child -> {

            PostTransferBean childBean = decoratePostBean(PostTransferBean.of(child), siteId, topic, currentUserId, settings, null);
            addDecoratedChildren(childBean, siteId, topic, currentUserId, settings);
            children.add(childBean);
        });
        postBean.posts = children;
    }

    public PostTransferBean hidePost(String siteId, String topicId, String postId, boolean hidden) throws ConversationsPermissionsException {

        if (!securityService.unlock(Permissions.MODERATE.label, "/site/" + siteId)) {
            throw new ConversationsPermissionsException("Current user cannot hide/show posts.");
        }

        ConversationsTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        ConversationsPost post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("No post for id " + postId));

        post.setHidden(hidden);
        Settings settings = getSettingsForSite(siteId);
        String currentUserId = getCheckedCurrentUserId();
        PostTransferBean bean = decoratePostBean(PostTransferBean.of(postRepository.save(post)), siteId, topic, currentUserId, settings, null);
        postsCache.remove(topicId);
        return bean;
    }

    public Map<Reaction, Integer> savePostReactions(String topicId, String postId, Map<Reaction, Boolean> reactions) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        ConversationsPost post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("No post for id " + postId));

        if (post.getMetadata().getCreator().equals(currentUserId)) {
            throw new ConversationsPermissionsException("You can't react to your own posts");
        }

        List<PostReaction> current = postReactionRepository.findByPostIdAndUserId(postId, currentUserId);

        reactions.entrySet().forEach(es -> {

            PostReactionTotal total
                = postReactionTotalRepository.findByPostIdAndReaction(postId, es.getKey())
                    .orElseGet(() -> {
                        PostReactionTotal t = new PostReactionTotal();
                        t.setPostId(postId);
                        t.setReaction(es.getKey());
                        t.setTotal(0);
                        return t;
                    });

            Optional<PostReaction> optExistingReaction = current.stream().filter(tr -> tr.getReaction() == es.getKey()).findAny();
            if (optExistingReaction.isPresent()) {
                PostReaction existingReaction = optExistingReaction.get();
                if (!existingReaction.getState() && es.getValue()) {
                    // This reaction is being turned on. Increment the total.
                    total.setTotal(total.getTotal() + 1);
                } else if (existingReaction.getState() && !es.getValue()) {
                    // This reaction is being turned off. Decrement the total.
                    total.setTotal(total.getTotal() - 1);
                }
                existingReaction.setState(es.getValue());
                postReactionRepository.save(existingReaction);
            } else {
                PostReaction newReaction = new PostReaction();
                newReaction.setPostId(post.getId());
                newReaction.setUserId(currentUserId);
                newReaction.setReaction(es.getKey());
                newReaction.setState(es.getValue());
                postReactionRepository.save(newReaction);
                if (es.getValue()) {
                    total.setTotal(total.getTotal() + 1);
                }
            }
            postReactionTotalRepository.save(total);
        });

        // This post has now been updated. Removed any viewed stati from the db so that other users
        // are alerted to the change.
        postStatusRepository.findByPostIdAndUserIdNot(postId, currentUserId).forEach(status -> {

            status.setViewed(false);
            status.setViewedDate(null);
            postStatusRepository.save(status);
        });

        // Do we need to uncache posts if it's just a reaction?
        postsCache.remove(topicId);

        return postReactionTotalRepository.findByPostId(postId)
                .stream().collect(Collectors.toMap(rt -> rt.getReaction(), rt -> rt.getTotal()));
    }

    public void markPostsViewed(Set<String> postIds, String topicId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();
        postIds.forEach(postId -> this.markPostViewed(topicId, postId, currentUserId));

        ConversationsTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        long numberOfPosts = getNumberOfPostsInTopic(topic, currentUserId);
        long read = postStatusRepository
            .findByTopicIdAndUserIdAndViewed(topicId, currentUserId, true).stream().count();
        long numberOfUnreadPosts = numberOfPosts - read;

        TopicStatus topicStatus = topicStatusRepository.findByTopicIdAndUserId(topicId, currentUserId)
            .orElseGet(() -> new TopicStatus(topic.getSiteId(), topicId, currentUserId));
        topicStatus.setViewed(numberOfUnreadPosts == 0L);
        topicStatus = topicStatusRepository.save(topicStatus);

        Map<String, Map<String, Object>> topicCache = postsCache.get(topicId);
        if (topicCache != null) topicCache.remove(currentUserId);
    }

    private void markPostViewed(String topicId, String postId, String currentUserId) {

        PostStatus status = postStatusRepository.findByPostIdAndUserId(postId, currentUserId)
            .orElseGet(() -> new PostStatus(topicId, postId, currentUserId));
        status.setViewed(true);
        status.setViewedDate(Instant.now());
        try {
            postStatusRepository.save(status);
        } catch (Exception e) {
            log.debug("Caught exception while marking post viewed. This can happen " +
                "due to the way the client detects posts scrolling into view");
        }
    }

    public Optional<CommentTransferBean> getComment(String commentId) throws ConversationsPermissionsException {
        return commentRepository.findById(commentId).map(CommentTransferBean::of);
    }

    public CommentTransferBean saveComment(CommentTransferBean commentBean) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        String siteRef = "/site/" + commentBean.siteId;

        boolean isNew = StringUtils.isBlank(commentBean.id);
        boolean isMine = !isNew && commentBean.creator.equals(currentUserId);

        if (isNew) {
            if (!securityService.unlock(Permissions.COMMENT_CREATE.label, siteRef)) {
                throw new ConversationsPermissionsException("Current user cannot create comments.");
            }
        } else if (!securityService.unlock(Permissions.COMMENT_UPDATE_ANY.label, siteRef)
                && (isMine && !securityService.unlock(Permissions.COMMENT_UPDATE_OWN.label, siteRef))) {
            throw new ConversationsPermissionsException("Current user cannot update comments.");
        }

        // We're creating a new topic, so set the initial dates of creation and modification
        Instant now = Instant.now();
        if (isNew) {
            commentBean.setCreator(currentUserId);
            commentBean.setCreated(now);
        }
        commentBean.setModifier(currentUserId);
        commentBean.setModified(now);

        ConversationsComment comment = commentBean.asComment();

        ConversationsComment updatedComment = commentRepository.save(comment);

        if (isNew) {
            // Up the comment count by one
            postRepository.findById(updatedComment.getPostId()).ifPresent(p -> {

                p.setNumberOfComments(p.getNumberOfComments() + 1);
                postRepository.save(p);
            });
        }

        this.afterCommit(() -> {

            postsCache.remove(commentBean.topicId);

            ConversationsEvents event = isNew ? ConversationsEvents.COMMENT_CREATED : ConversationsEvents.COMMENT_UPDATED;
            String reference = ConversationsReferenceReckoner.reckoner()
                .siteId(commentBean.siteId)
                .type("c")
                .id(updatedComment.getId()).reckon().getReference();
            eventTrackingService.post(eventTrackingService.newEvent(event.label, reference, commentBean.siteId, true, NotificationService.NOTI_OPTIONAL));
        });

        return decorateCommentBean(CommentTransferBean.of(updatedComment), commentBean.siteId, currentUserId);
    }

    public void deleteComment(String siteId, String commentId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        ConversationsComment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("No comment with id " + commentId));

        String siteRef = "/site/" + siteId;

        boolean isMine = comment.getMetadata().getCreator().equals(currentUserId);
        if (!securityService.unlock(Permissions.COMMENT_DELETE_ANY.label, siteRef)
            && (isMine && !securityService.unlock(Permissions.COMMENT_DELETE_OWN.label, siteRef))) {
            throw new ConversationsPermissionsException("Current user cannot delete comment");
        }
        commentRepository.deleteById(commentId);
        // Drop the comment count by one
        postRepository.findById(comment.getPostId()).ifPresent(p -> {

            p.setNumberOfComments(p.getNumberOfComments() - 1);
            postRepository.save(p);
        });
    }

    private void setTopicResolved(ConversationsTopic topic) {

        String siteRef = "/site/" + topic.getSiteId();

        topic.setResolved(postRepository.findByTopicId(topic.getId()).stream().anyMatch(p -> {
            return !p.getDraft() && securityService.unlock(p.getMetadata().getCreator(), Permissions.ROLETYPE_INSTRUCTOR.label, siteRef);
        }));
    }

    private List<TopicTransferBean> decorateTopics(List<ConversationsTopic> topics, String currentUserId, Settings settings) {
        return topics.stream().map(t -> decorateTopicBean(TopicTransferBean.of(t), t, currentUserId, settings)).collect(Collectors.toList());
    }

    private TopicTransferBean decorateTopicBean(TopicTransferBean topicBean, ConversationsTopic topic, String currentUserId, Settings settings) {

        try {
            User creator = userDirectoryService.getUser(topicBean.creator);
            topicBean.creatorDisplayName = creator.getDisplayName();
        } catch (UserNotDefinedException e) {
            log.error("No user for id: {}", topicBean.creator);
        }

        String siteRef = "/site/" + topicBean.siteId;

        topicBean.isMine = topicBean.creator.equals(currentUserId);

        topicBean.formattedCreatedDate = userTimeService.dateTimeFormat(topicBean.created, FormatStyle.MEDIUM, FormatStyle.SHORT);
        topicBean.formattedModifiedDate = userTimeService.dateTimeFormat(topicBean.modified, FormatStyle.MEDIUM, FormatStyle.SHORT);

        if (topicBean.dueDate != null) {
            topicBean.formattedDueDate = userTimeService.dateTimeFormat(topicBean.dueDate, FormatStyle.MEDIUM, FormatStyle.SHORT);
        }

        topicBean.canModerate = securityService.unlock(Permissions.MODERATE.label, siteRef);

        if (topic != null) {
            topicBean.tags = topic.getTagIds().stream().map(tagId -> {

                Optional<Tag> optTag = tagRepository.findById(tagId);
                if (optTag.isPresent()) {
                    return optTag.get();
                } else {
                    return null;
                }
            }).collect(Collectors.toList());

            topicStatusRepository.findByTopicIdAndUserId(topic.getId(), currentUserId)
                .ifPresent(s -> {

                    topicBean.bookmarked = s.getBookmarked();
                    topicBean.hasPosted = s.getPosted();
                });

            topicBean.myReactions = topicReactionRepository.findByTopicIdAndUserId(topic.getId(), currentUserId)
                .stream().collect(Collectors.toMap(tr -> tr.getReaction(), tr -> tr.getState()));

            Reaction.stream().forEach(r -> {
                if (!topicBean.myReactions.keySet().contains(r)) {
                    topicBean.myReactions.put(r, Boolean.FALSE);
                }
            });

            topicBean.reactionTotals = topicReactionTotalRepository.findByTopicId(topic.getId())
                    .stream().collect(Collectors.toMap(rt -> rt.getReaction(), rt -> rt.getTotal()));

            if (topicBean.anonymous && !securityService.unlock(Permissions.VIEW_ANONYMOUS.label, siteRef)) {
                topicBean.creatorDisplayName = resourceLoader.getString("anonymous");
            }

            topicBean.numberOfPosts = getNumberOfPostsInTopic(topic, currentUserId);
            topicBean.numberOfThreads = postRepository.findByTopicIdAndParentPostIdIsNull(topicBean.id)
                .stream().filter(p -> canUserViewPost(p, currentUserId)).count();
            Long read = postStatusRepository
                .findByTopicIdAndUserIdAndViewed(topic.getId(), currentUserId, true).stream().count();
            topicBean.numberOfUnreadPosts = topicBean.numberOfPosts - read;
        }

        if (!topicBean.locked) {
            topicBean.canEdit = securityService.unlock(Permissions.TOPIC_UPDATE_ANY.label, siteRef)
                || (topicBean.isMine && securityService.unlock(Permissions.TOPIC_UPDATE_OWN.label, siteRef));
            topicBean.canDelete = securityService.unlock(Permissions.TOPIC_DELETE_ANY.label, siteRef)
                || (topicBean.isMine && securityService.unlock(Permissions.TOPIC_DELETE_OWN.label, siteRef));
            topicBean.canPost = securityService.unlock(Permissions.POST_CREATE.label, siteRef)
                && (!topicBean.mustPostBeforeViewing || topicBean.hasPosted || securityService.unlock(Permissions.ROLETYPE_INSTRUCTOR.label, siteRef));
            topicBean.canPin = settings.getAllowPinning() && securityService.unlock(Permissions.TOPIC_PIN.label, siteRef);
            topicBean.canBookmark = settings.getAllowBookmarking();
            topicBean.canTag = securityService.unlock(Permissions.TOPIC_TAG.label, siteRef);
            topicBean.canReact = !topicBean.isMine && settings.getAllowReactions();
        } else {
            topicBean.canEdit = securityService.unlock(Permissions.MODERATE.label, siteRef);
        }


        topicBean.url = "/api/sites/" + topicBean.siteId + "/topics/" + topicBean.id;
        getTopicPortalUrl(topicBean.id).ifPresent(portalUrl -> topicBean.portalUrl = portalUrl);
        topicBean.reference = ConversationsReferenceReckoner.reckoner().siteId(topicBean.siteId).type("t").id(topicBean.id).reckon().getReference();

        return topicBean;
    }

    @Transactional(readOnly = true)
    private long getNumberOfPostsInTopic(ConversationsTopic topic, String currentUserId) {

        return postRepository.findByTopicId(topic.getId()).stream().filter(p -> {

            if (p.getHidden()) return false;

            if (!p.getDraft() && securityService.isSuperUser()) return true;

            if (p.getMetadata().getCreator().equals(currentUserId)) return true;

            if (p.getPrivatePost()) {
                String parentPostId = p.getParentPostId();
                Optional<ConversationsPost> optParentPost = Optional.empty();

                if (parentPostId != null) {
                    optParentPost = postRepository.findById(parentPostId);
                }
                ConversationsPost parentPost = optParentPost.orElse(null);
                String parentCreator = parentPost != null
                    ? parentPost.getMetadata().getCreator() : topic.getMetadata().getCreator();
                return parentCreator.equals(currentUserId);
            }

            if (p.getHidden() && securityService.unlock(Permissions.MODERATE.label, "/site/" + topic.getSiteId())) {
                return true;
            }

            if (!p.getHidden() && !p.getDraft()) return true;

            return false;
        }).count();
    }

    private PostTransferBean decoratePostBean(PostTransferBean postBean, String siteId, ConversationsTopic topic, String currentUserId, Settings settings, Map<String, PostStatus> postStati) {

        try {
            User creator = userDirectoryService.getUser(postBean.creator);
            postBean.setCreatorDisplayName(creator.getDisplayName());
        } catch (UserNotDefinedException e) {
            log.error("No user for id: {}", postBean.creator);
        }

        String siteRef = siteService.siteReference(siteId);

        postBean.isMine = postBean.creator.equals(currentUserId);
        postBean.formattedCreatedDate = userTimeService.dateTimeFormat(postBean.created, FormatStyle.MEDIUM, FormatStyle.SHORT);
        postBean.formattedModifiedDate = userTimeService.dateTimeFormat(postBean.modified, FormatStyle.MEDIUM, FormatStyle.SHORT);

        if (!postBean.locked) {
            postBean.canEdit = securityService.unlock(Permissions.POST_UPDATE_ANY.label, siteRef)
                    || (postBean.isMine && securityService.unlock(Permissions.POST_UPDATE_OWN.label, siteRef));
            boolean hasChildren = postRepository.countByParentPostId(postBean.id) > 0L;
            postBean.canDelete = !hasChildren &&
                (securityService.unlock(Permissions.POST_DELETE_ANY.label, siteRef)
                    || (postBean.isMine && securityService.unlock(Permissions.POST_DELETE_OWN.label, siteRef)));
            postBean.canUpvote = !postBean.isMine && settings.getAllowUpvoting() && !postBean.hidden && securityService.unlock(Permissions.POST_UPVOTE.label, siteRef);
            postBean.canReact = !postBean.isMine && settings.getAllowReactions() && !postBean.hidden && securityService.unlock(Permissions.POST_REACT.label, siteRef);
            postBean.canComment = !postBean.hidden && securityService.unlock(Permissions.COMMENT_CREATE.label, siteRef);
            postBean.canReply = !postBean.hidden && securityService.unlock(Permissions.POST_CREATE.label, siteRef);
        }

        postBean.canView = !postBean.hidden ? true : securityService.unlock(Permissions.MODERATE.label, siteRef);
        postBean.isInstructor = !postBean.anonymous && securityService.unlock(postBean.creator, Permissions.ROLETYPE_INSTRUCTOR.label, siteRef);
        postBean.canModerate = securityService.unlock(Permissions.MODERATE.label, siteRef);

        if (postBean.anonymous && !securityService.unlock(Permissions.VIEW_ANONYMOUS.label, siteRef)) {
            postBean.creatorDisplayName = resourceLoader.getString("anonymous");
        }

        if (topic.getDueDate() != null && postBean.created.isAfter(topic.getDueDate())) {
            postBean.late = true;
        }

        postStatusRepository.findByPostIdAndUserId(postBean.id, currentUserId).ifPresent(s -> {

            postBean.upvoted = s.getUpvoted();
            postBean.viewed = s.getViewed();
        });

        postBean.comments = decorateComments(commentRepository.findByPostId(postBean.id), siteId, currentUserId);

        postBean.myReactions = postReactionRepository.findByPostIdAndUserId(postBean.id, currentUserId)
            .stream().collect(Collectors.toMap(pr -> pr.getReaction(), pr -> pr.getState()));

        Reaction.stream().forEach(r -> {
            if (!postBean.myReactions.keySet().contains(r)) {
                postBean.myReactions.put(r, Boolean.FALSE);
            }
        });

        postBean.reactionTotals = postReactionTotalRepository.findByPostId(postBean.id)
                .stream().collect(Collectors.toMap(rt -> rt.getReaction(), rt -> rt.getTotal()));

        if (postBean.hidden && !securityService.unlock(Permissions.MODERATE.label, siteRef)) {
            postBean.clear();
        }

        if (postStati != null) {
            PostStatus postStatus = postStati.get(postBean.id);
            postBean.viewed = postStatus != null ? postStatus.getViewed() : false;
        } else {
            postStatusRepository.findByPostIdAndUserId(postBean.id, currentUserId).ifPresent(s -> {
                postBean.viewed = s.getViewed();
            });
        }

        postBean.url = "/api/sites/" + siteId + "/topics/" + topic.getId() + "/posts/" + postBean.id;
        getPostPortalUrl(topic.getId(), postBean.id).ifPresent(portalUrl -> postBean.portalUrl = portalUrl);
        postBean.reference = ConversationsReferenceReckoner.reckoner().siteId(siteId).type("p").id(postBean.id).reckon().getReference();

        return postBean;
    }

    private List<CommentTransferBean> decorateComments(List<ConversationsComment> comments, String siteId, String currentUserId) {
        return comments.stream().map(c -> decorateCommentBean(CommentTransferBean.of(c), siteId, currentUserId)).collect(Collectors.toList());
    }

    private CommentTransferBean decorateCommentBean(CommentTransferBean commentBean, String siteId, String currentUserId) {

        try {
            User creator = userDirectoryService.getUser(commentBean.creator);
            commentBean.setCreatorDisplayName(creator.getDisplayName());
        } catch (UserNotDefinedException e) {
            log.error("No user for id: {}", commentBean.creator);
        }

        String siteRef = "/site/" + siteId;

        boolean isMine = commentBean.creator.equals(currentUserId);

        commentBean.formattedCreatedDate = userTimeService.dateTimeFormat(commentBean.created, FormatStyle.MEDIUM, FormatStyle.SHORT);
        commentBean.formattedModifiedDate = userTimeService.dateTimeFormat(commentBean.modified, FormatStyle.MEDIUM, FormatStyle.SHORT);

        commentBean.canEdit = securityService.unlock(Permissions.COMMENT_UPDATE_ANY.label, siteRef)
                                || (isMine && securityService.unlock(Permissions.COMMENT_UPDATE_OWN.label, siteRef));
        commentBean.canDelete = securityService.unlock(Permissions.COMMENT_DELETE_ANY.label, siteRef)
                                || (isMine && securityService.unlock(Permissions.COMMENT_DELETE_OWN.label, siteRef));

        return commentBean;
    }

    public PostTransferBean upvotePost(String siteId, String topicId, String postId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        ConversationsPost post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("No post for id " + postId));

        if (post.getMetadata().getCreator().equals(currentUserId)) {
            throw new IllegalArgumentException("Users cannot upvote their own posts");
        }

        String siteRef = "/site/" + siteId;

        if (!securityService.unlock(Permissions.POST_UPVOTE.label, siteRef)) {
            throw new ConversationsPermissionsException("Current user cannot upvote posts");
        }

        boolean alreadyUpvoted = false;
        Optional<PostStatus> optStatus = postStatusRepository.findByPostIdAndUserId(postId, currentUserId);
        if (optStatus.isPresent()) {
            PostStatus status = optStatus.get();
            alreadyUpvoted = status.getUpvoted();
            status.setUpvoted(Boolean.TRUE);
            postStatusRepository.save(status);
        } else {
            PostStatus status = new PostStatus(topicId, postId, currentUserId);
            status.setUpvoted(Boolean.TRUE);
            postStatusRepository.save(status);
        }

        if (!alreadyUpvoted) {
            post.setUpvotes(post.getUpvotes() + 1);
        }
        return PostTransferBean.of(postRepository.save(post));
    }

    public PostTransferBean unUpvotePost(String siteId, String postId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        ConversationsPost post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("No post for id " + postId));

        if (post.getMetadata().getCreator().equals(currentUserId)) {
            throw new IllegalArgumentException("Users cannot unupvote their own posts");
        }

        String siteRef = "/site/" + siteId;

        if (!securityService.unlock(Permissions.POST_UPVOTE.label, siteRef)) {
            throw new ConversationsPermissionsException("Current user cannot upvote posts");
        }

        boolean alreadyUpvoted = false;
        PostStatus status = postStatusRepository.findByPostIdAndUserId(postId, currentUserId)
            .orElseThrow(() -> new IllegalArgumentException("Post for id " + postId + " has not been upvoted yet"));

        if (!status.getUpvoted()) {
            throw new IllegalArgumentException("Post for id " + postId + " has not been upvoted yet");
        }
        status.setUpvoted(Boolean.FALSE);
        postStatusRepository.save(status);
        post.setUpvotes(post.getUpvotes() - 1);
        return PostTransferBean.of(postRepository.save(post));
    }

    public Tag saveTag(Tag tag) throws ConversationsPermissionsException {

        getCheckedCurrentUserId();

        String siteRef = "/site/" + tag.getSiteId();

        if (!securityService.unlock(Permissions.TAG_CREATE.label, siteRef)) {
            throw new ConversationsPermissionsException("Current user cannot create tags");
        }

        return tagRepository.save(tag);
    }

    public List<Tag> createTags(List<Tag> tags) throws ConversationsPermissionsException {

        getCheckedCurrentUserId();

        String siteRef = "/site/" + tags.get(0).getSiteId();

        if (!securityService.unlock(Permissions.TAG_CREATE.label, siteRef)) {
            throw new ConversationsPermissionsException("Current user cannot create tags");
        }

        return tags.stream().map(tagRepository::save).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Tag> getTagsForSite(String siteId) throws ConversationsPermissionsException {

        getCheckedCurrentUserId();

        String siteRef = "/site/" + siteId;

        if (!securityService.unlock(Permissions.TOPIC_TAG.label, siteRef)) {
            return Collections.<Tag>emptyList();
        }

        return tagRepository.findBySiteId(siteId);
    }

    public void deleteTag(Long tagId) throws ConversationsPermissionsException {

        getCheckedCurrentUserId();

        Optional<Tag> optTag = tagRepository.findById(tagId);

        if (optTag.isPresent()) {
            String siteRef = "/site/" + optTag.get().getSiteId();

            if (!securityService.unlock(Permissions.TAG_CREATE.label, siteRef)) {
                throw new ConversationsPermissionsException("Current user cannot delete tags");
            }

            tagRepository.deleteById(tagId);

            topicRepository.findByTags_Id(tagId).forEach(t -> t.getTagIds().remove(tagId));
        } else {
            throw new IllegalArgumentException("No tag with id " + tagId);
        }
    }

    private String getCheckedCurrentUserId() throws ConversationsPermissionsException {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (currentUserId == null) {
            throw new ConversationsPermissionsException("No current user.");
        }

        return currentUserId;
    }

    public Settings getSettingsForSite(String siteId) throws ConversationsPermissionsException {

        getCheckedCurrentUserId();

        return settingsRepository.findBySiteId(siteId).orElse(new Settings(siteId));
    }

    public Settings saveSettings(Settings settings) throws ConversationsPermissionsException {

        getCheckedCurrentUserId();

        if (!securityService.unlock(SiteService.SECURE_UPDATE_SITE, "/site/" + settings.getSiteId())) {
            throw new ConversationsPermissionsException("Current user cannot set site settings");
        }

        Settings old = getSettingsForSite(settings.getSiteId());
        Boolean oldSiteLocked = old.getSiteLocked();
        Boolean newSiteLocked = settings.getSiteLocked();

        if (oldSiteLocked != newSiteLocked) {
            for (ConversationsTopic topic : topicRepository.findBySiteId(settings.getSiteId())) {
                if (!oldSiteLocked && newSiteLocked) {
                    topicRepository.lockBySiteId(settings.getSiteId(), true);
                    postRepository.lockBySiteId(settings.getSiteId(), true);
                    commentRepository.lockBySiteId(settings.getSiteId(), true);
                }
                if (oldSiteLocked && !newSiteLocked) {
                    topicRepository.lockBySiteId(settings.getSiteId(), false);
                    postRepository.lockBySiteId(settings.getSiteId(), false);
                    commentRepository.lockBySiteId(settings.getSiteId(), false);
                }
            }
        }

        Settings newSettings = settingsRepository.save(settings);

        topicRepository.findBySiteId(settings.getSiteId()).forEach(t -> postsCache.remove(t.getId()));

        return newSettings;
    }

    public ConvStatus getConvStatusForSiteAndUser(String siteId, String userId) throws ConversationsPermissionsException {

        getCheckedCurrentUserId();

        return convStatusRepository.findBySiteIdAndUserId(siteId, userId).orElse(new ConvStatus(siteId, userId));

    }

    public void saveConvStatus(ConvStatus convStatus) throws ConversationsPermissionsException {

        getCheckedCurrentUserId();

        convStatusRepository.save(convStatus);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSiteStats(String siteId, Instant from, Instant to, int page, String sort) throws ConversationsPermissionsException {

        int pageSize = 10;

        getCheckedCurrentUserId();

        String siteRef = "/site/" + siteId;

        if (!securityService.unlock(Permissions.VIEW_STATISTICS.label, siteRef)) {
            throw new ConversationsPermissionsException("Current user cannot view site statistics");
        }

        AuthzGroup azGroup = null;
        try {
            azGroup = authzGroupService.getAuthzGroup(siteRef);
        } catch (GroupNotDefinedException e) {
            log.error("No group for {}", siteRef);
            throw new IllegalArgumentException("No group for " + siteRef);
        }

        List<String> userIds = new ArrayList<>(azGroup.getUsers());
        List<User> users = userDirectoryService.getUsers(userIds);

        List<Stat> topicCreatedStats = statsManager.getEventStats(siteId,
            Arrays.asList(new String[] { ConversationsEvents.TOPIC_CREATED.label }),
            from != null ? Date.from(from) : null,
            to != null ? Date.from(to) : null,
            userIds,
            false, null, null, null, false, 0);

        Map<String, Long> topicCountsByUser = new HashMap<>();
        topicCreatedStats.forEach(stat -> {

            Long current = topicCountsByUser.getOrDefault(stat.getUserId(), 0L);
            current = current + stat.getCount();
            topicCountsByUser.put(stat.getUserId(), current);
        });

        List<Stat> postCreatedStats = statsManager.getEventStats(siteId,
            Arrays.asList(new String[] { ConversationsEvents.POST_CREATED.label }),
            from != null ? Date.from(from) : null,
            to != null ? Date.from(to) : null,
            userIds,
            false, null, null, null, false, 0);

        Map<String, Long> postCountsByUser = new HashMap<>();
        postCreatedStats.forEach(stat -> {

            Long current = postCountsByUser.getOrDefault(stat.getUserId(), 0L);
            current = current + stat.getCount();
            postCountsByUser.put(stat.getUserId(), current);
        });

        List<Stat> reactedStats = statsManager.getEventStats(siteId,
            Arrays.asList(new String[] { ConversationsEvents.REACTED_TO_TOPIC.label }),
            from != null ? Date.from(from) : null,
            to != null ? Date.from(to) : null,
            userIds,
            false, null, null, null, false, 0);

        Map<String, Long> reactedCountsByUser = new HashMap<>();
        reactedStats.forEach(stat -> {

            Long current = reactedCountsByUser.getOrDefault(stat.getUserId(), 0L);
            current = current + stat.getCount();
            reactedCountsByUser.put(stat.getUserId(), current);
        });

        Map<String, Object> data = new HashMap<>();
        data.put("total", userIds.size());
        data.put("pageSize", pageSize);
        data.put("currentPage", page);

        Map<String, Long> topicViewedCounts
            = topicStatusRepository.countBySiteIdAndViewed(siteId, Boolean.TRUE).stream().collect(Collectors.toMap(pair -> (String) pair[0], pair -> (Long) pair[1]));

        List<ConversationsStat> stats = users.stream().map(user -> {

            ConversationsStat stat = new ConversationsStat();
            stat.name = user.getSortName();
            Long topicCount = topicCountsByUser.get(user.getId());
            stat.topicsCreated = topicCount != null ? topicCount : 0;
            stat.topicsViewed = topicViewedCounts.getOrDefault(user.getId(), 0L);
            Long reactedCount = reactedCountsByUser.get(user.getId());
            stat.reactionsMade = reactedCount != null ? reactedCount : 0;
            Long postCount = postCountsByUser.get(user.getId());
            stat.postsCreated = postCount != null ? postCount : 0;
            return stat;
        }).collect(Collectors.toList());

        String baseCacheKey = siteId + "/conversations/";
        String nameAscendingKey = baseCacheKey + SORT_NAME_ASCENDING;

        List<ConversationsStat> sortedStats = new ArrayList<>();
        if (sort == null) {
            sortedStats = sortedStatsCache.get(nameAscendingKey);
            if (sortedStats == null) {
                sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getName)).collect(Collectors.toList());
                sortedStatsCache.put(nameAscendingKey, sortedStats);
            }
        } else {
            switch (sort) {
                case SORT_NAME_ASCENDING:
                    sortedStats = sortedStatsCache.get(nameAscendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getName)).collect(Collectors.toList());
                        sortedStatsCache.put(nameAscendingKey, sortedStats);
                    }
                    break;
                case SORT_NAME_DESCENDING:
                    String nameDescendingKey = baseCacheKey + SORT_NAME_DESCENDING;
                    sortedStats = sortedStatsCache.get(nameDescendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getName).reversed()).collect(Collectors.toList());
                        sortedStatsCache.put(nameDescendingKey, sortedStats);
                    }
                    break;
                case SORT_TOPICS_CREATED_ASCENDING:
                    String topicsCreatedAscendingKey = baseCacheKey + SORT_TOPICS_CREATED_ASCENDING;
                    sortedStats = sortedStatsCache.get(topicsCreatedAscendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getTopicsCreated)).collect(Collectors.toList());
                        sortedStatsCache.put(topicsCreatedAscendingKey, sortedStats);
                    }
                    break;
                case SORT_TOPICS_CREATED_DESCENDING:
                    String topicsCreatedDescendingKey = baseCacheKey + SORT_TOPICS_CREATED_DESCENDING;
                    sortedStats = sortedStatsCache.get(topicsCreatedDescendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getTopicsCreated).reversed()).collect(Collectors.toList());
                        sortedStatsCache.put(topicsCreatedDescendingKey, sortedStats);
                    }
                    break;
                case SORT_TOPICS_VIEWED_ASCENDING:
                    String topicsViewedAscendingKey = baseCacheKey + SORT_TOPICS_VIEWED_ASCENDING;
                    sortedStats = sortedStatsCache.get(topicsViewedAscendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getTopicsViewed)).collect(Collectors.toList());
                        sortedStatsCache.put(topicsViewedAscendingKey, sortedStats);
                    }
                    break;
                case SORT_TOPICS_VIEWED_DESCENDING:
                    String topicsViewedDescendingKey = baseCacheKey + SORT_TOPICS_VIEWED_DESCENDING;
                    sortedStats = sortedStatsCache.get(topicsViewedDescendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getTopicsViewed).reversed()).collect(Collectors.toList());
                        sortedStatsCache.put(topicsViewedDescendingKey, sortedStats);
                    }
                    break;
                case SORT_POSTS_CREATED_ASCENDING:
                    String postsCreatedAscendingKey = baseCacheKey + SORT_POSTS_CREATED_ASCENDING;
                    sortedStats = sortedStatsCache.get(postsCreatedAscendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getPostsCreated)).collect(Collectors.toList());
                        sortedStatsCache.put(postsCreatedAscendingKey, sortedStats);
                    }
                    break;
                case SORT_POSTS_CREATED_DESCENDING:
                    String postsCreatedDescendingKey = baseCacheKey + SORT_POSTS_CREATED_DESCENDING;
                    sortedStats = sortedStatsCache.get(postsCreatedDescendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getPostsCreated).reversed()).collect(Collectors.toList());
                        sortedStatsCache.put(postsCreatedDescendingKey, sortedStats);
                    }
                    break;
                case SORT_REACTIONS_MADE_ASCENDING:
                    String reactionsMadeAscendingKey = baseCacheKey + SORT_REACTIONS_MADE_ASCENDING;
                    sortedStats = sortedStatsCache.get(reactionsMadeAscendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getReactionsMade)).collect(Collectors.toList());
                        sortedStatsCache.put(reactionsMadeAscendingKey, sortedStats);
                    }
                    break;
                case SORT_REACTIONS_MADE_DESCENDING:
                    String reactionsMadeDescendingKey = baseCacheKey + SORT_REACTIONS_MADE_DESCENDING;
                    sortedStats = sortedStatsCache.get(reactionsMadeDescendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getReactionsMade).reversed()).collect(Collectors.toList());
                        sortedStatsCache.put(reactionsMadeDescendingKey, sortedStats);
                    }
                    break;
                default:
            }
        }

        int start = pageSize * (page - 1);
        int end = start + pageSize;

        if (end > sortedStats.size()) end = sortedStats.size();

        data.put("stats", sortedStats.subList(start, end));

        return data;
    }

    private void afterCommit(Runnable runnable) {

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }

    public String[] getEventKeys() {
        return new String[] { ConversationsEvents.TOPIC_CREATED.label, ConversationsEvents.POST_CREATED.label, ConversationsEvents.REACTED_TO_TOPIC.label };
    }
}
