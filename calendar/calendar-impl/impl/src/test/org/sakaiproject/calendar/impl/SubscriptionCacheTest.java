package org.sakaiproject.calendar.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.sakaiproject.memory.api.Cache;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test the early timeout of failures.
 */
public class SubscriptionCacheTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private SubscriptionCache subscriptionCache;
    private Clock clock;

    @Mock
    private Cache cache;

    @Before
    public void setUp() {
        // Lock the clock.
        clock =  Clock.fixed(Instant.now(), ZoneId.systemDefault());
        subscriptionCache = new SubscriptionCache(cache, clock);
    }

    @Test
    public void testCachePut() {
        BaseExternalSubscriptionDetails item = new BaseExternalSubscriptionDetails(
                "test", "http://example.com/", "siteId", null, false);
        subscriptionCache.put(item);
        verify(cache).put("http://example.com/", item);
    }

    @Test
    public void testCacheGetMissing() {
        assertNull(subscriptionCache.get("http://example.com/"));
        verify(cache).get("http://example.com/");
    }

    @Test
    public void testCacheGetPresent() {
        BaseExternalSubscriptionDetails value = new BaseExternalSubscriptionDetails(
                "test", "http://example.com/", "siteId", null, false, true, null, Instant.now(clock));
        when(cache.get("http://example.com/")).thenReturn(value);
        assertEquals(value, subscriptionCache.get("http://example.com/"));
        assertEquals(value, subscriptionCache.get("http://example.com/"));
    }

    @Test
    public void testCacheGetFailureNew() {
        BaseExternalSubscriptionDetails value = new BaseExternalSubscriptionDetails(
                "test", "http://example.com/", "siteId", null, false, false, null, Instant.now(clock));
        when(cache.get("http://example.com/")).thenReturn(value);
        // Still not expired.
        assertEquals(value, subscriptionCache.get("http://example.com/"));
    }

    @Test
    public void testCacheGetFailureOld() {
        BaseExternalSubscriptionDetails value = new BaseExternalSubscriptionDetails(
                "test", "http://example.com/", "siteId", null, false, false, null, Instant.now(clock).minus(2, ChronoUnit.MINUTES));
        when(cache.get("http://example.com/")).thenReturn(value);
        // Now should have expired.
        assertNull(subscriptionCache.get("http://example.com/"));
    }

}
