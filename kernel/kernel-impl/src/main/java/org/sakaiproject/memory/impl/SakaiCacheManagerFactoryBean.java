/******************************************************************************
 * $URL: https://source.sakaiproject.org/svn/master/trunk/header.java $
 * $Id: header.java 307632 2014-03-31 15:29:37Z azeckoski@unicon.net $
 ******************************************************************************
 *
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************/

package org.sakaiproject.memory.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.*;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import org.sakaiproject.component.api.ServerConfigurationService;

/**
 * NOTE: This file was modeled after org/springframework/cache/ehcache/EhCacheManagerFactoryBean.java from URL
 * http://grepcode.com/file_/repo1.maven.org/maven2/org.springframework/spring-context-support/3.2.3.RELEASE/org/springframework/cache/ehcache/EhCacheManagerFactoryBean.java/?v=source
 *
 * @author rlong Bob Long rlong@unicon.net
 * @author azeckoski Aaron Zeckoski azeckoski@unicon.net
 *
 * ORIGINAL JAVADOC BELOW:
 * {@link FactoryBean} that exposes an EhCache {@link net.sf.ehcache.CacheManager}
 * instance (independent or shared), configured from a specified config location.
 *
 * <p>If no config location is specified, a CacheManager will be configured from
 * "ehcache.xml" in the root of the class path (that is, default EhCache initialization
 * - as defined in the EhCache docs - will apply).
 *
 * <p>Setting up a separate EhCacheManagerFactoryBean is also advisable when using
 * EhCacheFactoryBean, as it provides a (by default) independent CacheManager instance
 * and cares for proper shutdown of the CacheManager. EhCacheManagerFactoryBean is
 * also necessary for loading EhCache configuration from a non-default config location.
 *
 * <p>Note: As of Spring 3.0, Spring's EhCache support requires EhCache 1.3 or higher.
 * As of Spring 3.2, we recommend using EhCache 2.1 or higher.
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 */
@Slf4j
public class SakaiCacheManagerFactoryBean implements FactoryBean<CacheManager>, InitializingBean, DisposableBean {

    // Check whether EhCache 2.1+ CacheManager.create(Configuration) method is available...
    private static final Method createWithConfiguration =
            ClassUtils.getMethodIfAvailable(CacheManager.class, "create", Configuration.class);
    /** cache defaults **/
    private final static String DEFAULT_CACHE_SERVER_URL = "localhost:9510";
    private final static int DEFAULT_CACHE_TIMEOUT = 600; // 10 mins
    private final static int DEFAULT_CACHE_MAX_OBJECTS = 10000;
    protected ServerConfigurationService serverConfigurationService;
    private Resource configLocation;
    private boolean shared = false;
    private String cacheManagerName = "Sakai";
    private CacheManager cacheManager;
    private Boolean cacheEnabled;

    public SakaiCacheManagerFactoryBean() {
    }

    public SakaiCacheManagerFactoryBean(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
        this.cacheManagerName = "SakaiTest";
        try {
            this.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the location of the EhCache config file. A typical value is "/WEB-INF/ehcache.xml".
     * <p>Default is "ehcache.xml" in the root of the class path, or if not found,
     * "ehcache-failsafe.xml" in the EhCache jar (default EhCache initialization).
     * @see net.sf.ehcache.CacheManager#create(java.io.InputStream)
     * @see net.sf.ehcache.CacheManager#CacheManager(java.io.InputStream)
     */
    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }

    /**
     * Set whether the EhCache CacheManager should be shared (as a singleton at the VM level)
     * or independent (typically local within the application). Default is "false", creating
     * an independent instance.
     * @see net.sf.ehcache.CacheManager#create()
     * @see net.sf.ehcache.CacheManager#CacheManager()
     */
    public void setShared(boolean shared) {
        this.shared = shared;
    }

    /**
     * Set the name of the EhCache CacheManager (if a specific name is desired).
     * @see net.sf.ehcache.CacheManager#setName(String)
     */
    public void setCacheManagerName(String cacheManagerName) {
        this.cacheManagerName = cacheManagerName;
    }
    
    /**
     * Creates a CacheConfiguration based on the cache name.
     * Any Cache properties below that are not set will use the default values
     * Valid properties include: maxSize, timeToIdle, timeToLive, eternal
     * Defaults: maxSize=10000, timeToIdle=600, timeToLive=600, eternal=false
     * Configure cluster caches using: memory.cluster.{cacheName}.{property)={value}
     *
     * @param clusterCacheName the full name of the cache (e.g. org.sakaiproject.event.impl.ClusterEventTracking.eventsCache)
     * @return Terracotta cluster cache configuration
     */
    private CacheConfiguration createClusterCacheConfiguration(String clusterCacheName) {
        String clusterConfigName = "memory.cluster."+clusterCacheName;
        CacheConfiguration clusterCache = new CacheConfiguration(
                clusterCacheName,
                serverConfigurationService.getInt(clusterConfigName + ".maxEntries", DEFAULT_CACHE_MAX_OBJECTS));
        boolean isEternal = serverConfigurationService.getBoolean(clusterConfigName + ".eternal", false);
        if (isEternal) {
            clusterCache.eternal(true).timeToIdleSeconds(0).timeToLiveSeconds(0);
        } else {
            clusterCache.eternal(false)
                    .timeToIdleSeconds(serverConfigurationService.getInt(clusterConfigName + ".timeToIdle", DEFAULT_CACHE_TIMEOUT))
                    .timeToLiveSeconds(serverConfigurationService.getInt(clusterConfigName + ".timeToLive", DEFAULT_CACHE_TIMEOUT));
        }
        clusterCache.terracotta(new TerracottaConfiguration()
                .nonstop(new NonstopConfiguration()
                        .timeoutBehavior(new TimeoutBehaviorConfiguration()
                                .type(TimeoutBehaviorConfiguration.LOCAL_READS_TYPE_NAME))
                        .enabled(true)));
        // Make sure we don't go to local disk
        clusterCache.overflowToOffHeap(false);
        // Required to control the L2 cache size in terracotta itself, default should be adequate
        clusterCache.maxElementsOnDisk(10000);
        return clusterCache;
    }
    
    /**
     * This is the init method
     * If using Terracotta, enable caching via sakai.properties and ensure the Terracotta server is reachable
     * Use '-Dcom.tc.tc.config.total.timeout=10000' to specify how long we should try to connect to the TC server
     */
    public void afterPropertiesSet() throws IOException {
        log.info("Initializing EhCache CacheManager");
        InputStream is = (this.configLocation != null ? this.configLocation.getInputStream() : null);
        if (this.cacheEnabled == null) {
            this.cacheEnabled = serverConfigurationService.getBoolean("memory.cluster.enabled", false);
        }

        try {
            Configuration configuration = (is != null) ? ConfigurationFactory.parseConfiguration(is) : ConfigurationFactory.parseConfiguration();
            configuration.setName(this.cacheManagerName);
            // force the sizeof calculations to not generate lots of warnings OR degrade server performance
            configuration.getSizeOfPolicyConfiguration().maxDepthExceededBehavior(SizeOfPolicyConfiguration.MaxDepthExceededBehavior.ABORT);
            configuration.getSizeOfPolicyConfiguration().maxDepth(100);

            // Setup the Terracotta cluster config
            TerracottaClientConfiguration terracottaConfig = new TerracottaClientConfiguration();

            // use Terracotta server if running and available
            if (this.cacheEnabled) {
                log.info("Attempting to load cluster caching using Terracotta at: "+
                        serverConfigurationService.getString("memory.cluster.server.urls", DEFAULT_CACHE_SERVER_URL)+".");
                // set the URL to the server
                String[] serverUrls = serverConfigurationService.getStrings("memory.cluster.server.urls");
                // create comma-separated string of URLs
                String serverUrlsString = StringUtils.join(serverUrls, ",");
                terracottaConfig.setUrl(serverUrlsString);
                terracottaConfig.setRejoin(true);
                configuration.addTerracottaConfig(terracottaConfig);

                // retrieve the names of all caches that will be managed by Terracotta and create cache configurations for them
                String[] caches = serverConfigurationService.getStrings("memory.cluster.names");
                if (ArrayUtils.isNotEmpty(caches)) {
                    for (String cacheName : caches) {
                        CacheConfiguration cacheConfiguration = this.createClusterCacheConfiguration(cacheName);
                        if (cacheConfiguration != null) {
                            configuration.addCache(cacheConfiguration);
                        }
                    }
                }

                // create new cache manager with the above configuration
                if (this.shared) {
                    this.cacheManager = (CacheManager) ReflectionUtils.invokeMethod(createWithConfiguration, null, configuration);
                } else {
                    this.cacheManager = new CacheManager(configuration);
                }
            } else {
                // This block contains the original code from org/springframework/cache/ehcache/EhCacheManagerFactoryBean.java
                // A bit convoluted for EhCache 1.x/2.0 compatibility.
                // To be much simpler once we require EhCache 2.1+
                log.info("Attempting to load default cluster caching.");
                configuration.addTerracottaConfig(terracottaConfig);
                if (this.cacheManagerName != null) {
                    if (this.shared && createWithConfiguration == null) {
                        // No CacheManager.create(Configuration) method available before EhCache 2.1;
                        // can only set CacheManager name after creation.
                        this.cacheManager = (is != null ? CacheManager.create(is) : CacheManager.create());
                        this.cacheManager.setName(this.cacheManagerName);
                    } else {
                        configuration.setName(this.cacheManagerName);
                        if (this.shared) {
                            this.cacheManager = (CacheManager) ReflectionUtils.invokeMethod(createWithConfiguration, null, configuration);
                        } else {
                            this.cacheManager = new CacheManager(configuration);
                        }
                    }
                } else if (this.shared) {
                    // For strict backwards compatibility: use simplest possible constructors...
                    this.cacheManager = (is != null ? CacheManager.create(is) : CacheManager.create());
                } else {
                    this.cacheManager = (is != null ? new CacheManager(is) : new CacheManager());
                }
            }
        } catch (CacheException ce) {
            // this is thrown if we can't connect to the Terracotta server on initialization
            if (this.cacheEnabled && this.cacheManager == null) {
                log.error("You have cluster caching enabled in sakai.properties, but do not have a Terracotta server running at "+
                        serverConfigurationService.getString("memory.cluster.server.urls", DEFAULT_CACHE_SERVER_URL)+
                        ". Please ensure the server is running and available.", ce);
                // use the default cache instead
                this.cacheEnabled = false;
                afterPropertiesSet();
            } else {
                log.error("An error occurred while creating the cache manager: ", ce);
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public CacheManager getObject() {
        return this.cacheManager;
    }

    public Class<? extends CacheManager> getObjectType() {
        return (this.cacheManager != null ? this.cacheManager.getClass() : CacheManager.class);
    }

    public boolean isSingleton() {
        return true;
    }

    public void destroy() {
        log.info("Shutting down EhCache CacheManager");
        this.cacheManager.shutdown();
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

}
