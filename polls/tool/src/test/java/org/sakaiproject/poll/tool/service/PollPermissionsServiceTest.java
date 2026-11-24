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
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PollPermissionsServiceTest {

    private SecurityService securityService;
    private PollsService pollsService;
    private ToolManager toolManager;
    private PollPermissionsService pollPermissionsService;

    @Before
    public void setUp() {
        securityService = mock(SecurityService.class);
        pollsService = mock(PollsService.class);
        toolManager = mock(ToolManager.class);
        pollPermissionsService = new PollPermissionsService(
                securityService,
                mock(SiteService.class),
                pollsService,
                toolManager);
    }

    @Test
    public void permissionsAreFalseWithoutCurrentPlacement() {
        Assert.assertFalse(pollPermissionsService.canAddPoll());
        verify(pollsService).isAllowedPollAdd(null);
        Assert.assertFalse(pollPermissionsService.isSiteOwner());

        verify(securityService, never()).unlock(anyString(), anyString());
    }

    @Test
    public void superUserIsSiteOwnerWithoutCurrentPlacement() {
        when(securityService.isSuperUser()).thenReturn(true);

        Assert.assertTrue(pollPermissionsService.isSiteOwner());
    }

    @Test
    public void canAddPollUsesTheServiceRuleForTheCurrentSite() {
        Placement placement = mock(Placement.class);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("poll-site");
        when(pollsService.isAllowedPollAdd("poll-site")).thenReturn(true);

        Assert.assertTrue(pollPermissionsService.canAddPoll());
    }

    @Test
    public void canEditPollUsesTheServiceRuleForThePoll() {
        Poll poll = new Poll();
        poll.setSiteId("poll-site");

        when(pollsService.isAllowedPollEdit(poll)).thenReturn(true);

        Assert.assertTrue(pollPermissionsService.canEditPoll(poll));

        verify(toolManager, never()).getCurrentPlacement();
    }
}
