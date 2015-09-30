/**
 * Copyright 2015 Apereo Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.entitybroker.providers.model;

import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Statistics;

/**
 * EntityCache
 *
 * @author Earle Nietzel
 * Created on Sep 11, 2013
 *
 */
public class EntityCache {

	/**
	 * Fast but not accurate setting.
	 */
	public static final int STATISTICS_ACCURACY_NONE = 0;

	/**
	 * Best efforts accuracy setting.
	 */
	public static final int STATISTICS_ACCURACY_BEST_EFFORT = 1;

	/**
	 * Guaranteed accuracy setting.
	 */
	public static final int STATISTICS_ACCURACY_GUARANTEED = 2;

	@EntityId
	private String id;

	private String statisticsAccuracy;
	private long cacheHits;
	private long onDiskHits;
	private long offHeapHits;
	private long inMemoryHits;
	private long cacheMisses;
	private long onDiskMisses;
	private long offHeapMisses;
	private long inMemoryMisses;
	private long cacheSize;
	private long memoryStoreSize;
	private long offHeapStoreSize;
	private long diskStoreSize;
	private float averageGetTime;
	private long evictionCount;
	private long searchesPerSecond;
	private long averageSearchTime;
	private long writerQueueLength;
	private long cacheHitRatio;
	private long diskHitRatio;
	private long memoryHitRatio;

	public EntityCache() {
	}

	public EntityCache(Ehcache cache) {
		if (cache == null) {
			throw new UnsupportedOperationException();
		}
		Statistics stats = cache.getStatistics();

		this.id = cache.getName();

		this.cacheHits = stats.getCacheHits();
		this.onDiskHits = stats.getOnDiskHits();
		this.offHeapHits = stats.getOffHeapHits();
		this.inMemoryHits =	stats.getInMemoryHits();
		this.cacheMisses = stats.getCacheMisses();
		this.onDiskMisses = stats.getOnDiskMisses();
		this.offHeapMisses = stats.getOffHeapMisses();
		this.inMemoryMisses = stats.getInMemoryMisses();
		this.cacheSize = stats.getObjectCount();
		this.averageGetTime = stats.getAverageGetTime();
		this.evictionCount = stats.getEvictionCount();
		this.memoryStoreSize = stats.getMemoryStoreObjectCount();
		this.offHeapStoreSize = stats.getOffHeapStoreObjectCount();
		this.diskStoreSize = stats.getDiskStoreObjectCount();
		this.searchesPerSecond = stats.getSearchesPerSecond();
		this.averageSearchTime = stats.getAverageSearchTime();
		this.writerQueueLength = stats.getWriterQueueSize();
		this.cacheHitRatio = (cacheHits + cacheMisses) > 0 ? (100L * cacheHits) / (cacheHits + cacheMisses) : 0;
		this.diskHitRatio = (onDiskHits + onDiskMisses) > 0 ? (100L * this.onDiskHits) / (onDiskHits + onDiskMisses) : 0;
		this.memoryHitRatio = (inMemoryHits + inMemoryMisses) > 0 ? (100L * this.inMemoryHits) / (inMemoryHits + inMemoryMisses) : 0;

		switch(stats.getStatisticsAccuracy()) {
			case STATISTICS_ACCURACY_NONE: 			this.statisticsAccuracy = "none"; break;
			case STATISTICS_ACCURACY_BEST_EFFORT: 	this.statisticsAccuracy = "best effort"; break;
			case STATISTICS_ACCURACY_GUARANTEED: 	this.statisticsAccuracy = "guarunteed"; break;
			default: this.statisticsAccuracy = "unknown";
		}
	}

	public String getId() {
		return id;
	}

	public String getStatisticsAccuracy() {
		return statisticsAccuracy;
	}

	public long getCacheHits() {
		return cacheHits;
	}

	public long getOnDiskHits() {
		return onDiskHits;
	}

	public long getOffHeapHits() {
		return offHeapHits;
	}

	public long getInMemoryHits() {
		return inMemoryHits;
	}

	public long getCacheMisses() {
		return cacheMisses;
	}

	public long getOnDiskMisses() {
		return onDiskMisses;
	}

	public long getOffHeapMisses() {
		return offHeapMisses;
	}

	public long getInMemoryMisses() {
		return inMemoryMisses;
	}

	public long getCacheSize() {
		return cacheSize;
	}

	public long getMemoryStoreSize() {
		return memoryStoreSize;
	}

	public long getOffHeapStoreSize() {
		return offHeapStoreSize;
	}

	public long getDiskStoreSize() {
		return diskStoreSize;
	}

	public float getAverageGetTime() {
		return averageGetTime;
	}

	public long getEvictionCount() {
		return evictionCount;
	}

	public long getSearchesPerSecond() {
		return searchesPerSecond;
	}

	public long getAverageSearchTime() {
		return averageSearchTime;
	}

	public long getWriterQueueLength() {
		return writerQueueLength;
	}

	public long getCacheHitRatio() {
		return cacheHitRatio;
	}

	public long getDiskHitRatio() {
		return diskHitRatio;
	}

	public long getMemoryHitRatio() {
		return memoryHitRatio;
	}
}