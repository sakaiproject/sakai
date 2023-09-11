/*
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
package org.sakaiproject.portal.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.model.PinnedSite;
import org.sakaiproject.portal.api.repository.PinnedSiteRepository;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiTests;
import org.sakaiproject.tool.api.SessionManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;

import org.hibernate.SessionFactory;

import static org.mockito.Mockito.*;

import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PortalTestConfiguration.class})
public class PortalServiceTests extends SakaiTests {

    @Autowired private MemoryService memoryService;
    @Autowired private PinnedSiteRepository pinnedSiteRepository;
    @Autowired private PortalService portalService;
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private ServerConfigurationService serverConfigurationService;

    @Before
    public void setup() {

        super.setup();
        when(serverConfigurationService.getString("sakai.xml.sax.parser", "com.sun.org.apache.xerces.internal.parsers.SAXParser")).thenReturn("com.sun.org.apache.xerces.internal.parsers.SAXParser");
    }

    @Test
    public void pinnedSites() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);

        String user1SiteId = "~user1";

        when(siteService.getUserSiteId(user1)).thenReturn(user1SiteId);

        List<String> siteIds = new ArrayList<>();
        siteIds.add(site1Id);
        siteIds.add("site2");

        portalService.savePinnedSites(siteIds);

        assertEquals(2, portalService.getPinnedSites().size());

        String site3Id = "site3";

        Site site3 = mock(Site.class);
        when(site3.isPublished()).thenReturn(false);
        try {
            when(siteService.getSite(site3Id)).thenReturn(site3);
        } catch (IdUnusedException idue) {
            System.out.println(idue.toString());
        }

        Event event = mock(Event.class);

        when(event.getEvent()).thenReturn(SiteService.EVENT_USER_SITE_MEMBERSHIP_ADD);
        when(event.getContext()).thenReturn(site3Id);
        when(event.getResource()).thenReturn("uid=user1;role=access;active=true;siteId=site3");
        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, event);

        assertEquals(2, portalService.getPinnedSites().size());

        when(site3.isPublished()).thenReturn(true);
        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, event);
        assertEquals(3, portalService.getPinnedSites().size());

        when(event.getContext()).thenReturn(site1Id);
        when(event.getEvent()).thenReturn(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP);

        Set<String> user2Set = new HashSet<>();
        user2Set.add(user2);

        // Simulate user1 having been removed from site1
        when(site1.getUsers()).thenReturn(user2Set);
        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, event);

        assertEquals(2, portalService.getPinnedSites().size());

        when(event.getEvent()).thenReturn(SiteService.SOFT_DELETE_SITE);

        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, event);

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user2);
        assertEquals(0, portalService.getPinnedSites().size());
    }

    @Test
    public void pinnedSites2() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);

        List<String> siteIds = new ArrayList<>();
        siteIds.add(site1Id);

        Set<String> users = new HashSet<>();
        users.add(user1);

        Member member1 = mock(Member.class);
        when(member1.getUserId()).thenReturn(user1);
        Set<Member> members = new HashSet<>();
        members.add(member1);

        Site site1 = mock(Site.class);
        when(site1.getUsers()).thenReturn(users);
        when(site1.getMembers()).thenReturn(members);
        when(site1.isPublished()).thenReturn(false);
        try {
            when(siteService.getSite(site1Id)).thenReturn(site1);
        } catch (IdUnusedException idue) {
            System.out.println(idue.toString());
        }

        Event event = mock(Event.class);

        when(event.getEvent()).thenReturn(SiteService.EVENT_USER_SITE_MEMBERSHIP_ADD);
        when(event.getContext()).thenReturn(site1Id);
        when(event.getResource()).thenReturn("uid=" + user1 + ";role=access;active=true;siteId=" + site1Id);
        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, event);

        assertEquals(0, portalService.getPinnedSites().size());

        when(site1.isPublished()).thenReturn(true);
        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, event);

        assertEquals(1, portalService.getPinnedSites().size());

        when(event.getEvent()).thenReturn(SiteService.EVENT_SITE_UNPUBLISH);
        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, event);
        assertEquals(0, portalService.getPinnedSites().size());

        when(event.getEvent()).thenReturn(SiteService.EVENT_SITE_PUBLISH);
        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, event);
        assertEquals(1, portalService.getPinnedSites().size());
    }

    @Test
    public void pinnedSitePosition() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);

        String site2Id = "site2";
        String site3Id = "site3";
        String site4Id = "site4";

        List<String> siteIds = new ArrayList<>();
        siteIds.add(site1Id);
        siteIds.add(site2Id);
        siteIds.add(site3Id);

        portalService.savePinnedSites(siteIds);

        List<String> pinnedSites = portalService.getPinnedSites();
        assertEquals(3, pinnedSites.size());

        assertEquals(site1Id, pinnedSites.get(0));
        assertEquals(site2Id, pinnedSites.get(1));
        assertEquals(site3Id, pinnedSites.get(2));

        portalService.removePinnedSite(user1, site2Id);
        pinnedSites = portalService.getPinnedSites();
        assertEquals(2, pinnedSites.size());

        List<PinnedSite> pinned = pinnedSiteRepository.findByUserIdOrderByPosition(user1);
        assertEquals(2, pinned.size());
        assertEquals(0, pinned.get(0).getPosition());
        assertEquals(site1Id, pinned.get(0).getSiteId());
        assertEquals(1, pinned.get(1).getPosition());
        assertEquals(site3Id, pinned.get(1).getSiteId());

        portalService.addPinnedSite(user1, site4Id);

        pinned = pinnedSiteRepository.findByUserIdOrderByPosition(user1);
        assertEquals(3, pinned.size());
        assertEquals(2, pinned.get(2).getPosition());
        assertEquals(site4Id, pinned.get(2).getSiteId());

        siteIds.add(site4Id);
        portalService.savePinnedSites(siteIds);
        pinned = pinnedSiteRepository.findByUserIdOrderByPosition(user1);
        assertEquals(4, pinned.size());
        assertEquals(site2Id, pinned.get(3).getSiteId());
    }

    @Test
    public void pinnedQueries() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);

        String site2Id = "site2";
        String site3Id = "site3";
        String site4Id = "site4";
        String site5Id = "site5";
        String site6Id = "site6";
        String site7Id = "site7";
        String site8Id = "site8";

        List<String> siteIds = new ArrayList<>();
        siteIds.add(site1Id);
        siteIds.add(site2Id);
        siteIds.add(site3Id);
        siteIds.add(site4Id);
        siteIds.add(site5Id);
        siteIds.add(site6Id);
        siteIds.add(site7Id);
        siteIds.add(site8Id);

        portalService.savePinnedSites(siteIds);
    }

    @Test
    public void recentSites() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);

        portalService.addRecentSite(site1Id);
        assertEquals(1, portalService.getRecentSites().size());

        portalService.addRecentSite("site2");
        assertEquals(2, portalService.getRecentSites().size());

        portalService.addRecentSite("site3");
        assertEquals(3, portalService.getRecentSites().size());

        portalService.addRecentSite(SiteService.SITE_ERROR);
        assertEquals(3, portalService.getRecentSites().size());

        Iterator<String> recentSites = portalService.getRecentSites().iterator();
        assertEquals("site3", recentSites.next());
        assertEquals("site2", recentSites.next());
        assertEquals("site1", recentSites.next());

        portalService.addRecentSite("site4");
        assertEquals(3, portalService.getRecentSites().size());

        recentSites = portalService.getRecentSites().iterator();
        assertEquals("site4", recentSites.next());
        assertEquals("site3", recentSites.next());
        assertEquals("site2", recentSites.next());

        portalService.addRecentSite(site1Id);
        assertEquals(3, portalService.getRecentSites().size());

        recentSites = portalService.getRecentSites().iterator();
        assertEquals("site1", recentSites.next());
        assertEquals("site4", recentSites.next());
        assertEquals("site3", recentSites.next());

        Event event = mock(Event.class);
        when(event.getContext()).thenReturn(site1Id);
        when(event.getEvent()).thenReturn(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP);

        Set<String> user2Set = new HashSet<>();
        user2Set.add(user2);

        // Simulate user1 having been removed from site1
        when(site1.getUsers()).thenReturn(user2Set);

        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, event);

        assertEquals(2, portalService.getRecentSites().size());

        recentSites = portalService.getRecentSites().iterator();
        assertEquals("site4", recentSites.next());
        assertEquals("site3", recentSites.next());

        when(event.getEvent()).thenReturn(SiteService.SOFT_DELETE_SITE);
        when(event.getContext()).thenReturn("site4");

        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, event);

        assertEquals(1, portalService.getRecentSites().size());
    }

    @Test
    public void testSoftDeletedSiteRemovalFromPinsAndRecent() {
        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);

        portalService.addRecentSite(site1Id);
        portalService.addPinnedSite(user1, site1Id);
        assertEquals(1, portalService.getRecentSites().size());
        assertEquals(1, portalService.getPinnedSites().size());

        Event softDeleteEvent = mock(Event.class);
        when(softDeleteEvent.getContext()).thenReturn(site1Id);
        when(softDeleteEvent.getEvent()).thenReturn(SiteService.SOFT_DELETE_SITE);

        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, softDeleteEvent);

        assertEquals(0, portalService.getRecentSites().size());
        assertEquals(0, portalService.getPinnedSites().size());
    }
}

