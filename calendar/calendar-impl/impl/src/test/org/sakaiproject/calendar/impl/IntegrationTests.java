package org.sakaiproject.calendar.impl;

import org.sakaiproject.calendar.api.ExternalSubscription;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class IntegrationTests extends AbstractDependencyInjectionSpringContextTests {

	private SubscriptionCache cache;

	protected String[] getConfigPaths() {
		return new String[]{"/calendar-caches.xml", "/test-resources.xml"};
	}

	protected void onSetUp() {
		cache = (SubscriptionCache) getApplicationContext().getBean("org.sakaiproject.calendar.impl.BaseExternalCacheSubscriptionService.institutionalCache");
	}

	public void testNothing() {
		assertNull(cache.get("not-in-cache"));
	}

	public void testCacheRoundtrip() throws InterruptedException {
		ExternalSubscription subscription = new BaseExternalSubscription();
		subscription.setSubscriptionUrl("http://example.com");
		subscription.setSubscriptionName("Example");
		subscription.setContext("context");
		subscription.setInstitutional(false);
		cache.put(subscription);
		assertEquals(subscription, cache.get("http://example.com"));
	}

}
