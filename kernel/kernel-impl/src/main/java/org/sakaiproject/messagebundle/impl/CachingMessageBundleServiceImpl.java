/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.messagebundle.impl;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;

/**
 * CachingMessageBundleServiceImpl
 * 
 * Extends MessageBundleServiceImpl adding a level of caching for ResouceBundle's
 * to mitigate redundant bundle loads from the database. 
 *
 * @see MessageBundleServiceImpl
 *
 * @author Earle Nietzel
 * Created on Aug 29, 2013
 * 
 */
@Slf4j
public class CachingMessageBundleServiceImpl extends MessageBundleServiceImpl {
	private static String CACHE_NAME = "org.sakaiproject.messagebundle.cache.bundles"; 

	private MemoryService memoryService;
	private Cache<String, Map<String, String>> cache;
	
	public CachingMessageBundleServiceImpl() {
		super();
	}

	public void init() {
		cache = memoryService.getCache(CACHE_NAME);
		super.init();
	}
	
	public void destroy() {
		cache.close();
		cache = null;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Map<String, String> getBundle(String baseName, String moduleName, Locale loc) {
        if (StringUtils.isBlank(baseName) || StringUtils.isBlank(moduleName) || loc == null) {
            return Collections.emptyMap();
        }

		Map<String, String> bundle = null;
		String key = super.getIndexKeyName(baseName, moduleName, loc.toString());
        if (log.isDebugEnabled()) { log.debug("Retrieve bundle from cache with key=" + key); }
		
		bundle = cache.get(key);
		if (bundle == null) {
		    // bundle not in cache or expired
		    bundle = super.getBundle(baseName, moduleName, loc);
            cache.put(key, bundle);
            if (log.isDebugEnabled()) { log.debug("Add bundle to cache with key=" + key); }
		}

		return bundle;
	}

	@Override
	@Transactional
	public void updateMessageBundleProperty(MessageBundleProperty mbp) {
		String key = super.getIndexKeyName(mbp.getBaseName(), mbp.getModuleName(), mbp.getLocale());
		
		super.updateMessageBundleProperty(mbp);

		cache.remove(key);
	}
	
	@Override
	@Transactional
	public void deleteMessageBundleProperty(MessageBundleProperty mbp) {
		String key = super.getIndexKeyName(mbp.getBaseName(), mbp.getModuleName(), mbp.getLocale());

		super.deleteMessageBundleProperty(mbp);

		cache.remove(key);
	}

	@Override
	@Transactional
	public void revert(MessageBundleProperty mbp) {
		String key = super.getIndexKeyName(mbp.getBaseName(), mbp.getModuleName(), mbp.getLocale());

		super.revert(mbp);
		
		cache.remove(key);
	}
	
	public void setMemoryService(MemoryService memoryService) {
		this.memoryService = memoryService;
	}
}
