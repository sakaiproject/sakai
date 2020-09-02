package org.sakaiproject.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.cache.spring.SpringCacheManager;
import org.apache.ignite.configuration.IgniteConfiguration;

import lombok.Setter;

public class IgniteSpringCacheManager extends SpringCacheManager {

    @Setter private Ignite sakaiIgnite;
    @Setter private IgniteConfiguration igniteConfiguration;

    @Override
    public IgniteConfiguration getConfiguration() {
        return igniteConfiguration;
    }

    public void init() {
        // this configuration is required so that SpringCacheManager looks up the existing
        // ignite instance that was started by Hibernate
        setConfiguration(null);
        setConfigurationPath(null);
        setIgniteInstanceName(igniteConfiguration.getIgniteInstanceName());
    }
}
