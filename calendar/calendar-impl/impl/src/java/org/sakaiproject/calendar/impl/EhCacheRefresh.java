package org.sakaiproject.calendar.impl;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class scans an EhCache for items and refreshes them.
 * It uses the last update value to determine if the item in the cache needs updating.
 * When the new item it put back into the cache it keeps the original insertion date and 
 * the last access date to that expiration works correctly.
 * 
 * So we have the TTL of the cache
 * <p>
 * We want to use get/put on the cache so that if we were replicating the cache any changes in the cached
 * object would have the opportunity to be replicated across the cache.
 * 
 * @author buckett
 * 
 */
public class EhCacheRefresh {

	private static final Log LOG = LogFactory.getLog(EhCacheRefresh.class);

	// The EhCache we want to refresh
	private Cache cache;

	// The class that does the refreshing of the elements in the cache.
	private ElementRefresher refresher;

	// The timer that we schedule our refreshes on.
	private Timer timer;

	// Default is to refresh the items every hour
	private int updateInterval = 1000 * 60 * 60;

	private int minAge = 0;

	public void init() {
		if (minAge == 0) {
			minAge = updateInterval;
		}
		// Creates our timer
		TimerTask task = new TimerTask(){
			@Override
			public void run() {
				try {
					refresh();
				} catch (Exception e) {
					LOG.error("Problem refreshing the cache: "+ cache.getName(), e);
				}
			}

		};
		// Run is every so often
		timer.schedule(task, 0, updateInterval);
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public void setRefresher(ElementRefresher refresher) {
		this.refresher = refresher;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	/**
	 * @param updateInterval The update interval in minutes. 
	 */
	public void setUpdateInterval(int updateInterval) {
		if (!(updateInterval > 0)) {
			throw new IllegalArgumentException("Update interval must be greater than 0.");
		}
		this.updateInterval = updateInterval * 1000 * 60;
	}

	/**
	 * This defaults to the updateInterval is not set.
	 * @param minAge Minimum age of an item in the cache before it is updated. Specified in milliseconds.
	 */
	public void setMinAge(int minAge) {
		this.minAge = minAge;
	}

	/**
	 * This checks the elements in the cache and any for any that are older than the update interval
	 * we refresh them.
	 */
	public void refresh() {
		List<?> keys = cache.getKeysWithExpiryCheck();
		int updated = 0;
		for (Object key : keys) {
			Element element = cache.getQuiet(key);
			// May have expired since our copy of the keys was taken.
			if (element != null) {
				long refreshElementOlder = System.currentTimeMillis()
						- minAge;
				if (element.getLatestOfCreationAndUpdateTime() < refreshElementOlder) {
					Object replacement = refresher.updateElement(
							element.getKey(), element.getObjectValue());

					if (replacement != null) {
						// This has a threading issue in that someone else may update the entry
						// details while we are updating it and so we could lose a change.
						Element newElement = new Element(element.getKey(),
								replacement,
								element.getVersion(),
								element.getCreationTime(),
								element.getLastAccessTime(),
								element.getLastUpdateTime(),
								element.getHitCount());
						newElement.updateUpdateStatistics();

						// We don't use put() as it would reset all the cache statistics which
						// we want to maintain.
						cache.putQuiet(newElement);
						updated++;
						if (LOG.isDebugEnabled()) {
							LOG.debug("Updated  "+ element.getKey()+ " to "+ replacement + " in cache "+ cache.getName());
						}
					} else {
						cache.remove(key);
						if (LOG.isDebugEnabled()) {
							LOG.debug("Removed "+ key+ " as replacement was null from cache "+ cache.getName());
						}
					}
				}
			}
		}
		LOG.info("Refreshed "+ updated+ " of "+ keys.size()+ " in cache "+ cache.getName());
	}
}
