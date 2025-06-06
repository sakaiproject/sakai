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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.component.api.ServerConfigurationService;

@RunWith(MockitoJUnitRunner.class)
public class SynopticUpdateBatchServiceImplTest {

    @Mock
    private SynopticMsgcntrManager synopticMsgcntrManager;
    
    @Mock
    private ServerConfigurationService serverConfigurationService;
    
    private SynopticUpdateBatchServiceImpl batchService;
    
    @Before
    public void setUp() {
        batchService = new SynopticUpdateBatchServiceImpl();
        batchService.setSynopticMsgcntrManager(synopticMsgcntrManager);
        batchService.setServerConfigurationService(serverConfigurationService);
    }
    
    @Test
    public void testBatchingQueuesUpdates() {
        // When we queue an update
        batchService.queueForumUpdate("user1", "site1", 5);
        
        // Then it should not call the manager immediately
        verify(synopticMsgcntrManager, never()).batchUpdateForumCounts(any());
        
        // When we process the queue
        batchService.processQueuedUpdates();
        
        // Then it should call the batch update method
        Map<String, Integer> expectedUpdates = new HashMap<>();
        expectedUpdates.put("user1:site1", 5);
        verify(synopticMsgcntrManager, times(1)).batchUpdateForumCounts(expectedUpdates);
    }
    
    @Test
    public void testBatchingCombinesUpdates() {
        // When we queue multiple updates for the same user/site
        batchService.queueForumUpdate("user1", "site1", 3);
        batchService.queueMessageUpdate("user1", "site1", 2);
        batchService.queueForumUpdate("user1", "site1", 5); // This should overwrite the first
        
        // When we process the queue
        batchService.processQueuedUpdates();
        
        // Then it should call both batch methods with the latest values
        Map<String, Integer> expectedForumUpdates = new HashMap<>();
        expectedForumUpdates.put("user1:site1", 5);
        verify(synopticMsgcntrManager).batchUpdateForumCounts(expectedForumUpdates);
        
        Map<String, Integer> expectedMessageUpdates = new HashMap<>();
        expectedMessageUpdates.put("user1:site1", 2);
        verify(synopticMsgcntrManager).batchUpdateMessageCounts(expectedMessageUpdates);
    }
}