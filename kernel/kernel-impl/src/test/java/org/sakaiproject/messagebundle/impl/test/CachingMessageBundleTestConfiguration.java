/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.messagebundle.impl.test;

import org.mockito.Mockito;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.sakaiproject.messagebundle.impl.CachingMessageBundleServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CachingMessageBundleTestConfiguration {

    @Bean(name = "CachingMessageBundleServiceImpl")
    public MessageBundleService cachingMessageBundleService() {
        CachingMessageBundleServiceImpl messageBundleService = new CachingMessageBundleServiceImpl();
        messageBundleService.setDbMessageBundleService(dbMessageBundleService());
        messageBundleService.setMemoryService(memoryService());
        messageBundleService.init();
        return messageBundleService;
    }

    @Bean(name = "MessageBundleServiceImpl")
    public MessageBundleService dbMessageBundleService() {
        MessageBundleService dbMessageBundleService = Mockito.mock(MessageBundleService.class);
        return dbMessageBundleService;
    }

    @Bean
    public MemoryService memoryService() {
        MemoryService memoryService = new org.sakaiproject.memory.mock.MemoryService();
        return memoryService;
    }
}
