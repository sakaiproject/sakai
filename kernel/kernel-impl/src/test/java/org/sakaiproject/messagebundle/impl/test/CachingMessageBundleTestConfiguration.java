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
