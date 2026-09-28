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
package org.sakaiproject.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.cache.CacheMetrics;

import org.sakaiproject.ignite.api.CacheAdminService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Ignite-backed implementation of {@link CacheAdminService}, sharing the same live
 * {@code sakaiIgnite} cluster handle as {@link IgniteSpringCacheManager}.
 */
@Slf4j
public class CacheAdminServiceImpl implements CacheAdminService {

    @Setter private Ignite sakaiIgnite;

    @Override
    public long getAvailableMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    @Override
    public void clearAllCaches() {
        sakaiIgnite.cacheNames().forEach(name -> sakaiIgnite.cache(name).clear());
        log.info("Cleared all {} caches", sakaiIgnite.cacheNames().size());
    }

    @Override
    public void evictExpiredMembers() {
        // Ignite's caches are configured with eagerTtl, so expired entries are already
        // swept in the background - nothing to do here.
    }

    @Override
    public String getStatus() {
        StringBuilder sb = new StringBuilder();

        Runtime runtime = Runtime.getRuntime();
        sb.append("JVM Memory: free=").append(runtime.freeMemory())
                .append(", total=").append(runtime.totalMemory())
                .append(", max=").append(runtime.maxMemory())
                .append("\n\n");

        IgniteCluster cluster = sakaiIgnite.cluster();
        sb.append("Ignite Cluster: name=").append(sakaiIgnite.name())
                .append(", id=").append(cluster.id())
                .append(", state=").append(cluster.state())
                .append(", nodes=").append(cluster.nodes().size())
                .append("\n\n");

        sb.append("Caches:\n");
        sakaiIgnite.cacheNames().stream().sorted().forEach(name -> {
            CacheMetrics metrics = sakaiIgnite.cache(name).metrics();
            sb.append("  ").append(name)
                    .append(": size=").append(metrics.getCacheSize())
                    .append(", hits=").append(metrics.getCacheHits())
                    .append(", misses=").append(metrics.getCacheMisses())
                    .append(", puts=").append(metrics.getCachePuts())
                    .append(", removals=").append(metrics.getCacheRemovals())
                    .append("\n");
        });

        return sb.toString();
    }
}
