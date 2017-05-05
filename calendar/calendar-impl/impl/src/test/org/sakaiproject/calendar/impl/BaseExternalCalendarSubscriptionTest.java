package org.sakaiproject.calendar.impl;

import net.sf.ehcache.CacheManager;
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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService.SAK_PROP_EXTSUBSCRIPTIONS_ENABLED;
import static org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService.TC_PROP_SUBCRIPTIONS;
import static org.sakaiproject.calendar.impl.BaseExternalCalendarSubscriptionService.SCHEDULE_TOOL_ID;

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

        when(importer.doImport(any(), any(), any(),any())).thenReturn(Collections.singletonList(event));

        assertNotNull(service.getCalendarSubscription(referenceString));
        // This should come from the cache
        assertNotNull(service.getCalendarSubscription(referenceString));
        assertNotNull(service.getCalendarSubscription(referenceString));
        // Only parsed once.
        verify(importer, times(1)).doImport(any(), any(), any(), any());
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

        {
            Site site = mock(Site.class);
            when(siteService.getSite("other")).thenReturn(site);
            ToolConfiguration configuration = mock(ToolConfiguration.class);
            when(site.getToolForCommonId(SCHEDULE_TOOL_ID)).thenReturn(configuration);
            Properties props = new Properties();
            props.setProperty(TC_PROP_SUBCRIPTIONS, "/calendar/external/subscription");
            when(configuration.getConfig()).thenReturn(props);

            Reference ref = mock(Reference.class);
            when(entityManager.newReference("/calendar/external/subscription")).thenReturn(ref);
            when(ref.getId()).thenReturn("/calendar/external/subscription");
        }
        Set<String> subs = service.getCalendarSubscriptionChannelsForChannels("/calendar/primary", Collections.singleton("/calendar/other"));
        assertEquals(1, subs.size());
        assertEquals("/calendar/external/subscription", subs.iterator().next());
    }
}
