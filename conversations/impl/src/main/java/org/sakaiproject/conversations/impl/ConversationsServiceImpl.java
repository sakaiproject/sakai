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
import java.util.Stack;
import java.util.stream.Collectors;

import org.sakaiproject.util.comparator.AlphaNumericComparator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.conversations.api.ConversationsEvent;
import org.sakaiproject.conversations.api.ConversationsPermissionsException;
import org.sakaiproject.conversations.api.ConversationsReferenceReckoner;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.ConversationsStat;
import org.sakaiproject.conversations.api.Permissions;
import org.sakaiproject.conversations.api.PostSort;
import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.conversations.api.ShowDateContext;
import org.sakaiproject.conversations.api.TopicShowDateMessager;
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
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GradeDefinition;
import org.sakaiproject.grading.api.GradingSecurityException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.messaging.api.Message;
import org.sakaiproject.messaging.api.MessageMedium;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.CalendarEventType;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.entity.api.EntityTransferrer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import lombok.Setter;

import javax.persistence.PersistenceException;

@Slf4j
@Setter
@Transactional
public class ConversationsServiceImpl implements ConversationsService, EntityTransferrer, Observer {

    private AuthzGroupService authzGroupService;

    private CalendarService calendarService;

    private FunctionManager functionManager;

    private ConversationsCommentRepository commentRepository;

    private ConvStatusRepository convStatusRepository;

    private EntityManager entityManager;

    private EventTrackingService eventTrackingService;

    private MemoryService memoryService;

    private ConversationsPostRepository postRepository;

    private GradingService gradingService;

    private PostReactionRepository postReactionRepository;

    private PostReactionTotalRepository postReactionTotalRepository;

    private PostStatusRepository postStatusRepository;

    private ScheduledInvocationManager scheduledInvocationManager;

    private SecurityService securityService;

    private ServerConfigurationService serverConfigurationService;

    private SessionManager sessionManager;

    private SettingsRepository settingsRepository;

    private SiteService siteService;

    private StatsManager statsManager;

    private TagRepository tagRepository;

    private TimeService timeService;

    private TopicReactionRepository topicReactionRepository;

    private TopicReactionTotalRepository topicReactionTotalRepository;

    private TopicShowDateMessager topicShowDateMessager;

    private ConversationsTopicRepository topicRepository;

    private TopicStatusRepository topicStatusRepository;

    private UserDirectoryService userDirectoryService;

    private UserMessagingService userMessagingService;

    private UserTimeService userTimeService;

    private ResourceLoader resourceLoader;

    private LTIService ltiService;

    private Cache<String, List<ConversationsStat>> sortedStatsCache;
    private Cache<String, Map<String, Map<String, Object>>> postsCache;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void init() {

        Permissions.stream().forEach(p -> functionManager.registerFunction(p.label, true));
        this.sortedStatsCache = memoryService.<String, List<ConversationsStat>>getCache(STATS_CACHE_NAME);
        this.postsCache = memoryService.<String, Map<String, Map<String, Object>>>getCache(POSTS_CACHE_NAME);
        eventTrackingService.addObserver(this);

        entityManager.registerEntityProducer(this, REFERENCE_ROOT);

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
                this.sortedStatsCache.remove(baseCacheKey + SORT_TOPIC_REACTIONS_ASCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_TOPIC_REACTIONS_DESCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_TOPIC_UPVOTES_ASCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_TOPIC_UPVOTES_DESCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_POSTS_CREATED_ASCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_POSTS_CREATED_DESCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_POSTS_READ_ASCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_POSTS_READ_DESCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_POST_REACTIONS_ASCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_POST_REACTIONS_DESCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_POST_UPVOTES_ASCENDING);
                this.sortedStatsCache.remove(baseCacheKey + SORT_POST_UPVOTES_DESCENDING);
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

        String currentUserId = sessionManager.getCurrentSessionUserId();

        TopicTransferBean blankTopic = new TopicTransferBean();

        blankTopic.id = "";
        blankTopic.creator = currentUserId;
        blankTopic.siteId = siteId;
        blankTopic.title = "";
        blankTopic.message = "";
        if (securityService.unlock(Permissions.QUESTION_CREATE.label, siteRef)) {
            blankTopic.type = TopicType.QUESTION.name();
        } else if (securityService.unlock(Permissions.DISCUSSION_CREATE.label, siteRef)) {
            blankTopic.type = TopicType.DISCUSSION.name();
        }
        blankTopic.availability = "AVAILABILITY_NOW";
        blankTopic.pinned = false;
        blankTopic.aboutReference = siteRef;

        return decorateTopicBean(blankTopic, null, currentUserId, getSettingsForSite(siteId));
    }

    public Optional<TopicTransferBean> getTopic(String topicId) throws ConversationsPermissionsException {
        return topicRepository.findById(topicId).map(TopicTransferBean::of);
    }

    public boolean currentUserCanViewTopic(ConversationsTopic topic) {

        if (topic == null) return false;

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

        final String reference = siteService.siteReference(siteId);
        if (!securityService.unlock(SiteService.SITE_VISIT, reference)) {
            throw new ConversationsPermissionsException("Current user cannot view topics.");
        }

        Settings settings = getSettingsForSite(siteId);

        List<ConversationsTopic> topics = topicRepository.findBySiteId(siteId).stream()
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
            topicId = postRepository.findById(postId).map(p -> p.getTopic().getId())
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

        String postId = commentRepository.findById(commentId).map(c -> c.getPost().getId())
            .orElseThrow(() -> new IllegalArgumentException("No comment for id: " + commentId));

        String topicId = postRepository.findById(postId).map(p -> p.getTopic().getId())
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

    @Transactional
    public TopicTransferBean saveTopic(final TopicTransferBean topicBean, boolean sendMessage) throws ConversationsPermissionsException {

        String currentUserId = StringUtils.isNotBlank(topicBean.creator) ? topicBean.creator : getCheckedCurrentUserId();

        String siteRef = siteService.siteReference(topicBean.siteId);

        Settings settings = getSettingsForSite(topicBean.siteId);

        boolean isModerator = securityService.unlock(Permissions.MODERATE.label, siteRef);

        if (settings.getSiteLocked() && !isModerator) {
            throw new ConversationsPermissionsException("Current user cannot create topic.");
        }

        boolean isNew = StringUtils.isBlank(topicBean.id);
        boolean isMine = !isNew && topicBean.creator.equals(currentUserId);

        if (isNew) {
            if (!securityService.unlock(Permissions.QUESTION_CREATE.label, siteRef)
                && !securityService.unlock(Permissions.DISCUSSION_CREATE.label, siteRef)) {
                throw new ConversationsPermissionsException("Current user cannot create topic.");
            }
        } else if (!securityService.unlock(Permissions.TOPIC_UPDATE_ANY.label, siteRef)
                && (isMine && !securityService.unlock(Permissions.TOPIC_UPDATE_OWN.label, siteRef))) {
            throw new ConversationsPermissionsException("Current user cannot update topic.");
        }

        if (topicBean.type.equals(TopicType.QUESTION.name()) && !securityService.unlock(Permissions.QUESTION_CREATE.label, siteRef)) {
            throw new ConversationsPermissionsException("Current user cannot create questions.");
        }

        if (topicBean.type.equals(TopicType.DISCUSSION.name()) && !securityService.unlock(Permissions.DISCUSSION_CREATE.label, siteRef)) {
            throw new ConversationsPermissionsException("Current user cannot create discussions.");
        }

        boolean wasDraft = false;
        ConversationsTopic existingTopic = null;
        String oldDueDateCalendarEventId = null;
        String oldShowMessageScheduleId = null;
        boolean removeScheduledMessage = false;

        Long existingGradingItemId = -1L;

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
            existingTopic = topicRepository.findById(topicBean.id)
                .orElseThrow(() -> new IllegalArgumentException("No existing topic for " + topicBean.id));

            existingGradingItemId = existingTopic.getGradingItemId();

            oldDueDateCalendarEventId = existingTopic.getDueDateCalendarEventId();
            oldShowMessageScheduleId = existingTopic.getShowMessageScheduleId();

            wasDraft = existingTopic.getDraft() && !topicBean.draft;

            if (existingTopic.getLocked() && !isModerator) {
                throw new ConversationsPermissionsException("Current user cannot update topic.");
            }

            if (topicBean.showDate == null && existingTopic.getShowDate() != null) {
                // show date has been removed
                if (topicBean.hideDate == null || topicBean.hideDate.isAfter(now)) {
                    topicBean.hidden = false;
                }

                removeScheduledMessage = true;
            }

            if (topicBean.showDate != null && existingTopic.getShowDate() != null && !topicBean.showDate.equals(existingTopic.getShowDate())) {
                // The show date has been changed
                removeScheduledMessage = true;
            }

            // If a show date was set in the past (it was in this case) we need to remove the
            // scheduled message invocation that was previously set
            if (removeScheduledMessage && oldShowMessageScheduleId != null) {
                scheduledInvocationManager.deleteDelayedInvocation(oldShowMessageScheduleId);
            }

            if (topicBean.lockDate == null && existingTopic.getLockDate() != null) {
                // lock date has been removed
                topicBean.locked = false;
            }

            // Only moderators can set a show or lock date
            if ((!Objects.equals(existingTopic.getShowDate(), topicBean.showDate)
                || !Objects.equals(existingTopic.getHideDate(), topicBean.hideDate)
                || !Objects.equals(existingTopic.getLockDate(), topicBean.lockDate)) && !isModerator) {
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

        syncGradingItem(isNew, existingGradingItemId, topic, topicBean);
        
        topic = updateCalendarForTopic(oldDueDateCalendarEventId, topic);

        TopicTransferBean outTopicBean = TopicTransferBean.of(topic);

        if (isNew) {
            topicStatusRepository.setViewedByTopicId(topic.getId(), true);
            outTopicBean.viewed = true;
        } else {
            topicStatusRepository.setViewedByTopicId(topic.getId(), false);
            outTopicBean.viewed = false;
        }

        outTopicBean.tags = topic.getTagIds().stream()
            .map(tagId -> tagRepository.findById(tagId).orElse(null))
            .collect(Collectors.toList());

        TopicTransferBean decoratedBean = decorateTopicBean(outTopicBean, topic, currentUserId, settings);

        ConversationsTopic finalTopic = topic;

        boolean finalWasDraft = wasDraft;

        if (sendMessage) {
            this.afterCommit(() -> this.sendOrScheduleTopicMessages(finalTopic.getId(), isNew, finalWasDraft));
        }

        return decoratedBean;
    }

    private void syncGradingItem(boolean isNew, Long existingGradingItemId, ConversationsTopic topic, TopicTransferBean params) {

        // 1. New topic with createGradingItem true
        //      Create new external grading item
        //      Save new grading item id in topic
        // 2. New topic with existing item selected
        //      Save existing grading item's id in topic
        // 3. Existing topic with createGradingItem true
        //      if a previous external item exists:
        //          update the item
        //      else:
        //          Create new external grading item
        // 4. Existing topic with createGradingItem false
        //      if grading item selected:
        //          if selected grading item id changed
        //

        String topicRef = ConversationsReferenceReckoner.reckoner().topic(topic).reckon().getReference();
        if (params.graded) {

            if (isNew) {
                if (params.createGradingItem) {
                    // Brand new grading item.
                    addGradingItem(topic, params, topicRef);
                } else if (params.gradingItemId != null && params.gradingItemId != -1) {
                    // A new association with an existing grading item
                    topic.setGradingItemId(params.gradingItemId);
                    topic = topicRepository.save(topic);
                } else {
                    // Invalid state. If we are grading a topic and create grading item has not been
                    // specified, then there has to be a grading item identified to associate with
                    log.warn("When grading a topic, either create grading item should be specified, or an existing item should have been selected");
                }
            } else {
                String topicUrl = getTopicPortalUrl(params.id).orElse("");

                if (params.createGradingItem) {
                    if (existingGradingItemId != null && gradingService.isExternalAssignmentDefined(params.siteId, topicRef)) {
                        gradingService.updateExternalAssessment(params.siteId, topicRef, topicUrl, null,
                                     params.title, null, params.gradingPoints, null, false);
                    } else {
                        addGradingItem(topic, params, topicRef);
                    }
                } else {
                    if (existingGradingItemId != null && existingGradingItemId != -1) {
                        if (existingGradingItemId != params.gradingItemId) {
                            if (gradingService.isExternalAssignmentDefined(params.siteId, topicRef)) {
                                gradingService.removeExternalAssignment(params.siteId, topicRef, null);
                            }

                            topic.setGradingItemId(params.gradingItemId);
                            topic = topicRepository.save(topic);
                        } else if (gradingService.isExternalAssignmentDefined(params.siteId, topicRef)) {
                            gradingService.updateExternalAssessment(params.siteId, topicRef, topicUrl, null,
                                     params.title, null, params.gradingPoints, null, false);
                        }
                    } else {
                        topic.setGradingItemId(params.gradingItemId);
                        topic = topicRepository.save(topic);
                    }
                }
            }
        } else {
            if (gradingService.isExternalAssignmentDefined(params.siteId, topicRef)) {
                gradingService.removeExternalAssignment(params.siteId, topicRef, null);
            }
            topic.setGradingItemId(null);
            topic = topicRepository.save(topic);
        }
    }

    private void addGradingItem(ConversationsTopic topic, TopicTransferBean params, String topicRef) {

        Assignment assignment = new Assignment();
        assignment.setName(params.title);
        assignment.setReference(topicRef);
        if (params.gradingPoints != -1D) {
            assignment.setPoints(params.gradingPoints);
        }
        assignment.setExternallyMaintained(Boolean.TRUE);
        assignment.setExternalId(topicRef);
        assignment.setExternalAppName(TOOL_ID);
        if (params.gradingCategory != -1L) {
            assignment.setCategoryId(params.gradingCategory);
        }
        Long gbId = gradingService.addAssignment(params.siteId, params.siteId, assignment);
        topic.setGradingItemId(gbId);
        topic = topicRepository.save(topic);
    }

    private void sendOrScheduleTopicMessages(String topicId, boolean isNew, boolean wasDraft) {

        ConversationsTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        Instant showDate = topic.getShowDate();

        if (showDate != null && showDate.isAfter(Instant.now())) {

            (new Thread(() -> {

                // This topic has a show date in the future. We need to set up a scheduled task to
                // send out email alerts on the show date

                try {
                    ShowDateContext showDateContext = new ShowDateContext();
                    showDateContext.setTopicId(topicId);
                    showDateContext.setWasNew(isNew);
                    String context = objectMapper.writeValueAsString(showDateContext);
                    if (!topic.getDraft()) {
                        String invocationId = scheduledInvocationManager.createDelayedInvocation(showDate, "org.sakaiproject.conversations.api.TopicShowDateMessager", context);
                        topic.setShowMessageScheduleId(invocationId);
                        topicRepository.save(topic);
                    }
                } catch (JsonProcessingException e) {
                    log.error("Exception while building json context for topic message sheduler: {}", e.toString());
                }
            })).start();
        } else {
            if (!topic.getDraft()) {
                topicShowDateMessager.message(topic, isNew);
            }
        }
    }

    private CalendarEventEdit populateCalendarEvent(CalendarEventEdit calEdit, ConversationsTopic topic) {

        long start = topic.getDueDate().toEpochMilli();
        calEdit.setRange(timeService.newTimeRange(timeService.newTime(start), timeService.newTime(start), true, false));
        calEdit.setDisplayName(resourceLoader.getString("topic_due") + " " + topic.getTitle());
        calEdit.setDescriptionFormatted(topic.getMessage());
        calEdit.setType(CalendarEventType.DEADLINE.getType());
        calEdit.setEventUrl(getTopicPortalUrl(topic.getId()).orElse(""));
        return calEdit;
    }

    private ConversationsTopic updateCalendarForTopic(String oldCalId, ConversationsTopic newTopic) {

        Calendar cal = this.getCalendar(newTopic.getSiteId());

        if ((newTopic.getDueDate() == null || newTopic.getDraft()) && oldCalId != null) {

            // Either the due date has been removed, or the topic has been set to draft at some point.

            try {
                cal.removeEvent(cal.getEditEvent(oldCalId, CalendarService.EVENT_REMOVE_CALENDAR));
                newTopic.setDueDateCalendarEventId(null);
                return topicRepository.save(newTopic);
            } catch (Exception e) {
                log.error("Failed to remove due date from calendar: {}", e.toString());
            }
        }

        if (newTopic.getDueDate() != null && !newTopic.getDraft()) {

            try {
                CalendarEventEdit calEdit = populateCalendarEvent(oldCalId != null ? cal.getEditEvent(oldCalId, CalendarService.EVENT_MODIFY_CALENDAR) : cal.addEvent(), newTopic);
                cal.commitEvent(calEdit);
                newTopic.setDueDateCalendarEventId(calEdit.getId());
                return topicRepository.save(newTopic);
            } catch (Exception e) {
                log.error("Failed to add due date to calendar: {}", e.toString());
            }
        }

        return newTopic;
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

        return topicRepository.save(topic);
    }

    public void bookmarkTopic(String topicId, boolean bookmarked) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        ConversationsTopic topic = topicRepository.getReferenceById(topicId);

        TopicStatus topicStatus = topicStatusRepository.findByTopicIdAndUserId(topicId, currentUserId)
            .orElseGet(() -> new TopicStatus(topic, currentUserId));
        topicStatus.setBookmarked(bookmarked);
        topicStatusRepository.save(topicStatus);
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

        if (topic.getDueDateCalendarEventId() != null) {
            Calendar cal = this.getCalendar(topic.getSiteId());
            try {
                cal.removeEvent(cal.getEditEvent(topic.getDueDateCalendarEventId(), CalendarService.EVENT_REMOVE_CALENDAR));
            } catch (Exception e) {
                log.error("Failed to remove due date event from calendar: {}", e.toString());
            }
        }

        String ref = ConversationsReferenceReckoner.reckoner().topic(topic).reckon().getReference();
        if (topic.getGradingItemId() != null && gradingService.isExternalAssignmentDefined(topic.getSiteId(), ref)) {
            gradingService.removeExternalAssignment(topic.getSiteId(), ref, null);
        }

        afterCommit(() -> {
            eventTrackingService.post(eventTrackingService.newEvent(ConversationsEvent.TOPIC_DELETED.label, ref, topic.getSiteId(), true, NotificationService.NOTI_OPTIONAL));
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
                        t.setTopic(topic);
                        t.setReaction(es.getKey());
                        t.setTotal(0);
                        return t;
                    });

            String ref = ConversationsReferenceReckoner.reckoner()
                .siteId(topic.getSiteId())
                .type("t").id(topicId).reckon().getReference();
            boolean topicReactedEvent = true;
            Optional<TopicReaction> optExistingReaction = current.stream().filter(tr -> tr.getReaction() == es.getKey()).findAny();
            if (optExistingReaction.isPresent()) {
                TopicReaction existingReaction = optExistingReaction.get();
                if (!existingReaction.getState() && es.getValue()) {
                    // This reaction is being turned on. Increment the total.
                    total.setTotal(total.getTotal() + 1);
                } else if (existingReaction.getState() && !es.getValue()) {
                    // This reaction is being turned off. Decrement the total.
                    total.setTotal(total.getTotal() - 1);
                    topicReactedEvent = false;
                    afterCommit(() -> {
                        eventTrackingService.post(eventTrackingService.newEvent(ConversationsEvent.UNREACTED_TO_TOPIC.label, ref, topic.getSiteId(), false, NotificationService.NOTI_OPTIONAL));
                    });
                }
                existingReaction.setState(es.getValue());
                topicReactionRepository.save(existingReaction);
            } else {
                TopicReaction newReaction = new TopicReaction();
                newReaction.setTopic(topic);
                newReaction.setUserId(currentUserId);
                newReaction.setReaction(es.getKey());
                newReaction.setState(es.getValue());
                topicReactionRepository.save(newReaction);
                if (es.getValue()) {
                    total.setTotal(total.getTotal() + 1);
                } else {
                    topicReactedEvent = false;
                }
            }

            if (topicReactedEvent) {
                eventTrackingService.post(eventTrackingService.newEvent(ConversationsEvent.REACTED_TO_TOPIC.label, ref, topic.getSiteId(), false, NotificationService.NOTI_OPTIONAL));
            }
            topicReactionTotalRepository.save(total);
        });

        return topicReactionTotalRepository.findByTopicId(topic.getId())
                .stream().collect(Collectors.toMap(rt -> rt.getReaction(), rt -> rt.getTotal()));
    }

    @Override
    public TopicTransferBean upvoteTopic(String siteId, String topicId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        ConversationsTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        if (topic.getMetadata().getCreator().equals(currentUserId)) {
            throw new IllegalArgumentException("Users cannot upvote their own topics");
        }

        if (!securityService.unlock(Permissions.POST_UPVOTE.label, siteService.siteReference(siteId))) {
            throw new ConversationsPermissionsException("Current user cannot upvote topics");
        }

        boolean alreadyUpvoted = false;
        Optional<TopicStatus> optStatus = topicStatusRepository.findByTopicIdAndUserId(topicId, currentUserId);
        if (optStatus.isPresent()) {
            TopicStatus status = optStatus.get();
            alreadyUpvoted = status.getUpvoted();
            status.setUpvoted(Boolean.TRUE);
            topicStatusRepository.save(status);
        } else {
            TopicStatus status = new TopicStatus(topic, currentUserId);
            status.setUpvoted(Boolean.TRUE);
            topicStatusRepository.save(status);
        }

        if (!alreadyUpvoted) {
            topic.setUpvotes(topic.getUpvotes() + 1);
            afterCommit(() -> {
                String ref = ConversationsReferenceReckoner.reckoner().topic(topic).reckon().getReference();
                eventTrackingService.post(eventTrackingService.newEvent(ConversationsEvent.TOPIC_UPVOTED.label, ref, siteId, true, NotificationService.NOTI_OPTIONAL));
            });
        }

        return TopicTransferBean.of(topicRepository.save(topic));
    }

    @Override
    public TopicTransferBean unUpvoteTopic(String siteId, String topicId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        ConversationsTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        if (topic.getMetadata().getCreator().equals(currentUserId)) {
            throw new IllegalArgumentException("Users cannot unupvote their own topics");
        }

        if (!securityService.unlock(Permissions.POST_UPVOTE.label, siteService.siteReference(siteId))) {
            throw new ConversationsPermissionsException("Current user cannot upvote topics");
        }

        boolean alreadyUpvoted = false;
        TopicStatus status = topicStatusRepository.findByTopicIdAndUserId(topicId, currentUserId)
            .orElseThrow(() -> new IllegalArgumentException("Topic for id " + topicId + " has not been upvoted yet"));

        if (!status.getUpvoted()) {
            throw new IllegalArgumentException("Topic for id " + topicId + " has not been upvoted yet");
        }
        status.setUpvoted(Boolean.FALSE);
        topicStatusRepository.save(status);
        topic.setUpvotes(topic.getUpvotes() - 1);

        return TopicTransferBean.of(topicRepository.save(topic));
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

        post.setTopic(topic);
        post.setLocked(topic.getLocked());
        post = postRepository.save(post);

        postsCache.remove(postBean.topic);

        if (StringUtils.isNotBlank(postBean.parentThread) && !postBean.draft) {
            postRepository.findById(postBean.parentThread).ifPresent(thread -> {

                thread.setNumberOfThreadReplies(thread.getNumberOfThreadReplies() + 1);
                postRepository.save(thread);
                updatePostHowActiveScore(thread);
            });
        }
        this.markPostViewed(postBean.topic, post, currentUserId);

        if (!post.getDraft() && !post.getPrivatePost() && !post.getAnonymous()
            && securityService.unlock(postBean.creator, Permissions.ROLETYPE_INSTRUCTOR.label, siteRef)) {

            topic.setResolved(true);
            topicRepository.save(topic);
        }

        TopicStatus topicStatus = topicStatusRepository.findByTopicIdAndUserId(topic.getId(), currentUserId)
            .orElse(new TopicStatus(topic, currentUserId));
        topicStatus.setPosted(true);
        boolean topicWasViewed = topicStatus.getViewed();
        topicStatusRepository.save(topicStatus);

        // If a post is either created or saved, it doesn't matter. The topic has changes and is
        // therefore not viewed.
        topicStatusRepository.setViewedByTopicId(topic.getId(), false);

        // However, if the current user had previously viewed the topic, set that back. This post
        // is the current user's, so they've viewed the post.
        if (topicWasViewed) {
            topicStatusRepository.setViewedByTopicIdAndUserId(topic.getId(), currentUserId, true);
        }

        PostTransferBean decoratedBean = decoratePostBean(PostTransferBean.of(post), postBean.siteId, topic, currentUserId, settings, null, null);

        if ((isNew || wasDraft) && !postBean.draft) {
            ConversationsEvent event = isNew ? ConversationsEvent.POST_CREATED : ConversationsEvent.POST_UPDATED;
            eventTrackingService.post(eventTrackingService.newEvent(event.label, decoratedBean.reference, postBean.siteId, true, NotificationService.NOTI_OPTIONAL));

            event = parent.isPresent() ? ConversationsEvent.POST_REPLIED : ConversationsEvent.POSTED_TO_TOPIC;
            eventTrackingService.post(eventTrackingService.newEvent(event.label, decoratedBean.reference, postBean.siteId, true, NotificationService.NOTI_OPTIONAL));
        }

        // We have to do this to satisfy the lambda requirements
        final boolean finalWasDraft = wasDraft;
        Optional<ConversationsPost> optParent = parent;
        this.afterCommit(() -> {

            if (sendMessage && (isNew || finalWasDraft) && !postBean.draft) {
                try {
                    Site site = siteService.getSite(decoratedBean.siteId);

                    // Use a mutable HashMap instead of Map.of() to avoid UnsupportedOperationException
                    // The email template service tries to modify this map with putAll() operation
                    Map<String, Object> replacements = new HashMap<>();
                    replacements.put("siteTitle", site.getTitle());
                    replacements.put("topicTitle", topic.getTitle());
                    replacements.put("postUrl", decoratedBean.portalUrl);
                    replacements.put("creatorDisplayName", decoratedBean.creatorDisplayName);
                    replacements.put("bundle", new ResourceLoader("conversations_notifications"));

                    if (topic.getType() == TopicType.QUESTION) {
                        String topicCreator = topic.getMetadata().getCreator();
                        Set<User> questionCreator = Collections.singleton(userDirectoryService.getUser(topicCreator));
                        if (decoratedBean.isInstructor) {
                            Set<User> siteUsers = new HashSet<>(userDirectoryService.getUsers(site.getUsers()));
                            siteUsers.removeAll(questionCreator);
                            sendMessage(siteUsers, decoratedBean.siteId, replacements, "instructoranswer");
                            // Send a specific message to the question poster
                            sendMessage(questionCreator, decoratedBean.siteId, replacements, "instructorreply");
                        } else {
                            // The creator of this post is not an instructor, so just send the
                            // generic reply message.
                            sendMessage(questionCreator, decoratedBean.siteId, replacements, "reply");
                        }

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

        if (post == null) return false;

        String currentUserId = sessionManager.getCurrentSessionUserId();
        if (StringUtils.isBlank(currentUserId)) return false;

        return canUserViewPost(post, currentUserId);
    }

    public boolean currentUserCanViewComment(ConversationsComment comment) {

        if (comment == null) return false;

        String currentUserId = sessionManager.getCurrentSessionUserId();
        if (StringUtils.isBlank(currentUserId)) return false;

        ConversationsPost post = postRepository.findById(comment.getPost().getId())
            .orElseThrow(() -> new IllegalArgumentException("No post for id " + comment.getPost().getId()));
        return canUserViewPost(post, currentUserId);
    }

    private ConversationsTopic lockIfAfterLockDate(ConversationsTopic topic) {

        Instant now = Instant.now();
        if ((!topic.getLocked() && (topic.getLockDate() != null && topic.getLockDate().isBefore(now)))
                || (topic.getDueDate() != null && topic.getLockDate() == null && topic.getDueDate().isBefore(now))) {
            try {
                return this.lockTopic(topic.getId(), true, false).asTopic();
            } catch (ConversationsPermissionsException cpe) {
                return topic;
            }
        } else {
            return topic;
        }
    }

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

    private void updatePostHowActiveScore(ConversationsPost post) {

        int howActive = 0;

        if (post.getNumberOfThreadReplies() != null) howActive += post.getNumberOfThreadReplies();
        if (post.getReactionCount() != null) howActive += post.getReactionCount();
        if (post.getNumberOfThreadReactions() != null) howActive += post.getNumberOfThreadReactions();
        if (post.getUpvotes() != null) howActive += post.getUpvotes();
        if (post.getNumberOfThreadUpvotes() != null) howActive += post.getNumberOfThreadUpvotes();

        post.setHowActive(howActive);
        postRepository.save(post);
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
            ConversationsTopic topic = topicRepository.findById(post.getTopic().getId())
                .orElseThrow(() -> new IllegalArgumentException("No topic for id " + post.getTopic().getId()));
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

        if (!securityService.unlock(SiteService.SITE_VISIT, siteService.siteReference(siteId))) {
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

        if (!securityService.unlock(SiteService.SITE_VISIT, siteService.siteReference(siteId))) {
            throw new ConversationsPermissionsException("Current user cannot view posts.");
        }

        ConversationsTopic topic = topicRepository.findById(topicId).orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        if (topic.getMustPostBeforeViewing() && !securityService.unlock(Permissions.ROLETYPE_INSTRUCTOR.label, siteService.siteReference(siteId))) {
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

            List<ConversationsPost> threads = postRepository.findByTopicIdAndParentPostIdIsNull(topicId)
                .stream().filter(p -> canUserViewPost(p, currentUserId)).collect(Collectors.toList());

            List<ConversationsPost> posts = new ArrayList<>(threads);

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
                .stream().collect(Collectors.toMap(s -> s.getPost().getId(), s -> s));

            Settings settings = getSettingsForSite(siteId);

            // Load up the grade map for the posters to this topic.
            List<String> creatorIds
                = postRepository.findByTopicId(topicId).stream()
                    .map(p -> p.getMetadata().getCreator()).collect(Collectors.toList());

            Map<String, GradeDefinition> posterGrades = Collections.emptyMap();
            Long gradingItemId = topic.getGradingItemId();
            String siteRef = siteService.siteReference(siteId);
            // Check permissions before attempting to get grades to prevent GradingSecurityException
            // Students who don't have the conversations.grade permission should skip this call
            if (gradingItemId != null && securityService.unlock(Permissions.GRADE.label, siteRef)) {
                try {
                    posterGrades = gradingService.getGradesForStudentsForItem(siteId, siteId, gradingItemId, creatorIds)
                        .stream().collect(Collectors.toMap(GradeDefinition::getStudentUid, gd -> gd));
                } catch (GradingSecurityException se) {
                    log.warn("Failed to getGradesForStudentsForItem with exception: {}", se.toString());
                }
            } else {
                log.debug("Grading item ID is null or user lacks grade permission for topic: {}", topic);
            }

            Map<String, GradeDefinition> finalPosterGrades = posterGrades;
            List<PostTransferBean> postBeans
                = posts.stream().map(p -> decoratePostBean(PostTransferBean.of(p), siteId, topic, currentUserId, settings, postStati, finalPosterGrades))
                    .collect(Collectors.toList());

            Map<String, PostTransferBean> postBeanMap = postBeans.stream().collect(Collectors.toMap(pb -> pb.id, pb -> pb));

            if (topic.getType() == TopicType.DISCUSSION) {
                postBeans.forEach(pb -> {

                    if (StringUtils.isNotBlank(pb.parentPost)) {
                        PostTransferBean parent = postBeanMap.get(pb.parentPost);
                        if (parent != null) {
                            parent.posts.add(pb);
                            pb.parentIsPrivate = parent.privatePost;
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

        String siteRef = siteService.siteReference(siteId);

        boolean isMine = post.getMetadata().getCreator().equals(currentUserId);

        if (!securityService.unlock(Permissions.POST_DELETE_ANY.label, siteRef)
            && !(isMine && securityService.unlock(Permissions.POST_DELETE_OWN.label, siteRef))) {
            throw new ConversationsPermissionsException("Current user is not allowed to delete post.");
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

        if (setTopicResolved && securityService.unlock(post.getMetadata().getCreator(), Permissions.ROLETYPE_INSTRUCTOR.label, siteRef)) {
            topicRepository.findById(post.getTopic().getId()).ifPresent(t -> {
                setTopicResolved(t);
                topicRepository.save(t);
            });
        }

        postsCache.remove(topicId);

        this.afterCommit(() -> {
            String reference = ConversationsReferenceReckoner.reckoner().siteId(siteId).type("p").id(postId).reckon().getReference();
            eventTrackingService.post(eventTrackingService.newEvent(ConversationsEvent.POST_DELETED.label, reference, siteId, true, NotificationService.NOTI_OPTIONAL));
        });
    }

    public PostTransferBean lockPost(String siteId, String topicId, String postId, boolean locked) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        ConversationsTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        ConversationsPost post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("No post for id " + postId));

        if (!securityService.unlock(Permissions.MODERATE.label, siteService.siteReference(siteId))) {
            throw new ConversationsPermissionsException("Current user cannot lock/unlock posts.");
        }

        post.setLocked(locked);
        recursivelyLockPosts(post, locked);
        Settings settings = getSettingsForSite(siteId);
        PostTransferBean postBean = decoratePostBean(PostTransferBean.of(postRepository.save(post)), siteId, topic, currentUserId, settings, null, null);
        addDecoratedChildren(postBean, siteId, topic, currentUserId, settings);
        postsCache.remove(topicId);
        return postBean;
    }

    private void addDecoratedChildren(PostTransferBean postBean, String siteId, ConversationsTopic topic, String currentUserId, Settings settings) {

        List<PostTransferBean> children = new ArrayList<>();
        postRepository.findByParentPostId(postBean.id).forEach(child -> {

            PostTransferBean childBean = decoratePostBean(PostTransferBean.of(child), siteId, topic, currentUserId, settings, null, null);
            addDecoratedChildren(childBean, siteId, topic, currentUserId, settings);
            children.add(childBean);
        });
        postBean.posts = children;
    }

    public PostTransferBean hidePost(String siteId, String topicId, String postId, boolean hidden) throws ConversationsPermissionsException {

        if (!securityService.unlock(Permissions.MODERATE.label, siteService.siteReference(siteId))) {
            throw new ConversationsPermissionsException("Current user cannot hide/show posts.");
        }

        ConversationsTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        ConversationsPost post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("No post for id " + postId));

        post.setHidden(hidden);
        Settings settings = getSettingsForSite(siteId);
        String currentUserId = getCheckedCurrentUserId();
        PostTransferBean bean = decoratePostBean(PostTransferBean.of(postRepository.save(post)), siteId, topic, currentUserId, settings, null, null);
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
                        t.setPost(post);
                        t.setReaction(es.getKey());
                        t.setTotal(0);
                        return t;
                    });

            Optional<PostReaction> optExistingReaction = current.stream().filter(tr -> tr.getReaction() == es.getKey()).findAny();
            boolean postReactedEvent = false;
            if (optExistingReaction.isPresent()) {
                PostReaction existingReaction = optExistingReaction.get();
                if (!existingReaction.getState() && es.getValue()) {
                    // This reaction is being turned on. Increment the total.
                    total.setTotal(total.getTotal() + 1);
                    postReactedEvent = true;
                } else if (existingReaction.getState() && !es.getValue()) {
                    // This reaction is being turned off. Decrement the total.
                    total.setTotal(total.getTotal() - 1);
                    postReactedEvent = false;
                }
                existingReaction.setState(es.getValue());
                postReactionRepository.save(existingReaction);
            } else {
                PostReaction newReaction = new PostReaction();
                newReaction.setPost(post);
                newReaction.setUserId(currentUserId);
                newReaction.setReaction(es.getKey());
                newReaction.setState(es.getValue());
                postReactionRepository.save(newReaction);
                if (es.getValue()) {
                    total.setTotal(total.getTotal() + 1);
                    postReactedEvent = true;
                }
            }

            if (postReactedEvent) {
                String ref = ConversationsReferenceReckoner.reckoner().post(post).reckon().getReference();
                eventTrackingService.post(eventTrackingService.newEvent(ConversationsEvent.REACTED_TO_POST.label, ref, post.getSiteId(), false, NotificationService.NOTI_OPTIONAL));
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

        Map<Reaction, Integer> reactionTotals = postReactionTotalRepository.findByPostId(postId)
                .stream().collect(Collectors.toMap(rt -> rt.getReaction(), rt -> rt.getTotal()));

        post.setReactionCount(reactionTotals.values().stream().mapToInt(t -> t).sum());

        updatePostHowActiveScore(post);

        return reactionTotals;
    }

    public void markPostsViewed(Set<String> postIds, String topicId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();
        postRepository.findAllById(postIds).forEach(post -> this.markPostViewed(topicId, post, currentUserId));

        ConversationsTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("No topic for id " + topicId));

        long numberOfPosts = getNumberOfPostsInTopic(topic, currentUserId);
        long read = postStatusRepository
            .findByTopicIdAndUserIdAndViewed(topicId, currentUserId, true).stream().count();
        long numberOfUnreadPosts = numberOfPosts - read;

        TopicStatus topicStatus = topicStatusRepository.findByTopicIdAndUserId(topicId, currentUserId)
            .orElseGet(() -> new TopicStatus(topic, currentUserId));
        topicStatus.setViewed(numberOfUnreadPosts == 0L);
        try {
            topicStatusRepository.save(topicStatus);
        } catch (PersistenceException pe) {
            log.debug("Caught an exception while saving topic status. This can happen "
                    + "due to the way the client detects posts scrolling into view", pe);
        }

        Map<String, Map<String, Object>> topicCache = postsCache.get(topicId);
        if (topicCache != null) topicCache.remove(currentUserId);
    }

    private void markPostViewed(String topicId, ConversationsPost post, String currentUserId) {

        topicRepository.findById(topicId).ifPresent(topic -> {

            PostStatus status = postStatusRepository.findByPostIdAndUserId(post.getId(), currentUserId)
                .orElseGet(() -> new PostStatus(topic, post, currentUserId));
            status.setViewed(true);
            status.setViewedDate(Instant.now());
            try {
                postStatusRepository.save(status);

                if (post.getMetadata().getCreator().equals(currentUserId)) {
                    // No need to mark a user's own posts as viewed.
                    return;
                }

                String ref = ConversationsReferenceReckoner.reckoner().post(post).reckon().getReference();
                eventTrackingService.post(eventTrackingService.newEvent(ConversationsEvent.POST_VIEWED.label, ref, post.getSiteId(), true, NotificationService.NOTI_OPTIONAL));
            } catch (PersistenceException pe) {
                log.debug("Caught constraint exception while marking post viewed. This can happen "
                        + "due to the way the client detects posts scrolling into view", pe);
            }
        });
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
        comment.setPost(postRepository.getReferenceById(commentBean.postId));
        comment.setTopic(topicRepository.getReferenceById(commentBean.topicId));

        ConversationsComment updatedComment = commentRepository.save(comment);

        if (isNew) {
            // Up the comment count by one
            postRepository.findById(updatedComment.getPost().getId()).ifPresent(p -> {

                p.setNumberOfComments(p.getNumberOfComments() + 1);
                postRepository.save(p);
            });
        }

        this.afterCommit(() -> {

            postsCache.remove(commentBean.topicId);

            ConversationsEvent event = isNew ? ConversationsEvent.COMMENT_CREATED : ConversationsEvent.COMMENT_UPDATED;
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

        String siteRef = siteService.siteReference(siteId);

        boolean isMine = comment.getMetadata().getCreator().equals(currentUserId);
        if (!securityService.unlock(Permissions.COMMENT_DELETE_ANY.label, siteRef)
            && (isMine && !securityService.unlock(Permissions.COMMENT_DELETE_OWN.label, siteRef))) {
            throw new ConversationsPermissionsException("Current user cannot delete comment");
        }
        commentRepository.deleteById(commentId);
        // Drop the comment count by one
        postRepository.findById(comment.getPost().getId()).ifPresent(p -> {

            p.setNumberOfComments(p.getNumberOfComments() - 1);
            postRepository.save(p);
        });
    }

    private void setTopicResolved(ConversationsTopic topic) {

        String siteRef = siteService.siteReference(topic.getSiteId());

        topic.setResolved(postRepository.findByTopicId(topic.getId()).stream().anyMatch(p -> {

            return !p.getDraft() && !p.getAnonymous()
                && securityService.unlock(p.getMetadata().getCreator(), Permissions.ROLETYPE_INSTRUCTOR.label, siteRef);
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

        String siteRef = siteService.siteReference(topicBean.siteId);

        topicBean.isMine = topicBean.creator.equals(currentUserId);

        topicBean.formattedCreatedDate = userTimeService.dateTimeFormat(topicBean.created, FormatStyle.MEDIUM, FormatStyle.SHORT);
        topicBean.formattedModifiedDate = userTimeService.dateTimeFormat(topicBean.modified, FormatStyle.MEDIUM, FormatStyle.SHORT);

        if (topicBean.dueDate != null) {
            topicBean.formattedDueDate = userTimeService.dateTimeFormat(topicBean.dueDate, FormatStyle.MEDIUM, FormatStyle.SHORT);
        }

        topicBean.canModerate = securityService.unlock(Permissions.MODERATE.label, siteRef);

        if (topic != null) {

            topicBean.tags = topic.getTagIds().stream()
                    .map(id -> tagRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            topicStatusRepository.findByTopicIdAndUserId(topic.getId(), currentUserId)
                .ifPresent(s -> {

                    topicBean.bookmarked = s.getBookmarked();
                    topicBean.hasPosted = s.getPosted();
                    topicBean.viewed = s.getViewed();
                    topicBean.upvoted = s.getUpvoted();
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
            topicBean.canReact = !topicBean.isMine && settings.getAllowReactions() && securityService.unlock(Permissions.POST_REACT.label, siteRef);
            topicBean.canUpvote = !topicBean.isMine && settings.getAllowUpvoting() && !topicBean.hidden && securityService.unlock(Permissions.POST_UPVOTE.label, siteRef);
            topicBean.canViewUpvotes = settings.getAllowUpvoting();
        } else {
            topicBean.canEdit = securityService.unlock(Permissions.MODERATE.label, siteRef);
        }

        if (topicBean.gradingItemId != null && topicBean.gradingItemId != -1) {
            try {
                topicBean.gradingPoints = gradingService.getAssignment(topicBean.siteId, topicBean.siteId, topicBean.gradingItemId).getPoints();
            } catch (AssessmentNotFoundException anfe) {
                log.warn("No grading assignment for id {}. Points have NOT been set for topic {}", topicBean.gradingItemId, topicBean.id);
            }
        }

        topicBean.url = "/api/sites/" + topicBean.siteId + "/topics/" + topicBean.id;
        getTopicPortalUrl(topicBean.id).ifPresent(portalUrl -> topicBean.portalUrl = portalUrl);
        topicBean.reference = ConversationsReferenceReckoner.reckoner().siteId(topicBean.siteId).type("t").id(topicBean.id).reckon().getReference();

        return topicBean;
    }

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

    private PostTransferBean decoratePostBean(PostTransferBean postBean, String siteId, ConversationsTopic topic, String currentUserId, Settings settings, Map<String, PostStatus> postStati, Map<String, GradeDefinition> posterGrades) {

        try {
            User creator = userDirectoryService.getUser(postBean.creator);
            postBean.setCreatorDisplayName(creator.getDisplayName());
        } catch (UserNotDefinedException e) {
            log.error("No user for id: {}", postBean.creator);
        }

        if (posterGrades != null) {
            postBean.grade = posterGrades.get(postBean.creator);
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
            postBean.canViewUpvotes = settings.getAllowUpvoting();
            postBean.canReact = !postBean.isMine && settings.getAllowReactions() && !postBean.hidden && securityService.unlock(Permissions.POST_REACT.label, siteRef);
            postBean.canComment = !postBean.hidden && securityService.unlock(Permissions.COMMENT_CREATE.label, siteRef);
            postBean.canReply = !postBean.hidden && securityService.unlock(Permissions.POST_CREATE.label, siteRef);
        }

        postBean.canView = !postBean.hidden ? true : securityService.unlock(Permissions.MODERATE.label, siteRef);
        postBean.isInstructor = !postBean.anonymous && securityService.unlock(postBean.creator, Permissions.ROLETYPE_INSTRUCTOR.label, siteRef);
        postBean.canModerate = securityService.unlock(Permissions.MODERATE.label, siteRef);
        postBean.canGrade = !postBean.isMine && topic.getGradingItemId() != null && securityService.unlock(Permissions.GRADE.label, siteRef);

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

        String siteRef = siteService.siteReference(siteId);

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

        String siteRef = siteService.siteReference(siteId);

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
            ConversationsTopic topic = topicRepository.getReferenceById(topicId);
            PostStatus status = new PostStatus(topic, post, currentUserId);
            status.setUpvoted(Boolean.TRUE);
            postStatusRepository.save(status);
        }

        if (!alreadyUpvoted) {
            post.setUpvotes(post.getUpvotes() + 1);
            String ref = ConversationsReferenceReckoner.reckoner().post(post).reckon().getReference();
            eventTrackingService.post(eventTrackingService.newEvent(ConversationsEvent.POST_UPVOTED.label, ref, siteId, true, NotificationService.NOTI_OPTIONAL));
        }

        if (StringUtils.isNotBlank(post.getParentThreadId())) {
            postRepository.findById(post.getParentThreadId()).ifPresent(thread -> {

                thread.setNumberOfThreadUpvotes(thread.getNumberOfThreadUpvotes() - 1);
                updatePostHowActiveScore(thread);
            });
        } else {
            updatePostHowActiveScore(post);
        }

        postsCache.remove(topicId);

        return PostTransferBean.of(postRepository.save(post));
    }

    public PostTransferBean unUpvotePost(String siteId, String postId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        ConversationsPost post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("No post for id " + postId));

        if (post.getMetadata().getCreator().equals(currentUserId)) {
            throw new IllegalArgumentException("Users cannot unupvote their own posts");
        }

        String siteRef = siteService.siteReference(siteId);

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

        if (StringUtils.isNotBlank(post.getParentThreadId())) {
            postRepository.findById(post.getParentThreadId()).ifPresent(thread -> {

                thread.setNumberOfThreadUpvotes(thread.getNumberOfThreadUpvotes() - 1);
                updatePostHowActiveScore(thread);
            });
        } else {
            updatePostHowActiveScore(post);
        }

        postsCache.remove(post.getTopic().getId());

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

    @Override
    @Transactional(readOnly = true)
    public List<Tag> getTagsForSite(String siteId) throws ConversationsPermissionsException {

        getCheckedCurrentUserId();

        String siteRef = siteService.siteReference(siteId);

        if (!securityService.unlock(Permissions.TOPIC_TAG.label, siteRef)) {
            return Collections.<Tag>emptyList();
        }

        List<Tag> tags = tagRepository.findBySiteId(siteId);
        // Sort tags alphabetically by label
        tags.sort(Comparator.comparing(Tag::getLabel, new AlphaNumericComparator()));
        return tags;
    }

    @Override
    public void deleteTag(Long tagId) throws ConversationsPermissionsException {

        getCheckedCurrentUserId();

        Optional<Tag> optTag = tagRepository.findById(tagId);

        if (optTag.isPresent()) {
            String siteRef = "/site/" + optTag.get().getSiteId();

            if (!securityService.unlock(Permissions.TAG_CREATE.label, siteRef)) {
                throw new ConversationsPermissionsException("Current user cannot delete tags");
            }

            tagRepository.deleteById(tagId);

            // Update the topics that were using this tag
            List<ConversationsTopic> topicsToUpdate = topicRepository.findByTags_Id(tagId);
            topicsToUpdate.forEach(t -> t.getTagIds().remove(tagId));
            topicRepository.saveAll(topicsToUpdate);
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

        String siteRef = siteService.siteReference(siteId);

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

        Map<String, Long> topicCountsByUser = new HashMap<>();
        getEventStats(ConversationsEvent.TOPIC_CREATED, siteId, from, to, userIds).forEach(stat -> {

            Long current = topicCountsByUser.getOrDefault(stat.getUserId(), 0L);
            current += stat.getCount();
            topicCountsByUser.put(stat.getUserId(), current);
        });

        Map<String, Long> reactedTopicCountsByUser = new HashMap<>();
        getEventStats(ConversationsEvent.REACTED_TO_TOPIC, siteId, from, to, userIds).forEach(stat -> {

            Long current = reactedTopicCountsByUser.getOrDefault(stat.getUserId(), 0L);
            current += stat.getCount();
            reactedTopicCountsByUser.put(stat.getUserId(), current);
        });

        Map<String, Long> upvotedTopicCountsByUser = new HashMap<>();
        getEventStats(ConversationsEvent.TOPIC_UPVOTED, siteId, from, to, userIds).forEach(stat -> {

            Long current = upvotedTopicCountsByUser.getOrDefault(stat.getUserId(), 0L);
            current += stat.getCount();
            upvotedTopicCountsByUser.put(stat.getUserId(), current);
        });

        Map<String, Long> topicViewedCounts
            = topicStatusRepository.countBySiteIdAndViewed(siteId, Boolean.TRUE).stream()
                .collect(Collectors.toMap(pair -> (String) pair[0], pair -> (Long) pair[1]));

        Map<String, Long> postCountsByUser = new HashMap<>();
        getEventStats(ConversationsEvent.POST_CREATED, siteId, from, to, userIds).forEach(stat -> {

            Long current = postCountsByUser.getOrDefault(stat.getUserId(), 0L);
            current += stat.getCount();
            postCountsByUser.put(stat.getUserId(), current);
        });

        Map<String, Long> reactedPostCountsByUser = new HashMap<>();
        getEventStats(ConversationsEvent.REACTED_TO_POST, siteId, from, to, userIds).forEach(stat -> {

            Long current = reactedPostCountsByUser.getOrDefault(stat.getUserId(), 0L);
            current += stat.getCount();
            reactedPostCountsByUser.put(stat.getUserId(), current);
        });

        Map<String, Long> upvotedPostCountsByUser = new HashMap<>();
        getEventStats(ConversationsEvent.POST_UPVOTED, siteId, from, to, userIds).forEach(stat -> {

            Long current = upvotedPostCountsByUser.getOrDefault(stat.getUserId(), 0L);
            current += stat.getCount();
            upvotedPostCountsByUser.put(stat.getUserId(), current);
        });

        Map<String, Long> viewedPostCountsByUser = new HashMap<>();
        getEventStats(ConversationsEvent.POST_VIEWED, siteId, from, to, userIds).forEach(stat -> {

            Long current = viewedPostCountsByUser.getOrDefault(stat.getUserId(), 0L);
            current += stat.getCount();
            viewedPostCountsByUser.put(stat.getUserId(), current);
        });

        List<ConversationsStat> stats = users.stream().map(user -> {

            ConversationsStat stat = new ConversationsStat();
            stat.name = user.getSortName();

            stat.topicsCreated = topicCountsByUser.getOrDefault(user.getId(), 0L);
            stat.topicsViewed = topicViewedCounts.getOrDefault(user.getId(), 0L);
            stat.topicReactions = reactedTopicCountsByUser.getOrDefault(user.getId(), 0L);
            stat.topicUpvotes = upvotedTopicCountsByUser.getOrDefault(user.getId(), 0L);

            stat.postsCreated = postCountsByUser.getOrDefault(user.getId(), 0L);
            stat.postsViewed = viewedPostCountsByUser.getOrDefault(user.getId(), 0L);
            stat.postReactions = reactedPostCountsByUser.getOrDefault(user.getId(), 0L);
            stat.postUpvotes = upvotedPostCountsByUser.getOrDefault(user.getId(), 0L);
            return stat;
        }).collect(Collectors.toList());

        String baseCacheKey = siteId + "/conversations/";
        String nameAscendingKey = baseCacheKey + SORT_NAME_ASCENDING;

        List<ConversationsStat> sortedStats = new ArrayList<>();
        if (sort == null) {
            sortedStats = sortedStatsCache.get(nameAscendingKey);
            if (sortedStats == null) {
                sortedStats = stats.stream().sorted(Comparator.comparing(s -> s.name)).collect(Collectors.toList());
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
                case SORT_TOPIC_REACTIONS_ASCENDING:
                    String topicReactionsAscendingKey = baseCacheKey + SORT_TOPIC_REACTIONS_ASCENDING;
                    sortedStats = sortedStatsCache.get(topicReactionsAscendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getTopicReactions)).collect(Collectors.toList());
                        sortedStatsCache.put(topicReactionsAscendingKey, sortedStats);
                    }
                    break;
                case SORT_TOPIC_REACTIONS_DESCENDING:
                    String topicReactionsDescendingKey = baseCacheKey + SORT_TOPIC_REACTIONS_DESCENDING;
                    sortedStats = sortedStatsCache.get(topicReactionsDescendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getTopicReactions).reversed()).collect(Collectors.toList());
                        sortedStatsCache.put(topicReactionsDescendingKey, sortedStats);
                    }
                    break;
                case SORT_TOPIC_UPVOTES_ASCENDING:
                    String topicUpvotesAscendingKey = baseCacheKey + SORT_TOPIC_UPVOTES_ASCENDING;
                    sortedStats = sortedStatsCache.get(topicUpvotesAscendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getTopicUpvotes)).collect(Collectors.toList());
                        sortedStatsCache.put(topicUpvotesAscendingKey, sortedStats);
                    }
                    break;
                case SORT_TOPIC_UPVOTES_DESCENDING:
                    String topicUpvotesDescendingKey = baseCacheKey + SORT_TOPIC_UPVOTES_DESCENDING;
                    sortedStats = sortedStatsCache.get(topicUpvotesDescendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getTopicUpvotes).reversed()).collect(Collectors.toList());
                        sortedStatsCache.put(topicUpvotesDescendingKey, sortedStats);
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
                case SORT_POSTS_READ_ASCENDING:
                    String postsReadAscendingKey = baseCacheKey + SORT_POSTS_READ_ASCENDING;
                    sortedStats = sortedStatsCache.get(postsReadAscendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getPostsViewed)).collect(Collectors.toList());
                        sortedStatsCache.put(postsReadAscendingKey, sortedStats);
                    }
                    break;
                case SORT_POSTS_READ_DESCENDING:
                    String postsReadDescendingKey = baseCacheKey + SORT_POSTS_READ_DESCENDING;
                    sortedStats = sortedStatsCache.get(postsReadDescendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getPostsViewed).reversed()).collect(Collectors.toList());
                        sortedStatsCache.put(postsReadDescendingKey, sortedStats);
                    }
                    break;
                case SORT_POST_REACTIONS_ASCENDING:
                    String postReactionsAscendingKey = baseCacheKey + SORT_POST_REACTIONS_ASCENDING;
                    sortedStats = sortedStatsCache.get(postReactionsAscendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getPostReactions)).collect(Collectors.toList());
                        sortedStatsCache.put(postReactionsAscendingKey, sortedStats);
                    }
                    break;
                case SORT_POST_REACTIONS_DESCENDING:
                    String postReactionsDescendingKey = baseCacheKey + SORT_POST_REACTIONS_DESCENDING;
                    sortedStats = sortedStatsCache.get(postReactionsDescendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getPostReactions).reversed()).collect(Collectors.toList());
                        sortedStatsCache.put(postReactionsDescendingKey, sortedStats);
                    }
                    break;
                case SORT_POST_UPVOTES_ASCENDING:
                    String postUpvotesAscendingKey = baseCacheKey + SORT_POST_UPVOTES_ASCENDING;
                    sortedStats = sortedStatsCache.get(postUpvotesAscendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getPostUpvotes)).collect(Collectors.toList());
                        sortedStatsCache.put(postUpvotesAscendingKey, sortedStats);
                    }
                    break;
                case SORT_POST_UPVOTES_DESCENDING:
                    String postUpvotesDescendingKey = baseCacheKey + SORT_POST_UPVOTES_DESCENDING;
                    sortedStats = sortedStatsCache.get(postUpvotesDescendingKey);
                    if (sortedStats == null) {
                        sortedStats = stats.stream().sorted(Comparator.comparing(ConversationsStat::getPostUpvotes).reversed()).collect(Collectors.toList());
                        sortedStatsCache.put(postUpvotesDescendingKey, sortedStats);
                    }
                    break;
                default:
            }
        }

        int start = pageSize * (page - 1);
        int end = start + pageSize;

        if (end > sortedStats.size()) end = sortedStats.size();

        return Map.of("total", userIds.size(),
                        "pageSize", pageSize,
                        "currentPage", page,
                        "stats", sortedStats.subList(start, end));
    }

    private List<Stat> getEventStats(ConversationsEvent event, String siteId, Instant from, Instant to, List<String> userIds) {

        return statsManager.getEventStats(siteId,
            List.of(event.label),
            from != null ? Date.from(from) : null,
            to != null ? Date.from(to) : null,
            userIds,
            false, null, null, null, false, 0);
    }

    @Override
    public void clearCacheForGradedTopic(Long gradingItemId) {

        topicRepository.findTopicsByGradingItemId(gradingItemId)
            .forEach(t -> postsCache.remove(t.getId()));
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
        return new String[] { ConversationsEvent.TOPIC_CREATED.label,
                                ConversationsEvent.TOPIC_UPVOTED.label,
                                ConversationsEvent.REACTED_TO_TOPIC.label,
                                ConversationsEvent.POST_CREATED.label,
                                ConversationsEvent.POST_VIEWED.label,
                                ConversationsEvent.POST_UPVOTED.label,
                                ConversationsEvent.REACTED_TO_POST.label };
    }

    @Override
    public String[] myToolIds() {
        return new String[] { TOOL_ID };
    }

    @Override
    public Optional<List<String>> getTransferOptions() {
        return Optional.of(Arrays.asList(new String[] { EntityTransferrer.COPY_PERMISSIONS_OPTION }));
    }

    @Override
    public List<Map<String, String>> getEntityMap(String fromContext) {

        try {
            return getTopicsForSite(fromContext).stream()
                .map(t -> Map.of("id", t.id, "title", t.title)).collect(Collectors.toList());
        } catch (ConversationsPermissionsException cpe) {
            log.warn("Failed to get topics for site {}: {}", fromContext, cpe.toString());
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public String getToolPermissionsPrefix() {
        return Permissions.PREFIX;
    }

    @Override
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions) {

        Map<String, String> traversalMap = new HashMap<>();

        try {
            getTopicsForSite(fromContext).stream().map(fromBean -> {

                    TopicTransferBean newBean = new TopicTransferBean();

                    newBean.id = fromBean.id;
                    newBean.title = fromBean.title;
                    newBean.message = ltiService.fixLtiLaunchUrls(fromBean.message, fromContext, toContext, traversalMap);
                    newBean.siteId = toContext;
                    newBean.draft = true;
                    newBean.type = fromBean.type;

                    return newBean;
                }).forEach(tb -> {

                    if (CollectionUtils.isEmpty(ids) || ids.contains(tb.id)) {

                        String fromId = tb.id;
                        tb.id = null;

                        try {
                            TopicTransferBean newTopicBean = saveTopic(tb, false);
                            String fromRef
                                = ConversationsReferenceReckoner.reckoner().siteId(fromContext).type("t").id(fromId).reckon().getReference();
                            String toRef
                                = ConversationsReferenceReckoner.reckoner().siteId(toContext).type("t").id(newTopicBean.id).reckon().getReference();
                            traversalMap.put(fromRef, toRef);
                        } catch (ConversationsPermissionsException e) {
                            log.error("Failed to save topic \"{}\" during site import : {}", tb.title, e.toString());
                        }
                    }
                });
        } catch (ConversationsPermissionsException e) {
            log.warn("Failed to get topics for site {} during site import : {}", fromContext, e.toString());
        }

        return traversalMap;
    }

    @Override
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions, boolean cleanup) {

        if (cleanup) {
            topicRepository.findBySiteId(toContext).forEach(t -> {

                try {
                    deleteTopic(t.getId());
                } catch (ConversationsPermissionsException cpe) {
                    log.warn("No permission to delete topic {}", t.getId());
                }
            });
        }

        return transferCopyEntities(fromContext, toContext, ids, transferOptions);
    }

    @Override
    public boolean willArchiveMerge() {
        return true;
    }

    @Override
    public String getLabel() {
        return "conversations";
    }

    @Override
    public String archive(String siteId, Document doc, Stack<Element> stack, String archivePath, List<Reference> attachments) {

        StringBuilder results = new StringBuilder();
        results.append("begin archiving ").append(getLabel()).append(" for site ").append(siteId).append(System.lineSeparator());

        Element element = doc.createElement(getLabel());
        stack.peek().appendChild(element);
        stack.push(element);

        Element topicsEl = doc.createElement("topics");
        element.appendChild(topicsEl);

        topicRepository.findBySiteId(siteId).stream().sorted((t1, t2) -> t1.getTitle().compareTo(t2.getTitle())).forEach(topic -> {

            Element topicEl = doc.createElement("topic");
            topicsEl.appendChild(topicEl);
            topicEl.setAttribute("title", topic.getTitle());
            topicEl.setAttribute("type", topic.getType().name());
            topicEl.setAttribute("post-before-viewing", Boolean.toString(topic.getMustPostBeforeViewing()));
            topicEl.setAttribute("anonymous", Boolean.toString(topic.getAnonymous()));
            topicEl.setAttribute("allow-anonymous-posts", Boolean.toString(topic.getAllowAnonymousPosts()));
            topicEl.setAttribute("pinned", Boolean.toString(topic.getPinned()));
            topicEl.setAttribute("draft", Boolean.toString(topic.getDraft()));
            topicEl.setAttribute("visibility", topic.getVisibility().name());
            topicEl.setAttribute("creator", topic.getMetadata().getCreator());
            topicEl.setAttribute("created", Long.toString(topic.getMetadata().getCreated().getEpochSecond()));
            topicEl.setAttribute("graded", Boolean.toString(topic.getGraded()));

            if (topic.getShowDate() != null) {
              topicEl.setAttribute("show-date", Long.toString(topic.getShowDate().getEpochSecond()));
            }
            if (topic.getHideDate() != null) {
              topicEl.setAttribute("hide-date", Long.toString(topic.getHideDate().getEpochSecond()));
            }
            if (topic.getLockDate() != null) {
              topicEl.setAttribute("lock-date", Long.toString(topic.getLockDate().getEpochSecond()));
            }
            if (topic.getDueDate() != null) {
              topicEl.setAttribute("due-date", Long.toString(topic.getDueDate().getEpochSecond()));
            }

            Element messageEl = doc.createElement("message");
            messageEl.appendChild(doc.createCDATASection(topic.getMessage()));
            topicEl.appendChild(messageEl);
        });

        results.append("completed archiving ").append(getLabel()).append(" for site ").append(siteId).append(System.lineSeparator());
        return results.toString();
    }

    @Override
    public String merge(String toSiteId, Element root, String archivePath, String fromSiteId, Map<String, String> attachmentNames, Map<String, String> userIdTrans, Set<String> userListAllowImport) {

        StringBuilder results = new StringBuilder();
        results.append("begin merging ").append(getLabel()).append(" for site ").append(toSiteId).append(System.lineSeparator());

        if (!root.getTagName().equals(getLabel())) {
            log.warn("Tried to merge a non <{}> xml document", getLabel());
            return "Invalid xml document";
        }

        Set<String> currentTitles = topicRepository.findBySiteId(toSiteId)
            .stream().map(ConversationsTopic::getTitle).collect(Collectors.toSet());

        NodeList topicNodes = root.getElementsByTagName("topic");

        Instant now = Instant.now();

        for (int i = 0; i < topicNodes.getLength(); i++) {

            Element topicEl = (Element) topicNodes.item(i);
            String title = topicEl.getAttribute("title");

            if (currentTitles.contains(title)) {
                log.debug("Topic \"{}\" already exists in site {}. Skipping merge ...", title, toSiteId);
                continue;
            }

            TopicTransferBean topicBean = new TopicTransferBean();
            topicBean.siteId = toSiteId;
            topicBean.title = title;
            topicBean.type = topicEl.getAttribute("type");
            topicBean.created = now;
            topicBean.mustPostBeforeViewing = Boolean.parseBoolean(topicEl.getAttribute("post-before-viewing"));
            topicBean.anonymous = Boolean.parseBoolean(topicEl.getAttribute("anonymous"));
            topicBean.allowAnonymousPosts = Boolean.parseBoolean(topicEl.getAttribute("allow-anonymous-posts"));
            topicBean.draft = true;
            topicBean.pinned = Boolean.parseBoolean(topicEl.getAttribute("pinned"));
            topicBean.visibility = topicEl.getAttribute("visibility");
            topicBean.graded = Boolean.parseBoolean(topicEl.getAttribute("graded"));

            topicBean.showDate = topicEl.hasAttribute("show-date") ? Instant.ofEpochSecond(Long.parseLong(topicEl.getAttribute("show-date"))) : null;
            topicBean.hideDate = topicEl.hasAttribute("hide-date") ? Instant.ofEpochSecond(Long.parseLong(topicEl.getAttribute("hide-date"))) : null;
            topicBean.lockDate = topicEl.hasAttribute("lock-date") ? Instant.ofEpochSecond(Long.parseLong(topicEl.getAttribute("lock-date"))) : null;
            topicBean.dueDate = topicEl.hasAttribute("due-date") ? Instant.ofEpochSecond(Long.parseLong(topicEl.getAttribute("due-date"))) : null;

            NodeList messageNodes = topicEl.getElementsByTagName("message");
            if (messageNodes.getLength() == 1) {
                topicBean.message = ((Element) messageNodes.item(0)).getFirstChild().getNodeValue();
            }

            try {
                saveTopic(topicBean, false);
            } catch (Exception e) {
                log.warn("Failed to merge topic \"{}\": {}", topicBean.title, e.toString());
            }
        }

        return "";
    }

    @Override
    public boolean parseEntityReference(String referenceString, Reference ref) {

        if (referenceString.startsWith(REFERENCE_ROOT)) {
            ConversationsReferenceReckoner.ConversationsReference reference
                = ConversationsReferenceReckoner.reckoner().reference(referenceString).reckon();
            ref.set(TOOL_ID, reference.getType(), reference.getId(), null, reference.getSiteId());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Entity getEntity(Reference ref) {

        ConversationsReferenceReckoner.ConversationsReference reference
                = ConversationsReferenceReckoner.reckoner().reference(ref.getReference()).reckon();

        if (!securityService.unlock(SiteService.SITE_VISIT, siteService.siteReference(reference.getSiteId()))) {
            log.warn("Current user connot get topic entities for site {}", reference.getSiteId());
            return null;
        } else {
            switch (reference.getType()) {
                case "t":
                    return topicRepository.findById(reference.getId()).map(TopicTransferBean::of).orElse(null);
                case "p":
                    return postRepository.findById(reference.getId()).map(PostTransferBean::of).orElse(null);
                default:
                    log.warn("Unrecognised entity type {}. Returning null ...", reference.getType());
                    return null;
            }
        }
    }

    private Calendar getCalendar(String site) {

        Calendar calendar = null;

        String calendarId = calendarService.calendarReference(site, siteService.MAIN_CONTAINER);
       try {
            calendar = calendarService.getCalendar(calendarId);
        } catch (IdUnusedException e) {
            log.warn("No calendar found for site: {}", site);
        } catch (PermissionException e) {
            log.error("The current user does not have permission to access the calendar for site: {}", site, e.toString());
        } catch (Exception ex) {
            log.error("Unknown exception occurred retrieving calendar for site: {}", site, ex.toString());
        }

        return calendar;
    }
}
