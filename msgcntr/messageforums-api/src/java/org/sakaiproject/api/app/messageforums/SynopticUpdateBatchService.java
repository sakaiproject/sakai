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
package org.sakaiproject.api.app.messageforums;

/**
 * Service for batching synoptic updates to reduce database contention and optimistic lock conflicts.
 * Updates are queued and processed in batches to improve performance and reliability.
 */
public interface SynopticUpdateBatchService {

    /**
     * Queue a forum synoptic update for batch processing
     * 
     * @param userId the user ID
     * @param siteId the site ID 
     * @param newForumCount the new forum count
     */
    void queueForumUpdate(String userId, String siteId, int newForumCount);
    
    /**
     * Queue a messages synoptic update for batch processing
     * 
     * @param userId the user ID
     * @param siteId the site ID
     * @param newMessageCount the new message count
     */
    void queueMessageUpdate(String userId, String siteId, int newMessageCount);
    
    /**
     * Process all queued updates immediately
     */
    void processQueuedUpdates();
    
    /**
     * Start the batch processing service
     */
    void startBatchProcessing();
    
    /**
     * Stop the batch processing service
     */
    void stopBatchProcessing();
}