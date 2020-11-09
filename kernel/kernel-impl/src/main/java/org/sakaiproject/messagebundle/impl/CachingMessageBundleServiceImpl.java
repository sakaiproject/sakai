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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;
import org.sakaiproject.messagebundle.api.MessageBundleService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
public class CachingMessageBundleServiceImpl implements MessageBundleService {

    private static String CACHE_NAME = "org.sakaiproject.messagebundle.cache.bundles";

    @Setter private MessageBundleService dbMessageBundleService;
    @Setter private MemoryService memoryService;

    private Cache<String, Map<String, String>> cache;

    public void init() {
        cache = memoryService.getCache(CACHE_NAME);
    }

    public void destroy() {
        cache.close();
        cache = null;
    }

    @Override
    public Map<String, String> getBundle(String baseName, String moduleName, Locale locale) {
        String key = MessageBundleServiceImpl.getIndexKeyName(baseName, moduleName, locale != null ? locale.toString(): null);
        log.debug("Retrieve bundle from cache with key = {}", key);

        Map<String, String> bundle = cache.get(key);
        if (bundle == null) {
            // bundle not in cache or expired, never returns null
            bundle = dbMessageBundleService.getBundle(baseName, moduleName, locale);
            log.debug("Add bundle to cache with key = {}", key);
            cache.put(key, bundle);
        }

        return bundle;
    }

    @Override
    public boolean isEnabled() {
        return dbMessageBundleService.isEnabled();
    }

    @Override
    public List<MessageBundleProperty> search(String search, String module, String baseName, String locale) {
        return dbMessageBundleService.search(search, module, baseName, locale);
    }

    @Override
    public MessageBundleProperty getMessageBundleProperty(long id) {
        return dbMessageBundleService.getMessageBundleProperty(id);
    }

    @Override
    public void updateMessageBundleProperty(MessageBundleProperty mbp) {
        String key = MessageBundleServiceImpl.getIndexKeyName(mbp.getBaseName(), mbp.getModuleName(), mbp.getLocale());
        dbMessageBundleService.updateMessageBundleProperty(mbp);
        cache.remove(key);
    }

    @Override
    public List<MessageBundleProperty> getModifiedProperties(int sortOrder, int sortField, int startingIndex, int pageSize) {
        return dbMessageBundleService.getModifiedProperties(sortOrder, sortField, startingIndex, pageSize);
    }

    @Override
    public List<String> getLocales() {
        return dbMessageBundleService.getLocales();
    }

    @Override
    public int getModifiedPropertiesCount() {
        return dbMessageBundleService.getModifiedPropertiesCount();
    }

    @Override
    public int getAllPropertiesCount() {
        return dbMessageBundleService.getAllPropertiesCount();
    }

    @Override
    public List<MessageBundleProperty> getAllProperties(String locale, String basename, String modulename) {
        return dbMessageBundleService.getAllProperties(locale, basename, modulename);
    }

    @Override
    public int revertAll(String locale) {
        int count = dbMessageBundleService.revertAll(locale);
        cache.clear();
        return count;
    }

    @Override
    public int importProperties(List<MessageBundleProperty> properties) {
        return dbMessageBundleService.importProperties(properties);
    }

    @Override
    public List<String> getAllModuleNames() {
        return dbMessageBundleService.getAllModuleNames();
    }

    @Override
    public List<String> getAllBaseNames() {
        return dbMessageBundleService.getAllBaseNames();
    }

    @Override
    public void deleteMessageBundleProperty(MessageBundleProperty mbp) {
        String key = MessageBundleServiceImpl.getIndexKeyName(mbp.getBaseName(), mbp.getModuleName(), mbp.getLocale());
        dbMessageBundleService.deleteMessageBundleProperty(mbp);
        cache.remove(key);
    }

    @Override
    public void revert(MessageBundleProperty mbp) {
        String key = MessageBundleServiceImpl.getIndexKeyName(mbp.getBaseName(), mbp.getModuleName(), mbp.getLocale());
        dbMessageBundleService.revert(mbp);
        cache.remove(key);
    }

    @Override
    public int getSearchCount(String searchQuery, String module, String baseName, String locale) {
        return dbMessageBundleService.getSearchCount(searchQuery, module, baseName, locale);
    }

    @Override
    public void saveOrUpdate(String baseName, String moduleName, ResourceBundle newBundle, Locale locale) {
        // We avoid doing invalidation here as were unable to detect where bundles were already loaded
        // specifically when calling new ResourceLoader()
        dbMessageBundleService.saveOrUpdate(baseName, moduleName, newBundle, locale);
    }
}
