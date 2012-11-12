/**
 * $Id$
 * $URL$
 **************************************************************************
 * Copyright (c) 2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.entitybroker.providers;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.providers.model.EntityMember;
import org.sakaiproject.entitybroker.providers.model.EntityUser;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.mock.domain.*;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.BaseResourceProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MembershipEntityProviderTest {

    @Mock
    private SiteService siteService;
    @Mock
    private DeveloperHelperService developerHelperService;
    @Mock
    private UserEntityProvider userEntityProvider;
    private MembershipEntityProvider provider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        provider = new MembershipEntityProvider();
        provider.setSiteService(siteService);
        provider.setDeveloperHelperService(developerHelperService);
        provider.setUserEntityProvider(userEntityProvider);
    }

    @Test
    public void handleSiteMembershipPreservesDotsInSiteIdPathParams_GET() throws IdUnusedException {
        EntityView entityView = new EntityView("/membership/site/site.with.dots.json");
        entityView.setMethod(EntityView.Method.GET);

        Site site = new Site();
        site.setId("site.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = new Member();
        member.setUserId("user-foo");
        member.setUserEid("user-foo");
        Map<String,org.sakaiproject.authz.api.Member> members = new HashMap<String,org.sakaiproject.authz.api.Member>();
        members.put("user-foo", member);
        site.setMembers(members);
        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(siteService.allowViewRoster("site.with.dots")).thenReturn(true);
        when(userEntityProvider.getUserById("user-foo")).thenReturn(user);

        ActionReturn result =
                provider.handleSiteMemberships(entityView, new HashMap<String, Object>());
        assertEquals(1, result.getEntitiesList().size());
        assertEquals("user-foo::site:site.with.dots", result.getEntitiesList().get(0).getEntityId());
    }

    @Test
    public void handleSiteMembershipPreservesDotsInSiteIdQueryParams_GET() throws IdUnusedException {
        EntityView entityView = new EntityView("/membership/site");
        entityView.setMethod(EntityView.Method.GET);
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("siteId", "site.with.dots");

        Site site = new Site();
        site.setId("site.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = new Member();
        member.setUserId("user-foo");
        member.setUserEid("user-foo");
        Map<String,org.sakaiproject.authz.api.Member> members = new HashMap<String,org.sakaiproject.authz.api.Member>();
        members.put("user-foo", member);
        site.setMembers(members);
        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(siteService.allowViewRoster("site.with.dots")).thenReturn(true);
        when(userEntityProvider.getUserById("user-foo")).thenReturn(user);

        ActionReturn result =
                provider.handleSiteMemberships(entityView, params);
        assertEquals(1, result.getEntitiesList().size());
        assertEquals("user-foo::site:site.with.dots", result.getEntitiesList().get(0).getEntityId());
    }

    @Test
    public void handleSiteMembershipPreservesDotsInSiteIdPathParams_POST() throws IdUnusedException, PermissionException {
        EntityView entityView = new EntityView("/membership/site/site.with.dots.json");
        entityView.setMethod(EntityView.Method.POST);
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("memberRole", "role-foo");
        params.put("userSearchValues", "user-foo");

        Site site = new Site();
        site.setId("site.with.dots");
        EntityUser user = new EntityUser();
        user.setEid("user-foo");
        user.setId("user-foo");
        user.setEmail("user-foo@school.edu");
        Member member = new Member();
        member.setUserId("user-foo");
        member.setUserEid("user-foo");
        Map<String,org.sakaiproject.authz.api.Member> members = new HashMap<String,org.sakaiproject.authz.api.Member>();
        members.put("user-foo", member);
        site.setMembers(members);

        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(siteService.allowViewRoster("site.with.dots")).thenReturn(true);
        when(userEntityProvider.findUserFromSearchValue("user-foo")).thenReturn(user);
        when(userEntityProvider.getCurrentUser(null)).thenReturn(user);
        when(userEntityProvider.getUserById("user-foo")).thenReturn(user);

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
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("siteId", "site.with.dots");
        params.put("memberRole", "role-foo");
        params.put("userSearchValues", "user-foo");

        Site site = new Site();
        site.setId("site.with.dots");
        EntityUser user = new EntityUser();
        user.setEid("user-foo");
        user.setId("user-foo");
        user.setEmail("user-foo@school.edu");
        Member member = new Member();
        member.setUserId("user-foo");
        member.setUserEid("user-foo");
        Map<String,org.sakaiproject.authz.api.Member> members = new HashMap<String,org.sakaiproject.authz.api.Member>();
        members.put("user-foo", member);
        site.setMembers(members);

        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(siteService.allowViewRoster("site.with.dots")).thenReturn(true);
        when(userEntityProvider.findUserFromSearchValue("user-foo")).thenReturn(user);
        when(userEntityProvider.getCurrentUser(null)).thenReturn(user);
        when(userEntityProvider.getUserById("user-foo")).thenReturn(user);

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

        Site site = new Site();
        site.setId("site-foo");
        Group group = new Group(site);
        group.setId("group.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Map<String, org.sakaiproject.authz.api.Member> members = new HashMap<String, org.sakaiproject.authz.api.Member>();
        Member member = new Member();
        member.setUserId("user-foo");
        member.setUserEid("user-foo");
        members.put("user-foo", member);
        group.setMembers(members);

        when(siteService.findGroup("group.with.dots")).thenReturn(group);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(siteService.allowViewRoster("site-foo")).thenReturn(true);
        when(userEntityProvider.getUserById("user-foo")).thenReturn(user);

        List<EntityData> result =
                provider.getGroupMemberships(entityView, new HashMap<String, Object>());
        assertEquals(1, result.size());
        assertEquals("user-foo::group:group.with.dots", result.get(0).getEntityId());
    }

    @Test
    public void getGroupMembershipsPreservesDotsInGroupIdQueryParams_GET() {
        EntityView entityView = new EntityView("/membership/group");
        entityView.setMethod(EntityView.Method.GET);
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("groupId", "group.with.dots");

        Site site = new Site();
        site.setId("site-foo");
        Group group = new Group(site);
        group.setId("group.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Map<String, org.sakaiproject.authz.api.Member> members = new HashMap<String, org.sakaiproject.authz.api.Member>();
        Member member = new Member();
        member.setUserId("user-foo");
        member.setUserEid("user-foo");
        members.put("user-foo", member);
        group.setMembers(members);

        when(siteService.findGroup("group.with.dots")).thenReturn(group);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(siteService.allowViewRoster("site-foo")).thenReturn(true);
        when(userEntityProvider.getUserById("user-foo")).thenReturn(user);

        List<EntityData> result =
                provider.getGroupMemberships(entityView, params);
        assertEquals(1, result.size());
        assertEquals("user-foo::group:group.with.dots", result.get(0).getEntityId());
    }

    @Test
    public void getGroupMembershipsPreservesDotsInGroupIdPathParams_POST() throws PermissionException, IdUnusedException {
        EntityView entityView = new EntityView("/membership/group/group.with.dots.json");
        entityView.setMethod(EntityView.Method.POST);
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("action", "remove");
        params.put("userIds", "user-foo");

        Site site = new Site();
        site.setId("site-foo");
        Group group = new Group(site);
        group.setId("group.with.dots");
        ResourceProperties groupProperties = new BaseResourceProperties();
        groupProperties.addProperty("group_prop_wsetup_created", "true");
        group.setProperties(groupProperties);
        Map<String, org.sakaiproject.authz.api.Member> members = new HashMap<String, org.sakaiproject.authz.api.Member>();
        Member member = new Member();
        member.setUserId("user-foo");
        member.setUserEid("user-foo");
        members.put("user-foo", member);
        group.setMembers(members);

        when(siteService.findGroup("group.with.dots")).thenReturn(group);
        when(siteService.allowUpdateSite("site-foo")).thenReturn(true);
        when(userEntityProvider.findAndCheckUserId(null, "user-foo")).thenReturn("user-foo");

        List<EntityData> result =
                provider.getGroupMemberships(entityView, params);

        verify(siteService).save(site);
        assertEquals(0, group.getMembers().size());
        assertNull(result);
    }

    // groupId in query params not supported for getGroupMemberships() POSTs. So no test for that....

    // entityExists() doesn't actually do anything so no test for that...

    @Test
    public void getEntityPreservesDotsInEntityIds() throws IdUnusedException {
        Site site = new Site();
        site.setId("site.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = new Member();
        member.setUserId("user-foo");
        member.setUserEid("user-foo");
        Map<String,org.sakaiproject.authz.api.Member> members = new HashMap<String,org.sakaiproject.authz.api.Member>();
        members.put("user-foo", member);
        site.setMembers(members);

        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(developerHelperService.getCurrentUserId()).thenReturn("me");
        when(siteService.allowViewRoster("site.with.dots")).thenReturn(true);
        when(userEntityProvider.getUserById("user-foo")).thenReturn(user);

        // This is the constructor EB actually uses when building an EntityReference to pass to getEntity() for a GET
        // request. For a dotted ID to work at all, an extension would have been specified on the originally requested
        // ID, e.g. /membership/user-foo:site:site.with.dots.json. If a content type isn't specified for a dotted ID,
        // e.g. /membership/user-foo:site:site.with.dots, the AccessProvider will blow up with a 406 b/c it thinks
        // "dots" is the requested content type. As long as the content type is expected, though, it will be stripped
        // off before the ID is handed to the EntityReference constructor
        EntityMember membership =
                (EntityMember) provider.getEntity(new EntityReference("membership", "user-foo::site:site.with.dots"));
        assertEquals("user-foo::site:site.with.dots", membership.getId());
        assertEquals("user-foo", membership.getUserId());
    }

    @Test
    public void getEntitiesPreservesDotsInSiteIds() throws IdUnusedException {
        Search search = new Search();
        // Technically, just adding a content type extension, e.g. .json, would cause the location reference to
        // resolve, but nobody would actually do that b/c it doesn't affect the returned content type.
        search.addRestriction(new Restriction(CollectionResolvable.SEARCH_LOCATION_REFERENCE, "/site/site.with.dots"));

        Site site = new Site();
        site.setId("site.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = new Member();
        member.setUserId("user-foo");
        member.setUserEid("user-foo");
        Map<String,org.sakaiproject.authz.api.Member> members = new HashMap<String,org.sakaiproject.authz.api.Member>();
        members.put("user-foo", member);
        site.setMembers(members);

        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(developerHelperService.getCurrentUserId()).thenReturn("me");
        when(siteService.allowViewRoster("site.with.dots")).thenReturn(true);
        when(userEntityProvider.getUserById("user-foo")).thenReturn(user);

        List<EntityData> results = (List<EntityData>)provider.getEntities(null, search);
        assertEquals(1, results.size());
        assertEquals("user-foo::site:site.with.dots", results.get(0).getEntityId());
    }

    @Test
    public void getEntitiesPreservesDotsInGroupIds() throws IdUnusedException {
        Search search = new Search();
        // Technically, just adding a content type extension, e.g. .json, would cause the location reference to
        // resolve, but nobody would actually do that b/c it doesn't affect the returned content type.
        search.addRestriction(new Restriction(CollectionResolvable.SEARCH_LOCATION_REFERENCE, "/group/group.with.dots"));

        Site site = new Site();
        site.setId("site-foo");
        Group group = new Group(site);
        group.setId("group.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Map<String, org.sakaiproject.authz.api.Member> members = new HashMap<String, org.sakaiproject.authz.api.Member>();
        Member member = new Member();
        member.setUserId("user-foo");
        member.setUserEid("user-foo");
        members.put("user-foo", member);
        group.setMembers(members);

        when(siteService.findGroup("group.with.dots")).thenReturn(group);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(developerHelperService.getCurrentUserId()).thenReturn("me");
        when(siteService.allowViewRoster("site-foo")).thenReturn(true);
        when(userEntityProvider.getUserById("user-foo")).thenReturn(user);

        List<EntityData> results = (List<EntityData>)provider.getEntities(null, search);
        assertEquals(1, results.size());
        assertEquals("user-foo::group:group.with.dots", results.get(0).getEntityId());
    }

    // we don't have a createEntityPreservesDotsInSiteIdQueryParams() test b/c passing a
    // org.sakaiproject.mock.domain.Member to createEntity() doesn't actually work.

    @Test
    public void createEntityPreservesDotsInSiteIdsInEntityMembers() throws IdUnusedException, PermissionException {
        EntityMember member = new EntityMember();
        member.setUserId("user-foo");
        member.setMemberRole("role-foo");
        member.setActive(true);
        member.setLocationReference("/site/site.with.dots");

        Site site = new Site();
        site.setId("site.with.dots");
        site.setJoinable(true);

        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(userEntityProvider.findAndCheckUserId("user-foo", "user-foo")).thenReturn("user-foo");
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");

        String entityId = provider.createEntity(null, member, new HashMap<String, Object>());

        verify(siteService).saveSiteMembership(site);

        assertEquals("user-foo::site:site.with.dots", entityId);
    }

    @Test
    public void createEntityPreservesDotsInGroupIdsInEntityMembers() throws IdUnusedException, PermissionException {
        EntityMember member = new EntityMember();
        member.setUserId("user-foo");
        member.setMemberRole("role-foo");
        member.setActive(true);
        member.setLocationReference("/group/group.with.dots");

        Site site = new Site();
        site.setId("site-foo");
        site.setJoinable(true);
        Group group = new Group(site);
        group.setId("group.with.dots");


        when(siteService.findGroup("group.with.dots")).thenReturn(group);
        when(userEntityProvider.findAndCheckUserId("user-foo", "user-foo")).thenReturn("user-foo");
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");

        String entityId = provider.createEntity(null, member, new HashMap<String, Object>());

        verify(siteService).saveGroupMembership(site);

        assertEquals("user-foo::group:group.with.dots", entityId);
    }

    @Test
    public void deleteEntityPreservesDotsInSiteIds() throws IdUnusedException, PermissionException {
        EntityReference ref = new EntityReference("membership", "user-foo::site:site.with.dots");

        Site site = new Site();
        site.setId("site.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = new Member();
        member.setUserId("user-foo");
        member.setUserEid("user-foo");
        Map<String,org.sakaiproject.authz.api.Member> members = new HashMap<String,org.sakaiproject.authz.api.Member>();
        members.put("user-foo", member);
        site.setMembers(members);

        when(siteService.getSite("site.with.dots")).thenReturn(site);

        provider.deleteEntity(ref, new HashMap<String, Object>());

        verify(siteService).saveSiteMembership(site);
        assertEquals(0, site.getMembers().size());

    }

    @Test
    public void deleteEntityPreservesDotsInGroupIds() throws PermissionException, IdUnusedException {
        EntityReference ref = new EntityReference("membership", "user-foo::group:group.with.dots");

        Site site = new Site();
        site.setId("site-foo");
        Group group = new Group(site);
        group.setId("group.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Map<String, org.sakaiproject.authz.api.Member> members = new HashMap<String, org.sakaiproject.authz.api.Member>();
        Member member = new Member();
        member.setUserId("user-foo");
        member.setUserEid("user-foo");
        members.put("user-foo", member);
        group.setMembers(members);

        when(siteService.findGroup("group.with.dots")).thenReturn(group);

        provider.deleteEntity(ref, new HashMap<String, Object>());

        verify(siteService).saveGroupMembership(site);
        assertEquals(0, group.getMembers().size());
    }

    @Test
    public void joinCurrentUserToSitePreservesDotsInSiteIdPathParams_Format1() throws PermissionException, IdUnusedException {
        EntityView entityView = new EntityView("/membership/join/site/site.with.dots.json");
        entityView.setMethod(EntityView.Method.POST);
        entityView.setViewKey(EntityView.VIEW_NEW);

        assertTrue(provider.joinCurrentUserToSite(entityView, new HashMap<String, Object>()));
        verify(siteService).join("site.with.dots");
    }

    @Test
    public void joinCurrentUserToSitePreservesDotsInSiteIdPathParams_Format2() throws PermissionException, IdUnusedException {
        EntityView entityView = new EntityView("/membership/join/site.with.dots.json");
        entityView.setMethod(EntityView.Method.POST);
        entityView.setViewKey(EntityView.VIEW_NEW);

        assertTrue(provider.joinCurrentUserToSite(entityView, new HashMap<String, Object>()));
        verify(siteService).join("site.with.dots");
    }

    @Test
    public void joinCurrentUserToSitePreservesDotsInSiteIdQueryParams() throws PermissionException, IdUnusedException {
        EntityView entityView = new EntityView("/membership/join");
        entityView.setMethod(EntityView.Method.POST);
        entityView.setViewKey(EntityView.VIEW_NEW);
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("siteId", "site.with.dots");

        assertTrue(provider.joinCurrentUserToSite(entityView, params));
        verify(siteService).join("site.with.dots");
    }

    @Test
    public void unjoinCurrentUserToSitePreservesDotsInSiteIdPathParams_Format1() throws PermissionException, IdUnusedException {
        EntityView entityView = new EntityView("/membership/unjoin/site/site.with.dots.json");
        entityView.setMethod(EntityView.Method.POST);
        entityView.setViewKey(EntityView.VIEW_NEW);

        assertTrue(provider.unjoinCurrentUserFromSite(entityView, new HashMap<String, Object>()));
        verify(siteService).unjoin("site.with.dots");
    }

    @Test
    public void unjoinCurrentUserToSitePreservesDotsInSiteIdPathParams_Format2() throws PermissionException, IdUnusedException {
        EntityView entityView = new EntityView("/membership/unjoin/site.with.dots.json");
        entityView.setMethod(EntityView.Method.POST);
        entityView.setViewKey(EntityView.VIEW_NEW);

        assertTrue(provider.unjoinCurrentUserFromSite(entityView, new HashMap<String, Object>()));
        verify(siteService).unjoin("site.with.dots");
    }

    @Test
    public void unjoinCurrentUserToSitePreservesDotsInSiteIdQueryParams() throws PermissionException, IdUnusedException {
        EntityView entityView = new EntityView("/membership/unjoin");
        entityView.setMethod(EntityView.Method.POST);
        entityView.setViewKey(EntityView.VIEW_NEW);
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("siteId", "site.with.dots");

        assertTrue(provider.unjoinCurrentUserFromSite(entityView, params));
        verify(siteService).unjoin("site.with.dots");
    }

}