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
package org.sakaiproject.sitemanage.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzRealmLockException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.site.util.SiteGroupHelper;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * The kernel services are mocked at the boundary; groups keep real membership state so the tests
 * assert the resulting membership rather than the calls that produced it.
 */
public class RoleGroupEventWatcherTest {

    private static final String SITE_ID = "site1";
    private static final String SITE_REF = "/site/" + SITE_ID;
    private static final String TA = "Teaching Assistant";

    private RoleGroupEventWatcher watcher;
    private AuthzGroupService authzGroupService;
    private EntityManager entityManager;
    private SiteService siteService;
    private ThreadLocalManager threadLocalManager;
    private AuthzGroup realm;
    private Site site;

    @Before
    public void setUp() throws Exception {
        authzGroupService = mock(AuthzGroupService.class);
        entityManager = mock(EntityManager.class);
        siteService = mock(SiteService.class);
        threadLocalManager = mock(ThreadLocalManager.class);

        watcher = new RoleGroupEventWatcher();
        watcher.setAuthzGroupService(authzGroupService);
        watcher.setEntityManager(entityManager);
        watcher.setEventTrackingService(mock(EventTrackingService.class));
        watcher.setSecurityService(mock(SecurityService.class));
        watcher.setSiteService(siteService);
        watcher.setThreadLocalManager(threadLocalManager);

        realm = mock(AuthzGroup.class);
        when(authzGroupService.getAuthzGroup(SITE_REF)).thenReturn(realm);
        site = mock(Site.class);
        when(siteService.getSite(SITE_ID)).thenReturn(site);
    }

    @Test
    public void addsUsersWhoGainedTheRoleAndRemovesThoseWhoLostIt() throws Exception {
        Map<String, String> members = members("u1", TA, "u3", "access");
        Group group = roleGroup(members, TA);
        when(site.getGroups()).thenReturn(List.of(group));
        when(realm.getUsersHasRole(TA)).thenReturn(Set.of("u2"));

        watcher.update(null, realmEvent(SITE_REF));

        // u1 lost the role, u2 gained it, u3 was added to the group by hand with another role
        assertEquals(members("u2", TA, "u3", "access"), members);
        verify(siteService).saveGroupMembership(site);
    }

    @Test
    public void savesNothingWhenMembershipAlreadyMatches() throws Exception {
        Map<String, String> members = members("u1", TA);
        Group group = roleGroup(members, TA);
        when(site.getGroups()).thenReturn(List.of(group));
        when(realm.getUsersHasRole(TA)).thenReturn(Set.of("u1"));

        watcher.update(null, realmEvent(SITE_REF));

        assertEquals(members("u1", TA), members);
        verify(siteService, never()).saveGroupMembership(any(Site.class));
    }

    @Test
    public void skipsSitesWithoutRoleGroups() throws Exception {
        Group plainGroup = roleGroup(members());
        when(site.getGroups()).thenReturn(List.of(plainGroup));

        watcher.update(null, realmEvent(SITE_REF));

        verify(authzGroupService, never()).getAuthzGroup(anyString());
        verify(siteService, never()).saveGroupMembership(any(Site.class));
    }

    @Test
    public void groupRealmEventSyncsTheContainingSite() throws Exception {
        Map<String, String> members = members();
        Group group = roleGroup(members, TA);
        when(site.getGroups()).thenReturn(List.of(group));
        when(realm.getUsersHasRole(TA)).thenReturn(Set.of("u1"));

        watcher.update(null, realmEvent(SITE_REF + "/group/g1"));

        assertEquals(members("u1", TA), members);
    }

    @Test
    public void lockedGroupDoesNotStopOtherGroups() throws Exception {
        Group locked = roleGroup(members(), TA);
        doThrow(new AuthzRealmLockException("locked")).when(locked).insertMember(anyString(), anyString(), anyBoolean(), anyBoolean());
        Map<String, String> members = members();
        Group group = roleGroup(members, TA);
        when(site.getGroups()).thenReturn(List.of(locked, group));
        when(realm.getUsersHasRole(TA)).thenReturn(Set.of("u1"));

        watcher.update(null, realmEvent(SITE_REF));

        assertEquals(members("u1", TA), members);
        verify(siteService).saveGroupMembership(site);
    }

    @Test
    public void ignoresOtherEventsAndRealms() throws Exception {
        Event unrelated = mock(Event.class);
        when(unrelated.getEvent()).thenReturn("content.new");
        watcher.update(null, unrelated);
        watcher.update(null, realmEvent("/user/u1"));

        verify(siteService, never()).getSite(anyString());
    }

    @Test
    public void ignoresEventsFiredByItsOwnSave() throws Exception {
        when(threadLocalManager.get(anyString())).thenReturn(Boolean.TRUE);

        watcher.update(null, realmEvent(SITE_REF));

        verify(siteService, never()).getSite(anyString());
    }

    private Event realmEvent(String realmId) {
        String resource = "/realm/" + realmId;
        Event event = mock(Event.class);
        when(event.getEvent()).thenReturn(AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP);
        when(event.getResource()).thenReturn(resource);
        Reference reference = mock(Reference.class);
        when(reference.getId()).thenReturn(realmId);
        when(entityManager.newReference(resource)).thenReturn(reference);
        return event;
    }

    /** A group created in Site Info for the given roles, whose membership lives in the given map. */
    private Group roleGroup(Map<String, String> members, String... roles) throws Exception {
        Group group = mock(Group.class);
        ResourceProperties properties = mock(ResourceProperties.class);
        when(group.getProperties()).thenReturn(properties);
        when(properties.getProperty(Group.GROUP_PROP_WSETUP_CREATED)).thenReturn(Boolean.TRUE.toString());
        if (roles.length > 0) {
            when(properties.getProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID))
                    .thenReturn(SiteGroupHelper.pack(Arrays.asList(roles)));
        }
        when(group.getMembers()).thenAnswer(invocation -> members.entrySet().stream()
                .map(entry -> member(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet()));
        doAnswer(invocation -> members.remove(invocation.<String>getArgument(0)))
                .when(group).deleteMember(anyString());
        doAnswer(invocation -> members.put(invocation.getArgument(0), invocation.getArgument(1)))
                .when(group).insertMember(anyString(), anyString(), anyBoolean(), anyBoolean());
        return group;
    }

    private Member member(String userId, String roleId) {
        Member member = mock(Member.class);
        Role role = mock(Role.class);
        when(role.getId()).thenReturn(roleId);
        when(member.getUserId()).thenReturn(userId);
        when(member.getRole()).thenReturn(role);
        when(member.isActive()).thenReturn(true);
        return member;
    }

    /** userId to role id pairs. */
    private static Map<String, String> members(String... userAndRole) {
        Map<String, String> members = new HashMap<>();
        for (int i = 0; i < userAndRole.length; i += 2) {
            members.put(userAndRole[i], userAndRole[i + 1]);
        }
        return members;
    }
}
