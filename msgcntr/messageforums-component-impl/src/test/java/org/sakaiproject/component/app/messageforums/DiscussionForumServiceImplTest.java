/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.tool.api.SessionManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DiscussionForumServiceImplTest {

    private static final String SITE_ID = "site-id";
    private static final String USER_ID = "user-id";

    private DiscussionForumServiceImpl service;
    private DiscussionForumManager dfManager;
    private SessionManager sessionManager;

    @Before
    public void setUp() {
        service = new DiscussionForumServiceImpl();
        dfManager = mock(DiscussionForumManager.class);
        sessionManager = mock(SessionManager.class);

        service.setDfManager(dfManager);
        service.setSessionManager(sessionManager);

        when(sessionManager.getCurrentSessionUserId()).thenReturn(USER_ID);
    }

    @Test
    public void updateEntityReferencesClearsImportedDiscussionGradebookLinksWithoutGradebookMapping() {

        DiscussionForum forum = mock(DiscussionForum.class);
        DiscussionTopic topic = mock(DiscussionTopic.class);

        when(forum.getId()).thenReturn(20L);
        when(forum.getExtendedDescription()).thenReturn(null);
        when(forum.getDefaultAssignName()).thenReturn("2");
        when(forum.getDraft()).thenReturn(Boolean.FALSE);
        when(forum.getTopics()).thenReturn(List.of(topic));

        when(topic.getId()).thenReturn(30L);
        when(topic.getExtendedDescription()).thenReturn(null);
        when(topic.getDefaultAssignName()).thenReturn("3");
        when(topic.getDraft()).thenReturn(Boolean.FALSE);

        when(dfManager.getDiscussionForumsByContextId(SITE_ID)).thenReturn(List.of(forum));
        when(dfManager.saveForum(forum, Boolean.FALSE, SITE_ID, false, USER_ID)).thenReturn(forum);
        when(dfManager.getTopicById(30L)).thenReturn(topic);

        Map<String, String> transversalMap = new HashMap<>();
        transversalMap.put("forum/10", "forum/20");
        transversalMap.put("forum_topic/11", "forum_topic/30");

        service.updateEntityReferences(SITE_ID, transversalMap);

        verify(forum).setDefaultAssignName(null);
        verify(dfManager).saveForum(forum, Boolean.FALSE, SITE_ID, false, USER_ID);
        verify(topic).setDefaultAssignName(null);
        verify(dfManager).saveTopic(topic, Boolean.FALSE, null, USER_ID);
    }

    @Test
    public void updateEntityReferencesLeavesExistingDiscussionGradebookLinksAloneWithoutImportMapping() {

        DiscussionForum forum = mock(DiscussionForum.class);
        DiscussionTopic topic = mock(DiscussionTopic.class);

        when(forum.getId()).thenReturn(20L);
        when(forum.getExtendedDescription()).thenReturn(null);
        when(forum.getDefaultAssignName()).thenReturn("2");
        when(forum.getTopics()).thenReturn(List.of(topic));

        when(topic.getId()).thenReturn(30L);
        when(topic.getExtendedDescription()).thenReturn(null);
        when(topic.getDefaultAssignName()).thenReturn("3");

        when(dfManager.getDiscussionForumsByContextId(SITE_ID)).thenReturn(List.of(forum));
        when(dfManager.getTopicById(30L)).thenReturn(topic);

        service.updateEntityReferences(SITE_ID, Map.of("foo", "bar"));

        verify(forum, never()).setDefaultAssignName(null);
        verify(dfManager, never()).saveForum(forum, Boolean.FALSE, SITE_ID, false, USER_ID);
        verify(topic, never()).setDefaultAssignName(null);
        verify(dfManager, never()).saveTopic(topic, Boolean.FALSE, null, USER_ID);
    }
}
