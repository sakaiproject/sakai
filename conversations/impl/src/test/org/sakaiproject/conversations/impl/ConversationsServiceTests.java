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

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.ConversationsPermissionsException;
import org.sakaiproject.conversations.api.Permissions;
import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.conversations.api.TopicType;
import org.sakaiproject.conversations.api.TopicVisibility;
import org.sakaiproject.conversations.api.beans.PostTransferBean;
import org.sakaiproject.conversations.api.beans.TopicTransferBean;
import org.sakaiproject.conversations.api.model.Settings;
import org.sakaiproject.conversations.api.model.Tag;
import org.sakaiproject.conversations.api.model.Topic;
import org.sakaiproject.conversations.api.model.TopicStatus;
import org.sakaiproject.conversations.api.repository.TopicRepository;
import org.sakaiproject.conversations.api.repository.TopicStatusRepository;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.HashMap;
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

    @Autowired private ConversationsService conversationsService;
    @Resource private SecurityService securityService;
    @Resource private SessionManager sessionManager;
    @Resource private UserDirectoryService userDirectoryService;
    @Resource private SessionFactory sessionFactory;
    @Resource private TopicRepository topicRepository;
    @Resource private TopicStatusRepository topicStatusRepository;

    TopicTransferBean topicBean = null;
    PostTransferBean postBean = null;
    String user1 = "user1";
    User user1User = null;
    String user2 = "user2";
    User user2User = null;

    @Before
    public void setup() {

        reset(sessionManager);
        reset(securityService);
        reset(userDirectoryService);
        topicBean = new TopicTransferBean();
        topicBean.setTitle("Topic 1");
        topicBean.setMessage("Topic 1 messaage");
        topicBean.siteId = "xyz";
        postBean = new PostTransferBean();
        postBean.message = "Post message";
        postBean.siteId = "xyz";
        user1User = mock(User.class);
        when(user1User.getDisplayName()).thenReturn("User 1");
        user2User = mock(User.class);
        when(user2User.getDisplayName()).thenReturn("User 2");
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
            topicBean = createTopic();
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
            topicBean = createTopic();

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
            TopicTransferBean topicBean = createTopic();
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
            TopicTransferBean topicBean = createTopic();
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

        TopicTransferBean topicBean = createTopic();
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
            topicBean2.siteId = "xyz";
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
            topicBean = createTopic();
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
            topicBean = createTopic();
            assertTrue(topicBean != null && topicBean.id != null && topicBean.id.length() == 36);

            postBean.topic = topicBean.id;
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.savePost(postBean));

            when(securityService.unlock(Permissions.POST_CREATE.label, "/site/" + topicBean.siteId)).thenReturn(true);

            postBean = conversationsService.savePost(postBean);
            assertEquals(36, postBean.id.length());
            assertEquals(user1, postBean.creator);

            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id));

            when(securityService.unlock(SiteService.SITE_VISIT, "/site/" + topicBean.siteId)).thenReturn(true);

            List<PostTransferBean> posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id);
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

            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id);
            assertEquals(0, posts.size());
        } catch (ConversationsPermissionsException cpe) {
            fail("Unexpected exception when saving post");
        }
    }

    @Test
    public void upDownVotePost() {

        try {
            topicBean = createTopic();
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
            TopicTransferBean topicBean = createTopic();
            assertTrue(topicBean != null && topicBean.id != null && topicBean.id.length() == 36);
            when(securityService.unlock(Permissions.POST_CREATE.label, "/site/" + topicBean.siteId)).thenReturn(true);

            postBean.topic = topicBean.id;

            postBean = conversationsService.savePost(postBean);

            /*
            ExecutorService executor = Executors.newFixedThreadPool(10);

            PostTransferBean pb = new PostTransferBean();
            pb.message = "Post message";
            pb.siteId = "xyz";
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
        tag.setSiteId("xyz");
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

        TopicTransferBean topicBean = createTopic();
        Tag tag = new Tag();
        tag.setSiteId("xyz");
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
        settings.setSiteId("xyz");
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

            Settings siteSettings = conversationsService.getSettingsForSite("xyz");
            assertFalse(siteSettings.getAllowPinning());
        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail("Unexpected exception when creating settings");
        }
    }

    @Test
    public void pinTopic() {

        TopicTransferBean topicBean = createTopic();
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

        TopicTransferBean topicBean = createTopic();
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

        TopicTransferBean topicBean = createTopic();

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
    public void reactToPost() {

        TopicTransferBean topicBean = createTopic();

        when(securityService.unlock(SiteService.SITE_VISIT, "/site/" + topicBean.siteId)).thenReturn(true);

        try {
            postBean.topic = topicBean.id;

            when(securityService.unlock(Permissions.POST_CREATE.label, "/site/" + topicBean.siteId)).thenReturn(true);

            postBean = conversationsService.savePost(postBean);

            assertEquals(36, postBean.id.length());
            assertEquals(user1, postBean.creator);

            Map<Reaction, Boolean> reactions = new HashMap<>();
            reactions.put(Reaction.GOOD_ANSWER, Boolean.TRUE);

            // This should fail, as you can't react to your own topic
            assertThrows(ConversationsPermissionsException.class, () -> conversationsService.savePostReactions(postBean.id, reactions));

            switchToUser2();

            conversationsService.savePostReactions(postBean.id, reactions);

            List<PostTransferBean> posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id);
            assertEquals(1, posts.size());
            PostTransferBean updatedBean = posts.get(0);
            assertTrue(updatedBean.myReactions.get(Reaction.GOOD_ANSWER));

            assertTrue(1 == updatedBean.reactionTotals.get(Reaction.GOOD_ANSWER));

            reactions.put(Reaction.GOOD_ANSWER, Boolean.FALSE);
            conversationsService.savePostReactions(updatedBean.id, reactions);
            posts = conversationsService.getPostsByTopicId(topicBean.siteId, topicBean.id);
            assertEquals(1, posts.size());
            updatedBean = posts.get(0);
            assertTrue(0 == updatedBean.reactionTotals.get(Reaction.GOOD_ANSWER));
        } catch (ConversationsPermissionsException cpe) {
            cpe.printStackTrace();
            fail("Unexpected exception when reacting to post");
        }
    }

    private TopicTransferBean createTopic() {

        topicBean.type = TopicType.QUESTION.name();
        topicBean.visibility = TopicVisibility.SITE.name();
        topicBean.aboutReference = "/site/" + topicBean.siteId;

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);
        when(securityService.unlock(Permissions.TOPIC_CREATE.label, "/site/" + topicBean.siteId)).thenReturn(true);

        try {
            when(userDirectoryService.getUser(user1)).thenReturn(user1User);
        } catch (UserNotDefinedException unde) {
        }

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

}
