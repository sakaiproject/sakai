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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheLoader;
import org.sakaiproject.memory.api.CacheStatistics;

import java.util.Properties;

/**
 * Contains general common implementation info related to a cache.
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
public abstract class BasicCache implements Cache {
    final Log log = LogFactory.getLog(BasicCache.class);
    /**
     * the name for this cache
     */
    protected String cacheName = "cache";
    /**
     * Optional object for dealing with cache events
     */
    protected org.sakaiproject.memory.api.CacheEventListener cacheEventListener = null;
    /**
     * Optional object that will deal with loading missing entries into the cache on get()
     */
    protected CacheLoader loader = null;
    /**
     * Indicates that a cache is distributed if true
     */
    protected boolean distributed = false;

    public void setDistributed(boolean distributed) {
        this.distributed = distributed;
    }

    @Override
    public boolean isDistributed() {
        return distributed;
    }

    /**
     * Construct the Cache
     * Set the listeners and cache refreshers later
     *
     * @param name the name for this cache
     */
    public BasicCache(String name) {
        this.cacheName = name;
    }

    @Override
    public String getName() {
        return this.cacheName;
    }

    @Override
    public void registerCacheEventListener(org.sakaiproject.memory.api.CacheEventListener cacheEventListener) {
        this.cacheEventListener = cacheEventListener;
    }

    @Override
    public String getDescription() {
        return "Basic ("+getName()+")";
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
            }
            @Override
            public long getCacheMisses() {
                return 0;
            }
        };
    }

    @Override
    public Properties getProperties(boolean includeExpensiveDetails) {
        Properties p = new Properties();
        p.put("name", getName());
        p.put("class", this.getClass().getSimpleName());
        return p;
    }


    // **************************************************************************
    // DEPRECATED methods - REMOVE THESE
    // **************************************************************************

    /**
     * @deprecated REMOVE THIS
     */
    public void destroy() {
        this.close();
    }

    /**
     * @deprecated REMOVE THIS
     */
    public void put(Object key, Object payload, int duration) {
        put(String.valueOf(key), payload);
    }

}
