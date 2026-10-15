/*
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
package org.sakaiproject.portal.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = PortalTestConfiguration.class)
public class PortalObserverTest {

    @Autowired private SessionFactory sessionFactory;
    @Autowired private PortalService portalService;
    @Autowired private EventTrackingService eventTrackingService;
    @Autowired private SiteService siteService;
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private UserDirectoryService userDirectoryService;

    @Test
    public void loginCallbackUsesServiceTransaction() throws Exception {
        String userId = UUID.randomUUID().toString();
        String siteId = UUID.randomUUID().toString();
        setUpLogin(userId, siteId);
        List<Boolean> transactions = new ArrayList<>();
        when(siteService.getSiteIds(eq(SiteService.SelectionType.MEMBER), isNull(), isNull(), isNull(),
                eq(SiteService.SortType.CREATED_ON_DESC), isNull()))
            .thenAnswer(invocation -> {
                transactions.add(TransactionSynchronizationManager.isActualTransactionActive());
                return List.of(siteId);
            });
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        registeredObserver().update(null, event(UsageSessionService.EVENT_LOGIN, userId, siteId));

        assertEquals(List.of(siteId), portalService.getPinnedSites(userId));
        assertEquals(List.of(true), transactions);
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }

    @Test
    public void loginFailureRollsBackNewPins() throws Exception {
        String userId = UUID.randomUUID().toString();
        String siteId = UUID.randomUUID().toString();
        setUpLogin(userId, siteId);
        AtomicInteger specialSiteChecks = new AtomicInteger();
        when(siteService.isSpecialSite(siteId)).thenAnswer(invocation -> {
            if (specialSiteChecks.incrementAndGet() == 2) {
                throw new IllegalStateException("Site lookup failed after pinning");
            }
            return false;
        });

        assertThrows(IllegalStateException.class, () -> registeredObserver().update(null,
            event(UsageSessionService.EVENT_LOGIN, userId, siteId)));

        assertTrue("A failed login sync must not leave newly committed pins", portalService.getPinnedSites(userId).isEmpty());
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }

    @Test
    public void membershipAddedCallbackUsesServiceTransaction() {
        String userId = UUID.randomUUID().toString();
        String siteId = UUID.randomUUID().toString();
        String reference = "/user/" + userId;
        when(userDirectoryService.idFromReference(reference)).thenReturn(userId);
        when(siteService.siteReference(siteId)).thenReturn("/site/" + siteId);
        List<Boolean> transactions = new ArrayList<>();
        when(securityService.unlock(userId, SiteService.SECURE_UPDATE_SITE, "/site/" + siteId))
            .thenAnswer(invocation -> {
                transactions.add(TransactionSynchronizationManager.isActualTransactionActive());
                return true;
            });
        Event event = event(SiteService.EVENT_USER_SITE_MEMBERSHIP_ADD, userId, siteId);
        when(event.getResource()).thenReturn(reference);
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        registeredObserver().update(null, event);

        assertEquals(List.of(siteId), portalService.getPinnedSites(userId));
        assertEquals(List.of(true), transactions);
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }

    @Test
    public void membershipFailureRollsBackRemovedPinsAndRecentSites() throws Exception {
        String removedUser = UUID.randomUUID().toString();
        String addedUser = UUID.randomUUID().toString();
        String siteId = UUID.randomUUID().toString();
        portalService.addPinnedSite(removedUser, siteId, true);
        portalService.addRecentSite(removedUser, siteId);
        Site site = mock(Site.class);
        when(site.getUsers()).thenReturn(Set.of(addedUser));
        when(siteService.getSite(siteId)).thenReturn(site)
            .thenThrow(new IllegalStateException("Site lookup failed after membership cleanup"));
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        assertThrows(IllegalStateException.class, () -> registeredObserver().update(null,
            event(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP, addedUser, siteId)));

        assertEquals("Failed membership sync must roll back removal of pins", List.of(siteId), portalService.getPinnedSites(removedUser));
        assertEquals("Failed membership sync must roll back removal of recent sites", List.of(siteId), portalService.getRecentSites(removedUser));
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }

    @Test
    public void unrelatedEventsDoNotOpenDatabaseTransactions() {
        Statistics statistics = sessionFactory.getStatistics();
        boolean enabled = statistics.isStatisticsEnabled();
        statistics.setStatisticsEnabled(true);
        try {
            long transactions = statistics.getTransactionCount();

            registeredObserver().update(null, event("unrelated.event", "user", "site"));
            registeredObserver().update(null, new Object());

            assertEquals(transactions, statistics.getTransactionCount());
            assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        } finally {
            statistics.setStatisticsEnabled(enabled);
        }
    }

    private Observer registeredObserver() {
        ArgumentCaptor<Observer> observers = ArgumentCaptor.forClass(Observer.class);
        verify(eventTrackingService, atLeastOnce()).addLocalObserver(observers.capture());
        return observers.getAllValues().stream().filter(observer -> observer instanceof PortalService)
            .findFirst().orElseThrow();
    }

    private void setUpLogin(String userId, String siteId) throws Exception {
        Session session = mock(Session.class);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(session.getAttribute(Session.JUST_LOGGED_IN)).thenReturn(true);
        when(session.getUserId()).thenReturn(userId);
        when(siteService.getSiteIds(eq(SiteService.SelectionType.MEMBER), isNull(), isNull(), isNull(),
            eq(SiteService.SortType.CREATED_ON_DESC), isNull())).thenReturn(List.of(siteId));
        Site site = mock(Site.class);
        Member member = mock(Member.class);
        when(site.getMember(userId)).thenReturn(member);
        when(member.isActive()).thenReturn(true);
        when(siteService.getSiteVisit(siteId)).thenReturn(site);
    }

    private Event event(String type, String userId, String siteId) {
        Event event = mock(Event.class);
        when(event.getEvent()).thenReturn(type);
        when(event.getUserId()).thenReturn(userId);
        when(event.getContext()).thenReturn(siteId);
        return event;
    }
}
