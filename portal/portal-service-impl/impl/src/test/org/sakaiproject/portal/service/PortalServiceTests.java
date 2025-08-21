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
import org.junit.BeforeClass;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PortalTestConfiguration.class})
public class PortalServiceTests extends SakaiTests {

    @Autowired private PinnedSiteRepository pinnedSiteRepository;
    @Autowired private PortalService portalService;
    @Autowired private PreferencesService preferencesService;
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private ServerConfigurationService serverConfigurationService;

    private static boolean isWindowsOS = false;
    @BeforeClass
    public static void setUpOnce() {
        isWindowsOS = System.getProperty("os.name").toLowerCase().contains("windows");
    }

    @Before
    public void setup() {
        super.setup();
    }

    @Test
    public void testPinnedSites() throws IdUnusedException {

        String user1SiteId = "~user1";

        String user1Ref = "/user/user1";

        when(siteService.getUserSiteId(user1)).thenReturn(user1SiteId);

        List<String> siteIds = new ArrayList<>();
        siteIds.add(site1Id);
        siteIds.add("site2");

        portalService.savePinnedSites(user1, siteIds);

        Assert.assertEquals(2, portalService.getPinnedSites(user1).size());

        String site3Id = "site3";

        Site site3 = mock(Site.class);
        when(site3.isPublished()).thenReturn(false);
        when(siteService.getSite(site3Id)).thenReturn(site3);

        Event event = mock(Event.class);

        when(event.getEvent()).thenReturn(SiteService.EVENT_USER_SITE_MEMBERSHIP_ADD);
        when(event.getContext()).thenReturn(site3Id);
        when(event.getResource()).thenReturn(user1Ref);
        ((Observer) portalService).update(null, event);

        Assert.assertEquals(2, portalService.getPinnedSites(user1).size());

        Member m1 = mock(Member.class);
        when(m1.isActive()).thenReturn(true);
        when(site3.getMember(user1)).thenReturn(m1);
        when(site3.isPublished()).thenReturn(true);
        when(userDirectoryService.idFromReference(user1Ref)).thenReturn(user1);
        ((Observer) portalService).update(null, event);
        Assert.assertEquals(3, portalService.getPinnedSites(user1).size());

        when(event.getContext()).thenReturn(site1Id);
        when(event.getEvent()).thenReturn(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP);

        Set<String> user2Set = new HashSet<>();
        user2Set.add(user2);

        // Simulate user1 having been removed from site1
        when(site1.getUsers()).thenReturn(user2Set);
        ((Observer) portalService).update(null, event);

        Assert.assertEquals(2, portalService.getPinnedSites(user1).size());

        when(event.getEvent()).thenReturn(SiteService.SOFT_DELETE_SITE);

        ((Observer) portalService).update(null, event);

        Assert.assertEquals(0, portalService.getPinnedSites(user2).size());
    }

    @Test
    public void testPinnedSites2() {
        List<String> siteIds = new ArrayList<>();
        siteIds.add(site1Id);

        Set<String> users = new HashSet<>();
        users.add(user1);

        String user1Ref = "/user/user1";
        when(userDirectoryService.idFromReference(user1Ref)).thenReturn(user1);

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
            Assert.fail(idue.toString());
        }

        Event event = mock(Event.class);

        when(event.getEvent()).thenReturn(SiteService.EVENT_USER_SITE_MEMBERSHIP_ADD);
        when(event.getContext()).thenReturn(site1Id);
        when(event.getResource()).thenReturn(user1Ref);
        ((Observer) portalService).update(null, event);
        Assert.assertEquals(0, portalService.getPinnedSites(user1).size());

        when(site1.isPublished()).thenReturn(true);
        ((Observer) portalService).update(null, event);
        Assert.assertEquals(0, portalService.getPinnedSites(user1).size());

        when(member1.isActive()).thenReturn(true);
        ((Observer) portalService).update(null, event);
        Assert.assertEquals(1, portalService.getPinnedSites(user1).size());

        when(event.getResource()).thenReturn(site1Ref);
        when(siteService.idFromSiteReference(site1Ref)).thenReturn(site1Id);
        when(event.getEvent()).thenReturn(SiteService.EVENT_SITE_UNPUBLISH);
        ((Observer) portalService).update(null, event);
        Assert.assertEquals(0, portalService.getPinnedSites(user1).size());

        when(event.getEvent()).thenReturn(SiteService.EVENT_SITE_PUBLISH);
        ((Observer) portalService).update(null, event);
        Assert.assertEquals(1, portalService.getPinnedSites(user1).size());
    }

    @Test
    public void testPinnedSitePosition() {

        String site2Id = "site2";
        String site3Id = "site3";
        String site4Id = "site4";

        when(serverConfigurationService.getBoolean("portal.new.pinned.sites.top", false)).thenReturn(true);

        List<String> siteIds = new ArrayList<>();
        siteIds.add(site1Id);
        siteIds.add(site2Id);
        siteIds.add(site3Id);

        portalService.savePinnedSites(user1, siteIds);

        List<String> pinnedSites = portalService.getPinnedSites(user1);
        Assert.assertEquals(3, pinnedSites.size());

        Assert.assertEquals(site3Id, pinnedSites.get(0));
        Assert.assertEquals(site2Id, pinnedSites.get(1));
        Assert.assertEquals(site1Id, pinnedSites.get(2));

        portalService.removePinnedSite(user1, site1Id);
        portalService.removePinnedSite(user1, site2Id);
        portalService.removePinnedSite(user1, site3Id);

        when(serverConfigurationService.getBoolean("portal.new.pinned.sites.top", false)).thenReturn(false);

        siteIds.add(site1Id);
        siteIds.add(site2Id);
        siteIds.add(site3Id);

        portalService.savePinnedSites(user1, siteIds);

        pinnedSites = portalService.getPinnedSites(user1);
        Assert.assertEquals(3, pinnedSites.size());

        Assert.assertEquals(site1Id, pinnedSites.get(0));
        Assert.assertEquals(site2Id, pinnedSites.get(1));
        Assert.assertEquals(site3Id, pinnedSites.get(2));

        portalService.removePinnedSite(user1, site2Id);
        pinnedSites = portalService.getPinnedSites(user1);
        Assert.assertEquals(2, pinnedSites.size());

        List<PinnedSite> pinned = pinnedSiteRepository.findByUserIdOrderByPosition(user1);
        Assert.assertEquals(2, pinned.size());
        Assert.assertEquals(0, pinned.get(0).getPosition());
        Assert.assertEquals(site1Id, pinned.get(0).getSiteId());
        Assert.assertEquals(1, pinned.get(1).getPosition());
        Assert.assertEquals(site3Id, pinned.get(1).getSiteId());

        portalService.addPinnedSite(user1, site4Id, true);

        pinned = pinnedSiteRepository.findByUserIdOrderByPosition(user1);
        Assert.assertEquals(3, pinned.size());
        Assert.assertEquals(2, pinned.get(2).getPosition());
        Assert.assertEquals(site4Id, pinned.get(2).getSiteId());

        siteIds.add(site4Id);
        portalService.savePinnedSites(user1, siteIds);
        pinned = pinnedSiteRepository.findByUserIdOrderByPosition(user1);
        Assert.assertEquals(4, pinned.size());
        Assert.assertEquals(site4Id, pinned.get(2).getSiteId());
    }

    @Test
    public void testRecentSites() {

        String user1SiteId = "~user1";
        when(siteService.getUserSiteId(user1)).thenReturn(user1SiteId);

        Set<String> users = new HashSet<>();
        users.add(user1);

        Member member1 = createMockMember(user1, true);
        Set<Member> members = new HashSet<>();
        members.add(member1);

        Site site1 = mock(Site.class);
        when(site1.getMembers()).thenReturn(members);
        when(site1.getMember(user1)).thenReturn(member1);
        when(site1.isPublished()).thenReturn(true);
        try {
            when(siteService.getSite(site1Id)).thenReturn(site1);
        } catch (IdUnusedException idue) {
            Assert.fail(idue.toString());
        }

        pauseForOneSecondIfWindows();
        portalService.addRecentSite(user1, site1Id);
        Assert.assertEquals(1, portalService.getRecentSites(user1).size());

        Site site2 = mock(Site.class);
        String site2Id = "site2";
        when(site2.getMembers()).thenReturn(members);
        when(site2.getMember(user1)).thenReturn(member1);
        when(site2.isPublished()).thenReturn(true);
        try {
            when(siteService.getSite(site2Id)).thenReturn(site2);
        } catch (IdUnusedException idue) {
            Assert.fail(idue.toString());
        }

        pauseForOneSecondIfWindows();
        portalService.addRecentSite(user1, site2Id);
        Assert.assertEquals(2, portalService.getRecentSites(user1).size());

        Site site3 = mock(Site.class);
        String site3Id = "site3";
        when(site3.getMembers()).thenReturn(members);
        when(site3.getMember(user1)).thenReturn(member1);
        when(site3.isPublished()).thenReturn(true);
        try {
            when(siteService.getSite(site3Id)).thenReturn(site3);
        } catch (IdUnusedException idue) {
            Assert.fail(idue.toString());
        }

        pauseForOneSecondIfWindows();
        portalService.addRecentSite(user1, site3Id);
        Assert.assertEquals(3, portalService.getRecentSites(user1).size());

        pauseForOneSecondIfWindows();
        portalService.addRecentSite(user1, SiteService.SITE_ERROR);
        Assert.assertEquals(3, portalService.getRecentSites(user1).size());

        Iterator<String> recentSites = portalService.getRecentSites(user1).iterator();
        Assert.assertEquals("site3", recentSites.next());
        Assert.assertEquals("site2", recentSites.next());
        Assert.assertEquals("site1", recentSites.next());

        Site site4 = mock(Site.class);
        String site4Id = "site4";
        when(site4.getMembers()).thenReturn(members);
        when(site4.getMember(user1)).thenReturn(member1);
        when(site4.isPublished()).thenReturn(true);
        try {
            when(siteService.getSite(site4Id)).thenReturn(site4);
        } catch (IdUnusedException idue) {
            Assert.fail(idue.toString());
        }

        pauseForOneSecondIfWindows();
        portalService.addRecentSite(user1, site4Id);
        Assert.assertEquals(3, portalService.getRecentSites(user1).size());

        recentSites = portalService.getRecentSites(user1).iterator();
        Assert.assertEquals("site4", recentSites.next());
        Assert.assertEquals("site3", recentSites.next());
        Assert.assertEquals("site2", recentSites.next());

        pauseForOneSecondIfWindows();
        portalService.addRecentSite(user1, site1Id);
        Assert.assertEquals(3, portalService.getRecentSites(user1).size());

        recentSites = portalService.getRecentSites(user1).iterator();
        Assert.assertEquals("site1", recentSites.next());
        Assert.assertEquals("site4", recentSites.next());
        Assert.assertEquals("site3", recentSites.next());

        Event event = mock(Event.class);
        when(event.getContext()).thenReturn(site1Id);
        when(event.getEvent()).thenReturn(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP);

        Set<String> user2Set = new HashSet<>();
        user2Set.add(user2);

        // Simulate user1 having been removed from site1
        when(site1.getUsers()).thenReturn(user2Set);

        ((Observer) portalService).update(null, event);

        Assert.assertEquals(2, portalService.getRecentSites(user1).size());

        recentSites = portalService.getRecentSites(user1).iterator();
        Assert.assertEquals("site4", recentSites.next());
        Assert.assertEquals("site3", recentSites.next());

        when(event.getEvent()).thenReturn(SiteService.SOFT_DELETE_SITE);
        when(event.getContext()).thenReturn("site4");

        ((Observer) portalService).update(null, event);

        Assert.assertEquals(1, portalService.getRecentSites(user1).size());
    }

    /**
     * This tests unpinning sites go to recent and unpinned and
     * when a user performs a login that nothing changes
     */
    @Test
    public void testRecentlyUnpinned() {
        String sessionId = UUID.randomUUID().toString();
        Session session = createMockSession(sessionId, user1);
        when(sessionManager.getCurrentSession()).thenReturn(session);

        Event event = createMockEvent("user.login", user1, sessionId, null);

        Member member1 = createMockMember(user1, true);
        Set<Member> members = Set.of(member1);

        List<String> siteIds = List.of("site1", "site2", "site3", "site4");
        siteIds.forEach(siteId -> {
            Site site = mock(Site.class);
            site.setTitle(siteId);
            when(site.getMembers()).thenReturn(members);
            when(site.getMember(user1)).thenReturn(member1);
            when(site.isPublished()).thenReturn(true);
            try {
                when(siteService.getSite(siteId)).thenReturn(site);
                when(siteService.getSiteVisit(siteId)).thenReturn(site);
            } catch (IdUnusedException | PermissionException idue) {
                Assert.fail(idue.toString());
            }
        });

        when(siteService.getSiteIds(SiteService.SelectionType.MEMBER, null, null, null, SiteService.SortType.CREATED_ON_DESC, null)).thenReturn(siteIds);

        // simulate a login
        ((Observer) portalService).update(null, event);

        // all 4 sites should be pinned, results in 4 pinned, 0 unpinned, 0 recent
        Assert.assertEquals(4, portalService.getPinnedSites(user1).size());
        Assert.assertEquals(0, portalService.getUnpinnedSites(user1).size());
        Assert.assertEquals(0, portalService.getRecentSites(user1).size());

        // unpin all 4 sites, results in 0 pinned, 4 unpinned, 3 recent
        siteIds.forEach(siteId -> portalService.addPinnedSite(user1, siteId, false));
        Assert.assertEquals(0, portalService.getPinnedSites(user1).size());
        Assert.assertEquals(4, portalService.getUnpinnedSites(user1).size());
        Assert.assertEquals(3, portalService.getRecentSites(user1).size());

        // simulate another login
        ((Observer) portalService).update(null, event);

        // no change after login, results in 0 pinned, 4 unpinned, 3 recent
        Assert.assertEquals(0, portalService.getPinnedSites(user1).size());
        Assert.assertEquals(4, portalService.getUnpinnedSites(user1).size());
        Assert.assertEquals(3, portalService.getRecentSites(user1).size());

        // re pin site3, results in 1 pinned, 3 unpinned, 3 recent
        portalService.addPinnedSite(user1, "site3", true);
        Assert.assertEquals(1, portalService.getPinnedSites(user1).size());
        Assert.assertEquals(3, portalService.getUnpinnedSites(user1).size());
        Assert.assertEquals(3, portalService.getRecentSites(user1).size());

        // simulate another login
        ((Observer) portalService).update(null, event);

        // no change after login, results in 1 pinned, 3 unpinned, 3 recent
        Assert.assertEquals(1, portalService.getPinnedSites(user1).size());
        Assert.assertEquals(3, portalService.getUnpinnedSites(user1).size());
        Assert.assertEquals(3, portalService.getRecentSites(user1).size());
    }

    @Test
    public void testSoftDeletedSiteRemovalFromPinsAndRecent() {

        Member user1Member = mock(Member.class);
        when(user1Member.isActive()).thenReturn(true);
        when(site1.getMember(user1)).thenReturn(user1Member);
        when(site1.isPublished()).thenReturn(true);

        pauseForOneSecondIfWindows();
        portalService.addRecentSite(user1, site1Id);
        portalService.addPinnedSite(user1, site1Id, true);
        Assert.assertEquals(1, portalService.getRecentSites(user1).size());
        Assert.assertEquals(1, portalService.getPinnedSites(user1).size());

        Event softDeleteEvent = mock(Event.class);
        when(softDeleteEvent.getContext()).thenReturn(site1Id);
        when(softDeleteEvent.getEvent()).thenReturn(SiteService.SOFT_DELETE_SITE);

        ((Observer) portalService).update(null, softDeleteEvent);

        Assert.assertEquals(0, portalService.getRecentSites(user1).size());
        Assert.assertEquals(0, portalService.getPinnedSites(user1).size());
    }

    /**
     * This tests the auto pinning of new sites when the user logs into Sakai.
     * A passing tests indicates that a site in which a user has not seen before
     * is auto pinned, when they login.
     */
    @Test
    public void testAutoPinning() {

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

        String sessionId = UUID.randomUUID().toString();
        Session session = createMockSession(sessionId, user1);
        when(sessionManager.getCurrentSession()).thenReturn(session);

        Member member1 = createMockMember(user1, true);
        when(site1.getMember(user1)).thenReturn(member1);
        when(site2.getMember(user1)).thenReturn(member1);

        when(securityService.isSuperUser(user1)).thenReturn(false);
        ResourceProperties properties = new BaseResourceProperties();
        properties.addPropertyToList(PortalService.FAVORITES_PROPERTY, site1Id);
        properties.addPropertyToList(PortalService.SEEN_SITES_PROPERTY, site2Id);
        Preferences preferences = createMockPreferences(user1, PreferencesService.SITENAV_PREFS_KEY, properties);
        when(preferencesService.getPreferences(user1)).thenReturn(preferences);

        List<String> userSiteIds = List.of(site1Id, site2Id);
        when(siteService.getSiteIds(SiteService.SelectionType.MEMBER, null, null, null, SiteService.SortType.CREATED_ON_DESC, null)).thenReturn(userSiteIds);

        Assert.assertFalse(preferences.getProperties(PreferencesService.SITENAV_PREFS_KEY).getPropertyList(PortalService.FAVORITES_PROPERTY).isEmpty());
        Assert.assertTrue(portalService.getRecentSites(user1).isEmpty());

        Event event = createMockEvent("user.login", user1, sessionId, site1.getId());
        ((Observer) portalService).update(null, event);

        List<String> pinnedSites = portalService.getPinnedSites(user1);
        Assert.assertEquals(1, pinnedSites.size());
        Assert.assertEquals(site1Id, pinnedSites.get(0));

        List<String> recentSites = portalService.getRecentSites(user1);
        Assert.assertEquals(1, recentSites.size());
        Assert.assertEquals(site2Id, recentSites.get(0));
    }

    /**
     * This tests syncing a users pinned sites, and removal when a user logs in
     *
     * @throws IdUnusedException
     * @throws PermissionException
     */
    @Test
    public void testSyncUserSitesWithPortalNav() throws IdUnusedException, PermissionException {

        String sessionId = UUID.randomUUID().toString();
        Session session = createMockSession(sessionId, user1);
        when(sessionManager.getCurrentSession()).thenReturn(session);

        ResourceProperties properties = new BaseResourceProperties();
        properties.addPropertyToList(PortalService.FAVORITES_PROPERTY, "site3");
        properties.addPropertyToList(PortalService.FAVORITES_PROPERTY, "site5");
        properties.addPropertyToList(PortalService.SEEN_SITES_PROPERTY, "site4");
        properties.addPropertyToList(PortalService.SEEN_SITES_PROPERTY, "site5");
        properties.addPropertyToList(PreferencesService.SITENAV_PREFS_EXCLUDE_KEY, "site6");
        Preferences preferences = createMockPreferences(user1, PreferencesService.SITENAV_PREFS_KEY, properties);
        when(preferencesService.getPreferences(user1)).thenReturn(preferences);

        Member member1 = createMockMember(user1, true);
        Set<Member> members = Set.of(member1);

        List<String> siteIds = List.of("site1", "site2", "site3", "site4", "site5", "site6");
        siteIds.forEach(siteId -> {
            Site site = mock(Site.class);
            site.setTitle(siteId);
            when(site.getMembers()).thenReturn(members);
            when(site.getMember(user1)).thenReturn(member1);
            when(site.isPublished()).thenReturn(true);
            try {
                when(siteService.getSite(siteId)).thenReturn(site);
                when(siteService.getSiteVisit(siteId)).thenReturn(site);
            } catch (IdUnusedException | PermissionException idue) {
                Assert.fail(idue.toString());
            }
        });

        when(siteService.getSiteIds(SiteService.SelectionType.MEMBER, null, null, null, SiteService.SortType.CREATED_ON_DESC, null)).thenReturn(siteIds);
        when(securityService.isSuperUser(user1)).thenReturn(false);

        Event event = createMockEvent("user.login", user1, sessionId, null);

        // simulate a login
        ((Observer) portalService).update(null, event);

        // after a login old favorites data should be removed
        properties = new BaseResourceProperties();
        properties.addPropertyToList(PreferencesService.SITENAV_PREFS_EXCLUDE_KEY, "site6");
        preferences = createMockPreferences(user1, PreferencesService.SITENAV_PREFS_KEY, properties);
        when(preferencesService.getPreferences(user1)).thenReturn(preferences);

        Preferences prefs = preferencesService.getPreferences(user1);
        ResourceProperties props = prefs.getProperties(PreferencesService.SITENAV_PREFS_KEY);
        Assert.assertNotNull(prefs.getProperties(PreferencesService.SITENAV_PREFS_KEY));
        Assert.assertEquals(1, props.getPropertyList(PreferencesService.SITENAV_PREFS_EXCLUDE_KEY).size());
        Assert.assertNull(props.getPropertyList(PortalService.FAVORITES_PROPERTY));
        Assert.assertNull(props.getPropertyList(PortalService.SEEN_SITES_PROPERTY));

        // unpinned sites should be 1, "site4"
        List<String> unpinnedSites = portalService.getUnpinnedSites(user1);
        Assert.assertEquals(1, unpinnedSites.size());
        Assert.assertEquals("site4", unpinnedSites.get(0));

        // recent sites should be 1, "site4"
        List<String> recentSites = portalService.getRecentSites(user1);
        Assert.assertEquals(1, recentSites.size());
        Assert.assertEquals("site4", recentSites.get(0));

        // sites 1, 2, 3, 5 should be pinned
        List<String> pinnedSites = portalService.getPinnedSites(user1);
        Assert.assertEquals(4, pinnedSites.size());
        Assert.assertTrue(pinnedSites.contains("site1"));
        Assert.assertTrue(pinnedSites.contains("site2"));
        Assert.assertTrue(pinnedSites.contains("site3"));
        Assert.assertTrue(pinnedSites.contains("site5"));
        // site6 should not be pinned
        Assert.assertFalse(pinnedSites.contains("site6"));

        // now lets make site2 inaccessible, but not update pinned table
        PermissionException pe = new PermissionException(user1, SiteService.SITE_VISIT, "/site/site2Id");
        when(siteService.getSiteVisit("site2")).thenThrow(pe);

        // check pinned sites havn't changed since making site2 inaccessible
        pinnedSites = portalService.getPinnedSites(user1);
        Assert.assertEquals(4, pinnedSites.size());

        // lets pin an excluded site
        portalService.addPinnedSite(user1, "site6", true);

        // lets pin site4, previously was unpinned from favorites
        portalService.addPinnedSite(user1, "site4", true);

        // now signal a new login which should update the users pinned sites, removing site2
        ((Observer) portalService).update(null, event);

        // sites 1, 3, 4, 5 should be pinned
        pinnedSites = portalService.getPinnedSites(user1);
        Assert.assertEquals(4, pinnedSites.size());
        Assert.assertTrue(pinnedSites.contains("site1"));
        Assert.assertTrue(pinnedSites.contains("site3"));
        Assert.assertTrue(pinnedSites.contains("site4"));
        Assert.assertTrue(pinnedSites.contains("site5"));
        // site6 should not be pinned
        Assert.assertFalse(pinnedSites.contains("site6"));

        // site 4 should be in recents
        recentSites = portalService.getRecentSites(user1);
        Assert.assertEquals(1, recentSites.size());
        Assert.assertTrue(recentSites.contains("site4"));
        // site 6 should not be in recents
        Assert.assertFalse(recentSites.contains("site6"));

        // site 4 not be in unpinned
        unpinnedSites = portalService.getUnpinnedSites(user1);
        Assert.assertEquals(0, unpinnedSites.size());
        Assert.assertFalse(unpinnedSites.contains("site6"));
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

    public void pauseForOneSecondIfWindows() {
        if (isWindowsOS) {
            try {
                Thread.sleep(1000); // Pause for 1 second
            } catch (InterruptedException e) {
                // Typically, you can ignore this in test scenarios or log it if needed.
                // Optionally re-interrupt the thread if your test context requires it:
                // Thread.currentThread().interrupt();
            }
        }
    }
}
