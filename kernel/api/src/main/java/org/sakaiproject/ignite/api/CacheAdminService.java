/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.ignite.api;

/**
 * Admin-facing diagnostic and maintenance operations for the Ignite-backed Spring
 * {@code CacheManager} that replaced {@code org.sakaiproject.memory.api.MemoryService}.
 *
 * This is the single, shared place for the aggregate (all-cache) operations that
 * Spring's {@code CacheManager} doesn't provide (it only exposes per-cache
 * {@code getCache(name)}), so that admin-facing consumers (the "Memory" admin tool,
 * the {@code /direct/ignite/...} entity provider) don't each reimplement their own
 * cache-report/clear-all logic against the live Ignite cluster.
 */
public interface CacheAdminService {

    /**
     * @return the JVM's currently free heap memory, in bytes.
     */
    long getAvailableMemory();

    /**
     * Clear every cache's entries (not the caches themselves).
     */
    void clearAllCaches();

    /**
     * Force an eviction sweep of expired entries across every cache.
     *
     * Ignite's caches are configured with an eager TTL policy, so expired entries are
     * already swept in the background without this being called - this exists only to
     * preserve the old Memory admin tool's "Evict" action, and is a no-op today.
     */
    void evictExpiredMembers();

    /**
     * @return a human-readable report of JVM memory, cluster info, and every cache's
     * name and metrics.
     */
    String getStatus();
}
