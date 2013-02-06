package org.sakaiproject.calendar.impl;

import static org.junit.Assert.*;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// Simple tests of the cache that we're doing the right thing.
public class SubscriptionCacheTest {

	private final static String URL = "http://example.org/";;


	private SubscriptionCache cache;
	private Cache ehCache;

	@Before
	public void setUp() throws Exception {
		cache = new SubscriptionCache();
		CacheManager.getInstance().addCache("test");
		ehCache =  CacheManager.getInstance().getCache("test");
		cache.setCache(ehCache);
	}

	@After
	public void tearDown() throws Exception {
		CacheManager.getInstance().shutdown();
	}

	@Test
	public void testEmpty() {
		// Empty cache.
		assertNull(cache.get(""));
	}

	@Test
	public void testRoundTrip() {
		BaseExternalSubscription sub = new BaseExternalSubscription();
		sub.setSubscriptionUrl(URL);
		cache.put(sub);
		assertNotNull(cache.get(URL));
		assertEquals(URL, cache.get(URL).getSubscriptionUrl());
	}

	@Test
	public void testCacheClone() {
		// Check it's a different object we get back.
		BaseExternalSubscription sub = new BaseExternalSubscription();
		sub.setSubscriptionUrl(URL);
		cache.put(sub);
		assertNotSame(sub, cache.get(URL));

		// Check changes aren't coming through
		cache.get(URL).setContext("context");
		assertNull(cache.get(URL).getContext());

		// And changing the original doesn't do anything
		sub.setContext("context");
		assertNull(cache.get(URL).getContext());
	}


}
