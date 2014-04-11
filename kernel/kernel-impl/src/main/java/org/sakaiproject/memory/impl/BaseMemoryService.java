/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/memory/util/EhCacheFactoryBean.java $
 * $Id: EhCacheFactoryBean.java 129412 2013-09-06 17:38:55Z azeckoski@unicon.net $
 ***********************************************************************************
 *
 * Copyright (c) 2012 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.memory.impl;

import net.sf.ehcache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.GenericMultiRefCache;
import org.sakaiproject.memory.api.MemoryService;

import java.util.Properties;

/**
 * Allows us to configure which MemoryService implementation is used using config settings
 *
 * Force the use of the legacy memory service instead of the newer one with JSR-107 support.
 * When true, use BasicMemoryService, MemCache, GenericMultiRefCacheImpl
 * Default: false
 * memory.use.legacy=true
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
public class BaseMemoryService implements MemoryService {

    final Log log = LogFactory.getLog(BaseMemoryService.class);

    SecurityService securityService;
    ServerConfigurationService serverConfigurationService;
    CacheManager cacheManager;

    MemoryService memoryService;

    /**
     * Service INIT
     */
    public void init() {
        if (memoryService == null) {
            boolean useLegacy = false;
            if (serverConfigurationService != null) {
                useLegacy = serverConfigurationService.getBoolean("memory.use.legacy", false);
            }
            if (useLegacy) {
                final EventTrackingService eventTrackingService = (EventTrackingService) ComponentManager.get(EventTrackingService.class);
                BasicMemoryService bms = new BasicMemoryService() {
                    @Override
                    protected EventTrackingService eventTrackingService() {
                        return eventTrackingService;
                    }
                    @Override
                    protected SecurityService securityService() {
                        return securityService;
                    }
                    @Override
                    protected ServerConfigurationService serverConfigurationService() {
                        return serverConfigurationService;
                    }
                };
                if (cacheManager == null) {
                    throw new IllegalStateException("Unable to find the org.sakaiproject.memory.api.MemoryService.cacheManager");
                }
                bms.setCacheManager(cacheManager);
                bms.init();
                memoryService = bms;
                log.info("INIT: legacy: BasicMemoryService");
            } else {
                // use the newer service implementation
                EhcacheMemoryService ems = new EhcacheMemoryService(cacheManager, securityService, serverConfigurationService);
                ems.init();
                memoryService = ems;
                log.info("INIT: new: EhcacheMemoryService");
            }
        } else {
            // using the passed in MemoryService
            log.info("INIT: injection ("+memoryService.getClass().getName()+")");
        }
    }

    /**
     * Service SHUTDOWN
     */
    public void destroy() {
        if (memoryService != null) {
            if (memoryService instanceof EhcacheMemoryService) {
                ((EhcacheMemoryService)memoryService).destroy();
            } else if (memoryService instanceof BasicMemoryService) {
                ((BasicMemoryService)memoryService).destroy();
            }
        }
        memoryService = null;
        log.info("SHUTDOWN");
    }

    // DELEGATED methods

    @Override
    public ClassLoader getClassLoader() {
        return memoryService.getClassLoader();
    }

    @Override
    public Properties getProperties() {
        return memoryService.getProperties();
    }

    @Override
    public Cache getCache(String cacheName) {
        return memoryService.getCache(cacheName);
    }

    @Override
    public Iterable<String> getCacheNames() {
        return memoryService.getCacheNames();
    }

    @Override
    public void destroyCache(String cacheName) {
        memoryService.destroyCache(cacheName);
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        //noinspection unchecked
        return memoryService.unwrap(clazz);
    }

    @Override
    public long getAvailableMemory() {
        return memoryService.getAvailableMemory();
    }

    @Override
    public void resetCachers() {
        memoryService.resetCachers();
    }

    @Override
    public void evictExpiredMembers() {
        memoryService.evictExpiredMembers();
    }

    @Override
    public Cache newCache(String cacheName) {
        return memoryService.newCache(cacheName);
    }

    @Override
    public String getStatus() {
        return memoryService.getStatus();
    }

    // DEPRECATED METHODS BELOW

    @Override
    @SuppressWarnings("deprecation")
    public Cache newCache(String cacheName, CacheRefresher refresher, String pattern) {
        if (refresher != null) {
            log.warn("Creating refresher/pattern Cache("+cacheName+"), CacheRefresher is not supported anymore (and in fact is broken since 2.8), CacheRefresher will not be called and is deprecated and will be removed in the next release");
            return getCache(cacheName);
        } else {
            return newCache(cacheName, pattern);
        }
    }

    @Override
    public Cache newCache(String cacheName, String pattern) {
        log.warn("Creating pattern Cache("+cacheName+"), pattern is deprecated and will no longer work in the next release");
        //noinspection deprecation
        return memoryService.newCache(cacheName, pattern);
    }

    @SuppressWarnings("deprecation")
    @Override
    public GenericMultiRefCache newGenericMultiRefCache(String cacheName) {
        log.warn("Creating MultiRefCache("+cacheName+"), GenericMultiRefCache is deprecated and will no longer work in the next release");
        return memoryService.newGenericMultiRefCache(cacheName);
    }

    // SETTERS

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setMemoryService(MemoryService memoryService) {
        this.memoryService = memoryService;
    }
}
