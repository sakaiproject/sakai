/******************************************************************************
 * $URL$
 * $Id$
 ******************************************************************************
 *
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************/

package org.sakaiproject.memory.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheLoader;
import org.sakaiproject.memory.api.CacheStatistics;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Contains Hazelcast implementation related to a HC Map based cache.
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
public class SakaiHazelcastCache<K, V> extends BasicMapCache<K, V> {
    final Log log = LogFactory.getLog(SakaiHazelcastCache.class);

    private IMap<K, V> hcCache;
    private HazelcastInstance hcInstance;
    private Cache ehcache;
    
    /**
     * Construct the Cache
     * Set the listeners and cache refreshers later
     *
     * @param hcMap the hazelcast Map (IMap)
     */
    public SakaiHazelcastCache(IMap hcMap, HazelcastInstance hcInstance, Cache ems) {
        super(hcMap.getName());
        this.hcInstance = hcInstance;
        hcCache = hcMap;
        this.ehcache = ems;
    }

    @Override
    public String getName() {
        return hcCache.getName();
    }

    @Override
    public void registerCacheEventListener(org.sakaiproject.memory.api.CacheEventListener cacheEventListener) {
        this.cacheEventListener = cacheEventListener;
    }

    @Override
    public String getDescription() {
        return "HCMap("+getName()+"):"+hcCache.getLocalMapStats(); // TODO we really want the cluster stats
    }

    @Override
    public void attachLoader(CacheLoader cacheLoader) {
        this.loader = cacheLoader;
    }

    @Override
    public CacheStatistics getCacheStatistics() {
        return new CacheStatistics() {
            @Override
            public long getCacheHits() {
                return 0;
            } // TODO get real numbers
            @Override
            public long getCacheMisses() {
                return 0;
            } // TODO get real numbers
        };
    }

    @Override
    public Properties getProperties(boolean includeExpensiveDetails) {
        Properties p = new Properties();
        p.put("name", getName());
        p.put("class", this.getClass().getSimpleName());
        // TODO fill in more info about the cache (like stats)
        return p;
    }

    // BULK operations - KNL-1246

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        //noinspection unchecked
        return hcCache.getAll((Set<K>) keys);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        //noinspection unchecked
        hcCache.putAll(map);
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        if (!keys.isEmpty()) {
            for (K key : keys) {
                if (key == null) {
                    throw new NullPointerException("keys Set for removeAll cannot contain nulls (but it does)");
                }
                hcCache.remove(key);
            }
        }
    }

    @Override
    public void close() {
        this.hcCache.destroy();
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
       return (T) ehcache.unwrap(clazz);
    }
    
    //    @Override
    //    public Cache getL2CacheForHibernate(String name, Properties properties) {
    //        return new HazelcastCache(hcInstance, name, properties);
    //    }
}