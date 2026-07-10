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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

@RunWith(MockitoJUnitRunner.class)
public class DiscussionForumServiceImplTest {

    private static final String FROM_CONTEXT = "site-from";
    private static final String TO_CONTEXT = "site-to";
    private static final String FORUMS_PLACEMENT_ID = "placement-forums";

    @Mock private AreaManager areaManager;
    @Mock private DiscussionForumManager dfManager;
    @Mock private MessageForumsTypeManager typeManager;
    @Mock private ServerConfigurationService serverConfigurationService;
    @Mock private SessionManager sessionManager;
    @Mock private SiteService siteService;
    @Mock private Session session;
    @Mock private Site site;
    @Mock private ToolConfiguration forumsPlacement;
    @Mock private ToolSession forumsToolSession;

    private DiscussionForumServiceImpl discussionForumService;

    @Before
    public void setUp() {
        discussionForumService = new DiscussionForumServiceImpl();
        discussionForumService.setAreaManager(areaManager);
        discussionForumService.setDfManager(dfManager);
        discussionForumService.setTypeManager(typeManager);
        discussionForumService.setServerConfigurationService(serverConfigurationService);
        discussionForumService.setSessionManager(sessionManager);
        discussionForumService.setSiteService(siteService);

        when(dfManager.getDiscussionForumsWithTopicsMembershipNoAttachments(FROM_CONTEXT))
                .thenReturn(Collections.emptyList());
        when(dfManager.getDiscussionForumsByContextId(TO_CONTEXT)).thenReturn(Collections.emptyList());
        when(sessionManager.getCurrentSession()).thenReturn(session);
    }

    @Test
    public void transferCopyEntitiesClearsTheForumsToolSessionOfTheDestinationSite() throws Exception {
        when(siteService.getSite(TO_CONTEXT)).thenReturn(site);
        when(forumsPlacement.getId()).thenReturn(FORUMS_PLACEMENT_ID);
        when(site.getTools(DiscussionForumService.FORUMS_TOOL_ID))
                .thenReturn(Collections.singletonList(forumsPlacement));
        when(session.getToolSession(FORUMS_PLACEMENT_ID)).thenReturn(forumsToolSession);

        discussionForumService.transferCopyEntities(FROM_CONTEXT, TO_CONTEXT, Collections.emptyList(), null);

        verify(forumsToolSession).clearAttributes();
    }

    @Test
    public void transferCopyEntitiesClearsNothingWhenTheDestinationSiteHasNoForumsTool() throws Exception {
        when(siteService.getSite(TO_CONTEXT)).thenReturn(site);
        when(site.getTools(DiscussionForumService.FORUMS_TOOL_ID)).thenReturn(Collections.emptyList());

        discussionForumService.transferCopyEntities(FROM_CONTEXT, TO_CONTEXT, Collections.emptyList(), null);

        verify(session, never()).getToolSession(anyString());
    }

    @Test
    public void transferCopyEntitiesSurvivesAMissingDestinationSite() throws Exception {
        when(siteService.getSite(TO_CONTEXT)).thenThrow(new IdUnusedException(TO_CONTEXT));

        discussionForumService.transferCopyEntities(FROM_CONTEXT, TO_CONTEXT, Collections.emptyList(), null);

        verify(session, never()).getToolSession(anyString());
    }

    @Test
    public void transferCopyEntitiesWithCleanupClearsTheForumsToolSession() throws Exception {
        when(siteService.getSite(TO_CONTEXT)).thenReturn(site);
        when(forumsPlacement.getId()).thenReturn(FORUMS_PLACEMENT_ID);
        when(site.getTools(DiscussionForumService.FORUMS_TOOL_ID))
                .thenReturn(Collections.singletonList(forumsPlacement));
        when(session.getToolSession(FORUMS_PLACEMENT_ID)).thenReturn(forumsToolSession);
        when(areaManager.getDiscussionArea(anyString(), anyBoolean())).thenReturn(null);

        discussionForumService.transferCopyEntities(FROM_CONTEXT, TO_CONTEXT, Collections.emptyList(), null, true);

        verify(forumsToolSession).clearAttributes();
    }
}
