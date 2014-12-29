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

package org.sakaiproject.memory.api;

import java.util.EventListener;
import java.util.EventObject;

/**
 * This allows a developer to track changes that are happening in a cache. This is basically a listener
 * for cache events which is attached to the cache via methods in the {@link org.sakaiproject.memory.api.Cache}
 * or the {@link org.sakaiproject.memory.api.MemoryService}.
 *
 * This emulates the JSR-107 interfaces for cache events handling.
 * Based on https://github.com/jsr107/jsr107spec/tree/master/src/main/java/javax/cache/event
 *
 * Invoked when cache entries change or some cache event happens.
 *
 * @param <K> the type of key in the cache
 * @param <V> the type of value in the cache
 *
 * See https://jira.sakaiproject.org/browse/KNL-1162
 * Send questions to Aaron Zeckoski
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
public interface CacheEventListener<K, V> extends EventListener {

    /**
     * Evaluates specified {@link CacheEntryEvent}.
     *
     * @param event the event that occurred
     * @return true if the evaluation passes, otherwise false.
     *         The effect of returning true is that listener will be invoked
     * @throws java.lang.RuntimeException if there is problem executing the listener
     */
    boolean evaluate(CacheEntryEvent<? extends K, ? extends V> event);

    /**
     * Called after one or more entries have been created.
     *
     * @param events The entries just created.
     * @throws java.lang.RuntimeException if there is problem executing the listener
     */
    void onCreated(Iterable<CacheEntryEvent<? extends K, ? extends V>> events);

    /**
     * Called after one or more entries have been updated.
     *
     * @param events The entries just updated.
     * @throws java.lang.RuntimeException if there is problem executing the listener
     */
    void onUpdated(Iterable<CacheEntryEvent<? extends K, ? extends V>> events);

    /**
     * Called after one or more entries have been expired by the cache. This is not
     * necessarily when an entry is expired, but when the cache detects the expiry.
     *
     * @param events The entries just removed.
     * @throws java.lang.RuntimeException if there is problem executing the listener
     */
    void onExpired(Iterable<CacheEntryEvent<? extends K, ? extends V>> events);

    /**
     * Called after one or more entries have been removed. If no entry existed for
     * a key an event is not raised for it.
     *
     * @param events The entries just removed.
     * @throws java.lang.RuntimeException if there is problem executing the listener
     */
    void onRemoved(Iterable<CacheEntryEvent<? extends K, ? extends V>> events);

    /**
     * The type of event received by the listener.
     * Based on https://github.com/jsr107/jsr107spec/blob/master/src/main/java/javax/cache/event/EventType.java
     */
    public static enum EventType {
        /**
         * An event type indicating that the cache entry was created.
         */
        CREATED,
        /**
         * An event type indicating that the cache entry was updated. i.e. a previous
         * mapping existed
         */
        UPDATED,
        /**
         * An event type indicating that the cache entry was removed.
         */
        REMOVED,
        /**
         * An event type indicating that the cache entry has expired.
         */
        EXPIRED
    }

    /**
     * A Cache entry event
     * Based on https://github.com/jsr107/jsr107spec/blob/master/src/main/java/javax/cache/event/CacheEntryEvent.java
     * @param <K> the type of key
     * @param <V> the type of value
     */
    public static class CacheEntryEvent<K, V> extends EventObject {

        protected K key;
        protected V value;
        protected EventType eventType;
        protected V oldValue;

        /**
         * Constructs a cache entry event from a given cache as source
         *
         * @param source the cache that originated the event
         * @param key cache key for this event
         * @param value cache value for this event (if there is one)
         * @param eventType event type
         */
        public CacheEntryEvent(Cache source, K key, V value, EventType eventType) {
            super(source);
            this.key = key;
            this.value = value;
            this.eventType = eventType;
        }

        /**
         * Returns the key corresponding to this entry.
         *
         * @return the key corresponding to this entry
         */
        public K getKey() {
            return key;
        }

        /**
         * Returns the value stored in the cache when this entry was created.
         *
         * @return the value corresponding to this entry
         */
        public V getValue() {
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final Cache getSource() {
            return (Cache) super.getSource();
        }

        /**
         * Returns the previous value, that existed prior to the
         * modification of the Entry value.
         *
         * @return the previous value or <code>null</code> if there was no previous value
         */
        public V getOldValue() {
            return oldValue;
        }

        /**
         * Whether the old value is available.
         *
         * @return true if the old value is populated
         */
        public boolean isOldValueAvailable() {
            return oldValue != null;
        }

        /**
         * Gets the event type of this event
         *
         * @return the event type.
         */
        public final EventType getEventType() {
            return eventType;
        }
    }
}
