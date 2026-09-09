/*
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
package org.sakaiproject.poll.test.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sakaiproject.poll.api.PollConstants.PERMISSION_ADD;
import static org.sakaiproject.poll.api.PollConstants.PERMISSION_VOTE;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PollsServiceTestConfiguration.class)
public class PollsServiceTest {

    @Autowired private PollsService pollsService;
    @Autowired private SiteService siteService;
    @Autowired private AuthzGroupService authzGroupService;
    @Autowired private SecurityService securityService;

    private Poll poll;

    @Before
    public void setUp() throws Exception {
        poll = new Poll();
        poll.setSiteId("voter-count-site");
        poll.setTypeOfAccess(Poll.Access.GROUP);
        poll.setGroupIds(Set.of("group-1"));
        when(siteService.siteReference(poll.getSiteId())).thenReturn("/site/voter-count-site");
        when(authzGroupService.getUsersIsAllowed(PERMISSION_VOTE, List.of("/site/voter-count-site")))
                .thenReturn(Set.of("instructor", "student-1", "student-2", "overlapping", "outside"));
        when(securityService.unlock("instructor", PERMISSION_ADD, "/site/voter-count-site"))
                .thenReturn(true);

        Site site = mock(Site.class);
        Group first = mock(Group.class);
        Group second = mock(Group.class);
        when(first.getId()).thenReturn("group-1");
        when(second.getId()).thenReturn("group-2");
        when(siteService.getSite(poll.getSiteId())).thenReturn(site);
        when(site.getGroupsWithMember("student-1")).thenReturn(List.of(first));
        when(site.getGroupsWithMember("student-2")).thenReturn(List.of(second));
        when(site.getGroupsWithMember("overlapping")).thenReturn(List.of(first, second));
        when(site.getGroupsWithMember("outside")).thenReturn(List.of());
        when(site.getGroupsWithMember("cannot-vote")).thenReturn(List.of(first));
    }

    @Test
    public void groupPollCountsOnlyEligibleMembersAndInstructors() {
        assertEquals(3, pollsService.getNumberUsersCanVote(poll));
    }

    @Test
    public void overlappingGroupsDoNotCountVotersTwice() {
        poll.setGroupIds(Set.of("group-1", "group-2"));
        assertEquals(4, pollsService.getNumberUsersCanVote(poll));
    }

    @Test
    public void deletedGroupDoesNotGrantAccessToOtherStudents() {
        poll.setGroupIds(Set.of("deleted-group"));
        assertEquals(1, pollsService.getNumberUsersCanVote(poll));
    }

    @Test
    public void sitePollPreservesSiteWideCount() {
        poll.setTypeOfAccess(Poll.Access.SITE);
        assertEquals(5, pollsService.getNumberUsersCanVote(poll));
        assertEquals(5, pollsService.getNumberUsersCanVote(poll.getSiteId()));
    }

    @Test
    public void publicPollPreservesPublicAccessException() {
        poll.setPublic(true);
        assertEquals(5, pollsService.getNumberUsersCanVote(poll));
    }
}
