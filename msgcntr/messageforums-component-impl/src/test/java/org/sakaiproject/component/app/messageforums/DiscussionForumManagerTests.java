/**
 * Copyright (c) 2026 The Apereo Foundation
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.HashSet;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AreaImpl;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MsgcntrTestConfiguration.class)
public class DiscussionForumManagerTests extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired private DiscussionForumManager service;
    @Autowired private MessageForumsForumManager forums;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private SessionManager sessions;
    @Autowired private SecurityService security;
    @Autowired private SiteService sites;
    @Autowired private org.sakaiproject.api.app.messageforums.PermissionLevelManager permissionLevels;
    @Autowired private org.sakaiproject.authz.api.AuthzGroupService realms;
    @Autowired private org.sakaiproject.api.common.type.TypeManager types;
    private DiscussionForum forum;

    @Before
    public void setup() {
        reset(security, sessions, sites, types, realms);
        org.sakaiproject.api.common.type.Type type = mock(org.sakaiproject.api.common.type.Type.class);
        when(type.getUuid()).thenReturn("discussion");
        when(types.createType(any(), any(), any(), any(), any())).thenReturn(type);
        when(sessions.getCurrentSessionUserId()).thenReturn("reader");
        when(sites.siteReference("site-a")).thenReturn("/site/site-a");
        org.sakaiproject.site.api.Site site = mock(org.sakaiproject.site.api.Site.class);
        when(site.getGroups()).thenReturn(java.util.List.of());
        try { when(sites.getSite("site-a")).thenReturn(site); }
        catch (org.sakaiproject.exception.IdUnusedException e) { throw new AssertionError(e); }
        Area area = new AreaImpl();
        area.setContextId("site-a");
        area.setAutoMarkThreadsRead(false);
        area.setSendToEmail(0);
        area.setTypeUuid("discussion");
        area.setName("Test area");
        area.setUuid(java.util.UUID.randomUUID().toString());
        area.setCreated(new java.util.Date());
        area.setModified(new java.util.Date());
        area.setCreatedBy("reader");
        area.setModifiedBy("reader");
        area.setHidden(false);
        area.setEnabled(true);
        area.setSendEmailOut(false);
        area.setLocked(false);
        area.setModerated(false);
        area.setPostFirst(false);
        area.setAvailabilityRestricted(false);
        area.setAvailability(true);
        area.setOpenForums(new java.util.ArrayList<>());
        sessionFactory.getCurrentSession().persist(area);
        forum = forums.createDiscussionForum();
        forum.setArea(area);
        forum.setCreatedBy("reader");
        forum.setModifiedBy("reader");
        forum.setModified(new java.util.Date());
        forum.setTitle("Restricted forum");
        forum.setSortIndex(0);
        forum.setUuid(java.util.UUID.randomUUID().toString());
        forum.setShortDescription("");
        forum.setAvailabilityRestricted(true);
        forum.setOpenDate(java.util.Date.from(Instant.now().plusSeconds(3600)));
        forum.setCloseDate(java.util.Date.from(Instant.now().plusSeconds(7200)));
        sessionFactory.getCurrentSession().persist(forum);
        sessionFactory.getCurrentSession().flush();
    }

    @Test
    public void readerCannotRemoveAvailabilityRestriction() {
        assertThrows(SecurityException.class, () -> service.updateForumAvailability("site-a", forum.getId(), false, null, null));
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        assertTrue(service.getForumById(forum.getId()).getAvailabilityRestricted());
    }

    @Test
    public void siteEditorCanUpdateAvailabilityWithoutToolPlacement() {
        when(security.unlock(SiteService.SECURE_UPDATE_SITE, "/site/site-a")).thenReturn(true);
        assertFalse(service.updateForumAvailability("site-a", forum.getId(), false, null, null).getAvailabilityRestricted());
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        assertFalse(service.getForumById(forum.getId()).getAvailabilityRestricted());
    }

    @Test
    public void customForumRoleCanEditWithoutSiteUpdateOrToolPlacement() {
        // The module's existing TestUtil uses this identity while running its wired service tests.
        when(realms.getUserRole("test-user", "/site/site-a")).thenReturn("custom-role");
        org.sakaiproject.api.app.messageforums.PermissionLevel level = permissionLevels.getDefaultOwnerPermissionLevel();
        level.setUuid(java.util.UUID.randomUUID().toString());
        level.setCreatedBy("reader");
        level.setModifiedBy("reader");
        org.sakaiproject.api.app.messageforums.DBMembershipItem membership =
            permissionLevels.createDBMembershipItem("custom-role", "Custom", org.sakaiproject.api.app.messageforums.MembershipItem.TYPE_ROLE);
        membership.setUuid(java.util.UUID.randomUUID().toString());
        membership.setCreatedBy("reader");
        membership.setModifiedBy("reader");
        ((org.sakaiproject.component.app.messageforums.dao.hibernate.DBMembershipItemImpl) membership).setForum(forum);
        membership.setPermissionLevel(level);
        sessionFactory.getCurrentSession().persist(membership);
        sessionFactory.getCurrentSession().flush();
        assertFalse(security.unlock(SiteService.SECURE_UPDATE_SITE, "/site/site-a"));
        assertFalse(service.updateForumAvailability("site-a", forum.getId(), false, null, null).getAvailabilityRestricted());
    }

    @Test
    public void editorCannotUseAnotherSiteToAuthorizeForum() {
        when(security.unlock(SiteService.SECURE_UPDATE_SITE, "/site/site-a")).thenReturn(true);
        assertThrows(SecurityException.class, () -> service.updateForumAvailability("site-b", forum.getId(), false, null, null));
        assertTrue(service.getForumById(forum.getId()).getAvailabilityRestricted());
    }
}
