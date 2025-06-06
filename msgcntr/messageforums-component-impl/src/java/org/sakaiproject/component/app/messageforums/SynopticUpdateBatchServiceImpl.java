/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.component.app.messageforums;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.SynopticUpdateBatchService;
import org.sakaiproject.component.api.ServerConfigurationService;

/**
 * Implementation of SynopticUpdateBatchService that batches synoptic updates to reduce
 * database contention and optimistic lock conflicts.
 */
@Slf4j
@Setter
public class SynopticUpdateBatchServiceImpl implements SynopticUpdateBatchService {

    private static final String BATCH_INTERVAL_PROPERTY = "msgcntr.synoptic.batch.interval.seconds";

    private static final long DEFAULT_BATCH_INTERVAL_SECONDS = 10;

    private SynopticMsgcntrManager synopticMsgcntrManager;
    private ServerConfigurationService serverConfigurationService;

    private final Map<String, SynopticUpdate> pendingUpdates = new ConcurrentHashMap<>();
    private final AtomicBoolean batchingStarted = new AtomicBoolean(false);
    private ScheduledExecutorService batchExecutor;

    /**
     * Inner class to hold synoptic update data
     */
    private static class SynopticUpdate {
        private final String userId;
        private final String siteId;
        private volatile Integer newForumCount;
        private volatile Integer newMessageCount;
        private volatile long lastUpdateTime;

        public SynopticUpdate(String userId, String siteId) {
            this.userId = userId;
            this.siteId = siteId;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public void updateForumCount(int count) {
            this.newForumCount = count;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public void updateMessageCount(int count) {
            this.newMessageCount = count;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public String getKey() {
            return userId + ":" + siteId;
        }
    }

    public void init() {
        log.info("Initializing SynopticUpdateBatchService");
        startBatchProcessing();
    }

    public void destroy() {
        log.info("Destroying SynopticUpdateBatchService");
        stopBatchProcessing();
    }

    @Override
    public void queueForumUpdate(String userId, String siteId, int newForumCount) {
        String key = userId + ":" + siteId;
        pendingUpdates.compute(key, (k, existing) -> {
            if (existing == null) {
                existing = new SynopticUpdate(userId, siteId);
            }
            existing.updateForumCount(newForumCount);
            return existing;
        });

        log.debug("Queued forum update for user {} in site {} with count {}", userId, siteId, newForumCount);
    }

    @Override
    public void queueMessageUpdate(String userId, String siteId, int newMessageCount) {
        String key = userId + ":" + siteId;
        pendingUpdates.compute(key, (k, existing) -> {
            if (existing == null) {
                existing = new SynopticUpdate(userId, siteId);
            }
            existing.updateMessageCount(newMessageCount);
            return existing;
        });

        log.debug("Queued message update for user {} in site {} with count {}", userId, siteId, newMessageCount);
    }

    @Override
    public void processQueuedUpdates() {
        if (pendingUpdates.isEmpty()) {
            return;
        }

        int processed = 0;
        long startTime = System.currentTimeMillis();

        log.debug("Processing {} queued synoptic updates", pendingUpdates.size());

        // Take a snapshot of entries to process to avoid concurrent modification
        List<Map.Entry<String, SynopticUpdate>> entriesToProcess = new ArrayList<>(pendingUpdates.entrySet());
        
        // Process all queued updates
        for (Map.Entry<String, SynopticUpdate> entry : entriesToProcess) {
            String key = entry.getKey();
            SynopticUpdate update = entry.getValue();

            try {
                // Process forum update if present
                if (update.newForumCount != null) {
                    log.debug("Processing forum update for user {} in site {} with count {}", 
                             update.userId, update.siteId, update.newForumCount);
                    synopticMsgcntrManager.setForumSynopticInfoHelper(
                        update.userId, update.siteId, update.newForumCount);
                }

                // Process message update if present
                if (update.newMessageCount != null) {
                    log.debug("Processing message update for user {} in site {} with count {}", 
                             update.userId, update.siteId, update.newMessageCount);
                    synopticMsgcntrManager.setMessagesSynopticInfoHelper(
                        update.userId, update.siteId, update.newMessageCount);
                }

                // Remove successfully processed update
                pendingUpdates.remove(key);
                processed++;

            } catch (Exception e) {
                log.warn("Failed to process synoptic update for key {}: {}", key, e.getMessage());
                // Leave the update in the queue for retry in next batch
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.debug("Processed {} synoptic updates in {}ms", processed, duration);
    }

    @Override
    public void startBatchProcessing() {
        if (batchingStarted.compareAndSet(false, true)) {
            long intervalSeconds = getBatchIntervalSeconds();
            
            // Validate interval to prevent IllegalArgumentException
            if (intervalSeconds <= 0) {
                log.warn("Invalid batch interval: {}s, using default: {}s", intervalSeconds, DEFAULT_BATCH_INTERVAL_SECONDS);
                intervalSeconds = DEFAULT_BATCH_INTERVAL_SECONDS;
            }
            
            batchExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "SynopticUpdateBatch");
                t.setDaemon(true);
                return t;
            });

            try {
                batchExecutor.scheduleWithFixedDelay(
                    this::processQueuedUpdates,
                    intervalSeconds,
                    intervalSeconds,
                    TimeUnit.SECONDS
                );

                log.info("Started synoptic update batch processing with {}s interval", intervalSeconds);
            } catch (IllegalArgumentException e) {
                log.error("Failed to start batch processing with interval {}s: {}", intervalSeconds, e.getMessage());
                batchExecutor.shutdown();
                batchingStarted.set(false);
            }
        }
    }

    @Override
    public void stopBatchProcessing() {
        if (batchingStarted.compareAndSet(true, false)) {
            if (batchExecutor != null) {
                batchExecutor.shutdown();
                try {
                    if (!batchExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                        batchExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    batchExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            // Process any remaining updates
            processQueuedUpdates();
            log.info("Stopped synoptic update batch processing");
        }
    }


    private long getBatchIntervalSeconds() {
        return serverConfigurationService.getLong(BATCH_INTERVAL_PROPERTY, DEFAULT_BATCH_INTERVAL_SECONDS);
    }
}
