/******************************************************************************
 * $URL$
 * $Id$
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

import net.sf.ehcache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.*;

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

    ServerConfigurationService serverConfigurationService;
    CacheManager cacheManager;

    MemoryService memoryService;

    /**
     * Service INIT
     */
    public void init() {
        log.info("INIT");
        if (memoryService == null) {
            boolean useLegacy = false;
            if (serverConfigurationService != null) {
                useLegacy = serverConfigurationService.getBoolean("memory.use.legacy", false);
            }
            if (useLegacy) {
                /* NOTE about the lazy loading:
                MemoryService uses SecurityService, EventTrackingService, ServerConfigurationService
                SecurityService uses MemoryService, EventTrackingService, ServerConfigurationService (and others)
                EventTrackingService uses SecurityService (and others)

                This could be tolerable as long as none of these services are used in the init.
                BasicMemoryService uses EventTrackingService in INIT to create the Observer (and the ehcache CacheManager of course).
                EhcacheMemoryService only uses the ehcache CacheManager during INIT.
                Unfortunately, SecurityService sets up caches so it requires MemoryService during init.
                Nothing uses SecurityService in the init EXCEPT the new MemoryService because
                it needs to insert it into the selected implementation (as previously done below).

                In essence, these circular dependencies between the various services make it impossible
                for Spring to establish a viable startup order and this results in the NPE (in newCache).

                The real fix for this is quite complex because the service dependency
                graph is cyclical and basically needs to be untangled and have the
                cycles removed (of which there are... many). The quick-ish fix for
                this is to change the MemoryService to lazy load the SecurityService
                and EventTrackingService (which is what we have done below).
                 */
                BasicMemoryService bms = new BasicMemoryService() {
                    EventTrackingService ets;
                    SecurityService ss;
                    @Override
                    protected EventTrackingService eventTrackingService() {
                        // has to be lazy
                        if (ets == null) {
                            ets = (EventTrackingService) ComponentManager.get(EventTrackingService.class);
                        }
                        return ets;
                    }
                    @Override
                    protected SecurityService securityService() {
                        // has to be lazy
                        if (ss == null) {
                            ss = (SecurityService) ComponentManager.get(SecurityService.class);
                        }
                        return ss;
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
                log.info("INIT complete: legacy: BasicMemoryService");
            } else {
                // use the newer service implementation
                EhcacheMemoryService ems = new EhcacheMemoryService(cacheManager, serverConfigurationService);
                ems.init();
                memoryService = ems;
                log.info("INIT complete: new: EhcacheMemoryService");
            }
        } else {
            // using the passed in MemoryService
            log.info("INIT complete: injection ("+memoryService.getClass().getName()+")");
        }
        if (memoryService == null) {
            throw new IllegalStateException("Unable to INIT MemoryService, no service could be started, system cannot operate with caching");
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

    // COMMON methods




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
    public <C extends Configuration> Cache createCache(String cacheName, C configuration) {
        return memoryService.createCache(cacheName, configuration);
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

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // OPTIONAL
    public void setMemoryService(MemoryService memoryService) {
        this.memoryService = memoryService;
    }
}
