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

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.ConversationsStat;
import org.sakaiproject.conversations.api.ConversationsPermissionsException;
import org.sakaiproject.conversations.api.ConversationsReferenceReckoner;
import static org.sakaiproject.conversations.api.ConversationsReferenceReckoner.ConversationsReference;
import org.sakaiproject.conversations.api.Permissions;
import org.sakaiproject.conversations.api.PostSort;
import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.conversations.api.TopicType;
import org.sakaiproject.conversations.api.TopicVisibility;
import org.sakaiproject.conversations.api.beans.CommentTransferBean;
import org.sakaiproject.conversations.api.beans.PostTransferBean;
import org.sakaiproject.conversations.api.beans.TopicTransferBean;
import org.sakaiproject.conversations.api.model.ConversationsComment;
import org.sakaiproject.conversations.api.model.Settings;
import org.sakaiproject.conversations.api.model.Tag;
import org.sakaiproject.conversations.api.model.ConversationsTopic;
import org.sakaiproject.conversations.api.model.TopicStatus;
import org.sakaiproject.conversations.api.repository.ConversationsCommentRepository;
import org.sakaiproject.conversations.api.repository.ConversationsPostRepository;
import org.sakaiproject.conversations.api.repository.ConversationsTopicRepository;
import org.sakaiproject.conversations.api.repository.TopicStatusRepository;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;
import org.springframework.transaction.annotation.Transactional;

import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.annotation.Resource;

import static org.mockito.Mockito.*;

import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ConversationsTestConfiguration.class})
public class ConversationsServiceTests extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired private AuthzGroupService authzGroupService;
    @Autowired private MemoryService memoryService;
    @Autowired private ConversationsCommentRepository commentRepository;
    @Autowired private ConversationsService conversationsService;
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private ConversationsPostRepository postRepository;
    @Autowired private SiteService siteService;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private ConversationsTopicRepository topicRepository;
    @Autowired private TopicStatusRepository topicStatusRepository;

    private ResourceLoader resourceLoader;

    TopicTransferBean topicBean = null;
    PostTransferBean postBean = null;
    String instructor = "instructor";
    User instructorUser = null;
    String user1 = "user1";
    User user1User = null;
    String user1DisplayName = "Adrian Fish";
    String user1SortName = "Fish, Adrian";
    String user2 = "user2";
    User user2User = null;
    String user2DisplayName = "Earle Nietzel";
    String user2SortName = "Nietzel, Earle";
    String user3 = "user3";
    User user3User = null;
    String user3DisplayName = "Zaphod Beeblebrox";
    String user3SortName = "Beeblebrox, Zaphod";
    String siteId = "xyz";
    String siteRef = "/site/" + siteId;

    @Before
    public void setup() {

        reset(sessionManager);
        reset(securityService);
        reset(userDirectoryService);
        topicBean = new TopicTransferBean();
        topicBean.setTitle("Topic 1");
        topicBean.setMessage("Topic 1 messaage");
        topicBean.siteId = siteId;
        postBean = new PostTransferBean();
        postBean.message = "Post message";
        postBean.siteId = siteId;
        instructorUser = mock(User.class);
        user1User = mock(User.class);
        when(user1User.getId()).thenReturn(user1);
        when(user1User.getDisplayName()).thenReturn(user1DisplayName);
        when(user1User.getSortName()).thenReturn(user1SortName);
        user2User = mock(User.class);
        when(user2User.getId()).thenReturn(user2);
        when(user2User.getDisplayName()).thenReturn(user2DisplayName);
        when(user2User.getSortName()).thenReturn(user2SortName);
        user3User = mock(User.class);
        when(user3User.getId()).thenReturn(user3);
        when(user3User.getDisplayName()).thenReturn(user3DisplayName);
        when(user3User.getSortName()).thenReturn(user3SortName);
        when(serverConfigurationService.getInt(ConversationsService.PROP_THREADS_PAGE_SIZE, 10)).thenReturn(10);
        when(serverConfigurationService.getPortalUrl()).thenReturn("http://localhost/portal");
        ToolConfiguration toolConfig = mock(ToolConfiguration.class);
        when(toolConfig.getId()).thenReturn("abcdefg");
        Site site = mock(Site.class);
        when(site.getToolForCommonId(ConversationsService.TOOL_ID)).thenReturn(toolConfig);

        resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getString("anonymous")).thenReturn("Anonymous");
        when(siteService.siteReference(siteId)).thenReturn(siteRef);

        // this is too late for the init method, I think.
        Cache postsCache = mock(Cache.class);
        //when(memoryService.<String, Map<String, List<PostTransferBean>>>getCache(ConversationsService.POSTS_CACHE_NAME)).thenReturn(postsCache);
        ((ConversationsServiceImpl) AopTestUtils.getTargetObject(conversationsService)).setPostsCache(postsCache);
        ((ConversationsServiceImpl) AopTestUtils.getTargetObject(conversationsService)).setSortedStatsCache(postsCache);
        ((ConversationsServiceImpl) AopTestUtils.getTargetObject(conversationsService)).setResourceLoader(resourceLoader);

        try {
          when(siteService.getSite(siteId)).thenReturn(site);
        } catch (Exception e) {
        }

        AuthzGroup siteGroup = mock(AuthzGroup.class);
        Set<String> userIds = new HashSet<>();
        userIds.add(user1);
        userIds.add(user2);
        userIds.add(user3);
        when(siteGroup.getUsers()).thenReturn(userIds);

        List<User> users = new ArrayList<>();
        users.add(user1User);
        users.add(user2User);
        users.add(user3User);

        try {
            when(authzGroupService.getAuthzGroup(siteRef)).thenReturn(siteGroup);
            when(userDirectoryService.getUsers(new ArrayList(userIds))).thenReturn(users);
        } catch (Exception e) {
        }
    }

    @Test
    public void createTopicWithoutPermission() {

        // No current user. We should get an exeception.
        assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTopic(topicBean, true));

        when(sessionManager.getCurrentSessionUserId()).thenReturn("sakaiuser");

        // You can't create a topic without TOPIC_CREATE.
        assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTopic(topicBean, true));
    }

    @Test
    public void getBlankTopic() {

        assertThrows(ConversationsPermissionsException.class, () -> conversationsService.getBlankTopic(siteId));

        switchToUser1();

        when(securityService.unlock(Permissions.TOPIC_CREATE.label, siteRef)).thenReturn(false);
        assertThrows(ConversationsPermissionsException.class, () -> conversationsService.getBlankTopic(siteId));
        when(securityService.unlock(Permissions.TOPIC_CREATE.label, siteRef)).thenReturn(true);

        try {
            TopicTransferBean blankTopic = conversationsService.getBlankTopic(siteId);
            assertEquals(blankTopic.type, TopicType.QUESTION.name());
            assertEquals(blankTopic.visibility, TopicVisibility.SITE.name());
            assertFalse(blankTopic.canModerate);
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(true);
            blankTopic = conversationsService.getBlankTopic(siteId);
            assertTrue(blankTopic.canModerate);
            assertFalse(blankTopic.draft);
        } catch (ConversationsPermissionsException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void crudTopic() {

        try {
            switchToUser1();
            // CREATE
            topicBean = createTopic(true);

            // You need SITE_VISIT to read the topics for a site
            when(securityService.unlock(SiteService.SITE_VISIT, siteRef)).thenReturn(false);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.getTopicsForSite(topicBean.siteId));
            when(securityService.unlock(SiteService.SITE_VISIT, siteRef)).thenReturn(true);
            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());

            String updatedTitle = "Updated Topic 1";
            String updatedMessage = "Updated Topic 1 message";

            topicBean.setTitle(updatedTitle);
            topicBean.setMessage(updatedMessage);

            // You need TOPIC_UPDATE_OWN or TOPIC_UPDATE_ANY to update your own topic
            when(securityService.unlock(Permissions.TOPIC_UPDATE_OWN.label, siteRef)).thenReturn(false);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTopic(topicBean, true));
            when(securityService.unlock(Permissions.TOPIC_UPDATE_OWN.label, siteRef)).thenReturn(true);
            topicBean = conversationsService.saveTopic(topicBean, true);
            assertEquals(updatedTitle, topicBean.title);
            assertEquals(updatedMessage, topicBean.message);

            // You need TOPIC_DELETE_OWN or TOPIC_DELETE_ANY to delete your own topic
            when(securityService.unlock(Permissions.TOPIC_DELETE_OWN.label, siteRef)).thenReturn(false);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.deleteTopic(topicBean.id));
            when(securityService.unlock(Permissions.TOPIC_DELETE_OWN.label, siteRef)).thenReturn(true);
            conversationsService.deleteTopic(topicBean.id);
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertTrue(topics.size() == 0);
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when saving topic");
        }
    }

    @Test
    public void saveTopic() {

        try {
            TopicTransferBean topicBean = new TopicTransferBean();
            topicBean.setTitle("Topic");
            topicBean.setMessage("Topic messaage");
            topicBean.siteId = siteId;
            topicBean.type = TopicType.QUESTION.name();
            topicBean.visibility = TopicVisibility.SITE.name();
            topicBean.aboutReference = siteRef;
            topicBean.showDate = Instant.now().plus(5, ChronoUnit.HOURS);

            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTopic(topicBean, true));

            switchToUser1();

            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(true);

            when(securityService.unlock(Permissions.TOPIC_CREATE.label, siteRef)).thenReturn(true);

            TopicTransferBean savedBean = conversationsService.saveTopic(topicBean, true);
            when(securityService.unlock(SiteService.SITE_VISIT, siteRef)).thenReturn(true);
            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(siteId);
            assertEquals(1, topics.size());

            // If the show date is set, then the topic should be set to hidden
            assertNotNull(topics.get(0).showDate);
            assertTrue(topics.get(0).hidden);

            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(false);

            topics = conversationsService.getTopicsForSite(siteId);

            // If a topic has been hidden, only moderators can see them.
            assertEquals(0, topics.size());
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(true);
            topics = conversationsService.getTopicsForSite(siteId);
            assertEquals(1, topics.size());
        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail("Unexpected exception when saving topic");
        }
    }

    @Test
    public void mustPostBeforeViewing() {

        switchToInstructor();

        try {
            TopicTransferBean topicBean = new TopicTransferBean();
            topicBean.setTitle("Topic");
            topicBean.setMessage("Topic Messaage");
            topicBean.siteId = siteId;
            topicBean.type = TopicType.QUESTION.name();
            topicBean.visibility = TopicVisibility.SITE.name();
            topicBean.aboutReference = siteRef;
            topicBean.mustPostBeforeViewing = true;
            topicBean = conversationsService.saveTopic(topicBean, true);

            String topicId = topicBean.id;

            switchToUser1();

            PostTransferBean postBean = new PostTransferBean();
            postBean.siteId = siteId;
            postBean.topic = topicBean.id;
            postBean.setMessage("Here is my message");
            conversationsService.savePost(postBean, true);

            switchToUser2();

            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.getPostsByTopicId(siteId, topicId, 0, null, null));

            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(siteId);
            assertEquals(1, topics.size());
            assertFalse(topics.get(0).hasPosted);

            PostTransferBean postBean2 = new PostTransferBean();
            postBean2.siteId = siteId;
            postBean2.topic = topicBean.id;
            postBean2.setMessage("Here is my message");
            conversationsService.savePost(postBean2, true);

            topics = conversationsService.getTopicsForSite(siteId);
            assertTrue(topics.get(0).hasPosted);

            Collection<PostTransferBean> posts = conversationsService.getPostsByTopicId(siteId, topicId, 0, null, null);
            assertEquals(2, posts.size());
        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail("Unexpected exception");
        }
    }

    @Test
    public void topicFiltering() {

        try {
            topicBean.draft = true;
            switchToUser1();
            topicBean = createTopic(true);

            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(siteId);
            assertEquals(1, topics.size());

            // user2 should not be able to see your draft topic
            switchToUser2();
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(0, topics.size());

            switchToUser1();

            // topics marked for INSTRUCTORS should ony be viewable by users in the instructor role
            // (as set by a permission), or their creator
            topicBean.visibility = "INSTRUCTORS";
            topicBean.draft = false;
            topicBean = conversationsService.saveTopic(topicBean, true);

            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());

            switchToUser2();

            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(0, topics.size());

            switchToInstructor();

            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());

            topicBean.visibility = "SITE";
            topicBean.hidden = true;
            topicBean = conversationsService.saveTopic(topicBean, true);

            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());

            switchToUser2();
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(0, topics.size());

            switchToInstructor();
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());

            topicBean.lockDate = Instant.now().minus(20, ChronoUnit.HOURS);
            topicBean.hidden = false;
            topicBean = conversationsService.saveTopic(topicBean, true);
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());
            assertTrue(topics.get(0).lockedByDate);

            topicBean.lockDate = null;
            topicBean.hidden = false;
            topicBean.hideDate = Instant.now().minus(20, ChronoUnit.HOURS);
            topicBean = conversationsService.saveTopic(topicBean, true);
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());
            //assertTrue(topics.get(0).hiddenByDate);

            topicBean.hideDate = null;
            topicBean.lockDate = null;
            topicBean.showDate = null;
            topicBean.hidden = false;

            topicBean = conversationsService.saveTopic(topicBean, true);

            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(false);

            topicBean.showDate = Instant.now().minus(20, ChronoUnit.HOURS);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTopic(topicBean, true));
            topicBean.showDate = null;
            topicBean.hideDate = Instant.now().minus(20, ChronoUnit.HOURS);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTopic(topicBean, true));
            topicBean.hideDate = null;
            topicBean.lockDate = Instant.now().minus(20, ChronoUnit.HOURS);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTopic(topicBean, true));

            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(true);
            topicBean = conversationsService.saveTopic(topicBean, true);
            assertFalse(topicBean.lockDate == null);

            topicBean.lockDate = null;
            topicBean = conversationsService.saveTopic(topicBean, true);
            assertNull(topicBean.lockDate);

            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());
            assertNull(topics.get(0).lockDate);
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when testing topic filtering");
        }
    }

    @Test
    public void dueDate() {

        try {
            switchToUser1();
            TopicTransferBean topicBean = createTopic(true);
            topicBean.dueDate = Instant.now().minus(20, ChronoUnit.HOURS);
            topicBean = conversationsService.saveTopic(topicBean, true);

            // Now lets post to the topic. The post should be marked as late.
            switchToUser2();

            PostTransferBean postBean = new PostTransferBean();
            postBean.siteId = siteId;
            postBean.topic = topicBean.id;
            postBean.setMessage("Here is my message");

            postBean = conversationsService.savePost(postBean, true);

            assertTrue(postBean.late);

            // Lock date will be set if we use the availability checkbox
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(true);
            topicBean.lockDate = Instant.now().minus(5, ChronoUnit.HOURS);
            topicBean = conversationsService.saveTopic(topicBean, true);

            Collection<TopicTransferBean> topics = conversationsService.getTopicsForSite(siteId);
            assertEquals(1, topics.size());
            //assertTrue(topics.iterator().next().lockedByDate);
            //
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(false);

            // Topic is now locked
            PostTransferBean postBean2 = new PostTransferBean();
            postBean2.siteId = siteId;
            postBean2.topic = topicBean.id;
            postBean2.setMessage("Here is my really late post");
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.savePost(postBean2, true));

            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(true);

            Collection<PostTransferBean> posts = conversationsService.getPostsByTopicId(siteId, topicBean.id, 0, null, null);
            assertEquals(1, posts.size());
            assertTrue(posts.iterator().next().late);

            topicBean = createTopic(true);
            topicBean.dueDate = Instant.now().minus(5, ChronoUnit.HOURS);
            topicBean = conversationsService.saveTopic(topicBean, true);
            postBean = new PostTransferBean();
            postBean.siteId = siteId;
            postBean.topic = topicBean.id;
            postBean.setMessage("Here is my message");
            postBean = conversationsService.savePost(postBean, true);
            posts = conversationsService.getPostsByTopicId(siteId, topicBean.id, 0, null, null);
            assertEquals(1, posts.size());
            assertTrue(posts.iterator().next().late);
            topicBean.dueDate = Instant.now().plus(5, ChronoUnit.HOURS);
            topicBean = conversationsService.saveTopic(topicBean, true);
            posts = conversationsService.getPostsByTopicId(siteId, topicBean.id, 0, null, null);
            assertFalse(posts.iterator().next().late);
        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail("Unexpected exception when testing topic due date");
        }
    }

    @Test
    public void hideTopic() {

        try {
            switchToUser1();
            TopicTransferBean topicBean = createTopic(true);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.hideTopic(topicBean.id, true, true));
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(true);
            conversationsService.hideTopic(topicBean.id, true, true);
            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(topicBean.siteId);

            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(false);
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(0, topics.size());

            topicBean.showDate = Instant.now().plus(20, ChronoUnit.HOURS);
            when(securityService.unlock(Permissions.TOPIC_UPDATE_ANY.label, siteRef)).thenReturn(true);
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(true);
            conversationsService.saveTopic(topicBean, true);
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());
            TopicTransferBean savedBean = topics.get(0);
            assertTrue(savedBean.hidden);

            savedBean.showDate = null;
            savedBean = conversationsService.saveTopic(savedBean, true);

            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());
            savedBean = topics.get(0);
            assertNull(savedBean.showDate);
            assertFalse(savedBean.hidden);

            // If the hide date was in the past, even if hidden isn't set, the topic should still
            // be hidden from non moderators
            savedBean.hidden = false;
            savedBean.hideDate = Instant.now().minus(1, ChronoUnit.HOURS);
            savedBean = conversationsService.saveTopic(savedBean, true);
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(false);
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(0, topics.size());
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(true);
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());
            assertTrue(topics.get(0).hiddenByDate);

            conversationsService.hideTopic(savedBean.id, false, true);
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertFalse(topics.get(0).hidden);

            // If the hide date is after the show date, hide date should always win.
            savedBean.showDate = Instant.now().minus(2, ChronoUnit.HOURS);
            savedBean.hideDate = Instant.now().minus(1, ChronoUnit.HOURS);
            conversationsService.saveTopic(savedBean, true);
            savedBean = conversationsService.getTopicsForSite(topicBean.siteId).get(0);
            //assertTrue(savedBean.hidden);
        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail("Unexpected exception when testing topic hiding");
        }
    }

    @Test
    public void lockTopic() {

        try {
            switchToUser1();
            TopicTransferBean topicBean = createTopic(true);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.lockTopic(topicBean.id, true, true));
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(true);
            conversationsService.lockTopic(topicBean.id, true, true);
            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());
            assertTrue(topics.get(0).locked);

            // This topic has now been locked. We should not be able to update it
            TopicTransferBean updatedBean = topics.get(0);
            updatedBean.message = "xxxxxx";
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(false);
            when(securityService.unlock(Permissions.TOPIC_UPDATE_ANY.label, siteRef)).thenReturn(true);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTopic(updatedBean, true));
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(true);
            TopicTransferBean testBean = conversationsService.saveTopic(updatedBean, true);

            // Moderators can still edit locked topics
            assertTrue(testBean.canEdit);
            postBean.topic = testBean.id;
            when(securityService.unlock(Permissions.POST_CREATE.label, siteRef)).thenReturn(true);

            // We should not be able to create a post on a locked topic, unless we have MODERATE
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(false);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.savePost(postBean, true));
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(true);
            PostTransferBean updatedPostBean = conversationsService.savePost(postBean, true);
            // Locking a topic should lock all the posts in the topic, even when adding new posts to a locked topic
            assertTrue(updatedPostBean.locked);

            updatedBean.lockDate = Instant.now().minus(20, ChronoUnit.HOURS);
            conversationsService.saveTopic(updatedBean, true);
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());
            TopicTransferBean updatedBean2 = topics.get(0);
            assertTrue(updatedBean2.locked);

            conversationsService.lockTopic(updatedBean2.id, false, true);
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());
            updatedBean2 = topics.get(0);
            assertNull(updatedBean2.lockDate);
            assertFalse(updatedBean2.locked);

            conversationsService.lockTopic(updatedBean2.id, true, true);
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            updatedBean2 = topics.get(0);
            assertTrue(updatedBean2.locked);

            updatedBean2.lockDate = Instant.now().minus(2, ChronoUnit.HOURS);
            updatedBean2 = conversationsService.saveTopic(updatedBean2, true);
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());
            assertTrue(topics.get(0).lockedByDate);

        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when testing topic locking");
        }
    }

    @Test
    public void lockSite() {

        switchToInstructor();

        TopicTransferBean topicBean = createTopic(true);

        try {
            postBean.topic = topicBean.id;
            postBean = conversationsService.savePost(postBean, true);
            Settings settings = new Settings();
            settings.setSiteId(topicBean.siteId);
            settings.setSiteLocked(true);
            conversationsService.saveSettings(settings);

            TopicTransferBean topicBean2 = new TopicTransferBean();
            topicBean2.setTitle("Topic 2");
            topicBean2.setMessage("Topic 2 messaage");
            topicBean2.siteId = siteId;
            topicBean2.type = TopicType.QUESTION.name();
            topicBean2.visibility = TopicVisibility.SITE.name();
            topicBean2.aboutReference = siteRef;

            // this site is now locked. You should only be able to create a topic if you have MODERATE
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(false);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTopic(topicBean2, true));

            postBean.setMessage("eggs");
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.savePost(postBean, true));
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(true);
            //conversationsService.savePost(postBean, true);
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when testing topic filtering");
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    @Test
    public void anonymousTopic() {

        try {
            switchToUser1();
            topicBean = createTopic(true);
            assertTrue(topicBean != null && topicBean.id != null && topicBean.id.length() == 36);
            topicBean.anonymous = true;
            topicBean = conversationsService.saveTopic(topicBean, true);
            assertNotEquals(topicBean.creatorDisplayName, user1User.getDisplayName());
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when testing topic filtering");
        }
    }

    @Test
    public void crudPost() {

        try {
            switchToUser1();
            topicBean = createTopic(true);

            postBean.topic = topicBean.id;

            when(securityService.unlock(Permissions.POST_CREATE.label, siteRef)).thenReturn(false);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.savePost(postBean, true));
            when(securityService.unlock(Permissions.POST_CREATE.label, siteRef)).thenReturn(true);

            postBean = conversationsService.savePost(postBean, true);
            assertEquals(36, postBean.id.length());
            assertEquals(user1, postBean.creator);

            // We should not be able to get a topic's posts if we can't visit the site
            when(securityService.unlock(SiteService.SITE_VISIT, siteRef)).thenReturn(false);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null));
            when(securityService.unlock(SiteService.SITE_VISIT, siteRef)).thenReturn(true);

            Collection<PostTransferBean> posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(1, posts.size());

            String updatedMessage = "Updated Message";
            postBean.message = updatedMessage;
            postBean.siteId = topicBean.siteId;

            when(securityService.unlock(Permissions.POST_UPDATE_OWN.label, siteRef)).thenReturn(false);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.savePost(postBean, true));
            when(securityService.unlock(Permissions.POST_UPDATE_OWN.label, siteRef)).thenReturn(true);

            postBean = conversationsService.savePost(postBean, true);
            assertEquals(updatedMessage, postBean.message);

            when(securityService.unlock(Permissions.POST_DELETE_OWN.label, siteRef)).thenReturn(false);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.deletePost(postBean.siteId, postBean.topic, postBean.id, false));
            when(securityService.unlock(Permissions.POST_DELETE_OWN.label, siteRef)).thenReturn(true);

            conversationsService.deletePost(postBean.siteId, postBean.topic, postBean.id, false);

            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(0, posts.size());
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when saving post");
        }
    }

    @Test
    public void getPostsByTopicId() {

        when(serverConfigurationService.getInt(ConversationsService.PROP_THREADS_PAGE_SIZE, 10)).thenReturn(2);

        try {
            switchToUser1();
            topicBean = createTopic(true);

            String t1Message = "Thread 1";
            String t2Message = "Thread 2";
            String t3Message = "Thread 3";
            String t1Creator = "Earle Nietzel";
            String t2Creator = "Adrian Fish";
            String t3Creator = "Zaphod Beeblebrox";

            // A "thread" is just a top level post to a topic
            PostTransferBean thread1 = new PostTransferBean();
            thread1.setMessage(t1Message);
            thread1.topic = topicBean.id;
            thread1.siteId = siteId;
            switchToUser1();
            thread1 = conversationsService.savePost(thread1, true);

            // You should only be able to get the posts if you have SITE_VISIT
            when(securityService.unlock(SiteService.SITE_VISIT, siteRef)).thenReturn(false);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null));
            when(securityService.unlock(SiteService.SITE_VISIT, siteRef)).thenReturn(true);

            Collection<PostTransferBean> posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(1, posts.size());

            PostTransferBean thread2 = new PostTransferBean();
            thread2.setMessage(t2Message);
            thread2.topic = topicBean.id;
            thread2.siteId = siteId;
            switchToUser2();
            postBean = conversationsService.savePost(thread2, true);
            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(2, posts.size());

            PostTransferBean thread3 = new PostTransferBean();
            thread3.setMessage(t3Message);
            thread3.topic = topicBean.id;
            thread3.siteId = siteId;
            switchToUser3();
            postBean = conversationsService.savePost(thread3, true);

            // First page
            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(2, posts.size());

            // Second page
            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 1, null, null);
            assertEquals(1, posts.size());

            PostTransferBean post1 = new PostTransferBean();
            post1.setMessage("Post 1");
            post1.topic = topicBean.id;
            post1.siteId = siteId;
            post1.parentPost = thread1.id;
            post1.parentThread = thread1.id;
            postBean = conversationsService.savePost(post1, true);

            // First page, ordered newest first
            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, PostSort.NEWEST, null);
            assertEquals(2, posts.size());

            Iterator it = posts.iterator();
            PostTransferBean t3 = (PostTransferBean) it.next();
            assertEquals(t3Message, t3.message);
            PostTransferBean t2 = (PostTransferBean) it.next();
            assertEquals(t2Message, t2.message);

            // Get the second page
            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 1, PostSort.NEWEST, null);
            assertEquals(1, posts.size());

            it = posts.iterator();
            PostTransferBean t1 = (PostTransferBean) it.next();
            assertEquals(t1Message, t1.message);
            assertEquals(1, t1.posts.size());
            assertEquals("Post 1", ((PostTransferBean) t1.posts.get(0)).message);

            when(serverConfigurationService.getInt(ConversationsService.PROP_THREADS_PAGE_SIZE, 10)).thenReturn(2);
            // First page, ordered oldest first
            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, PostSort.OLDEST, null);
            assertEquals(2, posts.size());
            it = posts.iterator();
            t1 = (PostTransferBean) it.next();
            assertEquals(t1Message, t1.message);
            t2 = (PostTransferBean) it.next();
            assertEquals(t2Message, t2.message);

            // Second page, ordered oldest first
            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 1, PostSort.OLDEST, null);
            assertEquals(1, posts.size());
            it = posts.iterator();
            t3 = (PostTransferBean) it.next();
            assertEquals(t3Message, t3.message);

            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, PostSort.ASC_CREATOR, null);
            assertEquals(2, posts.size());
            it = posts.iterator();
            PostTransferBean test = (PostTransferBean) it.next();
            assertEquals(user1DisplayName, test.creatorDisplayName);
            assertEquals(t1Message, test.message);
            test = (PostTransferBean) it.next();
            assertEquals(user2DisplayName, test.creatorDisplayName);
            assertEquals(t2Message, test.message);

            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 1, PostSort.ASC_CREATOR, null);
            assertEquals(1, posts.size());
            it = posts.iterator();
            test = (PostTransferBean) it.next();
            assertEquals(user3DisplayName, test.creatorDisplayName);
            assertEquals(t3Message, test.message);

            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, PostSort.DESC_CREATOR, null);
            assertEquals(2, posts.size());
            it = posts.iterator();
            test = (PostTransferBean) it.next();
            assertEquals(user3DisplayName, test.creatorDisplayName);
            assertEquals(t3Message, test.message);
            test = (PostTransferBean) it.next();
            assertEquals(user2DisplayName, test.creatorDisplayName);
            assertEquals(t2Message, test.message);

            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 1, PostSort.DESC_CREATOR, null);
            assertEquals(1, posts.size());
            it = posts.iterator();
            test = (PostTransferBean) it.next();
            assertEquals(user1DisplayName, test.creatorDisplayName);
            assertEquals(t1Message, test.message);
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when saving post");
        }
    }

    @Test
    public void upDownVotePost() {

        try {
            switchToUser1();
            topicBean = createTopic(true);
            postBean.topic = topicBean.id;
            postBean = conversationsService.savePost(postBean, true);
            int currentUpvotes = postBean.upvotes;
            assertTrue(postBean.id != null);
            assertEquals(0, postBean.upvotes);

            // We should not be able to upvote your own post
            assertThrows(IllegalArgumentException.class, () -> conversationsService.upvotePost(postBean.siteId, postBean.topic, postBean.id));

            switchToUser2();

            // We should not be able to upvote a post without POST_UPVOTE
            when(securityService.unlock(Permissions.POST_UPVOTE.label, siteRef)).thenReturn(false);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.upvotePost(postBean.siteId, postBean.topic, postBean.id));
            when(securityService.unlock(Permissions.POST_UPVOTE.label, siteRef)).thenReturn(true);

            postBean = conversationsService.upvotePost(postBean.siteId, postBean.topic, postBean.id);
            assertEquals(1, postBean.upvotes);

            // Now lets try and upvote it again. This should fail with the upvotes staying the same
            postBean = conversationsService.upvotePost(postBean.siteId, postBean.topic, postBean.id);
            assertEquals(1, postBean.upvotes);

            postBean = conversationsService.unUpvotePost(postBean.siteId, postBean.id);
            assertEquals(0, postBean.upvotes);

            // We should not be allowed to unupvote a post twice
            assertThrows(IllegalArgumentException.class, () -> conversationsService.unUpvotePost(postBean.siteId, postBean.id));
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when saving post");
        }
    }

    @Test
    @Transactional
    public void topicPostCount() {

        try {
            switchToUser1();
            TopicTransferBean topicBean = new TopicTransferBean();
            topicBean.siteId = this.siteId;
            topicBean.title = "Topic Title";
            topicBean = conversationsService.saveTopic(topicBean, true);

            switchToUser2();
            when(securityService.unlock(Permissions.POST_CREATE.label, siteRef)).thenReturn(true);

            PostTransferBean privatePost = new PostTransferBean();
            privatePost.siteId = this.siteId;
            privatePost.topic = topicBean.id;
            privatePost.message = "Private Post";
            privatePost.privatePost = true;

            privatePost = conversationsService.savePost(privatePost, true);

            assertTrue(privatePost.privatePost);

            switchToUser1();
            when(securityService.unlock(SiteService.SITE_VISIT, siteRef)).thenReturn(true);
            Collection<TopicTransferBean> topics = conversationsService.getTopicsForSite(this.siteId);
            assertEquals(1, topics.iterator().next().numberOfPosts);

            switchToUser3();
            when(securityService.unlock(SiteService.SITE_VISIT, siteRef)).thenReturn(true);
            topics = conversationsService.getTopicsForSite(this.siteId);
            assertEquals(0, topics.iterator().next().numberOfPosts);

            switchToUser1();

            when(securityService.unlock(Permissions.TOPIC_DELETE_OWN.label, siteRef)).thenReturn(true);
            conversationsService.deleteTopic(topicBean.id);

            TopicTransferBean discussionTopicBean = new TopicTransferBean();
            discussionTopicBean.siteId = this.siteId;
            discussionTopicBean.title = "Discussion Topic Title";
            discussionTopicBean.type = TopicType.DISCUSSION.name();
            discussionTopicBean = conversationsService.saveTopic(discussionTopicBean, true);

            PostTransferBean level1Post = new PostTransferBean();
            level1Post.siteId = this.siteId;
            level1Post.topic = discussionTopicBean.id;
            level1Post.message = "Level1";
            level1Post = conversationsService.savePost(level1Post, true);

            switchToUser2();
            PostTransferBean level2Post = new PostTransferBean();
            level2Post.siteId = this.siteId;
            level2Post.topic = discussionTopicBean.id;
            level2Post.message = "Level2";
            level2Post.parentPost = level1Post.id;
            level2Post.privatePost = true;
            level2Post = conversationsService.savePost(level2Post, true);
            assertEquals(level1Post.id, level2Post.parentPost);

            topics = conversationsService.getTopicsForSite(this.siteId);

            // user2 is the author of the reply, He should see the reply and the post being replied
            // to
            assertEquals(2, topics.iterator().next().numberOfPosts);

            switchToUser3();
            topics = conversationsService.getTopicsForSite(this.siteId);

            // user3 is not the author of the reply, or the post being replied to. He should not
            // see the reply
            assertEquals(1, topics.iterator().next().numberOfPosts);

            switchToUser1();
            topics = conversationsService.getTopicsForSite(this.siteId);

            // user1 is the author of the post being replied to, so he should see the private reply
            assertEquals(2, topics.iterator().next().numberOfPosts);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception when saving topic");
        }
    }

    @Test
    public void crudComment() {

        try {
            switchToUser1();
            TopicTransferBean topicBean = new TopicTransferBean();
            topicBean.siteId = this.siteId;
            topicBean.title = "Topic Title";
            topicBean = conversationsService.saveTopic(topicBean, true);
            PostTransferBean postBean = new PostTransferBean();
            postBean.setMessage("Post");
            postBean.topic = topicBean.id;
            postBean.siteId = siteId;
            postBean = conversationsService.savePost(postBean, true);

            CommentTransferBean commentBean = new CommentTransferBean();
            commentBean.post = postBean.id;
            commentBean.topicId = topicBean.id;
            commentBean.siteId = siteId;
            commentBean.message = "Comment";
            conversationsService.saveComment(commentBean);

            Collection<PostTransferBean> posts = conversationsService.getPostsByTopicId(siteId, topicBean.id, 0, null, null);
            assertEquals(1, posts.size());
            postBean = posts.iterator().next();

            assertEquals(1, postBean.comments.size());
            commentBean = postBean.comments.get(0);
            assertEquals("Comment", commentBean.message);

            commentBean.message = "New Comment";
            conversationsService.saveComment(commentBean);
            posts = conversationsService.getPostsByTopicId(siteId, topicBean.id, 0, null, null);
            postBean = posts.iterator().next();
            commentBean = postBean.comments.get(0);
            assertEquals("New Comment", commentBean.message);

            conversationsService.deleteComment(siteId, commentBean.id);
            posts = conversationsService.getPostsByTopicId(siteId, topicBean.id, 0, null, null);
            postBean = posts.iterator().next();
            assertEquals(0, postBean.comments.size());

            conversationsService.saveComment(commentBean);
            posts = conversationsService.getPostsByTopicId(siteId, topicBean.id, 0, null, null);
            postBean = posts.iterator().next();
            assertEquals(1, postBean.comments.size());

            conversationsService.deletePost(siteId, topicBean.id, postBean.id, false);
            List<ConversationsComment> comments = commentRepository.findByPostId(postBean.id);
            assertTrue(comments.isEmpty());

            postBean = conversationsService.savePost(postBean, true);
            commentBean.post = postBean.id;
            conversationsService.saveComment(commentBean);
            posts = conversationsService.getPostsByTopicId(siteId, topicBean.id, 0, null, null);
            postBean = posts.iterator().next();
            assertEquals(1, postBean.comments.size());
            conversationsService.deleteTopic(topicBean.id);
            comments = commentRepository.findByPostId(postBean.id);
            assertTrue(comments.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception when crudding comment");
        }
    }

    @Test
    public void crudTags() {

        Tag tag = new Tag();
        tag.setSiteId(siteId);
        tag.setLabel("chicken");

        // This should throw as there's no current user
        assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTag(tag));

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);

        when(securityService.unlock(Permissions.TAG_CREATE.label, siteRef)).thenReturn(true);
        try {
            Tag savedTag = conversationsService.saveTag(tag);
            assertFalse(savedTag.getId() == null);

            List<Tag> siteTags = conversationsService.getTagsForSite(tag.getSiteId());

            // Should only be able to pull the tags if we can tag topics
            assertEquals(siteTags.size(), 0);

            when(securityService.unlock(Permissions.TOPIC_TAG.label, siteRef)).thenReturn(true);
            siteTags = conversationsService.getTagsForSite(tag.getSiteId());
            assertEquals(1, siteTags.size());

            savedTag.setLabel("turkey");
            savedTag = conversationsService.saveTag(savedTag);
            assertEquals("turkey", savedTag.getLabel());

            conversationsService.deleteTag(savedTag.getId());
            siteTags = conversationsService.getTagsForSite(tag.getSiteId());
            assertEquals(0, siteTags.size());

            List<Tag> tags = new ArrayList<>();
            tags.add(tag);
            conversationsService.createTags(tags);

            siteTags = conversationsService.getTagsForSite(tag.getSiteId());
            assertEquals(1, siteTags.size());

        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail("Unexpected exception when creating tag");
        }
    }

    /**
     * Tagging a topic then deleting the tag should result in the tag disappearing from the main
     * list of tags, and being removed from any topics
     */
    @Test
    public void tagTopicThenDeleteTag() {

        switchToUser1();
        TopicTransferBean topicBean = createTopic(true);
        Tag tag = new Tag();
        tag.setSiteId(siteId);
        tag.setLabel("chicken");

        when(securityService.unlock(Permissions.TAG_CREATE.label, siteRef)).thenReturn(true);

        List<Tag> tags = new ArrayList<>();
        tags.add(tag);
        try {
            tags = conversationsService.createTags(tags);
            Long newTagId = tags.get(0).getId();
            topicBean.tags = tags;
            topicBean = conversationsService.saveTopic(topicBean, true);
            assertEquals(newTagId, topicBean.tags.iterator().next().getId());

            conversationsService.deleteTag(newTagId);

            Optional<ConversationsTopic> optTopic = topicRepository.findById(topicBean.id);
            assertTrue(optTopic.isPresent());

            assertEquals(0, optTopic.get().getTagIds().size());
        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail("Unexpected exception when creating tags");
        }
    }

    @Test
    public void createSettings() {

        Settings settings = new Settings();
        settings.setSiteId(siteId);
        try {
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveSettings(settings));
            when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveSettings(settings));
            when(securityService.unlock(SiteService.SECURE_UPDATE_SITE, siteRef)).thenReturn(true);
            Settings savedSettings = conversationsService.saveSettings(settings);
            assertTrue(savedSettings.getId() != null);
            assertTrue(savedSettings.getAllowBookmarking());
            assertTrue(savedSettings.getAllowPinning());
            assertTrue(savedSettings.getAllowReactions());
            assertFalse(savedSettings.getAllowUpvoting());
            assertTrue(savedSettings.getAllowAnonPosting());

            savedSettings.setAllowPinning(Boolean.FALSE);
            savedSettings = conversationsService.saveSettings(savedSettings);
            assertFalse(savedSettings.getAllowPinning());

            Settings siteSettings = conversationsService.getSettingsForSite(siteId);
            assertFalse(siteSettings.getAllowPinning());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception when creating settings");
        }
    }

    @Test
    public void createAnonymousAllowedTopic() {

        switchToUser1();
        TopicTransferBean topicBean = createTopic(true);
        topicBean.allowAnonymousPosts = true;
        try {
            topicBean = conversationsService.saveTopic(topicBean, false);

            switchToUser2();
            PostTransferBean postBean = new PostTransferBean();
            postBean.topic = topicBean.id;
            postBean.siteId = siteId;
            postBean.anonymous = true;
            postBean.message = "Hello world";
            postBean = conversationsService.savePost(postBean, true);

            switchToUser1();

            Collection<PostTransferBean> posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(1, posts.size());
            assertEquals("Anonymous", posts.iterator().next().creatorDisplayName);

            switchToInstructor();

            PostTransferBean postBean2 = new PostTransferBean();
            postBean2.topic = topicBean.id;
            postBean2.siteId = siteId;
            postBean2.anonymous = true;
            postBean2.message = "Hello world 2";
            postBean2 = conversationsService.savePost(postBean2, true);
            switchToUser1();
            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(2, posts.size());
            // Instructors who post anonymously should *not* have their posts marked as Instructor
            assertTrue(posts.stream().filter(p -> p.isInstructor).collect(Collectors.toList()).isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception when creating topic");
        }
    }

    @Test
    public void pinTopic() {

        switchToUser1();
        TopicTransferBean topicBean = createTopic(true);
        try {
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.pinTopic(topicBean.id, true));
            when(securityService.unlock(Permissions.TOPIC_PIN.label, siteRef)).thenReturn(true);
            conversationsService.pinTopic(topicBean.id, true);
            Optional<ConversationsTopic> optTopic = topicRepository.findById(topicBean.id);
            assertTrue(optTopic.isPresent());
            assertTrue(optTopic.get().getPinned());
            conversationsService.pinTopic(topicBean.id, false);
            optTopic = topicRepository.findById(topicBean.id);
            assertTrue(optTopic.isPresent());
            assertFalse(optTopic.get().getPinned());
        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail("Unexpected exception when creating settings");
        }
    }

    @Test
    public void bookmarkTopic() {

        switchToUser1();
        TopicTransferBean topicBean = createTopic(true);
        try {

            switchToUser2();

            conversationsService.bookmarkTopic(topicBean.id, true);
            Optional<TopicStatus> optTopicStatus = topicStatusRepository.findByTopicIdAndUserId(topicBean.id, user2);
            assertTrue(optTopicStatus.isPresent());
            assertTrue(optTopicStatus.get().getBookmarked());
            conversationsService.bookmarkTopic(topicBean.id, false);
            optTopicStatus = topicStatusRepository.findByTopicIdAndUserId(topicBean.id, user2);
            assertTrue(optTopicStatus.isPresent());
            assertFalse(optTopicStatus.get().getBookmarked());
        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail("Unexpected exception when bookmarking topic");
        }
    }

    @Test
    public void reactToTopic() {

        switchToUser1();

        TopicTransferBean topicBean = createTopic(true);

        when(securityService.unlock(SiteService.SITE_VISIT, siteRef)).thenReturn(true);

        try {
            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());
            TopicTransferBean updatedBean = topics.get(0);

            Map<Reaction, Boolean> reactions = new HashMap<>();
            reactions.put(Reaction.GOOD_QUESTION, Boolean.TRUE);

            // This should fail, as you can't react to your own topic
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTopicReactions(topicBean.id, reactions));

            switchToUser2();

            conversationsService.saveTopicReactions(topicBean.id, reactions);

            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());
            updatedBean = topics.get(0);
            assertTrue(updatedBean.myReactions.get(Reaction.GOOD_QUESTION));

            assertTrue(1 == updatedBean.reactionTotals.get(Reaction.GOOD_QUESTION));

            reactions.put(Reaction.GOOD_QUESTION, Boolean.FALSE);
            conversationsService.saveTopicReactions(updatedBean.id, reactions);
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());
            updatedBean = topics.get(0);
            assertTrue(0 == updatedBean.reactionTotals.get(Reaction.GOOD_QUESTION));
        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail("Unexpected exception when reacting to topic");
        }
    }

    @Test
    public void savePost() {

        switchToUser1();
        TopicTransferBean topicBean = createTopic(true);
        topicBean.type = TopicType.DISCUSSION.name();

        try {
            String thread1Message = "Thread 1";

            PostTransferBean thread1 = new PostTransferBean();
            thread1.siteId = siteId;
            thread1.topic = "none";
            thread1.message = thread1Message;

            when(securityService.unlock(Permissions.POST_CREATE.label, siteRef)).thenReturn(false);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.savePost(thread1, true));
            when(securityService.unlock(Permissions.POST_CREATE.label, siteRef)).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> conversationsService.savePost(thread1, true));

            thread1.topic = topicBean.id;

            PostTransferBean savedThread1 = conversationsService.savePost(thread1, true);

            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(siteId);
            assertEquals(1, topics.get(0).numberOfPosts);

            String replyMessage = "Yo yoghurt";

            PostTransferBean reply1 = new PostTransferBean();
            reply1.parentPost = "eggs";
            reply1.parentThread = savedThread1.id;
            reply1.message = replyMessage;
            reply1.siteId = topicBean.siteId;
            reply1.topic = topicBean.id;

            // This should fail as the parent post doesn't exist
            assertThrows(IllegalArgumentException.class, () -> conversationsService.savePost(reply1, true));

            reply1.parentPost = savedThread1.id;

            PostTransferBean savedReply1 = conversationsService.savePost(reply1, true);
            assertEquals(replyMessage, savedReply1.message);
            assertEquals(savedThread1.id, savedReply1.parentPost);
            assertEquals(savedThread1.id, savedReply1.parentThread);
            Collection<PostTransferBean> threads = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(1, threads.size());
            PostTransferBean parent = threads.iterator().next();
            assertEquals(1, parent.posts.size());
            assertEquals(replyMessage, ((PostTransferBean) parent.posts.get(0)).message);
            topics = conversationsService.getTopicsForSite(siteId);
            assertEquals(2, topics.get(0).numberOfPosts);

            String reply2Message = "Yo yoghurt ahoy";

            PostTransferBean reply2 = new PostTransferBean();
            reply2.parentPost = savedReply1.id;
            reply2.parentThread = savedReply1.parentThread;
            reply2.message = reply2Message;
            reply2.siteId = topicBean.siteId;
            reply2.topic = topicBean.id;
            PostTransferBean savedReply2 = conversationsService.savePost(reply2, true);

            threads = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(1, threads.size());
            assertEquals(2, ((List<PostTransferBean>) threads).get(0).numberOfThreadReplies);
            assertEquals(2, ((List<PostTransferBean>) threads).get(0).howActive);
            assertEquals(1, threads.iterator().next().posts.size());
            topics = conversationsService.getTopicsForSite(siteId);
            assertEquals(3, topics.get(0).numberOfPosts);

            PostTransferBean child = (PostTransferBean) threads.iterator().next().posts.get(0);
            assertEquals(1, child.posts.size());
            PostTransferBean grandChild = ((List<PostTransferBean>) child.posts).get(0);
            assertEquals(reply2Message, grandChild.message);
        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail("Unexpected exception when saving post");
        }
    }
    
    @Test
    public void deletePost() {

        switchToUser1();

        TopicTransferBean topicBean = createTopic(true);


        try {
            postBean.topic = topicBean.id;
            postBean = conversationsService.savePost(postBean, true);

            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(siteId);
            assertEquals(1, topics.get(0).numberOfPosts);

            when(securityService.unlock(Permissions.POST_DELETE_OWN.label, siteRef)).thenReturn(false);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.deletePost(topicBean.siteId, topicBean.id, postBean.id, false));
            when(securityService.unlock(Permissions.POST_DELETE_OWN.label, siteRef)).thenReturn(true);
            conversationsService.deletePost(topicBean.siteId, topicBean.id, postBean.id, false);
            Collection<PostTransferBean> posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(0, posts.size());

            topics = conversationsService.getTopicsForSite(siteId);
            assertEquals(0, topics.get(0).numberOfPosts);

            //when(securityService.unlock(Permissions.POST_UPDATEDELETE_ANY.label, siteRef)).thenReturn(true);
            postBean.id = "";
            postBean = conversationsService.savePost(postBean, true);
            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(1, posts.size());

            topics = conversationsService.getTopicsForSite(siteId);
            assertEquals(1, topics.get(0).numberOfPosts);

            String replyMessage = "Yo yoghurt";

            // If a post has children, we just soft delete it
            PostTransferBean reply1 = new PostTransferBean();
            reply1.parentPost = postBean.id;
            reply1.parentThread = postBean.id;
            reply1.message = replyMessage;
            reply1.siteId = topicBean.siteId;
            reply1.topic = topicBean.id;

            reply1 = conversationsService.savePost(reply1, true);
            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(1, posts.iterator().next().getNumberOfThreadReplies());

            topics = conversationsService.getTopicsForSite(siteId);
            assertEquals(2, topics.get(0).numberOfPosts);

            assertThrows(IllegalArgumentException.class, () -> conversationsService.deletePost(topicBean.siteId, topicBean.id, postBean.id, false));
            conversationsService.deletePost(topicBean.siteId, topicBean.id, reply1.id, false);
            assertFalse(postRepository.findById(reply1.id).isPresent());

            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(1, posts.size());
            assertEquals(0, posts.iterator().next().getNumberOfThreadReplies());
        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail("Unexpected exception when deleting post");
        }
    }

    @Test
    public void hidePost() {

        switchToUser1();

        TopicTransferBean topicBean = createTopic(true);

        when(securityService.unlock(SiteService.SITE_VISIT, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.POST_CREATE.label, siteRef)).thenReturn(true);

        try {
            postBean.topic = topicBean.id;
            PostTransferBean updatedPostBean = conversationsService.savePost(postBean, true);
            Collection<PostTransferBean> posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(1, posts.size());
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.hidePost(topicBean.siteId, topicBean.id, updatedPostBean.id, true));
            when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(true);
            postBean = conversationsService.hidePost(topicBean.siteId, topicBean.id, updatedPostBean.id, true);
            assertTrue(postBean.hidden);
            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(1, posts.size());
            assertEquals(updatedPostBean.id, posts.iterator().next().id);
            assertTrue(posts.iterator().next().hidden);
        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail("Unexpected exception when hiding post");
        }
    }

    @Test
    public void savePostReactions() {

        switchToUser1();

        TopicTransferBean topicBean = createTopic(true);

        try {
            // A "thread" is just a top level post to a topic
            PostTransferBean thread1 = new PostTransferBean();
            thread1.setMessage("T1");
            thread1.topic = topicBean.id;
            thread1.siteId = siteId;
            //switchToUser1();
            postBean.topic = topicBean.id;

            when(securityService.unlock(Permissions.POST_CREATE.label, siteRef)).thenReturn(true);

            PostTransferBean savedThread1 = conversationsService.savePost(thread1, true);

            Map<Reaction, Boolean> reactions = new HashMap<>();
            reactions.put(Reaction.GOOD_ANSWER, Boolean.TRUE);

            // This should fail, as you can't react to your own post
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.savePostReactions(topicBean.id, savedThread1.id, reactions));

            switchToUser2();

            conversationsService.savePostReactions(topicBean.id, savedThread1.id, reactions);

            Collection<PostTransferBean> posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);

            assertTrue(posts.iterator().next().myReactions.get(Reaction.GOOD_ANSWER));
            assertTrue(1 == posts.iterator().next().reactionTotals.get(Reaction.GOOD_ANSWER));

            PostTransferBean reply1 = new PostTransferBean();
            reply1.setMessage("R1");
            reply1.topic = topicBean.id;
            reply1.siteId = siteId;
            reply1.parentPost = savedThread1.id;
            reply1.parentThread = savedThread1.id;

            switchToUser1();

            PostTransferBean savedReply1 = conversationsService.savePost(reply1, true);

            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);

            // savedReply1 should be marked as viewed now, for user1. Test that.
            assertTrue(((PostTransferBean) posts.iterator().next().posts.iterator().next()).viewed);

            // Now, switch to user2 and react to that post. This should mark
            // the post as not viewed for anybody else
            switchToUser2();
            conversationsService.savePostReactions(topicBean.id, savedReply1.id, reactions);
            switchToUser1();
            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertFalse(((PostTransferBean)posts.iterator().next().posts.iterator().next()).viewed);

            switchToUser2();

            reactions.put(Reaction.GOOD_ANSWER, Boolean.FALSE);
            conversationsService.savePostReactions(topicBean.id, savedThread1.id, reactions);
            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertTrue(0 == posts.iterator().next().reactionTotals.get(Reaction.GOOD_ANSWER));
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when reacting to post");
        }
    }

    @Test
    public void topicResolved() {

        switchToUser1();

        TopicTransferBean topicBean = createTopic(true);

        try {
            PostTransferBean postBean = new PostTransferBean();
            postBean.message = "Great topic";
            postBean.topic = topicBean.id;
            postBean.siteId = siteId;

            PostTransferBean savedPost = conversationsService.savePost(postBean, true);

            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(siteId);
            assertFalse(topics.get(0).resolved);

            switchToInstructor();
            savedPost.message = "Great!";
            savedPost = conversationsService.savePost(savedPost, true);
            topics = conversationsService.getTopicsForSite(siteId);
            assertFalse(topics.get(0).resolved);

            PostTransferBean instructorPost = new PostTransferBean();
            instructorPost.message = "Here's an answer from above";
            instructorPost.topic = topicBean.id;
            instructorPost.siteId = siteId;

            conversationsService.savePost(instructorPost, true);

            topics = conversationsService.getTopicsForSite(siteId);
            assertTrue(topics.get(0).resolved);
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when testing topic resolved");
        }
    }

    @Test
    public void testReferenceReckoner() {

        String topicId = "2389sdoijcieo";
        String postId = "533fhslslk";
        String commentId = "50399dsfdg";

        String topicRef = Entity.SEPARATOR + "conversations" + Entity.SEPARATOR + siteId + Entity.SEPARATOR + "t" + Entity.SEPARATOR + topicId;
        ConversationsReference ref = ConversationsReferenceReckoner.reckoner().reference(topicRef).reckon();
        assertEquals(siteId, ref.getSiteId());
        assertEquals("t", ref.getType());
        assertEquals(topicId, ref.getId());

        String postRef = Entity.SEPARATOR + "conversations" + Entity.SEPARATOR + siteId + Entity.SEPARATOR + "p" + Entity.SEPARATOR + postId;
        ref = ConversationsReferenceReckoner.reckoner().reference(postRef).reckon();
        assertEquals(siteId, ref.getSiteId());
        assertEquals("p", ref.getType());
        assertEquals(postId, ref.getId());

        String commentRef = Entity.SEPARATOR + "conversations" + Entity.SEPARATOR + siteId + Entity.SEPARATOR + "c" + Entity.SEPARATOR + commentId;
        ref = ConversationsReferenceReckoner.reckoner().reference(commentRef).reckon();
        assertEquals(siteId, ref.getSiteId());
        assertEquals("c", ref.getType());
        assertEquals(commentId, ref.getId());

        ref = ConversationsReferenceReckoner.reckoner().siteId(siteId).type("t").id(topicId).reckon();
        String reckonedRef = ref.getReference();
        assertEquals(topicRef, reckonedRef);

        ref = ConversationsReferenceReckoner.reckoner().siteId(siteId).type("p").id(postId).reckon();
        reckonedRef = ref.getReference();
        assertEquals(postRef, reckonedRef);

        ref = ConversationsReferenceReckoner.reckoner().siteId(siteId).type("c").id(commentId).reckon();
        reckonedRef = ref.getReference();
        assertEquals(commentRef, reckonedRef);
    }

    @Test
    public void markPostsViewed() {

        switchToUser1();

        TopicTransferBean topicBean = createTopic(true);

        try {
            PostTransferBean postBean = new PostTransferBean();
            postBean.siteId = siteId;
            postBean.topic = topicBean.id;
            postBean.setMessage("Here is my message");
            postBean = conversationsService.savePost(postBean, true);

            PostTransferBean postBean2 = new PostTransferBean();
            postBean2.siteId = siteId;
            postBean2.topic = topicBean.id;
            postBean2.setMessage("Here is my message");
            postBean2 = conversationsService.savePost(postBean2, true);

            switchToUser2();

            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(siteId);
            assertEquals(1, topics.size());

            topicBean = topics.get(0);
            assertEquals(2, topicBean.numberOfUnreadPosts);

            conversationsService.markPostsViewed(new HashSet(Arrays.asList(new String[] {postBean.id, postBean2.id})), topicBean.id);

            topics = conversationsService.getTopicsForSite(siteId);
            topicBean = topics.get(0);
            assertEquals(0, topicBean.numberOfUnreadPosts);

            switchToInstructor();
            Map<String, Object> data = conversationsService.getSiteStats(siteId, null, null, 1, null);
            List<ConversationsStat> stats = (List<ConversationsStat>) data.get("stats");
            assertEquals(3, stats.size());

            Optional<ConversationsStat> stat = stats.stream().filter(cs -> cs.name.equals(user2SortName)).findAny();

            assertTrue(stat.isPresent());

            assertEquals(1L, (long) stat.get().topicsViewed);

            switchToUser1();

            PostTransferBean postBean3 = new PostTransferBean();
            postBean3.siteId = siteId;
            postBean3.topic = topicBean.id;
            postBean3.setMessage("Here is my message");
            postBean3 = conversationsService.savePost(postBean3, true);

            switchToInstructor();
            data = conversationsService.getSiteStats(siteId, null, null, 1, null);
            stats = (List<ConversationsStat>) data.get("stats");
            stat = stats.stream().filter(cs -> cs.name.equals(user2SortName)).findAny();
            assertTrue(stat.isPresent());
            assertEquals(0L, (long) stat.get().topicsViewed);
        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private TopicTransferBean createTopic(boolean discussion) {

        topicBean.type = discussion ? TopicType.DISCUSSION.name() : TopicType.QUESTION.name();
        topicBean.visibility = TopicVisibility.SITE.name();
        topicBean.aboutReference = siteRef;

        try {
            return conversationsService.saveTopic(topicBean, true);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception when creating topic");
        }
        return null;
    }

    private void setupStudentPermissions() {

        when(securityService.unlock(Permissions.ROLETYPE_INSTRUCTOR.label, siteRef)).thenReturn(false);
        when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(false);
        when(securityService.unlock(Permissions.TOPIC_CREATE.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.TOPIC_UPDATE_OWN.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.TOPIC_UPDATE_ANY.label, siteRef)).thenReturn(false);
        when(securityService.unlock(Permissions.TOPIC_DELETE_OWN.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.TOPIC_DELETE_ANY.label, siteRef)).thenReturn(false);
        when(securityService.unlock(Permissions.TOPIC_TAG.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.TOPIC_PIN.label, siteRef)).thenReturn(false);
        when(securityService.unlock(Permissions.TAG_CREATE.label, siteRef)).thenReturn(false);
        when(securityService.unlock(Permissions.VIEW_GROUP_TOPICS.label, siteRef)).thenReturn(false);
        when(securityService.unlock(Permissions.POST_CREATE.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.POST_UPDATE_OWN.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.POST_UPDATE_ANY.label, siteRef)).thenReturn(false);
        when(securityService.unlock(Permissions.POST_DELETE_OWN.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.POST_DELETE_ANY.label, siteRef)).thenReturn(false);
        when(securityService.unlock(Permissions.POST_REACT.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.POST_UPVOTE.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.COMMENT_CREATE.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.COMMENT_UPDATE_OWN.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.COMMENT_UPDATE_ANY.label, siteRef)).thenReturn(false);
        when(securityService.unlock(Permissions.COMMENT_DELETE_OWN.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.COMMENT_DELETE_ANY.label, siteRef)).thenReturn(false);
        when(securityService.unlock(Permissions.VIEW_ANONYMOUS.label, siteRef)).thenReturn(false);
        when(securityService.unlock(Permissions.VIEW_STATISTICS.label, siteRef)).thenReturn(false);

        when(securityService.unlock(SiteService.SITE_VISIT, siteRef)).thenReturn(true);
    }

    private void switchToInstructor() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(instructor);

        when(securityService.unlock(Permissions.ROLETYPE_INSTRUCTOR.label, siteRef)).thenReturn(true);
        when(securityService.unlock(instructor, Permissions.ROLETYPE_INSTRUCTOR.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.MODERATE.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.TOPIC_CREATE.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.TOPIC_UPDATE_OWN.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.TOPIC_UPDATE_ANY.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.TOPIC_DELETE_OWN.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.TOPIC_DELETE_ANY.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.TOPIC_TAG.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.TOPIC_PIN.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.TAG_CREATE.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.VIEW_GROUP_TOPICS.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.POST_CREATE.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.POST_UPDATE_OWN.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.POST_UPDATE_ANY.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.POST_DELETE_OWN.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.POST_DELETE_ANY.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.POST_REACT.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.POST_UPVOTE.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.COMMENT_CREATE.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.COMMENT_UPDATE_OWN.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.COMMENT_UPDATE_ANY.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.COMMENT_DELETE_OWN.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.COMMENT_DELETE_ANY.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.VIEW_ANONYMOUS.label, siteRef)).thenReturn(true);
        when(securityService.unlock(Permissions.VIEW_STATISTICS.label, siteRef)).thenReturn(true);

        when(securityService.unlock(SiteService.SITE_VISIT, siteRef)).thenReturn(true);
        when(securityService.unlock(SiteService.SECURE_UPDATE_SITE, siteRef)).thenReturn(true);

        try {
            when(userDirectoryService.getUser(instructor)).thenReturn(instructorUser);
        } catch (UserNotDefinedException unde) {
        }
    }

    private void switchToUser1() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);
        setupStudentPermissions();
        try {
            when(userDirectoryService.getUser(user1)).thenReturn(user1User);
        } catch (UserNotDefinedException unde) {
        }
    }

    private void switchToUser2() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user2);
        setupStudentPermissions();
        try {
            when(userDirectoryService.getUser(user2)).thenReturn(user2User);
        } catch (UserNotDefinedException unde) {
        }
    }

    private void switchToUser3() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user3);
        setupStudentPermissions();
        try {
            when(userDirectoryService.getUser(user3)).thenReturn(user3User);
        } catch (UserNotDefinedException unde) {
        }
    }
}
