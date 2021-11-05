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

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.ConversationsPermissionsException;
import org.sakaiproject.conversations.api.Permissions;
import org.sakaiproject.conversations.api.PostSort;
import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.conversations.api.TopicType;
import org.sakaiproject.conversations.api.TopicVisibility;
import org.sakaiproject.conversations.api.beans.PostTransferBean;
import org.sakaiproject.conversations.api.beans.TopicTransferBean;
import org.sakaiproject.conversations.api.model.Settings;
import org.sakaiproject.conversations.api.model.Tag;
import org.sakaiproject.conversations.api.model.Topic;
import org.sakaiproject.conversations.api.model.TopicStatus;
import org.sakaiproject.conversations.api.repository.PostRepository;
import org.sakaiproject.conversations.api.repository.TopicRepository;
import org.sakaiproject.conversations.api.repository.TopicStatusRepository;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;
import org.springframework.transaction.annotation.Transactional;

import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired private MemoryService memoryService;
    @Autowired private ConversationsService conversationsService;
    @Resource private SecurityService securityService;
    @Resource private SessionManager sessionManager;
    @Resource private UserDirectoryService userDirectoryService;
    @Resource private SessionFactory sessionFactory;
    @Resource private PostRepository postRepository;
    @Resource private SiteService siteService;
    @Resource private ServerConfigurationService serverConfigurationService;
    @Resource private TopicRepository topicRepository;
    @Resource private TopicStatusRepository topicStatusRepository;

    TopicTransferBean topicBean = null;
    PostTransferBean postBean = null;
    String user1 = "user1";
    User user1User = null;
    String adrian = "Adrian Fish";
    String user2 = "user2";
    User user2User = null;
    String earle = "Earle Nietzel";
    String user3 = "user3";
    User user3User = null;
    String zaphod = "Zaphod Beeblebrox";
    String siteId = "xyz";

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
        user1User = mock(User.class);
        when(user1User.getDisplayName()).thenReturn(adrian);
        user2User = mock(User.class);
        when(user2User.getDisplayName()).thenReturn(earle);
        user3User = mock(User.class);
        when(user3User.getDisplayName()).thenReturn(zaphod);
        when(serverConfigurationService.getInt(ConversationsService.PROP_THREADS_PAGE_SIZE, 10)).thenReturn(10);
        when(serverConfigurationService.getPortalUrl()).thenReturn("http://localhost/portal");
        ToolConfiguration toolConfig = mock(ToolConfiguration.class);
        when(toolConfig.getId()).thenReturn("abcdefg");
        Site site = mock(Site.class);
        when(site.getToolForCommonId(ConversationsService.TOOL_ID)).thenReturn(toolConfig);

        // this is too late for the init method, I think.
        Cache postsCache = mock(Cache.class);
        //when(memoryService.<String, Map<String, List<PostTransferBean>>>getCache(ConversationsService.POSTS_CACHE_NAME)).thenReturn(postsCache);
        ((ConversationsServiceImpl) AopTestUtils.getTargetObject(conversationsService)).setPostsCache(postsCache);

        try {
          when(siteService.getSite(siteId)).thenReturn(site);
        } catch (Exception e) {
        }
    }

    @Test
    public void createTopicWithoutPermission() {

        // No current user. We should get an exeception.
        assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTopic(topicBean));

        when(sessionManager.getCurrentSessionUserId()).thenReturn("sakaiuser");

        // You can't create a topic without TOPIC_CREATE.
        assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTopic(topicBean));
    }

    @Test
    public void crudTopic() {

        try {
            // CREATE
            topicBean = createTopic(true);
            assertTrue(topicBean != null && topicBean.id != null && topicBean.id.length() == 36);

            // You need SITE_VISIT to read the topics for a site
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.getTopicsForSite(topicBean.siteId));
            when(securityService.unlock(SiteService.SITE_VISIT, "/site/" + topicBean.siteId)).thenReturn(true);
            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());

            String updatedTitle = "Updated Topic 1";
            String updatedMessage = "Updated Topic 1 message";

            topicBean.setTitle(updatedTitle);
            topicBean.setMessage(updatedMessage);

            // You need TOPIC_UPDATE_OWN or TOPIC_UPDATE_ANY to update your own topic
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTopic(topicBean));
            when(securityService.unlock(Permissions.TOPIC_UPDATE_OWN.label, "/site/" + topicBean.siteId)).thenReturn(true);
            topicBean = conversationsService.saveTopic(topicBean);
            assertEquals(updatedTitle, topicBean.title);
            assertEquals(updatedMessage, topicBean.message);

            // You need TOPIC_DELETE_OWN or TOPIC_DELETE_ANY to delete your own topic
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.deleteTopic(topicBean.id));
            when(securityService.unlock(Permissions.TOPIC_DELETE_OWN.label, "/site/" + topicBean.siteId)).thenReturn(true);
            conversationsService.deleteTopic(topicBean.id);
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertTrue(topics.size() == 0);
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when saving topic");
        }
    }

    @Test
    public void topicFiltering() {

        try {
            topicBean.draft = true;
            topicBean = createTopic(true);

            when(securityService.unlock(SiteService.SITE_VISIT, "/site/" + topicBean.siteId)).thenReturn(true);

            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());

            // user2 should not be able to see your draft topic
            switchToUser2();
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(0, topics.size());

            switchToUser1();

            when(securityService.unlock(Permissions.TOPIC_UPDATE_ANY.label, "/site/" + topicBean.siteId)).thenReturn(true);

            // topics marked for INSTRUCTORS should ony be viewable by users in the instructor role
            // (as set by a permission), or their creator
            topicBean.visibility = "INSTRUCTORS";
            topicBean.draft = false;
            topicBean = conversationsService.saveTopic(topicBean);

            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());

            switchToUser2();

            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(0, topics.size());

            when(securityService.unlock(Permissions.ROLETYPE_INSTRUCTOR.label, "/site/" + topicBean.siteId)).thenReturn(true);

            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());

            when(securityService.unlock(Permissions.MODERATE.label, "/site/" + topicBean.siteId)).thenReturn(true);
            switchToUser1();
            topicBean.visibility = "SITE";
            topicBean.hidden = true;
            topicBean = conversationsService.saveTopic(topicBean);

            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());

            when(securityService.unlock(Permissions.MODERATE.label, "/site/" + topicBean.siteId)).thenReturn(false);
            switchToUser2();
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(0, topics.size());

            when(securityService.unlock(Permissions.MODERATE.label, "/site/" + topicBean.siteId)).thenReturn(true);
            topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when testing topic filtering");
        }
    }

    @Test
    public void hideTopic() {

        when(securityService.unlock(SiteService.SITE_VISIT, "/site/" + topicBean.siteId)).thenReturn(true);
        try {
            TopicTransferBean topicBean = createTopic(true);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.hideTopic(topicBean.id, true));
            when(securityService.unlock(Permissions.MODERATE.label, "/site/" + topicBean.siteId)).thenReturn(true);
            conversationsService.hideTopic(topicBean.id, true);
            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when testing topic filtering");
        }
    }

    @Test
    public void lockTopic() {

        when(securityService.unlock(SiteService.SITE_VISIT, "/site/" + topicBean.siteId)).thenReturn(true);
        try {
            TopicTransferBean topicBean = createTopic(true);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.lockTopic(topicBean.id, true));
            when(securityService.unlock(Permissions.MODERATE.label, "/site/" + topicBean.siteId)).thenReturn(true);
            conversationsService.lockTopic(topicBean.id, true);
            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(topicBean.siteId);
            assertEquals(1, topics.size());

            // This topic has now been locked. We should not be able to update it
            TopicTransferBean updatedBean = topics.get(0);
            updatedBean.message = "xxxxxx";
            when(securityService.unlock(Permissions.MODERATE.label, "/site/" + topicBean.siteId)).thenReturn(false);
            when(securityService.unlock(Permissions.TOPIC_UPDATE_ANY.label, "/site/" + topicBean.siteId)).thenReturn(true);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTopic(updatedBean));
            when(securityService.unlock(Permissions.MODERATE.label, "/site/" + topicBean.siteId)).thenReturn(true);
            conversationsService.saveTopic(updatedBean);
            postBean.topic = topicBean.id;
            when(securityService.unlock(Permissions.POST_CREATE.label, "/site/" + topicBean.siteId)).thenReturn(true);

            // We should not be able to create a post on a locked topic, unless we have MODERATE
            when(securityService.unlock(Permissions.MODERATE.label, "/site/" + topicBean.siteId)).thenReturn(false);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.savePost(postBean));
            when(securityService.unlock(Permissions.MODERATE.label, "/site/" + topicBean.siteId)).thenReturn(true);
            PostTransferBean updatedPostBean = conversationsService.savePost(postBean);
            // Locking a topic should lock all the posts in the topic, even when adding new posts to a locked topic
            assertTrue(updatedPostBean.locked);
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when testing topic filtering");
        }
    }

    @Test
    public void lockSite() {

        TopicTransferBean topicBean = createTopic(true);
        when(securityService.unlock(Permissions.POST_CREATE.label, "/site/" + topicBean.siteId)).thenReturn(true);
        when(securityService.unlock(Permissions.POST_UPDATE_ANY.label, "/site/" + topicBean.siteId)).thenReturn(true);
        when(securityService.unlock(SiteService.SECURE_UPDATE_SITE, "/site/" + topicBean.siteId)).thenReturn(true);

        try {
            postBean.topic = topicBean.id;
            postBean = conversationsService.savePost(postBean);
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
            topicBean2.aboutReference = "/site/" + topicBean.siteId;

            // this site is now locked. You should only be able to create a topic if you have MODERATE
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.saveTopic(topicBean2));

            postBean.setMessage("eggs");
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.savePost(postBean));
            when(securityService.unlock(Permissions.MODERATE.label, "/site/" + topicBean.siteId)).thenReturn(true);
            //conversationsService.savePost(postBean);
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when testing topic filtering");
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    @Test
    public void anonymousTopic() {

        when(securityService.unlock(SiteService.SITE_VISIT, "/site/" + topicBean.siteId)).thenReturn(true);
        try {
            topicBean = createTopic(true);
            assertTrue(topicBean != null && topicBean.id != null && topicBean.id.length() == 36);
            topicBean.anonymous = true;
            when(securityService.unlock(Permissions.TOPIC_UPDATE_OWN.label, "/site/" + topicBean.siteId)).thenReturn(true);
            topicBean = conversationsService.saveTopic(topicBean);
            assertNotEquals(topicBean.creatorDisplayName, user1User.getDisplayName());
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when testing topic filtering");
        }
    }

    @Test
    public void crudPost() {

        try {
            topicBean = createTopic(true);
            assertTrue(topicBean != null && topicBean.id != null && topicBean.id.length() == 36);

            postBean.topic = topicBean.id;
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.savePost(postBean));

            when(securityService.unlock(Permissions.POST_CREATE.label, "/site/" + topicBean.siteId)).thenReturn(true);

            postBean = conversationsService.savePost(postBean);
            assertEquals(36, postBean.id.length());
            assertEquals(user1, postBean.creator);

            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null));

            when(securityService.unlock(SiteService.SITE_VISIT, "/site/" + topicBean.siteId)).thenReturn(true);

            Collection<PostTransferBean> posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(1, posts.size());

            String updatedMessage = "Updated Message";
            postBean.message = updatedMessage;
            postBean.siteId = topicBean.siteId;
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.savePost(postBean));

            when(securityService.unlock(Permissions.POST_UPDATE_OWN.label, "/site/" + topicBean.siteId)).thenReturn(true);
            postBean = conversationsService.savePost(postBean);
            assertEquals(updatedMessage, postBean.message);

            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.deletePost(postBean.siteId, postBean.topic, postBean.id, false));

            when(securityService.unlock(Permissions.POST_DELETE_OWN.label, "/site/" + topicBean.siteId)).thenReturn(true);
            conversationsService.deletePost(postBean.siteId, postBean.topic, postBean.id, false);

            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(0, posts.size());
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when saving post");
        }
    }

    @Test
    public void getPostsByTopicId() {

        when(securityService.unlock(Permissions.POST_CREATE.label, "/site/" + topicBean.siteId)).thenReturn(true);

        when(serverConfigurationService.getInt(ConversationsService.PROP_THREADS_PAGE_SIZE, 10)).thenReturn(2);

        try {
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
            thread1 = conversationsService.savePost(thread1);
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null));
            when(securityService.unlock(SiteService.SITE_VISIT, "/site/" + topicBean.siteId)).thenReturn(true);
            Collection<PostTransferBean> posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(1, posts.size());

            PostTransferBean thread2 = new PostTransferBean();
            thread2.setMessage(t2Message);
            thread2.topic = topicBean.id;
            thread2.siteId = siteId;
            switchToUser2();
            postBean = conversationsService.savePost(thread2);
            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(2, posts.size());

            PostTransferBean thread3 = new PostTransferBean();
            thread3.setMessage(t3Message);
            thread3.topic = topicBean.id;
            thread3.siteId = siteId;
            switchToUser3();
            postBean = conversationsService.savePost(thread3);

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
            postBean = conversationsService.savePost(post1);

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
            assertEquals(adrian, test.creatorDisplayName);
            assertEquals(t1Message, test.message);
            test = (PostTransferBean) it.next();
            assertEquals(earle, test.creatorDisplayName);
            assertEquals(t2Message, test.message);

            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 1, PostSort.ASC_CREATOR, null);
            assertEquals(1, posts.size());
            it = posts.iterator();
            test = (PostTransferBean) it.next();
            assertEquals(zaphod, test.creatorDisplayName);
            assertEquals(t3Message, test.message);

            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, PostSort.DESC_CREATOR, null);
            assertEquals(2, posts.size());
            it = posts.iterator();
            test = (PostTransferBean) it.next();
            assertEquals(zaphod, test.creatorDisplayName);
            assertEquals(t3Message, test.message);
            test = (PostTransferBean) it.next();
            assertEquals(earle, test.creatorDisplayName);
            assertEquals(t2Message, test.message);

            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 1, PostSort.DESC_CREATOR, null);
            assertEquals(1, posts.size());
            it = posts.iterator();
            test = (PostTransferBean) it.next();
            assertEquals(adrian, test.creatorDisplayName);
            assertEquals(t1Message, test.message);
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when saving post");
        }
    }

    @Test
    public void upDownVotePost() {

        try {
            topicBean = createTopic(true);
            postBean.topic = topicBean.id;
            when(securityService.unlock(Permissions.POST_CREATE.label, "/site/" + topicBean.siteId)).thenReturn(true);
            postBean = conversationsService.savePost(postBean);
            int currentUpvotes = postBean.upvotes;
            assertTrue(postBean.id != null);
            assertEquals(0, postBean.upvotes);

            // We should not be able to upvote your own post
            assertThrows(IllegalArgumentException.class, () -> conversationsService.upvotePost(postBean.siteId, postBean.topic, postBean.id));

            // Switch to user2
            when(sessionManager.getCurrentSessionUserId()).thenReturn(user2);
            try {
                when(userDirectoryService.getUser(user2)).thenReturn(user2User);
            } catch (UserNotDefinedException unde) {
            }

            // We should not be able to upvote a post without POST_UPVOTE
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.upvotePost(postBean.siteId, postBean.topic, postBean.id));

            when(securityService.unlock(Permissions.POST_UPVOTE.label, "/site/" + topicBean.siteId)).thenReturn(true);

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
            TopicTransferBean topicBean = createTopic(true);
            assertTrue(topicBean != null && topicBean.id != null && topicBean.id.length() == 36);
            when(securityService.unlock(Permissions.POST_CREATE.label, "/site/" + topicBean.siteId)).thenReturn(true);

            postBean.topic = topicBean.id;

            postBean = conversationsService.savePost(postBean);

            /*
            ExecutorService executor = Executors.newFixedThreadPool(10);

            PostTransferBean pb = new PostTransferBean();
            pb.message = "Post message";
            pb.siteId = siteId;
            pb.topic = topicBean.id;

            for (int i = 0; i < 10; i++) {
                executor.execute(() -> {

                    try {
                        conversationsService.savePost(pb);
                    } catch (ConversationsPermissionsException e) {
                    }
                });
            }

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);

            optTopic = topicRepository.findById(topicBean.id);
            assertTrue(optTopic.isPresent());
            assertEquals(optTopic.get().getNumberOfPosts(), Long.valueOf(10));
            */
        } catch (Exception e) {
            fail("Unexpected exception when saving post");
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

        when(securityService.unlock(Permissions.TAG_CREATE.label, "/site/" + tag.getSiteId())).thenReturn(true);
        try {
            Tag savedTag = conversationsService.saveTag(tag);
            assertFalse(savedTag.getId() == null);

            List<Tag> siteTags = conversationsService.getTagsForSite(tag.getSiteId());

            // Should only be able to pull the tags if we can tag topics
            assertEquals(siteTags.size(), 0);

            when(securityService.unlock(Permissions.TOPIC_TAG.label, "/site/" + tag.getSiteId())).thenReturn(true);
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

        TopicTransferBean topicBean = createTopic(true);
        Tag tag = new Tag();
        tag.setSiteId(siteId);
        tag.setLabel("chicken");

        when(securityService.unlock(Permissions.TAG_CREATE.label, "/site/" + tag.getSiteId())).thenReturn(true);
        when(securityService.unlock(Permissions.TOPIC_UPDATE_OWN.label, "/site/" + tag.getSiteId())).thenReturn(true);

        List<Tag> tags = new ArrayList<>();
        tags.add(tag);
        try {
            tags = conversationsService.createTags(tags);
            Long newTagId = tags.get(0).getId();
            topicBean.tags = tags;
            topicBean = conversationsService.saveTopic(topicBean);
            assertEquals(newTagId, topicBean.tags.iterator().next().getId());

            conversationsService.deleteTag(newTagId);

            Optional<Topic> optTopic = topicRepository.findById(topicBean.id);
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
            when(securityService.unlock(SiteService.SECURE_UPDATE_SITE, "/site/" + settings.getSiteId())).thenReturn(true);
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
        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail("Unexpected exception when creating settings");
        }
    }

    @Test
    public void pinTopic() {

        TopicTransferBean topicBean = createTopic(true);
        when(securityService.unlock(Permissions.TOPIC_UPDATE_OWN.label, "/site/" + topicBean.siteId)).thenReturn(true);
        try {
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.pinTopic(topicBean.id, true));
            when(securityService.unlock(Permissions.TOPIC_PIN.label, "/site/" + topicBean.siteId)).thenReturn(true);
            conversationsService.pinTopic(topicBean.id, true);
            Optional<Topic> optTopic = topicRepository.findById(topicBean.id);
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

        TopicTransferBean topicBean = createTopic(true);
        when(securityService.unlock(Permissions.TOPIC_UPDATE_OWN.label, "/site/" + topicBean.siteId)).thenReturn(true);
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

        TopicTransferBean topicBean = createTopic(true);

        when(securityService.unlock(SiteService.SITE_VISIT, "/site/" + topicBean.siteId)).thenReturn(true);

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

        TopicTransferBean topicBean = createTopic(true);
        topicBean.type = TopicType.DISCUSSION.name();

        when(securityService.unlock(SiteService.SITE_VISIT, "/site/" + topicBean.siteId)).thenReturn(true);

        try {
            String thread1Message = "Thread 1";

            PostTransferBean thread1 = new PostTransferBean();
            thread1.siteId = siteId;
            thread1.topic = "none";
            thread1.message = thread1Message;
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.savePost(thread1));

            when(securityService.unlock(Permissions.POST_CREATE.label, "/site/" + topicBean.siteId)).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> conversationsService.savePost(thread1));

            thread1.topic = topicBean.id;

            PostTransferBean savedThread1 = conversationsService.savePost(thread1);

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
            assertThrows(IllegalArgumentException.class, () -> conversationsService.savePost(reply1));

            reply1.parentPost = savedThread1.id;

            PostTransferBean savedReply1 = conversationsService.savePost(reply1);
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
            PostTransferBean savedReply2 = conversationsService.savePost(reply2);

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

        TopicTransferBean topicBean = createTopic(true);

        when(securityService.unlock(SiteService.SITE_VISIT, "/site/" + topicBean.siteId)).thenReturn(true);
        when(securityService.unlock(Permissions.POST_CREATE.label, "/site/" + topicBean.siteId)).thenReturn(true);

        try {
            postBean.topic = topicBean.id;
            postBean = conversationsService.savePost(postBean);

            List<TopicTransferBean> topics = conversationsService.getTopicsForSite(siteId);
            assertEquals(1, topics.get(0).numberOfPosts);

            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.deletePost(topicBean.siteId, topicBean.id, postBean.id, false));
            when(securityService.unlock(Permissions.POST_DELETE_ANY.label, "/site/" + topicBean.siteId)).thenReturn(true);
            conversationsService.deletePost(topicBean.siteId, topicBean.id, postBean.id, false);
            Collection<PostTransferBean> posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(0, posts.size());

            topics = conversationsService.getTopicsForSite(siteId);
            assertEquals(0, topics.get(0).numberOfPosts);

            //when(securityService.unlock(Permissions.POST_UPDATEDELETE_ANY.label, "/site/" + topicBean.siteId)).thenReturn(true);
            postBean.id = "";
            postBean = conversationsService.savePost(postBean);
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

            reply1 = conversationsService.savePost(reply1);
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

        TopicTransferBean topicBean = createTopic(true);

        when(securityService.unlock(SiteService.SITE_VISIT, "/site/" + topicBean.siteId)).thenReturn(true);
        when(securityService.unlock(Permissions.POST_CREATE.label, "/site/" + topicBean.siteId)).thenReturn(true);

        try {
            postBean.topic = topicBean.id;
            PostTransferBean updatedPostBean = conversationsService.savePost(postBean);
            Collection<PostTransferBean> posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id, 0, null, null);
            assertEquals(1, posts.size());
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.hidePost(topicBean.siteId, topicBean.id, updatedPostBean.id, true));
            when(securityService.unlock(Permissions.MODERATE.label, "/site/" + topicBean.siteId)).thenReturn(true);
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

        TopicTransferBean topicBean = createTopic(true);

        when(securityService.unlock(SiteService.SITE_VISIT, "/site/" + topicBean.siteId)).thenReturn(true);

        try {
            // A "thread" is just a top level post to a topic
            PostTransferBean thread1 = new PostTransferBean();
            thread1.setMessage("T1");
            thread1.topic = topicBean.id;
            thread1.siteId = siteId;
            //switchToUser1();
            postBean.topic = topicBean.id;

            when(securityService.unlock(Permissions.POST_CREATE.label, "/site/" + topicBean.siteId)).thenReturn(true);

            PostTransferBean savedThread1 = conversationsService.savePost(thread1);

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

            PostTransferBean savedReply1 = conversationsService.savePost(reply1);

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
            cpe.printStackTrace();
            fail("Unexpected exception when reacting to post");
        }
    }

    private TopicTransferBean createTopic(boolean discussion) {

        topicBean.type = discussion ? TopicType.DISCUSSION.name() : TopicType.QUESTION.name();
        topicBean.visibility = TopicVisibility.SITE.name();
        topicBean.aboutReference = "/site/" + topicBean.siteId;

        switchToUser1();
        //when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);
        when(securityService.unlock(Permissions.TOPIC_CREATE.label, "/site/" + topicBean.siteId)).thenReturn(true);

        /*
        try {
            when(userDirectoryService.getUser(user1)).thenReturn(user1User);
        } catch (UserNotDefinedException unde) {
        }
        */

        try {
            return conversationsService.saveTopic(topicBean);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception when creating topic");
        }
        return null;
    }

    private void switchToUser1() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);
        try {
            when(userDirectoryService.getUser(user1)).thenReturn(user1User);
        } catch (UserNotDefinedException unde) {
        }
    }

    private void switchToUser2() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user2);
        try {
            when(userDirectoryService.getUser(user2)).thenReturn(user2User);
        } catch (UserNotDefinedException unde) {
        }
    }

    private void switchToUser3() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user3);
        try {
            when(userDirectoryService.getUser(user3)).thenReturn(user3User);
        } catch (UserNotDefinedException unde) {
        }
    }
}
