package org.sakaiproject.messagebundle.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

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
public class CachingMessageBundleServiceImpl extends MessageBundleServiceImpl {
	private static Logger LOG = LoggerFactory.getLogger(CachingMessageBundleServiceImpl.class);
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
        if (LOG.isDebugEnabled()) { LOG.debug("Retrieve bundle from cache with key=" + key); }
		
		bundle = cache.get(key);
		if (bundle == null) {
		    // bundle not in cache or expired
		    bundle = super.getBundle(baseName, moduleName, loc);
            cache.put(key, bundle);
            if (LOG.isDebugEnabled()) { LOG.debug("Add bundle to cache with key=" + key); }
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

