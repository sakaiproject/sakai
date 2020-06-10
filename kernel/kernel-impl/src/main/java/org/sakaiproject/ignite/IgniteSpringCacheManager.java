package org.sakaiproject.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.cache.spring.SpringCacheManager;
import org.springframework.context.event.ContextRefreshedEvent;

import lombok.Setter;

public class IgniteSpringCacheManager extends SpringCacheManager {

    @Setter private Ignite ignite;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (ignite == null) {
            throw new IllegalArgumentException("Ignite startup issue, please check the log for failures");
        }
    }
}
