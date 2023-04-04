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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.portal.api.PortalService;
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

        Set<String> siteIds = new HashSet<>();
        siteIds.add(site1Id);
        siteIds.add("site2");

        portalService.savePinnedSites(siteIds);

        assertEquals(2, portalService.getPinnedSites().size());

        Event event = mock(Event.class);
        when(event.getContext()).thenReturn(site1Id);
        when(event.getEvent()).thenReturn(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP);

        Set<String> user2Set = new HashSet<>();
        user2Set.add(user2);

        // Simulate user1 having been removed from site1
        when(site1.getUsers()).thenReturn(user2Set);

        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, event);

        assertEquals(1, portalService.getPinnedSites().size());

        when(event.getEvent()).thenReturn(SiteService.SECURE_REMOVE_SITE);

        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, event);

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user2);
        assertEquals(0, portalService.getPinnedSites().size());
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

        when(event.getEvent()).thenReturn(SiteService.SECURE_REMOVE_SITE);
        when(event.getContext()).thenReturn("site4");

        ((PortalServiceImpl) AopTestUtils.getTargetObject(portalService)).update(null, event);

        assertEquals(1, portalService.getRecentSites().size());
    }
}

