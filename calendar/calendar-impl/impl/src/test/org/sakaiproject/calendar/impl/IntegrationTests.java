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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.calendar.api.ExternalSubscriptionDetails;
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
		ExternalSubscriptionDetails subscription = new BaseExternalSubscriptionDetails();
		Assert.assertEquals(subscription, cache.get("http://example.com"));
	}

}
