package org.sakaiproject.messagebundle.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;

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
	private static Log LOG = LogFactory.getLog(CachingMessageBundleServiceImpl.class);
	private static String CACHE_NAME = "org.sakaiproject.messagebundle.cache.bundles"; 

	private MemoryService memoryService;
	private Cache cache;
	
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
	public Map<String, String> getBundle(String baseName, String moduleName, Locale loc) {
		
		Map<String, String> bundle = null;
		String key = super.getIndexKeyName(baseName, moduleName, loc.toString());
		
		if (cache.containsKey(key)) {
			bundle = (Map<String, String>) cache.get(key);
			if (LOG.isDebugEnabled()) { LOG.debug("Retrieve bundle from cache with key=" + key); }
		} else {
			 bundle = super.getBundle(baseName, moduleName, loc);
			 if (bundle != null) {
				 cache.put(key, bundle);
				 if (LOG.isDebugEnabled()) { LOG.debug("Add bundle to cache with key=" + key); }
			 }
		}
		
		return bundle;
	}

	@Override
	public void updateMessageBundleProperty(MessageBundleProperty mbp) {
		String key = super.getIndexKeyName(mbp.getBaseName(), mbp.getModuleName(), mbp.getLocale());
		
		super.updateMessageBundleProperty(mbp);

		invalidateCache(key);
	}
	
	@Override
	public void deleteMessageBundleProperty(MessageBundleProperty mbp) {
		String key = super.getIndexKeyName(mbp.getBaseName(), mbp.getModuleName(), mbp.getLocale());

		super.deleteMessageBundleProperty(mbp);

		invalidateCache(key);
	}

	@Override
	public void revert(MessageBundleProperty mbp) {
		String key = super.getIndexKeyName(mbp.getBaseName(), mbp.getModuleName(), mbp.getLocale());

		super.revert(mbp);
		
		invalidateCache(key);
	}
	
	private void invalidateCache(String key) {
		if (cache.containsKey(key)) {
			cache.remove(key);
			if (LOG.isDebugEnabled()) { LOG.debug("Remove bundle from cache with key=" + key); }
		}
	}
	
	public void setMemoryService(MemoryService memoryService) {
		this.memoryService = memoryService;
	}
}

