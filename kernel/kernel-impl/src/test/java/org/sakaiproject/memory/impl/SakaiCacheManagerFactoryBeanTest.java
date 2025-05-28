package org.sakaiproject.memory.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import net.sf.ehcache.CacheManager;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.component.api.ServerConfigurationService;

/**
 * Test the SakaiCacheManagerFactoryBean
 */
public class SakaiCacheManagerFactoryBeanTest {

    private SakaiCacheManagerFactoryBean cacheManagerFactoryBean;
    private ServerConfigurationService serverConfigurationService;

    @Before
    public void setUp() {
        serverConfigurationService = mock(ServerConfigurationService.class);
        cacheManagerFactoryBean = new SakaiCacheManagerFactoryBean(serverConfigurationService);
    }

    @Test
    public void testGetObject() {
        CacheManager cacheManager = cacheManagerFactoryBean.getObject();
        assertNotNull("CacheManager should not be null", cacheManager);
        
        // Test the default cache exists
        assertTrue("Default cache configuration should exist", 
                  cacheManager.getConfiguration().getDefaultCacheConfiguration() != null);
    }
}