/**********************************************************************************
 * $URL$
 * $Id$
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

import java.io.Serializable;
import java.util.Properties;

/**
 * This is an abstraction of the general concept of a cache configuration in Sakai
 * This only supports a limited set of config keys to remain compatible with the greatest possible number of caching systems
 * <br/>
 * This is designed to align with JSR-107
 * https://github.com/jsr107/jsr107spec/blob/master/src/main/java/javax/cache/configuration/Configuration.java
 * See https://jira.sakaiproject.org/browse/KNL-1162
 * Send questions to Aaron Zeckoski
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
public interface Configuration<K, V> extends Serializable { // Configuration<K, V> extends Serializable

    //Class<K> getKeyType();
    //Class<V> getValueType();
    //boolean isStoreByValue();
    //boolean isReadThrough();
    //boolean isWriteThrough();

    /**
     * Checks whether statistics collection is enabled in this cache.
     * <p>
     * The default value is <code>false</code>.
     *
     * @return true if statistics collection is enabled
     */
    boolean isStatisticsEnabled();

    //boolean isManagementEnabled();
    //Iterable<CacheEntryListenerConfiguration<K, V>> getCacheEntryListenerConfigurations();
    //Factory<CacheLoader<K, V>> getCacheLoaderFactory();
    //Factory<CacheWriter<? super K, ? super V>> getCacheWriterFactory();
    //Factory<ExpiryPolicy> getExpiryPolicyFactory();

    // SAKAI SPECIFIC

    /**
     * Max entries allowed in this cache before it begins to event old entries.
     * 0 indicates unlimited.
     * @return the maximum number of entries allowed
     */
    long getMaxEntries();

    /**
     * Sets the time to idle for an element before it expires.
     * i.e. The maximum amount of time between accesses before an entry expires
     * Only used if the entry is not eternal.
     * 0 means that an entry can be idle forever
     * @return the number of seconds before an entry is idle
     */
    long getTimeToLiveSeconds();

    /**
     * Sets the time to live for an element before it expires.
     * i.e. The maximum time between creation time and when an entry expires.
     * Only used if the entry is not eternal.
     * 0 means that an entry can live in the cache forever
     * @return the number of seconds before an entry is dead (not alive)
     */
    long getTimeToIdleSeconds();

    /**
     * Whether entries are eternal. If eternal, timeouts are ignored and the entry is never expired.
     * @return true if the entry is eternal
     */
    boolean isEternal();

    /**
     * Get all config data as a set of Properties
     * @return all known config data as key value pairs
     */
    Properties getAll();

}
