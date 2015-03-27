/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/api/src/main/java/org/sakaiproject/memory/api/Cache.java $
 * $Id: Cache.java 308142 2014-04-11 22:33:07Z azeckoski@unicon.net $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.memory.api;

import java.util.Properties;

/**
 * Simple version of the Configuration which allows for setting some of the typical config settings
 *
 * This is designed to align with JSR-107
 * See https://jira.sakaiproject.org/browse/KNL-1162
 * Send questions to Aaron Zeckoski
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
public class SimpleConfiguration<K, V> implements Configuration<K, V> {

    boolean stats = false;
    long maxEntries = -1;
    long timeToLiveSeconds = -1;
    long timeToIdleSeconds = -1;
    boolean eternal = false;

    /**
     * Most basic config, simply indicates the max number of entries for this cache
     *
     * @param maxEntries max number of entries allowed in this cache
     */
    public SimpleConfiguration(long maxEntries) {
        this.maxEntries = maxEntries;
    }

    /**
     * More advanced config
     * Allows control of the times for cache entries to exist in the cache
     * Can be used to create an eternal cache (set TTL and TTI to 0)
     * NOTE: -1 (or less than 0) indicates to use the Default for the settings
     *
     * @param maxEntries max number of entries allowed in this cache
     * @param timeToLiveSeconds max time an entry can be in the cache, 0 indicates forever
     * @param timeToIdleSeconds max time before an entry is marked as idle (if not accessed), 0 indicates forever
     */
    public SimpleConfiguration(long maxEntries, long timeToLiveSeconds, long timeToIdleSeconds) {
        this(maxEntries);
        if (timeToLiveSeconds == 0 && timeToIdleSeconds == 0) {
            this.eternal = true;
        } else {
            this.timeToLiveSeconds = timeToLiveSeconds;
            this.timeToIdleSeconds = timeToIdleSeconds;
            this.eternal = false;
        }
    }

    @Override
    public boolean isStatisticsEnabled() {
        return stats;
    }

    /**
     * Enabled statistics collection for this cache
     * @param stats true to enable (off by default)
     */
    public void setStatisticsEnabled(boolean stats) {
        this.stats = stats;
    }

    @Override
    public long getMaxEntries() {
        return maxEntries;
    }

    @Override
    public long getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    @Override
    public long getTimeToIdleSeconds() {
        return timeToIdleSeconds;
    }

    @Override
    public boolean isEternal() {
        return eternal;
    }

    @Override
    public Properties getAll() {
        Properties p = new Properties();
        p.put("maxEntries", maxEntries);
        p.put("timeToLiveSeconds", timeToLiveSeconds);
        p.put("timeToIdleSeconds", timeToIdleSeconds);
        p.put("eternal", eternal);
        p.put("statisticsEnabled", stats);
        return p;
    }

    @Override
    public String toString() {
        return "{" +
                "stats:" + stats +
                ", maxEntries:" + maxEntries +
                ", timeToLiveSeconds:" + timeToLiveSeconds +
                ", timeToIdleSeconds:" + timeToIdleSeconds +
                ", eternal:" + eternal +
                '}';
    }
}
