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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzRealmLockException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RoleGroupEventWatcherTest {

    private static final String CURRENT_EVENT_RESOURCE_REF = "current.event.resource.ref";
    private static final String SITE_ID = "site1";
    private static final String SITE_REF = "/site/" + SITE_ID;
    private static final String REALM_REF = "/realm/" + SITE_REF;
    private static final String TA_ROLE = "Teaching Assistant";

    private RoleGroupEventWatcher watcher;
    private AuthzGroupService authzGroupService;
    private EntityManager entityManager;
    private SecurityService securityService;
    private SiteService siteService;
    private ThreadLocalManager threadLocalManager;

    private AuthzGroup realm;
    private Site site;

    @Before
    public void setUp() throws Exception {
        watcher = new RoleGroupEventWatcher();
        authzGroupService = mock(AuthzGroupService.class);
        entityManager = mock(EntityManager.class);
        securityService = mock(SecurityService.class);
        siteService = mock(SiteService.class);
        threadLocalManager = mock(ThreadLocalManager.class);
        watcher.setAuthzGroupService(authzGroupService);
        watcher.setEntityManager(entityManager);
        watcher.setEventTrackingService(mock(EventTrackingService.class));
        watcher.setSecurityService(securityService);
        watcher.setSiteService(siteService);
        watcher.setThreadLocalManager(threadLocalManager);

        realm = mock(AuthzGroup.class);
        when(authzGroupService.getAuthzGroup(SITE_REF)).thenReturn(realm);
        site = mock(Site.class);
        when(siteService.getSite(SITE_ID)).thenReturn(site);
    }

    private Event mockEvent(String function, String resource, String refId) {
        Event event = mock(Event.class);
        when(event.getEvent()).thenReturn(function);
        when(event.getResource()).thenReturn(resource);
        Reference ref = mock(Reference.class);
        when(ref.getId()).thenReturn(refId);
        when(entityManager.newReference(resource)).thenReturn(ref);
        return event;
    }

    private Group mockRoleGroup(String... roles) {
        Group group = mock(Group.class);
        ResourceProperties properties = mock(ResourceProperties.class);
        when(group.getProperties()).thenReturn(properties);
        when(properties.getProperty(Group.GROUP_PROP_WSETUP_CREATED)).thenReturn(Boolean.TRUE.toString());
        if (roles.length > 0) {
            when(properties.getProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID))
                    .thenReturn(SiteGroupHelper.pack(Arrays.asList(roles)));
        }
        return group;
    }

    private Member mockMember(String userId, String roleId) {
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn(userId);
        Role role = mock(Role.class);
        when(role.getId()).thenReturn(roleId);
        when(member.getRole()).thenReturn(role);
        return member;
    }

    @Test
    public void syncsRoleGroupOnSiteRealmUpdate() throws Exception {
        Member taMember = mockMember("u1", TA_ROLE);
        Member accessMember = mockMember("u3", "access");
        Group taGroup = mockRoleGroup(TA_ROLE);
        when(taGroup.getMembers()).thenReturn(Set.of(taMember, accessMember));
        when(realm.getUsersHasRole(TA_ROLE)).thenReturn(new LinkedHashSet<>(Arrays.asList("u1", "u2")));

        Group plainGroup = mockRoleGroup();
        when(site.getGroups()).thenReturn(Arrays.asList(taGroup, plainGroup));

        watcher.update(null, mockEvent(AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP, REALM_REF, SITE_REF));

        // members holding the role are replaced with the current site users of the role
        verify(taGroup).deleteMember("u1");
        verify(taGroup, never()).deleteMember("u3");
        verify(taGroup).insertMember("u1", TA_ROLE, true, false);
        verify(taGroup).insertMember("u2", TA_ROLE, true, false);
        verify(plainGroup, never()).insertMember(anyString(), anyString(), anyBoolean(), anyBoolean());
        verify(siteService).saveGroupMembership(site);
        verify(securityService).pushAdvisor(any(SecurityAdvisor.class));
        verify(securityService).popAdvisor(any(SecurityAdvisor.class));
        verify(threadLocalManager).set(CURRENT_EVENT_RESOURCE_REF, null);
    }

    @Test
    public void mapsGroupRealmEventToContainingSite() throws Exception {
        when(site.getGroups()).thenReturn(Collections.emptyList());

        String groupRealmRef = "/realm/" + SITE_REF + "/group/g1";
        watcher.update(null, mockEvent(AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP, groupRealmRef, SITE_REF + "/group/g1"));

        verify(authzGroupService).getAuthzGroup(SITE_REF);
        verify(siteService).getSite(SITE_ID);
        verify(siteService, never()).saveGroupMembership(any(Site.class));
    }

    @Test
    public void lockedGroupDoesNotAbortSync() throws Exception {
        Member taMember = mockMember("u1", TA_ROLE);
        Group lockedGroup = mockRoleGroup(TA_ROLE);
        when(lockedGroup.getMembers()).thenReturn(Set.of(taMember));
        doThrow(new AuthzRealmLockException("locked")).when(lockedGroup).deleteMember("u1");

        Group taGroup = mockRoleGroup(TA_ROLE);
        when(taGroup.getMembers()).thenReturn(Collections.emptySet());
        when(realm.getUsersHasRole(TA_ROLE)).thenReturn(Set.of("u2"));
        when(site.getGroups()).thenReturn(Arrays.asList(lockedGroup, taGroup));

        watcher.update(null, mockEvent(AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP, REALM_REF, SITE_REF));

        verify(taGroup).insertMember("u2", TA_ROLE, true, false);
        verify(siteService).saveGroupMembership(site);
        verify(securityService).popAdvisor(any(SecurityAdvisor.class));
    }

    @Test
    public void ignoresUnwatchedEvents() {
        Event event = mock(Event.class);
        when(event.getEvent()).thenReturn("content.new");

        watcher.update(null, event);

        verify(entityManager, never()).newReference(anyString());
    }

    @Test
    public void ignoresNonSiteRealms() throws Exception {
        watcher.update(null, mockEvent(AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP, "/realm//user/u1", "/user/u1"));

        verify(siteService, never()).getSite(anyString());
    }

    @Test
    public void skipsWhenSyncAlreadyInProgress() throws Exception {
        when(threadLocalManager.get(CURRENT_EVENT_RESOURCE_REF)).thenReturn(SITE_REF);

        watcher.update(null, mockEvent(AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP, REALM_REF, SITE_REF));

        verify(siteService, never()).getSite(anyString());
        verify(securityService, never()).pushAdvisor(any(SecurityAdvisor.class));
    }
}
