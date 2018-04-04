/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.calendar.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService.SAK_PROP_EXTSUBSCRIPTIONS_ENABLED;
import static org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService.SUBS_NAME_DELIMITER;
import static org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService.TC_PROP_SUBCRIPTIONS;
import static org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService.TC_PROP_SUBCRIPTIONS_WITH_TZ;
import static org.sakaiproject.calendar.impl.BaseExternalCalendarSubscriptionService.SCHEDULE_TOOL_ID;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarImporterService;
import org.sakaiproject.calendar.api.ExternalSubscriptionDetails;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.memory.impl.EhcacheMemoryService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;

import net.sf.ehcache.CacheManager;

/**
 * EntityManager shouldn't have to be Mocked as it's simple enough to have a real instance
 * but this we aren't there yet.
 */
@PrepareForTest(ComponentManager.class)
@RunWith(PowerMockRunner.class)
public class BaseExternalCalendarSubscriptionTest {


    private BaseExternalCalendarSubscriptionService service;

    @Mock
    private SecurityService securityService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private SiteService siteService;
    @Mock
    private ServerConfigurationService serverConfigurationService;
    @Mock
    private CalendarImporterService importer;
    @Mock
    private BaseCalendarService calendarService;
    @Mock
    private SessionManager sessionManager;
    @Mock
    private TimeService timeService;

    private CacheManager cacheManager;


    @Before
    public void setUp() throws ImportException {
        PowerMockito.mockStatic(ComponentManager.class);
        // A mock component manager.
        when(ComponentManager.get(any(Class.class))).then(new Answer<Object>() {
            private Map<Class, Object> mocks = new HashMap<>();
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Class classToMock = (Class) invocation.getArguments()[0];
                return mocks.computeIfAbsent(classToMock, k -> mock(classToMock));
            }
        });
        service = new BaseExternalCalendarSubscriptionService();
        service.setSecurityService(securityService);
        service.setEntityManager(entityManager);
        service.setSiteService(siteService);
        service.setCalendarImporterService(importer);
        service.setServerConfigurationService(serverConfigurationService);
        service.setCalendarService(calendarService);
        service.setTimeService(timeService);
        service.setSessionManager(sessionManager);
        service.setClock(Clock.systemUTC());

        Time time = mock(Time.class);
        when(time.toString()).thenReturn(new Date().toString());
        when(timeService.newTime()).thenReturn(time);

        when(sessionManager.getCurrentSessionUserId()).thenReturn("currentUserId");

        // Enable the service
        when(serverConfigurationService.getBoolean(SAK_PROP_EXTSUBSCRIPTIONS_ENABLED, true)).thenReturn(true);

        cacheManager = new CacheManager();
        EhcacheMemoryService ehcacheMemoryService = new EhcacheMemoryService(cacheManager, serverConfigurationService);
        ehcacheMemoryService.init();

        when(importer.getDefaultColumnMap(CalendarImporterService.ICALENDAR_IMPORT)).thenReturn(Collections.emptyMap());

        service.setMemoryService(ehcacheMemoryService);
        service.init();
    }

    @After
    public void tearDown() {
        cacheManager.shutdown();
    }

    @Test
    public void testGetCalendarSubscription() throws Exception {
        // Doesn't actually get parsed as we mock out the parser
        String url = getClass().getResource("/simple.ics").toExternalForm();
        String packedUrl = BaseExternalSubscriptionDetails.getIdFromSubscriptionUrl(url);
        String referenceString = BaseExternalSubscriptionDetails.calendarSubscriptionReference("siteId", packedUrl);
        {
            Reference ref = mock(Reference.class);
            when(ref.getContext()).thenReturn("siteId");
            when(ref.getId()).thenReturn(packedUrl);
            when(entityManager.newReference(referenceString)).thenReturn(ref);
        }
        TimeRange range = mock(TimeRange.class);
        when(range.clone()).thenReturn(range);
        // All the actual implementations need services to function, would be useful to have one that didn't
        CalendarEventEdit event = mock(CalendarEventEdit.class);
        when(event.getRange()).thenReturn(range);
        when(event.getDisplayName()).thenReturn("Display Name");
        when(event.getDescription()).thenReturn("Description");
        when(event.getLocation()).thenReturn("Location");

        when(importer.doImport(any(), any(), any(),any(), any())).thenReturn(Collections.singletonList(event));

        assertNotNull(service.getCalendarSubscription(referenceString));
        // This should come from the cache
        assertNotNull(service.getCalendarSubscription(referenceString));
        assertNotNull(service.getCalendarSubscription(referenceString));
        // Only parsed once.
        verify(importer, times(1)).doImport(any(), any(), any(), any(), any());
    }

    @Test
    public void testGetCalendarSubscriptionMissing() throws Exception {
        // Doesn't actually get parsed as we mock out the parser
        String url = getClass().getResource("/simple.ics").toExternalForm();
        String packedUrl = BaseExternalSubscriptionDetails.getIdFromSubscriptionUrl(url+"missing");
        String referenceString = BaseExternalSubscriptionDetails.calendarSubscriptionReference("siteId", packedUrl);
        {
            Reference ref = mock(Reference.class);
            when(ref.getContext()).thenReturn("siteId");
            when(ref.getId()).thenReturn(packedUrl);
            when(entityManager.newReference(referenceString)).thenReturn(ref);
        }

        Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        service.setClock(clock);
        assertNull(service.getCalendarSubscription(referenceString));
        assertNull(service.getCalendarSubscription(referenceString));
        // Should only cache a bad response for a short time.
        service.setClock(Clock.offset(clock, Duration.ofMinutes(2)));
        assertNull(service.getCalendarSubscription(referenceString));
    }

    @Test
    public void testGetCalendarSubscriptionChannelsForChannels() throws IdUnusedException {
        {
            Reference ref = mock(Reference.class);
            when(entityManager.newReference("/calendar/primary")).thenReturn(ref);
            when(ref.getContext()).thenReturn("primary");
        }

        when(siteService.isUserSite("primary")).thenReturn(false);

        {
            Reference ref = mock(Reference.class);
            when(entityManager.newReference("/calendar/other")).thenReturn(ref);
            when(ref.getContext()).thenReturn("other");
        }
        
        String urlTest = "https://test/url";
        String subscription = "/calendar/other/"+BaseExternalSubscriptionDetails.getIdFromSubscriptionUrl(urlTest);
        {
            Reference ref = mock(Reference.class);
            when(entityManager.newReference(subscription)).thenReturn(ref);
            when(ref.getContext()).thenReturn("other");
            when(ref.getId()).thenReturn(BaseExternalSubscriptionDetails.getIdFromSubscriptionUrl(urlTest));
        }

        {
            Site site = mock(Site.class);
            when(siteService.getSite("other")).thenReturn(site);
            ToolConfiguration configuration = mock(ToolConfiguration.class);
            when(site.getToolForCommonId(SCHEDULE_TOOL_ID)).thenReturn(configuration);
            Properties props = new Properties();
            props.setProperty(TC_PROP_SUBCRIPTIONS, subscription+SUBS_NAME_DELIMITER+"calName");
            when(configuration.getConfig()).thenReturn(props);
        }
        
        Set<ExternalSubscriptionDetails> subs = service.getCalendarSubscriptionChannelsForChannels("/calendar/primary", Collections.singleton("/calendar/other"));
        assertEquals(1, subs.size());
        
        ExternalSubscriptionDetails esd = subs.iterator().next();
        assertEquals(urlTest, esd.getSubscriptionUrl());
        assertEquals("other", esd.getContext());
    }
    
    @Test
    public void testGetCalendarSubscriptionChannelsForChannelsWithTZ() throws IdUnusedException {
        {
            Reference ref = mock(Reference.class);
            when(entityManager.newReference("/calendar/primary")).thenReturn(ref);
            when(ref.getContext()).thenReturn("primary");
        }

        when(siteService.isUserSite("primary")).thenReturn(false);

        {
            Reference ref = mock(Reference.class);
            when(entityManager.newReference("/calendar/other")).thenReturn(ref);
            when(ref.getContext()).thenReturn("other");
        }
        
        String urlTest = "https://test/url";
        String subscription = "/calendar/other/"+BaseExternalSubscriptionDetails.getIdFromSubscriptionUrl(urlTest);
        {
            Reference ref = mock(Reference.class);
            when(entityManager.newReference(subscription)).thenReturn(ref);
            when(ref.getContext()).thenReturn("other");
            when(ref.getId()).thenReturn(BaseExternalSubscriptionDetails.getIdFromSubscriptionUrl(urlTest));
        }

        {
            Site site = mock(Site.class);
            when(siteService.getSite("other")).thenReturn(site);
            ToolConfiguration configuration = mock(ToolConfiguration.class);
            when(site.getToolForCommonId(SCHEDULE_TOOL_ID)).thenReturn(configuration);
            Properties props = new Properties();
            props.setProperty(TC_PROP_SUBCRIPTIONS_WITH_TZ, subscription+SUBS_NAME_DELIMITER+"userId"+SUBS_NAME_DELIMITER+"userTimeZone"+SUBS_NAME_DELIMITER+"calName");
            when(configuration.getConfig()).thenReturn(props);
        }
        
        Set<ExternalSubscriptionDetails> subs = service.getCalendarSubscriptionChannelsForChannels("/calendar/primary", Collections.singleton("/calendar/other"));
        assertEquals(1, subs.size());
        
        ExternalSubscriptionDetails esd = subs.iterator().next();
        assertEquals(urlTest, esd.getSubscriptionUrl());
        assertEquals("other", esd.getContext());
        assertEquals("userId", esd.getUserId());
        assertEquals("userTimeZone", esd.getTzid());
    }
    
}
