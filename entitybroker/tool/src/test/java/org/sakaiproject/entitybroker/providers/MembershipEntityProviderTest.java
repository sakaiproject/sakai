/**
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
package org.sakaiproject.entitybroker.providers;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.config.EntityRestTestConfiguration;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.providers.model.EntityMember;
import org.sakaiproject.entitybroker.providers.model.EntityUser;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.BaseResourceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {EntityRestTestConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class MembershipEntityProviderTest {

    @Autowired private SiteService siteService;
    @Autowired private UserEntityProvider userEntityProvider;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private MembershipEntityProvider provider;

    private DeveloperHelperService developerHelperService;

    @Before
    public void setUp() throws UserNotDefinedException {
        reset(siteService);
        developerHelperService = mock(DeveloperHelperService.class);
        provider.setDeveloperHelperService(developerHelperService);
        userEntityProvider.setDeveloperHelperService(developerHelperService);

        User user = mock(User.class);
        when(user.getId()).thenReturn("user-foo");
        when(user.getProperties()).thenReturn(new BaseResourceProperties());
        when(userDirectoryService.getUser("user-foo")).thenReturn(user);
        when(userDirectoryService.getUserByEid("user-foo")).thenReturn(user);
        when(userDirectoryService.getUserByAid("user-foo")).thenReturn(user);
        when(userDirectoryService.getCurrentUser()).thenReturn(user);
    }

    @Test
    public void handleSiteMembershipPreservesDotsInSiteIdPathParams_GET() throws IdUnusedException {
        EntityView entityView = new EntityView("/membership/site/site.with.dots.json");
        entityView.setMethod(EntityView.Method.GET);

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);
        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(siteService.allowViewRoster("site.with.dots")).thenReturn(true);

        ActionReturn result = provider.handleSiteMemberships(entityView, new HashMap<>());
        assertEquals(1, result.getEntitiesList().size());
        assertEquals("user-foo::site:site.with.dots", result.getEntitiesList().get(0).getEntityId());
    }

    @Test
    public void handleSiteMembershipPreservesDotsInSiteIdQueryParams_GET() throws IdUnusedException {
        EntityView entityView = new EntityView("/membership/site");
        entityView.setMethod(EntityView.Method.GET);
        Map<String,Object> params = new HashMap<>();
        params.put("siteId", "site.with.dots");

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);
        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(siteService.allowViewRoster("site.with.dots")).thenReturn(true);

        ActionReturn result =
                provider.handleSiteMemberships(entityView, params);
        assertEquals(1, result.getEntitiesList().size());
        assertEquals("user-foo::site:site.with.dots", result.getEntitiesList().get(0).getEntityId());
    }

    @Test
    public void handleSiteMembershipPreservesDotsInSiteIdPathParams_POST() throws IdUnusedException, PermissionException {
        EntityView entityView = new EntityView("/membership/site/site.with.dots.json");
        entityView.setMethod(EntityView.Method.POST);
        Map<String,Object> params = new HashMap<>();
        params.put("memberRole", "role-foo");
        params.put("userSearchValues", "user-foo");

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site.with.dots");
        EntityUser user = new EntityUser();
        user.setEid("user-foo");
        user.setId("user-foo");
        user.setEmail("user-foo@school.edu");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);

        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(siteService.allowViewRoster("site.with.dots")).thenReturn(true);

        ActionReturn result =
                provider.handleSiteMemberships(entityView, params);
        assertEquals(1, result.getEntitiesList().size());
        assertEquals("user-foo::site:site.with.dots", result.getEntitiesList().get(0).getEntityId());
        verify(siteService).saveSiteMembership(site);
    }

    @Test
    public void handleSiteMembershipPreservesDotsInSiteIdQueryParams_POST() throws IdUnusedException, PermissionException {
        EntityView entityView = new EntityView("/membership/site");
        entityView.setMethod(EntityView.Method.POST);
        Map<String,Object> params = new HashMap<>();
        params.put("siteId", "site.with.dots");
        params.put("memberRole", "role-foo");
        params.put("userSearchValues", "user-foo");

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site.with.dots");
        EntityUser user = new EntityUser();
        user.setEid("user-foo");
        user.setId("user-foo");
        user.setEmail("user-foo@school.edu");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);

        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(siteService.allowViewRoster("site.with.dots")).thenReturn(true);

        ActionReturn result =
                provider.handleSiteMemberships(entityView, params);
        assertEquals(1, result.getEntitiesList().size());
        assertEquals("user-foo::site:site.with.dots", result.getEntitiesList().get(0).getEntityId());
        verify(siteService).saveSiteMembership(site);
    }

    @Test
    public void getGroupMembershipsPreservesDotsInGroupIdPathParams_GET() {
        EntityView entityView = new EntityView("/membership/group/group.with.dots.json");
        entityView.setMethod(EntityView.Method.GET);

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site-foo");
        Group group = mock(Group.class);
        when(group.getId()).thenReturn("group.with.dots");
        when(group.getContainingSite()).thenReturn(site);
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<>();
        members.add(member);
        when(group.getMembers()).thenReturn(members);

        when(siteService.findGroup("group.with.dots")).thenReturn(group);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(siteService.allowViewRoster("site-foo")).thenReturn(true);

        List<EntityData> result =
                provider.getGroupMemberships(entityView, new HashMap<>());
        assertEquals(1, result.size());
        assertEquals("user-foo::group:group.with.dots", result.get(0).getEntityId());
    }

    @Test
    public void getGroupMembershipsPreservesDotsInGroupIdQueryParams_GET() {
        EntityView entityView = new EntityView("/membership/group");
        entityView.setMethod(EntityView.Method.GET);
        Map<String,Object> params = new HashMap<>();
        params.put("groupId", "group.with.dots");

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site-foo");
        Group group = mock(Group.class);
        when(group.getId()).thenReturn("group.with.dots");
        when(group.getContainingSite()).thenReturn(site);
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<>();
        members.add(member);
        when(group.getMembers()).thenReturn(members);

        when(siteService.findGroup("group.with.dots")).thenReturn(group);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(siteService.allowViewRoster("site-foo")).thenReturn(true);

        List<EntityData> result = provider.getGroupMemberships(entityView, params);
        assertEquals(1, result.size());
        assertEquals("user-foo::group:group.with.dots", result.get(0).getEntityId());
    }

    @Test
    public void getGroupMembershipsPreservesDotsInGroupIdPathParams_POST() throws PermissionException, IdUnusedException {
        EntityView entityView = new EntityView("/membership/group/group.with.dots.json");
        entityView.setMethod(EntityView.Method.POST);
        Map<String,Object> params = new HashMap<>();
        params.put("action", "remove");
        params.put("userIds", "user-foo");

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site-foo");
        Group group = mock(Group.class);
        when(group.getId()).thenReturn("group.with.dots");
        when(group.getContainingSite()).thenReturn(site);
        ResourceProperties groupProperties = new BaseResourceProperties();
        groupProperties.addProperty("group_prop_wsetup_created", "true");
        when(group.getProperties()).thenReturn(groupProperties);
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);

        when(siteService.findGroup("group.with.dots")).thenReturn(group);
        when(siteService.allowUpdateSite("site-foo")).thenReturn(true);

        List<EntityData> result =
                provider.getGroupMemberships(entityView, params);

        verify(siteService).save(site);
        assertEquals(0, group.getMembers().size());
        assertNull(result);
    }

    // groupId in query params not supported for getGroupMemberships() POSTs.

    // entityExists() doesn't actually do anything, so no test for that...

    @Test
    public void getEntityPreservesDotsInEntityIds() throws IdUnusedException {
        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);
        when(site.getMember("user-foo")).thenReturn(member);

        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(developerHelperService.getCurrentUserId()).thenReturn("me");
        when(siteService.allowViewRoster("site.with.dots")).thenReturn(true);

        // This is the constructor EB actually uses when building an EntityReference to pass to getEntity() for a GET
        // request. For a dotted ID to work at all, an extension would have been specified on the originally requested
        // ID, e.g., /membership/user-foo:site:site.with.dots.json. If a content type isn't specified for a dotted ID
        // e.g., /membership/user-foo:site:site.with.dots, the AccessProvider will blow up with a 406 b/c it thinks
        // "dots" is the requested content type. As long as the content type is expected, though, it will be stripped
        // off before the ID is handed to the EntityReference constructor
        EntityMember membership = (EntityMember) provider.getEntity(new EntityReference("membership", "user-foo::site:site.with.dots"));
        assertEquals("user-foo::site:site.with.dots", membership.getId());
        assertEquals("user-foo", membership.getUserId());
    }

    @Test
    public void getEntitiesPreservesDotsInSiteIds() throws IdUnusedException, UserNotDefinedException {
        Search search = new Search();
        // Technically, just adding a content type extension, e.g., .json, would cause the location reference to
        // resolve, but nobody would actually do that b/c it doesn't affect the returned content type.
        search.addRestriction(new Restriction(CollectionResolvable.SEARCH_LOCATION_REFERENCE, "/site/site.with.dots"));

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site.with.dots");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);

        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(developerHelperService.getCurrentUserId()).thenReturn("me");
        when(siteService.allowViewRoster("site.with.dots")).thenReturn(true);

        List<EntityData> results = provider.getEntities(null, search);
        assertEquals(1, results.size());
        assertEquals("user-foo::site:site.with.dots", results.get(0).getEntityId());
    }

    @Test
    public void getEntitiesPreservesDotsInGroupIds() {
        Search search = new Search();
        // Technically, just adding a content type extension, e.g., .json, would cause the location reference to
        // resolve, but nobody would actually do that b/c it doesn't affect the returned content type.
        search.addRestriction(new Restriction(CollectionResolvable.SEARCH_LOCATION_REFERENCE, "/group/group.with.dots"));

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site-foo");
        Group group = mock(Group.class);
        when(group.getId()).thenReturn("group.with.dots");
        when(group.getContainingSite()).thenReturn(site);
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<>();
        members.add(member);
        when(group.getMembers()).thenReturn(members);

        when(siteService.findGroup("group.with.dots")).thenReturn(group);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(developerHelperService.getCurrentUserId()).thenReturn("me");
        when(siteService.allowViewRoster("site-foo")).thenReturn(true);

        List<EntityData> results = provider.getEntities(null, search);
        assertEquals(1, results.size());
        assertEquals("user-foo::group:group.with.dots", results.get(0).getEntityId());
    }


    @Test
    public void getEntityDifferentUserNoRoster() throws UserNotDefinedException {
        Search search = new Search();
        search.addRestriction(new Restriction(CollectionResolvable.SEARCH_USER_REFERENCE, "otherUserId"));

        User user = mock(User.class);
        when(user.getId()).thenReturn("otherUserId");
        when(userDirectoryService.getUserByAid("otherUserId")).thenReturn(user);

        when(developerHelperService.getCurrentUserId()).thenReturn("currentUserId");

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("siteId");
        when(siteService.getUserSites(false, "otherUserId")).thenReturn(Collections.singletonList(site));
        when(siteService.allowViewRoster("siteId")).thenReturn(false);

        List<EntityData> entities = provider.getEntities(null, search);
        assertEquals(0, entities.size());
    }

    @Test
    public void getEntityDifferentUserWithRoster() {
        Search search = new Search();
        search.addRestriction(new Restriction(CollectionResolvable.SEARCH_USER_REFERENCE, "otherUserId"));

        User user = mock(User.class);
        when(user.getId()).thenReturn("otherUserId");

        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("otherUserId");
        when(member.getUserEid()).thenReturn("otherUserEid");

        when(developerHelperService.getCurrentUserId()).thenReturn("currentUserId");

        when(userEntityProvider.findAndCheckUserId("otherUserId", null)).thenReturn("otherUserId");

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("siteId");
        when(site.getReference()).thenReturn("/site/siteId");
        when(site.getType()).thenReturn("test");
        when(site.getMember("otherUserId")).thenReturn(member);
        when(siteService.getUserSites(false, "otherUserId")).thenReturn(Collections.singletonList(site));
        when(siteService.allowViewRoster("siteId")).thenReturn(true);

        List<EntityData> entities = provider.getEntities(null, search);
        assertEquals(1, entities.size());
        assertEquals("otherUserId::site:siteId", entities.get(0).getEntityId());
    }

    @Test
    public void getEntitySameUserNoRoster() throws UserNotDefinedException {
        User user = mock(User.class);
        when(user.getId()).thenReturn("currentUserId");
        when(userDirectoryService.getUserByAid("currentUserId")).thenReturn(user);

        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("currentUserId");
        when(member.getUserEid()).thenReturn("currentUserEid");

        when(developerHelperService.getCurrentUserId()).thenReturn("currentUserId");

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("siteId");
        when(site.getReference()).thenReturn("/site/siteId");
        when(site.getType()).thenReturn("test");
        when(site.getMember("currentUserId")).thenReturn(member);
        when(siteService.getUserSites(false, "currentUserId")).thenReturn(Collections.singletonList(site));
        // Check even when you don't have permission to view roster you still can see your own membership
        when(siteService.allowViewRoster("siteId")).thenReturn(false);

        List<EntityData> entities = provider.getEntities(null, null);
        assertEquals(1, entities.size());
        assertEquals("currentUserId::site:siteId", entities.get(0).getEntityId());
    }

    // we don't have a createEntityPreservesDotsInSiteIdQueryParams() test b/c passing an
    // org.sakaiproject.mock.domain.Member to createEntity() doesn't actually work.

    @Test
    public void createEntityPreservesDotsInSiteIdsInEntityMembers() throws IdUnusedException, PermissionException {
        EntityMember member = new EntityMember();
        member.setUserId("user-foo");
        member.setMemberRole("role-foo");
        member.setActive(true);
        member.setLocationReference("/site/site.with.dots");

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site.with.dots");
        site.setJoinable(true);

        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");

        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo@school.edu");

        String entityId = provider.createEntity(null, member, new HashMap<>());

        verify(siteService).saveSiteMembership(site);

        assertEquals("user-foo::site:site.with.dots", entityId);
    }

    @Test
    public void createEntityPreservesDotsInGroupIdsInEntityMembers() throws UserNotDefinedException, IdUnusedException, PermissionException {
        EntityMember member = new EntityMember();
        member.setUserId("user-foo");
        member.setMemberRole("role-foo");
        member.setActive(true);
        member.setLocationReference("/group/group.with.dots");

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site-foo");
        site.setJoinable(true);
        Group group = mock(Group.class);
        when(group.getId()).thenReturn("group.with.dots");
        when(group.getContainingSite()).thenReturn(site);


        when(siteService.findGroup("group.with.dots")).thenReturn(group);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");

        String entityId = provider.createEntity(null, member, new HashMap<>());

        verify(siteService).saveGroupMembership(site);

        assertEquals("user-foo::group:group.with.dots", entityId);
    }

    @Test
    public void deleteEntityPreservesDotsInSiteIds() throws IdUnusedException, PermissionException {
        EntityReference ref = new EntityReference("membership", "user-foo::site:site.with.dots");

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Role role = mock(Role.class);
        when(role.getId()).thenReturn("role-foo");
        when(site.getUserRole("user-foo")).thenReturn(role);

        Set<Member> members = new HashSet<>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);

        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(userDirectoryService.getCurrentUser()).thenReturn(user);

        provider.deleteEntity(ref, new HashMap<>());

        verify(siteService).saveSiteMembership(site);
        verify(site).removeMember("user-foo");
    }

    @Test
    public void deleteEntityPreservesDotsInGroupIds() throws PermissionException, IdUnusedException {
        EntityReference ref = new EntityReference("membership", "user-foo::group:group.with.dots");

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site-foo");
        Group group = mock(Group.class);
        when(group.getId()).thenReturn("group.with.dots");
        when(group.getContainingSite()).thenReturn(site);
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);

        when(siteService.findGroup("group.with.dots")).thenReturn(group);

        provider.deleteEntity(ref, new HashMap<>());

        verify(siteService).saveGroupMembership(site);
        assertEquals(0, group.getMembers().size());
    }

    @Test
    public void joinCurrentUserToSitePreservesDotsInSiteIdPathParams_Format1() throws PermissionException, IdUnusedException {
        EntityView entityView = new EntityView("/membership/join/site/site.with.dots.json");
        entityView.setMethod(EntityView.Method.POST);
        entityView.setViewKey(EntityView.VIEW_NEW);

        assertTrue(provider.joinCurrentUserToSite(entityView, new HashMap<>()));
        verify(siteService).join("site.with.dots");
    }

    @Test
    public void joinCurrentUserToSitePreservesDotsInSiteIdPathParams_Format2() throws PermissionException, IdUnusedException {
        EntityView entityView = new EntityView("/membership/join/site.with.dots.json");
        entityView.setMethod(EntityView.Method.POST);
        entityView.setViewKey(EntityView.VIEW_NEW);

        assertTrue(provider.joinCurrentUserToSite(entityView, new HashMap<>()));
        verify(siteService).join("site.with.dots");
    }

    @Test
    public void joinCurrentUserToSitePreservesDotsInSiteIdQueryParams() throws PermissionException, IdUnusedException {
        EntityView entityView = new EntityView("/membership/join");
        entityView.setMethod(EntityView.Method.POST);
        entityView.setViewKey(EntityView.VIEW_NEW);
        Map<String,Object> params = new HashMap<>();
        params.put("siteId", "site.with.dots");

        assertTrue(provider.joinCurrentUserToSite(entityView, params));
        verify(siteService).join("site.with.dots");
    }

    @Test
    public void unjoinCurrentUserToSitePreservesDotsInSiteIdPathParams_Format1() throws PermissionException, IdUnusedException {
        EntityView entityView = new EntityView("/membership/unjoin/site/site.with.dots.json");
        entityView.setMethod(EntityView.Method.POST);
        entityView.setViewKey(EntityView.VIEW_NEW);

        EntityUser entityUser = mock(EntityUser.class);
        when(entityUser.getEid()).thenReturn("user-foo");

        Site site = mock(Site.class);
        when(site.getJoinerRole()).thenReturn("role-foo");
        when(siteService.getSite("site.with.dots")).thenReturn(site);

        assertTrue(provider.unjoinCurrentUserFromSite(entityView, new HashMap<>()));
        verify(siteService).unjoin("site.with.dots");
    }

    @Test
    public void unjoinCurrentUserToSitePreservesDotsInSiteIdPathParams_Format2() throws PermissionException, IdUnusedException {
        EntityView entityView = new EntityView("/membership/unjoin/site.with.dots.json");
        entityView.setMethod(EntityView.Method.POST);
        entityView.setViewKey(EntityView.VIEW_NEW);

        EntityUser entityUser = mock(EntityUser.class);
        when(entityUser.getEid()).thenReturn("user-foo");

        Site site = mock(Site.class);
        when(site.getJoinerRole()).thenReturn("role-foo");
        when(siteService.getSite("site.with.dots")).thenReturn(site);

        assertTrue(provider.unjoinCurrentUserFromSite(entityView, new HashMap<>()));
        verify(siteService).unjoin("site.with.dots");
    }

    @Test
    public void unjoinCurrentUserToSitePreservesDotsInSiteIdQueryParams() throws PermissionException, IdUnusedException {
        EntityView entityView = new EntityView("/membership/unjoin");
        entityView.setMethod(EntityView.Method.POST);
        entityView.setViewKey(EntityView.VIEW_NEW);
        Map<String,Object> params = new HashMap<>();
        params.put("siteId", "site.with.dots");

        EntityUser entityUser = mock(EntityUser.class);
        when(entityUser.getEid()).thenReturn("user-foo");

        Site site = mock(Site.class);
        when(site.getJoinerRole()).thenReturn("role-foo");
        when(siteService.getSite("site.with.dots")).thenReturn(site);

        assertTrue(provider.unjoinCurrentUserFromSite(entityView, params));
        verify(siteService).unjoin("site.with.dots");
    }

}
