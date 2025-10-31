/**
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
package org.sakaiproject.component.app.messageforums;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;

@RunWith(MockitoJUnitRunner.class)
public class DiscussionForumServiceImplTest {

        private static final String SITE_ID = "site-id";

        private DiscussionForumServiceImpl discussionForumService;

        @Mock
        private DiscussionForumManager discussionForumManager;

        @Mock
        private MessageForumsForumManager forumManager;

        @Mock
        private AreaManager areaManager;

        @Before
        public void setUp() {
                discussionForumService = new DiscussionForumServiceImpl();
                discussionForumService.setDfManager(discussionForumManager);
                discussionForumService.setForumManager(forumManager);
                discussionForumService.setAreaManager(areaManager);
        }

        @Test
        public void hardDeleteRemovesForumsTopicsMessagesAndPermissions() {
                DiscussionForum forum = mock(DiscussionForum.class);
                DiscussionTopic topic = mock(DiscussionTopic.class);
                DiscussionTopic topicWithMessages = mock(DiscussionTopic.class);
                Message firstMessage = mock(Message.class);
                Message secondMessage = mock(Message.class);

                when(discussionForumManager.getDiscussionForumsByContextId(SITE_ID)).thenReturn(Arrays.asList(forum));
                when(forum.getTopics()).thenReturn(Arrays.asList(topic));
                when(topic.getId()).thenReturn(100L);
                when(discussionForumManager.getTopicByIdWithMessagesAndAttachments(100L)).thenReturn(topicWithMessages);
                when(topicWithMessages.getMessages()).thenReturn(Arrays.asList(firstMessage, secondMessage));

                Area area = mock(Area.class);
                DBMembershipItem membershipItem = mock(DBMembershipItem.class);
                Set<DBMembershipItem> membershipItems = new HashSet<>();
                membershipItems.add(membershipItem);

                when(areaManager.getDiscussionArea(SITE_ID, false)).thenReturn(area);
                when(area.getMembershipItemSet()).thenReturn(membershipItems);

                discussionForumService.hardDelete(SITE_ID);

                verify(discussionForumManager).deleteMessage(firstMessage);
                verify(discussionForumManager).deleteMessage(secondMessage);
                verify(forumManager).deleteDiscussionForum(forum);
                verify(area).removeMembershipItem(membershipItem);
                verify(areaManager).saveArea(area);
        }
}
