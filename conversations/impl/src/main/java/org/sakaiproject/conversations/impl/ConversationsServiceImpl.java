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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.sakaiproject.conversations.api.ConversationsPermissionsException;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.ConversationsStat;
import org.sakaiproject.conversations.api.Events;
import org.sakaiproject.conversations.api.Permissions;
import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.conversations.api.TopicVisibility;
import org.sakaiproject.conversations.api.beans.CommentTransferBean;
import org.sakaiproject.conversations.api.beans.TopicTransferBean;
import org.sakaiproject.conversations.api.beans.PostTransferBean;
import org.sakaiproject.conversations.api.model.Comment;
import org.sakaiproject.conversations.api.model.ConvStatus;
import org.sakaiproject.conversations.api.model.Post;
import org.sakaiproject.conversations.api.model.PostReaction;
import org.sakaiproject.conversations.api.model.PostReactionTotal;
import org.sakaiproject.conversations.api.model.PostStatus;
import org.sakaiproject.conversations.api.model.Settings;
import org.sakaiproject.conversations.api.model.Tag;
import org.sakaiproject.conversations.api.model.Topic;
import org.sakaiproject.conversations.api.model.TopicReaction;
import org.sakaiproject.conversations.api.model.TopicReactionTotal;
import org.sakaiproject.conversations.api.model.TopicStatus;
import org.sakaiproject.conversations.api.repository.CommentRepository;
import org.sakaiproject.conversations.api.repository.ConvStatusRepository;
import org.sakaiproject.conversations.api.repository.PostRepository;
import org.sakaiproject.conversations.api.repository.PostReactionRepository;
import org.sakaiproject.conversations.api.repository.PostReactionTotalRepository;
import org.sakaiproject.conversations.api.repository.PostStatusRepository;
import org.sakaiproject.conversations.api.repository.SettingsRepository;
import org.sakaiproject.conversations.api.repository.TagRepository;
import org.sakaiproject.conversations.api.repository.TopicRepository;
import org.sakaiproject.conversations.api.repository.TopicReactionRepository;
import org.sakaiproject.conversations.api.repository.TopicReactionTotalRepository;
import org.sakaiproject.conversations.api.repository.TopicStatusRepository;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
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
public class ConversationsServiceImpl implements ConversationsService, Observer {

    private AuthzGroupService authzGroupService;

    private FunctionManager functionManager;

    private CommentRepository commentRepository;

    private ConvStatusRepository convStatusRepository;

    private EventTrackingService eventTrackingService;

    private MemoryService memoryService;

    private PostRepository postRepository;

    private PostReactionRepository postReactionRepository;

    private PostReactionTotalRepository postReactionTotalRepository;

    private PostStatusRepository postStatusRepository;

    private SecurityService securityService;

    private SessionManager sessionManager;

    private SettingsRepository settingsRepository;

    private StatsManager statsManager;

    private TagRepository tagRepository;

    private TopicReactionRepository topicReactionRepository;

    private TopicReactionTotalRepository topicReactionTotalRepository;

    private TopicRepository topicRepository;

    private TopicStatusRepository topicStatusRepository;

    private UserDirectoryService userDirectoryService;

    private UserTimeService userTimeService;

    private static ResourceLoader bundle = new ResourceLoader("conversations");

    private Cache<String, List<ConversationsStat>> sortedStatsCache;

    public void init() {

        Permissions.stream().forEach(p -> functionManager.registerFunction(p.label, true));
        this.sortedStatsCache = memoryService.<String, List<ConversationsStat>>getCache("conversationsSortedStatsCache");
        eventTrackingService.addObserver(this);
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

    @Transactional(readOnly = true)
    public List<TopicTransferBean> getTopicsForSite(String siteId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        final String reference = "/site/" + siteId;
        if (!securityService.unlock(SiteService.SITE_VISIT, reference)) {
            throw new ConversationsPermissionsException("Current user cannot view topics.");
        }

        Settings settings = getSettingsForSite(siteId);

        List<Topic> topics = topicRepository.findBySiteId(siteId).stream().filter(t -> {

            if (securityService.isSuperUser()) return true;

            if (t.getHidden()) {
                if (securityService.unlock(Permissions.MODERATE.label, reference)) return true;
                else return false;
            }

            if (t.getMetadata().getCreator().equals(currentUserId)) return true;

            if (!t.getDraft() && t.getVisibility() == TopicVisibility.SITE) return true;

            if (t.getVisibility() == TopicVisibility.INSTRUCTORS
                        && securityService.unlock(Permissions.ROLETYPE_INSTRUCTOR.label, reference)) {
                return true;
            }

            if (t.getVisibility() == TopicVisibility.GROUP) {
                if (securityService.unlock(Permissions.VIEW_GROUP_TOPICS.label, reference)) return true;

                ArrayList<String> groups = new ArrayList<>(t.getGroups());
                if (authzGroupService.getAuthzUserGroupIds(groups, currentUserId).stream().findAny().isPresent()) {
                    return true;
                }
            }

            return false;
        }).collect(Collectors.toList());
        return decorateTopics(topics, currentUserId, settings);
    }

    @Transactional
    public TopicTransferBean saveTopic(TopicTransferBean topicBean) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        String siteRef = "/site/" + topicBean.siteId;

        Settings settings = getSettingsForSite(topicBean.siteId);

        if (settings.getSiteLocked() && !securityService.unlock(Permissions.MODERATE.label, siteRef)) {
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

        Instant now = Instant.now();
        if (isNew) {
            topicBean.setCreator(currentUserId);
            topicBean.setCreated(now);
        } else {
            Optional<Topic> optTopic = topicRepository.findById(topicBean.id);
            if (optTopic.isPresent()
                && optTopic.get().getLocked()
                && !securityService.unlock(Permissions.MODERATE.label, siteRef)) {
                throw new ConversationsPermissionsException("Current user cannot update topic.");
            }
        }
        topicBean.setModifier(currentUserId);
        topicBean.setModified(now);

        Topic topic = topicRepository.save(topicBean.asTopic());

        topicBean = TopicTransferBean.of(topic);

        topicBean.tags = topic.getTagIds().stream().map(tagId -> {

            Optional<Tag> optTag = tagRepository.findById(tagId);
            if (optTag.isPresent()) {
                return optTag.get();
            } else {
                return null;
            }
        }).collect(Collectors.toList());
        

        TopicTransferBean decoratedBean = decorateTopicBean(topicBean, topic, currentUserId, settings);

        this.afterCommit(() -> {

            Events event = isNew ? Events.TOPIC_CREATED : Events.TOPIC_UPDATED;
            eventTrackingService.post(eventTrackingService.newEvent(event.label, decoratedBean.reference, decoratedBean.siteId, true, NotificationService.NOTI_OPTIONAL));
        });
        
        return topicBean;
    }

    @Transactional
    public void pinTopic(String topicId, boolean pinned) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        Optional<Topic> optTopic = topicRepository.findById(topicId);
        if (optTopic.isPresent()) {
            Topic topic = optTopic.get();
            if (!securityService.unlock(Permissions.TOPIC_PIN.label, "/site/" + topic.getSiteId())) {
                throw new ConversationsPermissionsException("Current user cannot pin topics.");
            }
            topic.setPinned(pinned);
            topicRepository.save(topic);
        } else {
            log.error("No topic for id {}", topicId);
        }
    }

    @Transactional
    public TopicTransferBean lockTopic(String topicId, boolean locked) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        Optional<Topic> optTopic = topicRepository.findById(topicId);
        if (optTopic.isPresent()) {
            Topic topic = optTopic.get();
            if (!securityService.unlock(Permissions.MODERATE.label, "/site/" + topic.getSiteId())) {
                throw new ConversationsPermissionsException("Current user cannot lock/unlock topics.");
            }
            topic.setLocked(locked);

            postRepository.lockByTopic_Id(locked, topicId);
            postRepository.findByTopic_Id(topicId).forEach(p -> recursivelyLockPosts(p, locked));

            Settings settings = getSettingsForSite(topic.getSiteId());
            return decorateTopicBean(TopicTransferBean.of(topicRepository.save(topic)), topic, currentUserId, settings);
        } else {
            log.error("No topic for id {}", topicId);
            throw new IllegalArgumentException("No topic for id " + topicId);
        }
    }

    public void recursivelyLockPosts(Post post, Boolean locked) {

        postRepository.lockByParentPost_Id(locked, post.getId());
        commentRepository.lockByPost_Id(post.getId(), locked);
        postRepository.findByParentPost_Id(post.getId()).forEach(p -> recursivelyLockPosts(p, locked));
    }

    @Transactional
    public void hideTopic(String topicId, boolean hidden) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        Optional<Topic> optTopic = topicRepository.findById(topicId);
        if (optTopic.isPresent()) {
            Topic topic = optTopic.get();
            if (!securityService.unlock(Permissions.MODERATE.label, "/site/" + topic.getSiteId())) {
                throw new ConversationsPermissionsException("Current user cannot hide/show topics.");
            }
            topic.setHidden(hidden);
            topicRepository.save(topic);
        } else {
            log.error("No topic for id {}", topicId);
            throw new IllegalArgumentException("No topic for id " + topicId);
        }
    }

    @Transactional
    public void bookmarkTopic(String topicId, boolean bookmarked) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        topicRepository.findById(topicId).ifPresent(topic -> {

            TopicStatus topicStatus = topicStatusRepository.findByTopicIdAndUserId(topicId, currentUserId)
                .orElseGet(() -> new TopicStatus(topic.getSiteId(), topicId, currentUserId));
            topicStatus.setBookmarked(bookmarked);
            topicStatusRepository.save(topicStatus);
        });
    }

    @Transactional
    public boolean deleteTopic(String topicId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        Optional<Topic> optTopic = topicRepository.findById(topicId);

        if (optTopic.isPresent()) {
            Topic topic = optTopic.get();

            String siteRef = "/site/" + topic.getSiteId();

            boolean isMine = topic.getMetadata().getCreator().equals(currentUserId);
            if (!securityService.unlock(Permissions.TOPIC_DELETE_ANY.label, siteRef)
                && (isMine && !securityService.unlock(Permissions.TOPIC_DELETE_OWN.label, siteRef))) {
                throw new ConversationsPermissionsException("Current user is not allowed to delete topic.");
            }

            postRepository.findByTopic_Id(topicId).forEach(p -> {

                postReactionRepository.deleteByPost_Id(p.getId());
                postReactionTotalRepository.deleteByPost_Id(p.getId());
                postStatusRepository.deleteByPostId(p.getId());
                commentRepository.deleteByPost_Id(p.getId());
                postRepository.deleteById(p.getId());
            });
            topicStatusRepository.deleteByTopicId(topicId);
            topicReactionRepository.deleteByTopic_Id(topicId);
            topicReactionTotalRepository.deleteByTopic_Id(topicId);
            topicRepository.delete(topic);

            afterCommit(() -> {
                String ref = "/conversations/topics/" + topicId;
                eventTrackingService.post(eventTrackingService.newEvent(Events.TOPIC_DELETED.label, ref, topic.getSiteId(), true, NotificationService.NOTI_OPTIONAL));
            });
            return true;
        } else {
            log.error("No topic for id {}. Returning false ...", topicId);
            return false;
        }
    }

    @Transactional
    public Map<Reaction, Integer> saveTopicReactions(String topicId, Map<Reaction, Boolean> reactions) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        Optional<Topic> optTopic = topicRepository.findById(topicId);

        if (!optTopic.isPresent()) {
            throw new IllegalArgumentException("No topic for id " + topicId);
        }

        Topic topic = optTopic.get();

        if (topic.getMetadata().getCreator().equals(currentUserId)) {
            throw new ConversationsPermissionsException("You can't react to your own topics");
        }

        List<TopicReaction> current = topicReactionRepository.findByTopic_IdAndUserId(topicId, currentUserId);

        reactions.entrySet().forEach(es -> {

            TopicReactionTotal total
                = topicReactionTotalRepository.findByTopic_IdAndReaction(topicId, es.getKey())
                    .orElseGet(() -> {
                        TopicReactionTotal t = new TopicReactionTotal();
                        t.setTopic(topic);
                        t.setReaction(es.getKey());
                        t.setTotal(0);
                        return t;
                    });

            Optional<TopicReaction> optExistingReaction = current.stream().filter(tr -> tr.getReaction() == es.getKey()).findAny();
            if (optExistingReaction.isPresent()) {
                TopicReaction existingReaction = optExistingReaction.get();
                if (!existingReaction.getState() && es.getValue()) {
                    // This reaction is being turned on. Increment the total.
                    total.setTotal(total.getTotal() + 1);
                    afterCommit(() -> {
                        String ref = "/conversations/topics/" + topicId;
                        eventTrackingService.post(eventTrackingService.newEvent(Events.REACTED_TO_TOPIC.label, ref, topic.getSiteId(), false, NotificationService.NOTI_OPTIONAL));
                    });
                } else if (existingReaction.getState() && !es.getValue()) {
                    // This reaction is being turned off. Decrement the total.
                    total.setTotal(total.getTotal() - 1);
                    afterCommit(() -> {
                        String ref = "/conversations/topics/" + topicId;
                        eventTrackingService.post(eventTrackingService.newEvent(Events.UNREACTED_TO_TOPIC.label, ref, topic.getSiteId(), false, NotificationService.NOTI_OPTIONAL));
                    });
                }
                existingReaction.setState(es.getValue());
                topicReactionRepository.save(existingReaction);
            } else {
                TopicReaction newReaction = new TopicReaction();
                newReaction.setTopic(optTopic.get());
                newReaction.setUserId(currentUserId);
                newReaction.setReaction(es.getKey());
                newReaction.setState(es.getValue());
                topicReactionRepository.save(newReaction);
                if (es.getValue()) {
                    total.setTotal(total.getTotal() + 1);
                }
            }
            afterCommit(() -> {
                String ref = "/conversations/topics/" + topicId;
                eventTrackingService.post(eventTrackingService.newEvent(Events.REACTED_TO_TOPIC.label, ref, topic.getSiteId(), false, NotificationService.NOTI_OPTIONAL));
            });
            topicReactionTotalRepository.save(total);
        });

        return topicReactionTotalRepository.findByTopic_Id(topic.getId())
                .stream().collect(Collectors.toMap(rt -> rt.getReaction(), rt -> rt.getTotal()));
    }

    @Transactional
    public PostTransferBean savePost(PostTransferBean postBean) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        String siteRef = "/site/" + postBean.siteId;

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

        // We're creating a new topic, so set the initial dates of creation and modification
        Instant now = Instant.now();
        if (isNew) {
            postBean.setCreator(currentUserId);
            postBean.setCreated(now);
        }
        postBean.setModifier(currentUserId);
        postBean.setModified(now);

        Post post = postBean.asPost();
        Optional<Topic> optTopic = topicRepository.findById(postBean.topic);

        if (optTopic.isPresent()) {
            Topic topic = optTopic.get();
            if (topic.getLocked() && !securityService.unlock(Permissions.MODERATE.label, siteRef)) {
                throw new ConversationsPermissionsException("Current user cannot update posts on locked topics.");
            }
            post.setTopic(topic);
            post.setLocked(topic.getLocked());
            post = postRepository.save(post);
            if (!post.getDraft() && !post.getPrivatePost()
                && securityService.unlock(currentUserId, Permissions.ROLETYPE_INSTRUCTOR.label, siteRef)) {
                topic.setResolved(true);
            }
            topic = topicRepository.save(topic);

            PostTransferBean decoratedBean = decoratePostBean(PostTransferBean.of(post), postBean.siteId, postBean.topic, currentUserId, settings, null);

            this.afterCommit(() -> {

                Events event = isNew ? Events.POST_CREATED : Events.POST_UPDATED;
                eventTrackingService.post(eventTrackingService.newEvent(event.label, decoratedBean.reference, postBean.siteId, true, NotificationService.NOTI_OPTIONAL));
            });

            return decoratedBean;
        } else {
            log.error("No topic for id {}. Returning null ...", postBean.topic);
            return null;
        }
    }

    private boolean canViewPost(Post post, Topic topic, String currentUserId) {

        if (securityService.isSuperUser()) return true;

        if (post.getMetadata().getCreator().equals(currentUserId)) return true;

        if (post.getPrivatePost()) {
            String parentCreator = post.getParentPost() != null
                ? post.getParentPost().getMetadata().getCreator() : topic.getMetadata().getCreator();
            if (parentCreator.equals(currentUserId)) return true;
        }

        if (!post.getPrivatePost() && !post.getDraft()) return true;

        return false;
    }

    @Transactional(readOnly = true)
    public List<PostTransferBean> getPostsByTopicId(String siteId, String topicId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        if (!securityService.unlock(SiteService.SITE_VISIT, "/site/" + siteId)) {
            throw new ConversationsPermissionsException("Current user cannot view posts.");
        }

        Optional<Topic> optTopic = topicRepository.findById(topicId);

        if (optTopic.isPresent()) {
            Settings settings = getSettingsForSite(siteId);
            Topic topic = optTopic.get();
            List<Post> posts = postRepository.findByTopic_Id(topicId)
                .stream().filter(p -> canViewPost(p, topic, currentUserId)).collect(Collectors.toList());

            // Grab all the stati for this user and post, in one.
            Map<String, PostStatus> postStati = postStatusRepository.findByUserId(currentUserId)
                .stream().collect(Collectors.toMap(s -> s.getPostId(), s -> s));

            return decoratePosts(posts, siteId, topicId, currentUserId, settings, postStati);
        } else {
            log.warn("No topic for id {}", topicId);
            return Collections.<PostTransferBean>emptyList();
        }
    }

    @Transactional
    public boolean deletePost(String siteId, String topicId, String postId, boolean setTopicResolved) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        Optional<Post> optPost = postRepository.findById(postId);

        if (optPost.isPresent()) {
            Post post = optPost.get();

            String siteRef = "/site/" + siteId;

            boolean isMine = post.getMetadata().getCreator().equals(currentUserId);

            if (!securityService.unlock(Permissions.POST_DELETE_ANY.label, siteRef)
                && !(isMine && securityService.unlock(Permissions.POST_DELETE_OWN.label, siteRef))) {
                throw new ConversationsPermissionsException("Current user is not allowed to delete post.");
            }

            commentRepository.deleteByPost_Id(postId);
            if (setTopicResolved && securityService.unlock(post.getMetadata().getCreator(), Permissions.ROLETYPE_INSTRUCTOR.label, siteRef)) {
                Topic topic = post.getTopic();
                setTopicResolved(topic);
                topicRepository.save(topic);
            }

            postStatusRepository.deleteByPostId(postId);
            postReactionTotalRepository.deleteByPost_Id(postId);
            postReactionRepository.deleteByPost_Id(postId);

            postRepository.delete(post);

            this.afterCommit(() -> {
                String reference = "/conversations/topics/" + topicId + "/posts/" + postId;
                eventTrackingService.post(eventTrackingService.newEvent(Events.POST_DELETED.label, reference, siteId, true, NotificationService.NOTI_OPTIONAL));
            });

            return true;
        } else { 
            log.error("No post for id {}. Returning false ...", postId);
            return false;
        }
    }

    @Transactional
    public PostTransferBean lockPost(String siteId, String topicId, String postId, boolean locked) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        Optional<Post> optPost = postRepository.findById(postId);
        if (optPost.isPresent()) {
            Post post = optPost.get();
            if (!securityService.unlock(Permissions.MODERATE.label, "/site/" + siteId)) {
                throw new ConversationsPermissionsException("Current user cannot lock/unlock posts.");
            }
            post.setLocked(locked);
            recursivelyLockPosts(post, locked);
            Settings settings = getSettingsForSite(siteId);
            return decoratePostBean(PostTransferBean.of(postRepository.save(post)), siteId, topicId, currentUserId, settings, null);
        } else {
            log.error("No post for id {}", postId);
            throw new IllegalArgumentException("No post for id " + postId);
        }
    }

    @Transactional
    public void hidePost(String postId, boolean hidden, String siteId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        Optional<Post> optPost = postRepository.findById(postId);
        if (optPost.isPresent()) {
            Post post = optPost.get();
            if (!securityService.unlock(Permissions.MODERATE.label, "/site/" + siteId)) {
                throw new ConversationsPermissionsException("Current user cannot hide/show posts.");
            }
            post.setHidden(hidden);
            postRepository.save(post);
        } else {
            log.error("No post for id {}", postId);
        }
    }

    @Transactional
    public Map<Reaction, Integer> savePostReactions(String postId, Map<Reaction, Boolean> reactions) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        Optional<Post> optPost = postRepository.findById(postId);

        if (!optPost.isPresent()) {
            throw new IllegalArgumentException("No post for id " + postId);
        }

        Post topic = optPost.get();

        if (topic.getMetadata().getCreator().equals(currentUserId)) {
            throw new ConversationsPermissionsException("You can't react to your own posts");
        }

        List<PostReaction> current = postReactionRepository.findByPost_IdAndUserId(postId, currentUserId);

        reactions.entrySet().forEach(es -> {

            PostReactionTotal total
                = postReactionTotalRepository.findByPost_IdAndReaction(postId, es.getKey())
                    .orElseGet(() -> {
                        PostReactionTotal t = new PostReactionTotal();
                        t.setPost(topic);
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
                newReaction.setPost(optPost.get());
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

        return postReactionTotalRepository.findByPost_Id(topic.getId())
                .stream().collect(Collectors.toMap(rt -> rt.getReaction(), rt -> rt.getTotal()));
    }

    public void markPostsViewed(Set<String> postIds, String topicId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        postIds.forEach(postId -> {

            PostStatus status = postStatusRepository.findByPostIdAndUserId(postId, currentUserId)
                .orElseGet(() -> new PostStatus(topicId, postId, currentUserId));
            status.setViewed(true);
            status.setViewedDate(Instant.now());
            try {
                postStatusRepository.save(status);
            } catch (Exception e) {
                log.debug("Caught exception while marking posts viewed. This can happen " +
                    "due to the way the client detects posts scrolling into view");
            }
        });

        topicRepository.findById(topicId).ifPresent(topic -> {

            long all = postRepository.findByTopic_Id(topicId).stream().filter(p -> canViewPost(p, topic, currentUserId)).count();
            long viewed = postStatusRepository.findByTopicIdAndUserId(topicId, currentUserId).stream().count();

            TopicStatus topicStatus = topicStatusRepository.findByTopicIdAndUserId(topicId, currentUserId)
                .orElseGet(() -> new TopicStatus(topic.getSiteId(), topicId, currentUserId));
            topicStatus.setViewed(all == viewed);
            try {
                topicStatusRepository.save(topicStatus);
            } catch (Exception e) {
                log.debug("Caught exception while marking posts viewed. This can happen " +
                    "due to the way the client detects posts scrolling into view");
            }
        });
    }

    @Transactional
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

        Comment comment = commentBean.asComment();
        postRepository.findById(commentBean.post).ifPresent(p -> comment.setPost(p));

        Comment updatedComment = commentRepository.save(comment);

        if (isNew) {
            // Up the comment count by one
            Post post = updatedComment.getPost();
            post.setNumberOfComments(post.getNumberOfComments() + 1);
            postRepository.save(post);
        }

        return decorateCommentBean(CommentTransferBean.of(updatedComment), commentBean.siteId, currentUserId);
    }

    @Transactional
    public boolean deleteComment(String siteId, String commentId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        Optional<Comment> optComment = commentRepository.findById(commentId);

        if (optComment.isPresent()) {
            Comment comment = optComment.get();
            Post post = comment.getPost();

            String siteRef = "/site/" + siteId;

            boolean isMine = comment.getMetadata().getCreator().equals(currentUserId);
            if (!securityService.unlock(Permissions.COMMENT_DELETE_ANY.label, siteRef)
                && (isMine && !securityService.unlock(Permissions.COMMENT_DELETE_OWN.label, siteRef))) {
                throw new ConversationsPermissionsException("Current user cannot delete comment");
            }
            commentRepository.deleteById(commentId);
            // Drop the comment count by one
            post.setNumberOfComments(post.getNumberOfComments() - 1);
            postRepository.save(post);
            return true;
        } else {
            log.error("No comment for id {}. Returning false ...", commentId);
            return false;
        }
    }

    private void setTopicResolved(Topic topic) {

        String siteRef = "/site/" + topic.getSiteId();

        topic.setResolved(postRepository.findByTopic_Id(topic.getId()).stream().anyMatch(p -> {
            return !p.getDraft() && securityService.unlock(p.getMetadata().getCreator(), Permissions.ROLETYPE_INSTRUCTOR.label, siteRef);
        }));
    }

    @Transactional
    private List<TopicTransferBean> decorateTopics(List<Topic> topics, String currentUserId, Settings settings) {
        return topics.stream().map(t -> decorateTopicBean(TopicTransferBean.of(t), t, currentUserId, settings)).collect(Collectors.toList());
    }

    @Transactional
    private TopicTransferBean decorateTopicBean(TopicTransferBean topicBean, Topic topic, String currentUserId, Settings settings) {

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

        if (!topicBean.locked) {
            topicBean.canEdit = securityService.unlock(Permissions.TOPIC_UPDATE_ANY.label, siteRef)
                || (topicBean.isMine && securityService.unlock(Permissions.TOPIC_UPDATE_OWN.label, siteRef));
            topicBean.canDelete = securityService.unlock(Permissions.TOPIC_DELETE_ANY.label, siteRef)
                || (topicBean.isMine && securityService.unlock(Permissions.TOPIC_DELETE_OWN.label, siteRef));
            topicBean.canPost = securityService.unlock(Permissions.POST_CREATE.label, siteRef);
            topicBean.canPin = settings.getAllowPinning() && securityService.unlock(Permissions.TOPIC_PIN.label, siteRef);
            topicBean.canBookmark = settings.getAllowBookmarking();
            topicBean.canTag = securityService.unlock(Permissions.TOPIC_TAG.label, siteRef);
            topicBean.canReact = !topicBean.isMine && settings.getAllowReactions();
        }

        if (!topicBean.draft) {
            topicBean.canModerate = securityService.unlock(Permissions.MODERATE.label, siteRef);
        }

        topicBean.tags = topic.getTagIds().stream().map(tagId -> {

            Optional<Tag> optTag = tagRepository.findById(tagId);
            if (optTag.isPresent()) {
                return optTag.get();
            } else {
                return null;
            }
        }).collect(Collectors.toList());

        topicStatusRepository.findByTopicIdAndUserId(topic.getId(), currentUserId)
            .ifPresent(s -> topicBean.bookmarked = s.getBookmarked());

        topicBean.myReactions = topicReactionRepository.findByTopic_IdAndUserId(topic.getId(), currentUserId)
            .stream().collect(Collectors.toMap(tr -> tr.getReaction(), tr -> tr.getState()));

        Reaction.stream().forEach(r -> {
            if (!topicBean.myReactions.keySet().contains(r)) {
                topicBean.myReactions.put(r, Boolean.FALSE);
            }
        });

        topicBean.reactionTotals = topicReactionTotalRepository.findByTopic_Id(topic.getId())
                .stream().collect(Collectors.toMap(rt -> rt.getReaction(), rt -> rt.getTotal()));

        if (topicBean.anonymous && !securityService.unlock(Permissions.VIEW_ANONYMOUS.label, siteRef)) {
            topicBean.creatorDisplayName = bundle.getString("anonymous");
        }

        topicBean.numberOfPosts = getNumberOfPostsInTopic(topic, currentUserId);
        Long read = postStatusRepository
            .findByTopicIdAndUserIdAndViewed(topic.getId(), currentUserId, true).stream().count();
        topicBean.numberOfUnreadPosts = topicBean.numberOfPosts - read;

        topicBean.url = "/api/sites/" + topicBean.siteId + "/topics/" + topicBean.id;
        topicBean.reference = "/conversations/topics/" + topicBean.id;

        return topicBean;
    }

    /*
    private boolean canViewPost(Post post) {

        if (securityService.isSuperUser()) return true;

        if (p.getMetadata().getCreator().equals(currentUserId)) return true;

        if (p.getPrivatePost()) {
            String parentCreator = p.getParentPost() != null
                ? p.getParentPost().getMetadata().getCreator() : topic.getMetadata().getCreator();
            if (parentCreator.equals(currentUserId)) return true;
        }

        if (!p.getPrivatePost()) return true;

        return false;
    }
    */

    @Transactional
    private long getNumberOfPostsInTopic(Topic topic, String currentUserId) {

        return postRepository.findByTopic_Id(topic.getId()).stream().filter(p -> {

            if (securityService.isSuperUser()) return true;

            if (p.getMetadata().getCreator().equals(currentUserId)) return true;

            if (p.getPrivatePost()) {
                String parentCreator = p.getParentPost() != null
                    ? p.getParentPost().getMetadata().getCreator() : topic.getMetadata().getCreator();
                return parentCreator.equals(currentUserId);
            }

            if (p.getHidden() && securityService.unlock(Permissions.MODERATE.label, "/site/" + topic.getSiteId())) {
                return true;
            }

            if (!p.getHidden() && !p.getDraft()) return true;

            return false;
        }).count();
    }

    private List<PostTransferBean> decoratePosts(List<Post> posts, String siteId, String topicId, String currentUserId, Settings settings, Map<String, PostStatus> postStati) {
        return posts.stream().map(p -> decoratePostBean(PostTransferBean.of(p), siteId, topicId, currentUserId, settings, postStati)).collect(Collectors.toList());
    }

    private PostTransferBean decoratePostBean(PostTransferBean postBean, String siteId, String topicId, String currentUserId, Settings settings, Map<String, PostStatus> postStati) {

        try {
            User creator = userDirectoryService.getUser(postBean.creator);
            postBean.setCreatorDisplayName(creator.getDisplayName());
        } catch (UserNotDefinedException e) {
            log.error("No user for id: {}", postBean.creator);
        }

        String siteRef = "/site/" + siteId;

        postBean.isMine = postBean.creator.equals(currentUserId);
        postBean.formattedCreatedDate = userTimeService.dateTimeFormat(postBean.created, FormatStyle.MEDIUM, FormatStyle.SHORT);
        postBean.formattedModifiedDate = userTimeService.dateTimeFormat(postBean.modified, FormatStyle.MEDIUM, FormatStyle.SHORT);

        if (!postBean.locked) {
            postBean.canEdit = securityService.unlock(Permissions.POST_UPDATE_ANY.label, siteRef)
                    || (postBean.isMine && securityService.unlock(Permissions.POST_UPDATE_OWN.label, siteRef));
            postBean.canDelete = securityService.unlock(Permissions.POST_DELETE_ANY.label, siteRef)
                    || (postBean.isMine && securityService.unlock(Permissions.POST_DELETE_OWN.label, siteRef));
            postBean.canUpvote = !postBean.isMine && settings.getAllowUpvoting() && securityService.unlock(Permissions.POST_UPVOTE.label, siteRef);
            postBean.canReact = !postBean.isMine && settings.getAllowReactions() && securityService.unlock(Permissions.POST_REACT.label, siteRef);
            postBean.canComment = securityService.unlock(Permissions.COMMENT_CREATE.label, siteRef);
        }

        if (!postBean.draft) {
            postBean.canModerate = securityService.unlock(Permissions.MODERATE.label, siteRef);
        }

        postBean.canView = !postBean.hidden ? true : securityService.unlock(Permissions.MODERATE.label, siteRef);
        postBean.isInstructor = securityService.unlock(postBean.creator, Permissions.ROLETYPE_INSTRUCTOR.label, siteRef);
        postBean.canModerate = securityService.unlock(Permissions.MODERATE.label, siteRef);

        if (postBean.anonymous && !securityService.unlock(Permissions.VIEW_ANONYMOUS.label, siteRef)) {
            postBean.creatorDisplayName = bundle.getString("anonymous");
        }

        postStatusRepository.findByPostIdAndUserId(postBean.id, currentUserId).ifPresent(s -> {

            postBean.upvoted = s.getUpvoted();
            postBean.viewed = s.getViewed();
        });

        postBean.comments = decorateComments(commentRepository.findByPost_Id(postBean.id), siteId, currentUserId);

        postBean.myReactions = postReactionRepository.findByPost_IdAndUserId(postBean.id, currentUserId)
            .stream().collect(Collectors.toMap(pr -> pr.getReaction(), pr -> pr.getState()));

        Reaction.stream().forEach(r -> {
            if (!postBean.myReactions.keySet().contains(r)) {
                postBean.myReactions.put(r, Boolean.FALSE);
            }
        });

        postBean.reactionTotals = postReactionTotalRepository.findByPost_Id(postBean.id)
                .stream().collect(Collectors.toMap(rt -> rt.getReaction(), rt -> rt.getTotal()));

        if (postStati != null) {
            PostStatus postStatus = postStati.get(postBean.id);
            postBean.viewed = postStatus != null ? postStatus.getViewed() : false;
        } else {
            postStatusRepository.findByPostIdAndUserId(postBean.id, currentUserId).ifPresent(s -> {
                postBean.viewed = s.getViewed();
            });
        }

        postBean.url = "/api/sites/" + siteId + "/topics/" + topicId + "/posts/" + postBean.id;
        postBean.reference = "/conversations/topics/" + topicId + "/posts/" + postBean.id;

        return postBean;
    }

    private List<CommentTransferBean> decorateComments(List<Comment> comments, String siteId, String currentUserId) {
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

    @Transactional
    public PostTransferBean upvotePost(String siteId, String topicId, String postId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        Optional<Post> optPost = postRepository.findById(postId);
        if (optPost.isPresent()) {
            Post post = optPost.get();

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
        } else {
            log.error("No post for id {}", postId);
            throw new IllegalArgumentException("No post for id " + postId);
        }
    }

    @Transactional
    public PostTransferBean unUpvotePost(String siteId, String postId) throws ConversationsPermissionsException {

        String currentUserId = getCheckedCurrentUserId();

        Optional<Post> optPost = postRepository.findById(postId);
        if (optPost.isPresent()) {
            Post post = optPost.get();

            if (post.getMetadata().getCreator().equals(currentUserId)) {
                throw new IllegalArgumentException("Users cannot unupvote their own posts");
            }

            String siteRef = "/site/" + siteId;

            if (!securityService.unlock(Permissions.POST_UPVOTE.label, siteRef)) {
                throw new ConversationsPermissionsException("Current user cannot upvote posts");
            }

            boolean alreadyUpvoted = false;
            Optional<PostStatus> optStatus = postStatusRepository.findByPostIdAndUserId(postId, currentUserId);
            if (optStatus.isPresent()) {
                PostStatus status = optStatus.get();
                if (!status.getUpvoted()) {
                    throw new IllegalArgumentException("Post for id " + postId + " has not been upvoted yet");
                }
                status.setUpvoted(Boolean.FALSE);
                postStatusRepository.save(status);
                post.setUpvotes(post.getUpvotes() - 1);
                return PostTransferBean.of(postRepository.save(post));
            } else {
                throw new IllegalArgumentException("Post for id " + postId + " has not been upvoted yet");
            }
        } else {
            log.error("No post for id {}", postId);
            throw new IllegalArgumentException("No post for id " + postId);
        }
    }

    @Transactional
    public Tag saveTag(Tag tag) throws ConversationsPermissionsException {

        getCheckedCurrentUserId();

        String siteRef = "/site/" + tag.getSiteId();

        if (!securityService.unlock(Permissions.TAG_CREATE.label, siteRef)) {
            throw new ConversationsPermissionsException("Current user cannot create tags");
        }

        return tagRepository.save(tag);
    }

    @Transactional
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

    @Transactional
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

    @Transactional
    public Settings saveSettings(Settings settings) throws ConversationsPermissionsException {

        getCheckedCurrentUserId();

        if (!securityService.unlock(SiteService.SECURE_UPDATE_SITE, "/site/" + settings.getSiteId())) {
            throw new ConversationsPermissionsException("Current user cannot set site settings");
        }

        Settings old = getSettingsForSite(settings.getSiteId());
        Boolean oldSiteLocked = old.getSiteLocked();
        Boolean newSiteLocked = settings.getSiteLocked();

        if (oldSiteLocked != newSiteLocked) {
            for (Topic topic : topicRepository.findBySiteId(settings.getSiteId())) {
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

        return settingsRepository.save(settings);
    }

    public ConvStatus getConvStatusForSiteAndUser(String siteId, String userId) throws ConversationsPermissionsException {

        getCheckedCurrentUserId();

        return convStatusRepository.findBySiteIdAndUserId(siteId, userId).orElse(new ConvStatus(siteId, userId));

    }

    public void saveConvStatus(ConvStatus convStatus) throws ConversationsPermissionsException {

        getCheckedCurrentUserId();

        convStatusRepository.save(convStatus);
    }

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
            Arrays.asList(new String[] { Events.TOPIC_CREATED.label }),
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
            Arrays.asList(new String[] { Events.POST_CREATED.label }),
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
            Arrays.asList(new String[] { Events.REACTED_TO_TOPIC.label }),
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
        return new String[] { Events.TOPIC_CREATED.label, Events.POST_CREATED.label, Events.REACTED_TO_TOPIC.label };
    }
}
