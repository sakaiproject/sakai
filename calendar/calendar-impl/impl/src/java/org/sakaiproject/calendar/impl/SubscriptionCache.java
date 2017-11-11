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

import org.sakaiproject.calendar.api.ExternalSubscriptionDetails;
import org.sakaiproject.memory.api.Cache;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Cache of calendars. This class just maps the the requests onto a Cache which actually
 * holds the calendars.
 * 
 * Ideally a request for a calendar is made and data is preloaded so we don't have a long pause
 * while the remote ical is downloaded and parsed. We don't want to do this in the Cache
 * expiration listener as that only fires on retrieval from the cache.
 * <p>
 * 
 * We still want to use the LRU features of a cache so that we cache the most popular stuff.
 * Do we put an item into the cache which manages the refresh? No as we need iterate over the 
 * cache and refresh the items in it. We need to be careful that we don't keep existing items 
 * in the cache through our refreshing of them.
 *
 * Handles early expiry of failed lookups so that we re-try more often. By default we only cache
 * faul
 *
 * @author nfernandes
 */
public class SubscriptionCache {

	private Clock clock;

	// Although using EhCache directly we could have TTLs on each Element this is outside the
	// JSR-107 spec so ties us to EhCache too tightly.
	private Cache<String, BaseExternalSubscriptionDetails> cache;

	public Cache<String, BaseExternalSubscriptionDetails> getCache() {
		return cache;
	}

	SubscriptionCache(Cache<String, BaseExternalSubscriptionDetails> cache, Clock clock) {
		this.cache = cache;
		this.clock = clock;
	}

	public BaseExternalSubscriptionDetails get(String url) {
		BaseExternalSubscriptionDetails sub = cache.get(url);
		if (sub != null) {
			// Check if we should early expire it, we only cache failed lookups for a short time.
			if (sub.getState().equals(ExternalSubscriptionDetails.State.FAILED)) {
				if (Instant.now(clock).minus(1, ChronoUnit.MINUTES).isAfter(sub.getRefreshed())) {
					return null;
				}
			}
			// We clone this so that the caller can't muller the item in the cache
			// as we have examples of callers changing the context
			return new BaseExternalSubscriptionDetails(sub);
		}
		return null;
	}

	public void put(BaseExternalSubscriptionDetails sub) {
		String url = sub.getSubscriptionUrl();
		if (url == null) {
			throw new IllegalArgumentException("The ExternalSubscriptionDetails must have a URL set.");
		}
		cache.put(url, new BaseExternalSubscriptionDetails(sub));
	}

}