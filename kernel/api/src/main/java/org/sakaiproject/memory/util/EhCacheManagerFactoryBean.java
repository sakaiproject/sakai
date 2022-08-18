/**********************************************************************************
 * $URL: $
 * $Id: $
 **********************************************************************************
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

package org.sakaiproject.memory.util;

import lombok.extern.slf4j.Slf4j;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * @deprecated since Sakai 2.9, do not use this anymore (use the sakai config settings instead), this will be removed in 11
 */
@Slf4j
public class EhCacheManagerFactoryBean extends org.springframework.cache.ehcache.EhCacheManagerFactoryBean {
	private ServerConfigurationService serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);

	@Override
	public void afterPropertiesSet() {
        super.afterPropertiesSet();
		//Now look for any custom configuration.
    	CacheManager cm = (CacheManager) this.getObject();
    	if (cm != null) {
    		String cacheNames[]= cm.getCacheNames();

    		//Check for old configuration properties.
    		for (String cacheName:cacheNames) {
    			if(serverConfigurationService.getString(cacheName) == null) {
    				log.warn("Old cache configuration "+ cacheName+ " must be changed to memory."+ cacheName);
    			}
    			String config = serverConfigurationService.getString("memory."+ cacheName);
    			if (config != null && config.length() > 0) {
    				log.info("Found configuration override for cache: "+ cacheName+ " of: "+ config);
    				Cache cache = cm.getCache(cacheName);
    				if (cache != null) {
    					new CacheInitializer().configure(config).initialize(cache.getCacheConfiguration());
    				}
    			}
    		}
    	}
    }
}
