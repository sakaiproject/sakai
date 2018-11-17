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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
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
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.api.privacy.PrivacyManager;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
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
    @Mock
    private SecurityService securityService;
    @Mock
    private PrivacyManager privacyManager;
    private UserDirectoryService userDirectoryService;
    private UserAuditRegistration userAuditRegistrationService;

    @Before
    public void setUp() throws UserNotDefinedException {
        MockitoAnnotations.initMocks(this);
        provider = new MembershipEntityProvider();
        provider.setSiteService(siteService);
        provider.setDeveloperHelperService(developerHelperService);
        provider.setUserEntityProvider(userEntityProvider);
    	provider.setSecurityService(securityService);
        userDirectoryService = Mockito.mock(UserDirectoryService.class);
        userAuditRegistrationService = Mockito.mock(UserAuditRegistration.class);
        provider.setUserDirectoryService(userDirectoryService);
        provider.setUserAuditRegistration(userAuditRegistrationService);
        provider.setPrivacyManager(privacyManager);

        User user = mock(User.class);
        when(userDirectoryService.getUser("user-foo")).thenReturn(user);
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
        Set<Member> members = new HashSet<Member>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);
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

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<Member>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);
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

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site.with.dots");
        EntityUser user = new EntityUser();
        user.setEid("user-foo");
        user.setId("user-foo");
        user.setEmail("user-foo@school.edu");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<Member>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);

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

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site.with.dots");
        EntityUser user = new EntityUser();
        user.setEid("user-foo");
        user.setId("user-foo");
        user.setEmail("user-foo@school.edu");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<Member>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);

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
        Set<Member> members = new HashSet<Member>();
        members.add(member);
        when(group.getMembers()).thenReturn(members);;

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
        Set<Member> members = new HashSet<Member>();
        members.add(member);
        when(group.getMembers()).thenReturn(members);

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
        Set<Member> members = new HashSet<Member>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);

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
        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<Member>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);
        when(site.getMember("user-foo")).thenReturn(member);

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

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site.with.dots");
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<Member>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);

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
        Set<Member> members = new HashSet<Member>();
        members.add(member);
        when(group.getMembers()).thenReturn(members);

        when(siteService.findGroup("group.with.dots")).thenReturn(group);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");
        when(developerHelperService.getCurrentUserId()).thenReturn("me");
        when(siteService.allowViewRoster("site-foo")).thenReturn(true);
        when(userEntityProvider.getUserById("user-foo")).thenReturn(user);

        List<EntityData> results = (List<EntityData>)provider.getEntities(null, search);
        assertEquals(1, results.size());
        assertEquals("user-foo::group:group.with.dots", results.get(0).getEntityId());
    }


    @Test
    public void getEntityDifferentUserNoRoster() {
        Search search = new Search();
        search.addRestriction(new Restriction(CollectionResolvable.SEARCH_USER_REFERENCE, "otherUserId"));

        User user = mock(User.class);
        when(user.getId()).thenReturn("otherUserId");

        when(developerHelperService.getCurrentUserId()).thenReturn("currentUserId");

        when(userEntityProvider.findAndCheckUserId("otherUserId", null)).thenReturn("otherUserId");

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
    public void getEntitySameUserNoRoster() {
        User user = mock(User.class);
        when(user.getId()).thenReturn("currentUserId");

        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("currentUserId");
        when(member.getUserEid()).thenReturn("currentUserEid");

        when(developerHelperService.getCurrentUserId()).thenReturn("currentUserId");

        when(userEntityProvider.findAndCheckUserId("currentUserId", null)).thenReturn("currentUserId");

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

    // we don't have a createEntityPreservesDotsInSiteIdQueryParams() test b/c passing a
    // org.sakaiproject.mock.domain.Member to createEntity() doesn't actually work.

    @Test
    public void createEntityPreservesDotsInSiteIdsInEntityMembers() throws IdUnusedException, PermissionException, UserNotDefinedException {
        EntityMember member = new EntityMember();
        member.setUserId("user-foo");
        member.setMemberRole("role-foo");
        member.setActive(true);
        member.setLocationReference("/site/site.with.dots");

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site.with.dots");
        site.setJoinable(true);

        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(userEntityProvider.findAndCheckUserId("user-foo", "user-foo")).thenReturn("user-foo");
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/me");

        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo@school.edu");
        when(userDirectoryService.getCurrentUser()).thenReturn(user);

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

        Site site = mock(Site.class);
        when(site.getId()).thenReturn("site-foo");
        site.setJoinable(true);
        Group group = mock(Group.class);
        when(group.getId()).thenReturn("group.with.dots");
        when(group.getContainingSite()).thenReturn(site);


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

        Set<Member> members = new HashSet<Member>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);

        when(siteService.getSite("site.with.dots")).thenReturn(site);
        when(userDirectoryService.getCurrentUser()).thenReturn(user);

        provider.deleteEntity(ref, new HashMap<String, Object>());

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
        EntityUser user = new EntityUser();
        user.setId("user-foo");
        user.setEid("user-foo");
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn("user-foo");
        when(member.getUserEid()).thenReturn("user-foo");
        Set<Member> members = new HashSet<Member>();
        members.add(member);
        when(site.getMembers()).thenReturn(members);

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

        EntityUser entityUser = mock(EntityUser.class);
        when(entityUser.getEid()).thenReturn("user-foo");
        when(userEntityProvider.getCurrentUser(entityView)).thenReturn(entityUser);

        Site site = mock(Site.class);
        when(site.getJoinerRole()).thenReturn("role-foo");
        when(siteService.getSite("site.with.dots")).thenReturn(site);

        assertTrue(provider.unjoinCurrentUserFromSite(entityView, new HashMap<String, Object>()));
        verify(siteService).unjoin("site.with.dots");
    }

    @Test
    public void unjoinCurrentUserToSitePreservesDotsInSiteIdPathParams_Format2() throws PermissionException, IdUnusedException {
        EntityView entityView = new EntityView("/membership/unjoin/site.with.dots.json");
        entityView.setMethod(EntityView.Method.POST);
        entityView.setViewKey(EntityView.VIEW_NEW);

        EntityUser entityUser = mock(EntityUser.class);
        when(entityUser.getEid()).thenReturn("user-foo");
        when(userEntityProvider.getCurrentUser(entityView)).thenReturn(entityUser);

        Site site = mock(Site.class);
        when(site.getJoinerRole()).thenReturn("role-foo");
        when(siteService.getSite("site.with.dots")).thenReturn(site);

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

        EntityUser entityUser = mock(EntityUser.class);
        when(entityUser.getEid()).thenReturn("user-foo");
        when(userEntityProvider.getCurrentUser(entityView)).thenReturn(entityUser);

        Site site = mock(Site.class);
        when(site.getJoinerRole()).thenReturn("role-foo");
        when(siteService.getSite("site.with.dots")).thenReturn(site);

        assertTrue(provider.unjoinCurrentUserFromSite(entityView, params));
        verify(siteService).unjoin("site.with.dots");
    }

}
