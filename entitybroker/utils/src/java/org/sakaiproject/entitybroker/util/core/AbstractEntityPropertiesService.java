/**
 * $Id$
 * $URL$
 * EntityProviderProperties.java - entity-broker - Jul 18, 2008 6:20:19 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.util.core;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;

import org.sakaiproject.entitybroker.providers.EntityPropertiesService;

import lombok.extern.slf4j.Slf4j;

/**
 * This service allows us to track all the properties files related to describing the capabilities
 * of our entities and the entities themselves, it allows lookup of strings as well<br/>
 * NOTE: For internal use only, has no dependencies
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public abstract class AbstractEntityPropertiesService implements EntityPropertiesService {

    public static interface MessageBundle {
        /**
         * Try to resolve the message. Treat as an error if the message can't be found.
         * @param key the code to lookup up, such as 'calculator.noRateSet'
         * @param args Array of arguments that will be filled in for params within
         * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
         * or <code>null</code> if none.
         * @param locale the Locale in which to do the lookup
         * @return the resolved message
         * @throws IllegalArgumentException is the arguments are invalid
         * @throws MissingResourceException if the message wasn't found
         */
        public String getPropertyMessage(String key, Object[] args, Locale locale);
        /**
         * Get all the keys for a specific Locale
         * @param locale the Locale in which to do the lookup
         * @return the list of all keys found for the given Locale
         */
        public List<String> getPropertyKeys(Locale locale);
    }

    protected Map<String, MessageBundle> prefixMap = new ConcurrentHashMap<String, MessageBundle>();
    /**
     * Register this message bundle with the internal storage
     * @param prefix the related entity prefix for this message bundle
     * @param messageBundle the message bundle
     * @throws IllegalArgumentException is any arguments are null
     */
    public void registerPrefixMessageBundle(String prefix, MessageBundle messageBundle) {
        if (prefix == null || messageBundle == null) {
            throw new IllegalArgumentException("prefix and messageBundle cannot be null");
        }
        this.prefixMap.put(prefix, messageBundle);
    }
    /**
     * Unregister this message bundle
     * @param prefix the related entity prefix for a message bundle
     * @throws IllegalArgumentException is any arguments are null
     */
    public void unregisterPrefixMessageBundle(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix cannot be null");
        }
        this.prefixMap.remove(prefix);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.EntityPropertiesService#getLocale()
     */
    public Locale getLocale() {
        return Locale.getDefault();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.EntityPropertiesService#loadProperties(java.lang.String, java.lang.String, java.lang.ClassLoader)
     */
    public void loadProperties(String prefix, String baseName, ClassLoader classLoader) {
        if (prefix == null) {
            throw new IllegalArgumentException("Cannot register properties for a null prefix");
        }
        Locale locale = getLocale();
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (baseName == null) {
            baseName = prefix;
        }
        List<String> keys = registerLocaleMessages(prefix, baseName, locale, classLoader);
        if (keys.size() > 0) {
            log.info("EntityPropertiesService: Added "+keys.size()+" properties for entity prefix (" + prefix + ") and basename ("+baseName+")");
        } else {
            log.info("EntityPropertiesService: No properties to load for entity prefix (" + prefix + ") and basename ("+baseName+")");
        }
    }

    /**
     * Override this to provide custom message handling,
     * you must register the {@link MessageBundle} you create in this method so that the
     * messages can be looked up later, register using {@link #registerPrefixMessageBundle(String, MessageBundle)}
     * 
     * @param prefix an entity prefix
     * @param baseName (optional) the part before the .properties or _en.properties,
     * example: location/dir/myentity.properties, if null then prefix is used
     * @param locale the Locale to register messages for
     * @param classLoader (optional) the ClassLoader to find the properties files in,
     * if null then the default thread ClassLoader is used
     * @return the list of registered keys for this Locale
     */
    public abstract List<String> registerLocaleMessages(String prefix, String baseName, Locale locale, ClassLoader classLoader);

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.impl.entityprovider.EntityPropertiesServiceAPI#unloadProperties(java.lang.String)
     */
    public boolean unloadProperties(String prefix) {
        boolean unreg = false;
        if (prefix != null) {
            unreg = (prefixMap.remove(prefix) != null);
        }
        return unreg;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.impl.entityprovider.EntityPropertiesServiceAPI#getProperty(java.lang.String, java.lang.String)
     */
    public String getProperty(String prefix, String key) {
        return getProperty(prefix, key, null);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.impl.entityprovider.EntityPropertiesServiceAPI#getProperty(java.lang.String, java.lang.String, java.util.Locale)
     */
    public String getProperty(String prefix, String key, Locale locale) {
        return getProperty(prefix, key, locale, null);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.impl.entityprovider.EntityPropertiesServiceAPI#getProperty(java.lang.String, java.lang.String, java.util.Locale, java.lang.String)
     */
    public String getProperty(String prefix, String key, Locale locale, String defaultValue) {
        if (prefix == null) {
            throw new IllegalArgumentException("Cannot get properties for a null prefix");
        }
        if (key == null) {
            throw new IllegalArgumentException("Cannot get properties for a null key");
        }
        if (locale == null) {
            locale = getLocale();
        }
        String value = null;
        MessageBundle messageBundle = prefixMap.get(prefix);
        if (messageBundle != null) {
            try {
                value = (String) messageBundle.getPropertyMessage(key, null, locale);
            } catch (MissingResourceException e) {
                value = null;
            }
        }
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

}
