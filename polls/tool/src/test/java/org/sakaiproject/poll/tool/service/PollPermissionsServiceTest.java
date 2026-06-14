/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.poll.tool.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.poll.api.PollConstants;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PollPermissionsServiceTest {

    private SecurityService securityService;
    private SiteService siteService;
    private SessionManager sessionManager;
    private ToolManager toolManager;
    private PollPermissionsService pollPermissionsService;

    @Before
    public void setUp() {
        securityService = mock(SecurityService.class);
        siteService = mock(SiteService.class);
        sessionManager = mock(SessionManager.class);
        toolManager = mock(ToolManager.class);
        pollPermissionsService = new PollPermissionsService(
                securityService,
                siteService,
                sessionManager,
                toolManager);
    }

    @Test
    public void permissionsAreFalseWithoutCurrentPlacement() {
        Poll poll = new Poll();
        poll.setOwner("owner");

        Assert.assertFalse(pollPermissionsService.canAddPoll());
        Assert.assertFalse(pollPermissionsService.canEditPoll(poll));
        Assert.assertFalse(pollPermissionsService.isSiteOwner());

        verify(securityService, never()).unlock(anyString(), anyString());
    }

    @Test
    public void superUserDoesNotRequireCurrentPlacement() {
        when(securityService.isSuperUser()).thenReturn(true);

        Assert.assertTrue(pollPermissionsService.canAddPoll());
        Assert.assertTrue(pollPermissionsService.canEditPoll(new Poll()));
        Assert.assertTrue(pollPermissionsService.isSiteOwner());
    }

    @Test
    public void canEditPollIsFalseForNullPoll() {
        Assert.assertFalse(pollPermissionsService.canEditPoll(null));

        verify(securityService, never()).unlock(anyString(), anyString());
    }

    @Test
    public void canEditPollUsesPollSiteReference() {
        Poll poll = new Poll();
        poll.setSiteId("poll-site");

        when(siteService.siteReference("poll-site")).thenReturn("/site/poll-site");
        when(securityService.unlock(eq(PollConstants.PERMISSION_EDIT_ANY), eq("/site/poll-site"))).thenReturn(true);

        Assert.assertTrue(pollPermissionsService.canEditPoll(poll));

        verify(toolManager, never()).getCurrentPlacement();
    }
}
