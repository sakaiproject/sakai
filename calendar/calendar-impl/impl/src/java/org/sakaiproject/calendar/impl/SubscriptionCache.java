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

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.cache.Cache;

/**
 * Cache of external calendar subscription snapshots. This class just maps requests onto a
 * Spring/Ignite-backed Cache which actually holds the (plain, marshalable) snapshots -
 * conversion to/from the live, non-marshalable ExternalCalendarSubscription object graph is
 * done by the caller (BaseExternalCalendarSubscriptionService), since only it can construct
 * its own non-static inner classes.
 * <p>
 * This is intentionally a distributed (not local) cache: the cached value is the result of
 * fetching an external URL, which is the same regardless of which cluster node fetches it, so
 * distributing it means only one node has to pay the network-fetch cost per refresh interval
 * instead of every node paying it independently.
 * <p>
 * Handles early expiry of failed lookups so that we re-try more often. By default we only
 * cache failures for a short time.
 *
 * @author nfernandes
 */
public class SubscriptionCache {

	private final Clock clock;

	private final Cache cache;

	SubscriptionCache(Cache cache, Clock clock) {
		this.cache = cache;
		this.clock = clock;
	}

	public ExternalCalendarSubscriptionSnapshot get(String url) {
		ExternalCalendarSubscriptionSnapshot snapshot = cache.get(url, ExternalCalendarSubscriptionSnapshot.class);
		if (snapshot != null && !snapshot.ok) {
			// Check if we should early expire it, we only cache failed lookups for a short time.
			if (Instant.now(clock).minus(1, ChronoUnit.MINUTES).isAfter(snapshot.refreshed)) {
				return null;
			}
		}
		return snapshot;
	}

	public void put(ExternalCalendarSubscriptionSnapshot snapshot) {
		if (snapshot.subscriptionUrl == null) {
			throw new IllegalArgumentException("The ExternalCalendarSubscriptionSnapshot must have a URL set.");
		}
		cache.put(snapshot.subscriptionUrl, snapshot);
	}

}
