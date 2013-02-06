package org.sakaiproject.calendar.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EhCacheRefreshTest {

	private EhCacheRefresh refresh;
	private CacheManager manager;
	private Cache cache;

	@Before
	public void setUp() throws Exception {
		refresh = new EhCacheRefresh();
		manager = CacheManager.getInstance();
		manager.addCache("test");
		cache = manager.getCache("test");
		refresh.setCache(cache);
	}

	@After
	public void tearDown() throws Exception {
		manager.shutdown();
	}

	@Test
	public void testDeleting() {

		refresh.setRefresher(new ElementRefresher() {

			public Object updateElement(Object key, Object value) {
				// Throw things out of the cache.
				return null;
			}
		});

		cache.put(new Element("key", "value"));

		// Don't actually look at anything.
		refresh.setMinAge(100);
		refresh.refresh();

		Element actual = cache.get("key");
		assertNotNull(actual);
		assertEquals("value", actual.getValue());

		refresh.setMinAge(-100);
		refresh.refresh();
		assertNull(cache.get("key"));
	}

	@Test
	public void testKeeping() {
		refresh.setRefresher(new ElementRefresher() {

			public Object updateElement(Object key, Object value) {
				return "new";
			}
		});

		cache.put(new Element("key", "value"));

		// Check everything.
		refresh.setMinAge(-1);
		refresh.refresh();

		Element actual = cache.get("key");
		assertNotNull(actual);
		assertEquals("new", actual.getValue());
	}

}
