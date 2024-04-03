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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observer;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.model.PinnedSite;
import org.sakaiproject.portal.api.repository.PinnedSiteRepository;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiTests;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.BaseResourceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PortalTestConfiguration.class})
public class PortalServiceTests extends SakaiTests {

    @Autowired private MemoryService memoryService;
    @Autowired private PinnedSiteRepository pinnedSiteRepository;
    @Autowired private PortalService portalService;
    @Autowired private PreferencesService preferencesService;
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
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

        Member m1 = mock(Member.class);
        when(m1.isActive()).thenReturn(true);
        when(site3.getMember(user1)).thenReturn(m1);
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
        when(member1.isActive()).thenReturn(false);
        Set<Member> members = new HashSet<>();
        members.add(member1);

        Site site1 = mock(Site.class);
        when(site1.getUsers()).thenReturn(users);
        when(site1.getMember(user1)).thenReturn(member1);
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
        assertEquals(0, portalService.getPinnedSites().size());

        when(member1.isActive()).thenReturn(true);
        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, event);
        assertEquals(1, portalService.getPinnedSites().size());

        when(event.getResource()).thenReturn(site1Ref);
        when(siteService.idFromSiteReference(site1Ref)).thenReturn(site1Id);
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

        portalService.addPinnedSite(user1, site4Id, true);

        pinned = pinnedSiteRepository.findByUserIdOrderByPosition(user1);
        assertEquals(3, pinned.size());
        assertEquals(2, pinned.get(2).getPosition());
        assertEquals(site4Id, pinned.get(2).getSiteId());

        siteIds.add(site4Id);
        portalService.savePinnedSites(siteIds);
        pinned = pinnedSiteRepository.findByUserIdOrderByPosition(user1);
        assertEquals(4, pinned.size());
        assertEquals(site4Id, pinned.get(2).getSiteId());
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

        String user1SiteId = "~user1";
        when(siteService.getUserSiteId(user1)).thenReturn(user1SiteId);

        Set<String> users = new HashSet<>();
        users.add(user1);

        Member member1 = mock(Member.class);
        when(member1.getUserId()).thenReturn(user1);
        when(member1.isActive()).thenReturn(true);
        Set<Member> members = new HashSet<>();
        members.add(member1);

        Site site1 = mock(Site.class);
        when(site1.getMembers()).thenReturn(members);
        when(site1.getMember(user1)).thenReturn(member1);
        when(site1.isPublished()).thenReturn(true);
        try {
            when(siteService.getSite(site1Id)).thenReturn(site1);
        } catch (IdUnusedException idue) {
            System.out.println(idue.toString());
        }

        pauseForOneSecond();
        portalService.addRecentSite(site1Id);
        assertEquals(1, portalService.getRecentSites().size());

        Site site2 = mock(Site.class);
        String site2Id = "site2";
        when(site2.getMembers()).thenReturn(members);
        when(site2.getMember(user1)).thenReturn(member1);
        when(site2.isPublished()).thenReturn(true);
        try {
            when(siteService.getSite(site2Id)).thenReturn(site2);
        } catch (IdUnusedException idue) {
            System.out.println(idue.toString());
        }

        pauseForOneSecond();
        portalService.addRecentSite(site2Id);
        assertEquals(2, portalService.getRecentSites().size());

        Site site3 = mock(Site.class);
        String site3Id = "site3";
        when(site3.getMembers()).thenReturn(members);
        when(site3.getMember(user1)).thenReturn(member1);
        when(site3.isPublished()).thenReturn(true);
        try {
            when(siteService.getSite(site3Id)).thenReturn(site3);
        } catch (IdUnusedException idue) {
            System.out.println(idue.toString());
        }

        pauseForOneSecond();
        portalService.addRecentSite(site3Id);
        assertEquals(3, portalService.getRecentSites().size());

        pauseForOneSecond();
        portalService.addRecentSite(SiteService.SITE_ERROR);
        assertEquals(3, portalService.getRecentSites().size());

        Iterator<String> recentSites = portalService.getRecentSites().iterator();
        assertEquals("site3", recentSites.next());
        assertEquals("site2", recentSites.next());
        assertEquals("site1", recentSites.next());

        Site site4 = mock(Site.class);
        String site4Id = "site4";
        when(site4.getMembers()).thenReturn(members);
        when(site4.getMember(user1)).thenReturn(member1);
        when(site4.isPublished()).thenReturn(true);
        try {
            when(siteService.getSite(site4Id)).thenReturn(site4);
        } catch (IdUnusedException idue) {
            System.out.println(idue.toString());
        }

        pauseForOneSecond();
        portalService.addRecentSite(site4Id);
        assertEquals(3, portalService.getRecentSites().size());

        recentSites = portalService.getRecentSites().iterator();
        assertEquals("site4", recentSites.next());
        assertEquals("site3", recentSites.next());
        assertEquals("site2", recentSites.next());

        pauseForOneSecond();
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

        Member user1Member = mock(Member.class);
        when(user1Member.isActive()).thenReturn(true);
        when(site1.getMember(user1)).thenReturn(user1Member);
        when(site1.isPublished()).thenReturn(true);

        pauseForOneSecond();
        portalService.addRecentSite(site1Id);
        portalService.addPinnedSite(user1, site1Id, true);
        assertEquals(1, portalService.getRecentSites().size());
        assertEquals(1, portalService.getPinnedSites().size());

        Event softDeleteEvent = mock(Event.class);
        when(softDeleteEvent.getContext()).thenReturn(site1Id);
        when(softDeleteEvent.getEvent()).thenReturn(SiteService.SOFT_DELETE_SITE);

        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, softDeleteEvent);

        assertEquals(0, portalService.getRecentSites().size());
        assertEquals(0, portalService.getPinnedSites().size());
    }

    /**
     * This tests the auto pinning of new sites when the user logs into Sakai.
     * A passing tests indicates that a site in which a user has not seen before
     * is auto pinned, when they login.
     */
    @Test
    public void testAutoPinning() {
        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);

        String sessionId = UUID.randomUUID().toString();
        Session session = createMockSession(sessionId, user1);
        when(sessionManager.getCurrentSession()).thenReturn(session);

        Member member = createMockMember(user1, true);
        when(site1.getMember(user1)).thenReturn(member);
        when(site1.isPublished()).thenReturn(true);

        when(securityService.isSuperUser(user1)).thenReturn(false);
        ResourceProperties properties = new BaseResourceProperties();
        Preferences preferences = createMockPreferences(user1, PreferencesService.SITENAV_PREFS_KEY, properties);
        when(preferencesService.getPreferences(user1)).thenReturn(preferences);

        List<String> userSiteIds = List.of(site1Id);
        when(siteService.getSiteIds(SiteService.SelectionType.MEMBER, null, null, null, SiteService.SortType.CREATED_ON_DESC, null)).thenReturn(userSiteIds);

        Assert.assertTrue(portalService.getPinnedSites(user1).isEmpty());

        Event event = createMockEvent("user.login", user1, sessionId, site1.getId());
        ((Observer) portalService).update(null, event);

        List<String> pinnedSites = portalService.getPinnedSites(user1);
        Assert.assertEquals(1, pinnedSites.size());
        Assert.assertEquals(site1Id, pinnedSites.get(0));
    }

    /**
     * This will test the migration from the previous favorites that were stored in preferences
     * to pinned which are stored in the db. A passing test indicates that it took 2 sites
     * from preferences and added them to the appropriate tables for pinned and recent.
     *
     * @throws IdUnusedException
     * @throws PermissionException
     */
    @Test
    public void testFavoritesMigration() throws IdUnusedException, PermissionException {
        String site2Id = "site2";
        Site site2 = Mockito.mock(Site.class);
        when(siteService.getSite(site2Id)).thenReturn(site2);
        when(siteService.getSiteVisit(site2Id)).thenReturn(site2);

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);

        String sessionId = UUID.randomUUID().toString();
        Session session = createMockSession(sessionId, user1);
        when(sessionManager.getCurrentSession()).thenReturn(session);

        Member member1 = createMockMember(user1, true);
        when(site1.getMember(user1)).thenReturn(member1);
        when(site1.isPublished()).thenReturn(true);
        when(site2.getMember(user1)).thenReturn(member1);
        when(site2.isPublished()).thenReturn(true);

        when(securityService.isSuperUser(user1)).thenReturn(false);
        ResourceProperties properties = new BaseResourceProperties();
        properties.addPropertyToList(PortalService.FAVORITES_PROPERTY, site1Id);
        properties.addPropertyToList(PortalService.SEEN_SITES_PROPERTY, site2Id);
        Preferences preferences = createMockPreferences(user1, PreferencesService.SITENAV_PREFS_KEY, properties);
        when(preferencesService.getPreferences(user1)).thenReturn(preferences);

        List<String> userSiteIds = List.of(site1Id, site2Id);
        when(siteService.getSiteIds(SiteService.SelectionType.MEMBER, null, null, null, SiteService.SortType.CREATED_ON_DESC, null)).thenReturn(userSiteIds);

        Assert.assertFalse(preferences.getProperties(PreferencesService.SITENAV_PREFS_KEY).getPropertyList(PortalService.FAVORITES_PROPERTY).isEmpty());
        Assert.assertTrue(portalService.getRecentSites().isEmpty());
        Assert.assertTrue(portalService.getRecentSites().isEmpty());

        Event event = createMockEvent("user.login", user1, sessionId, site1.getId());
        ((Observer) portalService).update(null, event);

        List<String> pinnedSites = portalService.getPinnedSites();
        Assert.assertEquals(1, pinnedSites.size());
        Assert.assertEquals(site1Id, pinnedSites.get(0));

        List<String> recentSites = portalService.getRecentSites();
        Assert.assertEquals(1, recentSites.size());
        Assert.assertEquals(site2Id, recentSites.get(0));
    }

    private Event createMockEvent(String name, String userId, String sessionId, String siteId) {
        Event event = Mockito.mock(Event.class);
        when(event.getEvent()).thenReturn(name);
        when(event.getUserId()).thenReturn(userId);
        when(event.getSessionId()).thenReturn(sessionId);
        when(event.getContext()).thenReturn(siteId);
        return event;
    }

    private Session createMockSession(String sessionId, String userId) {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn(sessionId);
        when(session.getUserId()).thenReturn(userId);
        when(session.getAttribute(Session.JUST_LOGGED_IN)).thenReturn(true);
        return session;
    }

    private Member createMockMember(String userId, boolean isActive) {
        Member member = Mockito.mock(Member.class);
        when(member.getUserId()).thenReturn(userId);
        when(member.getUserEid()).thenReturn(userId);
        when(member.isActive()).thenReturn(isActive);
        Role role = Mockito.mock(Role.class);
        when(member.getRole()).thenReturn(role);
        return member;
    }

    private Preferences createMockPreferences(String userId, String propertyKey, ResourceProperties properties) {
        Preferences preferences = Mockito.mock(Preferences.class);
        when(preferences.getId()).thenReturn(userId);
        when(preferences.getProperties(propertyKey)).thenReturn(properties);
        return preferences;
    }

    public void pauseForOneSecond() {
        try {
            Thread.sleep(1000); // Pause for 1 second
        } catch (InterruptedException e) {
            // Typically, you can ignore this in test scenarios or log it if needed.
            // Optionally re-interrupt the thread if your test context requires it:
            // Thread.currentThread().interrupt();
        }
    }
}

