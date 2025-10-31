/*
 * Copyright (c) 2003-2024 The Apereo Foundation
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

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sakaiproject.conversations.api.model.ConversationsPost;
import org.sakaiproject.conversations.api.model.ConversationsTopic;
import org.sakaiproject.conversations.api.model.Metadata;
import org.sakaiproject.conversations.api.repository.ConversationsCommentRepository;
import org.sakaiproject.conversations.api.repository.ConversationsPostRepository;
import org.sakaiproject.conversations.api.repository.ConversationsTopicRepository;
import org.sakaiproject.conversations.api.repository.PostReactionRepository;
import org.sakaiproject.conversations.api.repository.PostReactionTotalRepository;
import org.sakaiproject.conversations.api.repository.PostStatusRepository;
import org.sakaiproject.conversations.api.repository.TopicReactionRepository;
import org.sakaiproject.conversations.api.repository.TopicReactionTotalRepository;
import org.sakaiproject.conversations.api.repository.TopicStatusRepository;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.security.api.SecurityService;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class ConversationsServiceImplTest {

    private static final String SITE_ID = "site-id";
    private static final String TOPIC_ID = "topic-id";
    private static final String POST_ID = "post-id";

    private ConversationsServiceImpl service;
    private ConversationsTopicRepository topicRepository;
    private ConversationsPostRepository postRepository;
    private ConversationsCommentRepository commentRepository;
    private PostReactionRepository postReactionRepository;
    private PostReactionTotalRepository postReactionTotalRepository;
    private PostStatusRepository postStatusRepository;
    private TopicStatusRepository topicStatusRepository;
    private TopicReactionRepository topicReactionRepository;
    private TopicReactionTotalRepository topicReactionTotalRepository;
    private SessionManager sessionManager;
    private SecurityService securityService;
    private EventTrackingService eventTrackingService;
    private Cache<String, Map<String, Map<String, Object>>> postsCache;

    @BeforeEach
    void setUp() {
        service = new ConversationsServiceImpl();

        topicRepository = Mockito.mock(ConversationsTopicRepository.class);
        postRepository = Mockito.mock(ConversationsPostRepository.class);
        commentRepository = Mockito.mock(ConversationsCommentRepository.class);
        postReactionRepository = Mockito.mock(PostReactionRepository.class);
        postReactionTotalRepository = Mockito.mock(PostReactionTotalRepository.class);
        postStatusRepository = Mockito.mock(PostStatusRepository.class);
        topicStatusRepository = Mockito.mock(TopicStatusRepository.class);
        topicReactionRepository = Mockito.mock(TopicReactionRepository.class);
        topicReactionTotalRepository = Mockito.mock(TopicReactionTotalRepository.class);
        sessionManager = Mockito.mock(SessionManager.class);
        securityService = Mockito.mock(SecurityService.class);
        eventTrackingService = Mockito.mock(EventTrackingService.class);
        postsCache = Mockito.mock(Cache.class);

        service.setTopicRepository(topicRepository);
        service.setPostRepository(postRepository);
        service.setCommentRepository(commentRepository);
        service.setPostReactionRepository(postReactionRepository);
        service.setPostReactionTotalRepository(postReactionTotalRepository);
        service.setPostStatusRepository(postStatusRepository);
        service.setTopicStatusRepository(topicStatusRepository);
        service.setTopicReactionRepository(topicReactionRepository);
        service.setTopicReactionTotalRepository(topicReactionTotalRepository);
        service.setSessionManager(sessionManager);
        service.setSecurityService(securityService);
        service.setEventTrackingService(eventTrackingService);
        service.setPostsCache(postsCache);

        when(sessionManager.getCurrentSessionUserId()).thenReturn("admin-user");
        when(securityService.unlock(anyString(), anyString())).thenReturn(true);

        Event event = mock(Event.class);
        when(eventTrackingService.newEvent(anyString(), anyString(), anyString(), anyBoolean(), anyInt())).thenReturn(event);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void hardDeleteRemovesTopicsAndRelatedData() {
        ConversationsTopic topic = new ConversationsTopic();
        topic.setId(TOPIC_ID);
        topic.setSiteId(SITE_ID);
        Metadata metadata = new Metadata();
        metadata.setCreator("creator-id");
        metadata.setCreated(Instant.now());
        topic.setMetadata(metadata);

        ConversationsPost post = new ConversationsPost();
        post.setId(POST_ID);
        post.setTopic(topic);
        post.setSiteId(SITE_ID);
        post.setMessage("message");
        post.setMetadata(metadata);

        when(topicRepository.findBySiteId(SITE_ID)).thenReturn(List.of(topic));
        when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(topic));
        when(postRepository.findByTopicId(TOPIC_ID)).thenReturn(List.of(post));

        TransactionSynchronizationManager.initSynchronization();

        service.hardDelete(SITE_ID);

        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }

        verify(commentRepository).deleteByTopicId(TOPIC_ID);
        verify(postReactionRepository).deleteByPostId(POST_ID);
        verify(postReactionTotalRepository).deleteByPostId(POST_ID);
        verify(postStatusRepository).deleteByPostId(POST_ID);
        verify(postRepository).deleteById(POST_ID);
        verify(topicStatusRepository).deleteByTopicId(TOPIC_ID);
        verify(topicReactionRepository).deleteByTopicId(TOPIC_ID);
        verify(topicReactionTotalRepository).deleteByTopicId(TOPIC_ID);
        verify(topicRepository).delete(topic);
    }
}
