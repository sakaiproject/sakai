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

import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import net.sf.ehcache.CacheManager;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.memory.api.*;

/**
 * Allows us to configure which MemoryService implementation is used using config settings
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
@Slf4j
public class BaseMemoryService implements MemoryService {

    public static final String TYPE_EHCACHE = "ehcache";
    public static final String TYPE_HAZELCAST = "hazelcast";

    ServerConfigurationService serverConfigurationService;
    CacheManager cacheManager;

    MemoryService memoryService;

    /**
     * Service INIT
     */
    public void init() {
        log.info("INIT");
        if (memoryService == null) {
            // defaults - ehcache (new)
            String cacheManagerType = TYPE_EHCACHE;
            if (serverConfigurationService != null) {
                cacheManagerType = serverConfigurationService.getString("memory.cachemanager", cacheManagerType);
                cacheManagerType = StringUtils.lowerCase(cacheManagerType);
                if (cacheManagerType == null) {
                    cacheManagerType = TYPE_EHCACHE;
                }
            }

                // use the newer service implementations
                if (TYPE_EHCACHE.equals(cacheManagerType)) {
                    // EhCache based implementation
                    EhcacheMemoryService ems = new EhcacheMemoryService(cacheManager, serverConfigurationService);
                    ems.init();
                    memoryService = ems;
                    log.info("INIT complete: new: EhcacheMemoryService");

                } else if (TYPE_HAZELCAST.equals(cacheManagerType)) {
                    // HazelCast based implementation
                    HazelcastMemoryService hcms = new HazelcastMemoryService(serverConfigurationService);
                    hcms.init();
                    memoryService = hcms;
                    log.info("INIT complete: new: HazelcastMemoryService");

                /* Add new implementation service init here -AZ
                } else if (TYPE_NEW.equals(cacheManagerType)) {
                    // NEW based implementation
                    NewMemoryService nms = new NewMemoryService(serverConfigurationService);
                    nms.init();
                    memoryService = nms;
                    log.info("INIT complete: new: EhcacheMemoryService");
                 */

                } else {
                    // die if we configure an unsupported caching system type
                    throw new IllegalStateException("Bad caching type ("+cacheManagerType+"): memory.cachemanager must be set to a valid type like ehcache or legacy");
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

            } else if (memoryService instanceof HazelcastMemoryService) {
                ((HazelcastMemoryService)memoryService).destroy();

            /* Add new implementation destroy here -AZ
            } else if (memoryService instanceof NewMemoryService) {
                ((NewMemoryService)memoryService).destroy();
            */

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
    public <K, V, C extends Configuration<K, V>> Cache createCache(String cacheName, C configuration){
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
