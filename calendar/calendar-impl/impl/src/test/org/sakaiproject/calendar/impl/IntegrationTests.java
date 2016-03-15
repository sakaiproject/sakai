package org.sakaiproject.calendar.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.calendar.api.ExternalSubscription;
import org.sakaiproject.calendar.impl.BaseExternalSubscription;
import org.sakaiproject.calendar.impl.SubscriptionCache;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations={"/calendar-caches.xml", "/test-resources.xml"})
public class IntegrationTests extends AbstractJUnit4SpringContextTests {

	private SubscriptionCache cache;

	@Before
	public void onSetUp() {
		cache = (SubscriptionCache) applicationContext.getBean("org.sakaiproject.calendar.impl.BaseExternalCacheSubscriptionService.institutionalCache");
	}

	@Test
	public void testNothing() {
		Assert.assertNull(cache.get("not-in-cache"));
	}

	@Test
	public void testCacheRoundtrip() throws InterruptedException {
		ExternalSubscription subscription = new BaseExternalSubscription();
		subscription.setSubscriptionUrl("http://example.com");
		subscription.setSubscriptionName("Example");
		subscription.setContext("context");
		subscription.setInstitutional(false);
		cache.put(subscription);
		Assert.assertEquals(subscription, cache.get("http://example.com"));
	}

}
