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

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.Configuration;
import org.sakaiproject.memory.api.MemoryService;

/**
 * Hazelcast based implementation of the MemoryService API which is automatically distributed by the nature of hazelcast
 *
 * See https://jira.sakaiproject.org/browse/KNL-1272
 * Send questions to Aaron Zeckoski
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
@Slf4j
public class HazelcastMemoryService implements MemoryService {
    ServerConfigurationService serverConfigurationService;
    SecurityService securityService;
    HazelcastInstance hcInstance;


    public HazelcastMemoryService() {}

    public HazelcastMemoryService(ServerConfigurationService serverConfigurationService) {
        assert serverConfigurationService != null;
        this.serverConfigurationService = serverConfigurationService;
    }

    /**
     * Service INIT
     */
    public void init() {
        String clientServers = serverConfigurationService.getString("memory.hc.server", null);
        boolean clientConfigured = StringUtils.isNotBlank(clientServers);
        if (clientConfigured) {
            ClientConfig clientConfig = new ClientConfig();
            ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
            clientNetworkConfig.addAddress(StringUtils.split(clientServers, ','));
            clientConfig.setNetworkConfig(clientNetworkConfig);
            hcInstance = HazelcastClient.newHazelcastClient(clientConfig);
        } else {
            // start up a local server instead
            Config localConfig = new Config();
            localConfig.setInstanceName(serverConfigurationService.getServerIdInstance());
            hcInstance = Hazelcast.newHazelcastInstance(localConfig);
        }
        if (hcInstance == null) {
            throw new IllegalStateException("init(): HazelcastInstance is null!");
        }
        log.info("INIT: " + hcInstance.getName() + " ("+(clientConfigured?"client:"+hcInstance.getClientService():"localServer")+"), cache maps: " + hcInstance.getDistributedObjects());
    }

    /**
     * Service SHUTDOWN
     */
    public void destroy() {
        try {
            hcInstance.shutdown();
        } catch (CacheException e) {
            // NOTHING TO DO HERE
            log.warn("destroy() HC instance shutdown failure: "+e);
        }
        hcInstance = null; // release
        log.info("SHUTDOWN");
    }

    @Override
    public ClassLoader getClassLoader() {
        return HazelcastMemoryService.class.getClassLoader();
    }

    @Override
    public Properties getProperties() {
        Config config = hcInstance.getConfig();
        Properties p = new Properties();
        p.put("name", hcInstance.getName());
        p.put("source", config.getConfigurationUrl().toExternalForm());
        return p;
    }

    @Override
    public <K, V, C extends Configuration<K, V>> Cache createCache(String cacheName, C configuration) {
        return new HazelcastCache(makeHazelcastCache(cacheName, configuration));
    }

    @Override
    public Cache getCache(String cacheName) {
        return new HazelcastCache(makeHazelcastCache(cacheName, null));
    }

    @Override
    public Iterable<String> getCacheNames() {
        if (this.hcInstance != null) {
            Collection<DistributedObject> distributedObjects = hcInstance.getDistributedObjects();
            ArrayList<String> names = new ArrayList<String>(distributedObjects.size());
            for (DistributedObject distributedObject : distributedObjects) {
                names.add(distributedObject.getName());
            }
            return names;
        } else {
            return new ArrayList<String>(0);
        }
    }

    @Override
    public void destroyCache(String cacheName) {
        if (this.hcInstance != null) {
            IMap hcMap = this.hcInstance.getMap(cacheName);
            if (hcMap != null) {
                hcMap.destroy();
            }
        }
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        //noinspection unchecked
        return (T) this.hcInstance;
    }

    @Override
    public long getAvailableMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    @Override
    public void resetCachers() {
        if (!getSecurityService().isSuperUser()) {
            throw new SecurityException("Only super admin can reset cachers, current user not super admin");
        }
        if (this.hcInstance != null) {
            Collection<DistributedObject> distributedObjects = hcInstance.getDistributedObjects();
            for (DistributedObject distributedObject : distributedObjects) {
                if (distributedObject instanceof IMap) {
                    ((IMap)distributedObject).clear();
                }
            }
        }
    }

    @Override
    public void evictExpiredMembers() {
        log.info("Eviction of expired members is meaningless for Hazelcast (so we are doing nothing)");
    }

    @Override
    public Cache newCache(String cacheName) {
        return getCache(cacheName);
    }

    @Override
    public String getStatus() {
        // MIRRORS the OLD status report
        final StringBuilder buf = new StringBuilder();
        buf.append("** Memory report\n");
        buf.append("freeMemory: ").append(Runtime.getRuntime().freeMemory());
        buf.append(" totalMemory: "); buf.append(Runtime.getRuntime().totalMemory());
        buf.append(" maxMemory: "); buf.append(Runtime.getRuntime().maxMemory());
        buf.append("\n\n");

        Collection<DistributedObject> distributedObjects = hcInstance.getDistributedObjects();
        TreeMap<String, IMap> caches = new TreeMap<String, IMap>();
        for (DistributedObject distributedObject : distributedObjects) {
            if (distributedObject instanceof IMap) {
                caches.put(distributedObject.getName(), (IMap) distributedObject);
            }
        }

        // summary (cache descriptions)
        for (Map.Entry<String, IMap> entry : caches.entrySet()) {
            Cache c = new HazelcastCache(entry.getValue());
            buf.append(c.getDescription()).append("\n");
        }

        /* TODO figure out how to get the configs from the IMap
        // config report
        buf.append("\n** Current Cache Configurations\n");
        // determine whether to use old or new form keys
        boolean legacyKeys = true; // set true for a 2.9/BasicMemoryService compatible set of keys
        String maxKey = "maxEntries";
        String ttlKey = "timeToLive";
        String ttiKey = "timeToIdle";
        String eteKey = "eternal";
        //noinspection ConstantConditions
        if (legacyKeys) {
            maxKey = "maxElementsInMemory";
            ttlKey = "timeToLiveSeconds";
            ttiKey = "timeToIdleSeconds";
        }
        // DEFAULT cache config
        CacheConfiguration defaults = cacheManager.getConfiguration().getDefaultCacheConfiguration();
        long maxEntriesDefault = defaults.getMaxEntriesLocalHeap();
        long ttlSecsDefault = defaults.getTimeToLiveSeconds();
        long ttiSecsDefault = defaults.getTimeToIdleSeconds();
        boolean eternalDefault = defaults.isEternal();
        buf.append("# DEFAULTS: ").append(maxKey).append("=").append(maxEntriesDefault).append(",").append(ttlKey).append("=").append(ttlSecsDefault).append(",").append(ttiKey).append("=").append(ttiSecsDefault).append(",").append(eteKey).append("=").append(eternalDefault).append("\n");
        // new: timeToLive=600,timeToIdle=360,maxEntries=5000,eternal=false
        // old: timeToLiveSeconds=3600,timeToIdleSeconds=900,maxElementsInMemory=20000,eternal=false
        for (Ehcache cache : caches) {
            long maxEntries = cache.getCacheConfiguration().getMaxEntriesLocalHeap();
            long ttlSecs = cache.getCacheConfiguration().getTimeToLiveSeconds();
            long ttiSecs = cache.getCacheConfiguration().getTimeToIdleSeconds();
            boolean eternal = cache.getCacheConfiguration().isEternal();
            if (maxEntries == maxEntriesDefault && ttlSecs == ttlSecsDefault && ttiSecs == ttiSecsDefault && eternal == eternalDefault) {
                // Cache ONLY uses the defaults
                buf.append("# memory.").append(cache.getName()).append(" *ALL DEFAULTS*\n");
            } else {
                // NOT only defaults cache, show the settings that differ from the defaults
                buf.append("memory.").append(cache.getName()).append("=");
                boolean first = true;
                if (maxEntries != maxEntriesDefault) {
                    //noinspection ConstantConditions
                    first = addKeyValueToConfig(buf, maxKey, maxEntries, first);
                }
                if (ttlSecs != ttlSecsDefault) {
                    first = addKeyValueToConfig(buf, ttlKey, ttlSecs, first);
                }
                if (ttiSecs != ttiSecsDefault) {
                    first = addKeyValueToConfig(buf, ttiKey, ttiSecs, first);
                }
                if (eternal != eternalDefault) {
                    addKeyValueToConfig(buf, eteKey, eternal, first);
                }
                buf.append("\n");
                // TODO remove the overflow to disk check
                //noinspection deprecation
                if (cache.getCacheConfiguration().isOverflowToDisk()) {
                    // overflowToDisk. maxEntriesLocalDisk
                    buf.append("# NOTE: ").append(cache.getName()).append(" is configured for Overflow(disk), ").append(cache.getCacheConfiguration().getMaxEntriesLocalDisk()).append(" entries\n");
                }
            }
        }
        */

        final String rv = buf.toString();
        log.info(rv);

        return rv;
    }

    /**
     * Simple code duplication reduction
     * Helps with generating the config strings
     * @param buf string builder
     * @param key the key to add
     * @param value the value for the key
     * @param first
     * @return string
     */
    private boolean addKeyValueToConfig(StringBuilder buf, String key, Object value, boolean first) {
        if (!first) {
            buf.append(",");
        } else {
            first = false;
        }
        buf.append(key).append("=").append(value);
        //noinspection ConstantConditions
        return first;
    }

    // DEPRECATED METHODS BELOW

    @Override
    @SuppressWarnings("deprecation")
    public Cache newCache(String cacheName, CacheRefresher refresher, String pattern) {
        return getCache(cacheName);
    }

    @Override
    public Cache newCache(String cacheName, String pattern) {
        log.warn("Creating pattern Cache("+cacheName+"), pattern is not supported in the distributed MemoryService implementation, the pattern update event entry removal will not happen!");
        return getCache(cacheName);
    }


    /**
     * @param cacheName the name of the cache
     * @param configuration [OPTIONAL] a config to use when building the cache, if null then use default methods to create cache
     * @return an Ehcache
     */
    private IMap makeHazelcastCache(String cacheName, org.sakaiproject.memory.api.Configuration configuration) {
        /** Indicates a cache is a new one and should be configured */
        boolean newCache = false;
        String name = cacheName;
        if (name == null || "".equals(name)) {
            name = "DefaultCache" + UUID.randomUUID().toString();
            log.warn("Creating cache without a name, generating dynamic name: ("+name+")");
            newCache = true;
        }

        IMap cache = hcInstance.getMap(name);
        // TODO figure out how to configure the IMap AND how to apply config to it (if it is new)

        /* TODO fetch an existing cache first if possible
        if ( !newCache && cache != null ) {
            if (log.isDebugEnabled()) log.debug("Retrieved existing cache (" + name + ")");
        } else {
            // create a new defaulted cache
            cacheManager.addCache(name);
            cache = cacheManager.getEhcache(name);
            newCache = true;
            log.info("Created ehcache (" + name + ") using defaults");
        }*/

        if (newCache) {
            // TODO this won't execute unless we know when a cache is actually newly created
            if (log.isDebugEnabled()) log.debug("Prepared to configure new ehcache (" + name + "): "+cache);
            // warn people if they are using an old config style
            if (serverConfigurationService.getString(name) == null) {
                log.warn("Old cache configuration for cache ("+name+"), must be changed to memory."+name+" or it will be ignored");
            }

            // load the ehcache config from the Sakai config service
            String config = serverConfigurationService.getString("memory."+ name);
            if (StringUtils.isNotBlank(config)) {
                log.info("Configuring ehcache (" + name + ") from Sakai config: " + config);
                try {
                    // TODO apply the cache settings from the object to the IMap
                } catch (Exception e) {
                    // nothing to do here but proceed
                    log.error("Failure configuring cache (" + name + "): " + config + " :: "+e, e);
                }
            }
        }

        // apply config to the cache (every time)
        /* TODO figure out how to set and change the IMap config
        if (configuration != null) {
            if (configuration.getMaxEntries() >= 0) {
                cache.getCacheConfiguration().setMaxEntriesLocalHeap(configuration.getMaxEntries());
            }
            if (configuration.isEternal()) {
                cache.getCacheConfiguration().setTimeToLiveSeconds(0l);
                cache.getCacheConfiguration().setTimeToIdleSeconds(0l);
            } else {
                if (configuration.getTimeToLiveSeconds() >= 0) {
                    cache.getCacheConfiguration().setTimeToLiveSeconds(configuration.getTimeToLiveSeconds());
                }
                if (configuration.getTimeToIdleSeconds() >= 0) {
                    cache.getCacheConfiguration().setTimeToIdleSeconds(configuration.getTimeToIdleSeconds());
                }
            }
            cache.getCacheConfiguration().setEternal(configuration.isEternal());
            log.info("Configured hcCache (" + name + ") from inputs: " + configuration);
        }
        */

        if (log.isDebugEnabled()) log.debug("Returning initialized ehcache (" + name + "): "+cache);
        return cache;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    // Lazy load the SecurityService on demand
    SecurityService getSecurityService() {
        // has to be lazy
        if (securityService == null) {
            securityService = (SecurityService) ComponentManager.get(SecurityService.class);
        }
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

}
