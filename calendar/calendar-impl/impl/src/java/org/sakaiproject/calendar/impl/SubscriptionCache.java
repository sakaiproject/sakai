package org.sakaiproject.calendar.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.sakaiproject.calendar.api.ExternalSubscription;

/**
 * Cache of calendars. This class just maps the the requests onto an EhCache which actually
 * holds the calendars.
 * 
 * The key feature of this map is that it expires and refreshes data that's contained
 * within it. This way when a request for a calendar is made we don't have a long pause
 * while the remote ical is downloaded and parsed. We don't want todo this in the EhCache
 * expiration listener as that only fires on retrieval from the cache.
 * {@link net.sf.ehcache.event.CacheEventListener.notifyElementExpired(Ehcache, Element)}
 * <p>
 * 
 * We still want to use the LRU features of a cache so that we cache the most popular stuff.
 * Do we put an item into the cache which manages the refresh? No as we need iterate over the 
 * cache and refresh the items in it. We need to be careful that we don't keep existing items 
 * in the cache through our refreshing of them.
 * 
 *  Ok, so we can use getQuiet to retrieve a cache object without updating it's stats, this
 *  allows us to refresh items, but means we must bind to EhCache directly.
 * 
 * @author nfernandes
 */
public class SubscriptionCache {

	// The EhCache
	private Cache cache;

	public void init() {
		// By default caches created with EhCacheFactoryBean don't have statistics enabled
		// and there isn't an option to set them. The sakai version of the bean does allow
		// them to be set but starts up the sakai component manager.
		cache.setStatisticsEnabled(true);
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public ExternalSubscription get(String url) {
		Element element = (Element) cache.get(url);
		if (element != null) {
			ExternalSubscription sub = (ExternalSubscription) element.getObjectValue();
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
		Element element = new Element(url, copy(sub));
		cache.put(element);
	}

	private ExternalSubscription copy(ExternalSubscription sub) {
		// We clone this so that the caller can't muller the item in the cache
		// as we have examples of callers changing the context
		return new BaseExternalSubscription(sub.getSubscriptionName(),
				sub.getSubscriptionUrl(), sub.getContext(),
				sub.getCalendar(), sub.isInstitutional());
	}
}