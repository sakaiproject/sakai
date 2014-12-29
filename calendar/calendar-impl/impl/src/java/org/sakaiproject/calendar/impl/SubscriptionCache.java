package org.sakaiproject.calendar.impl;

import org.sakaiproject.calendar.api.ExternalSubscription;
import org.sakaiproject.memory.api.Cache;

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
 * @author nfernandes
 */
public class SubscriptionCache {

	private Cache cache;

    public Cache getCache() {
        return cache;
    }

    SubscriptionCache(Cache cache) {
        this.cache = cache;
    }

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public ExternalSubscription get(String url) {
        ExternalSubscription sub = (ExternalSubscription) cache.get(url);
		if (sub != null) {
			// We clone this so that the caller can't muller the item in the cache
			// as we have examples of callers changing the context
			return copy(sub);
		}
		return null;
	}

	public void put(ExternalSubscription sub) {
		String url = sub.getSubscriptionUrl();
		if (url == null) {
			throw new IllegalArgumentException("The ExternalSubscription must have a URL set.");
		}
        cache.put(url, copy(sub));
	}

	private ExternalSubscription copy(ExternalSubscription sub) {
		// We clone this so that the caller can't muller the item in the cache
		// as we have examples of callers changing the context
		return new BaseExternalSubscription(sub.getSubscriptionName(),
				sub.getSubscriptionUrl(), sub.getContext(),
				sub.getCalendar(), sub.isInstitutional());
	}
}