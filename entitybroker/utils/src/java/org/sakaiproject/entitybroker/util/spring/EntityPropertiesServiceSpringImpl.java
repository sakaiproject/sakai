/**
 * $Id$
 * $URL$
 * EntityProviderProperties.java - entity-broker - Jul 18, 2008 6:20:19 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.util.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.providers.EntityPropertiesService;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.DefaultResourceLoader;

/**
 * This service allows us to track all the properties files related to describing the capabilities
 * of our entities and the entities themselves, it allows lookup of strings as well<br/>
 * NOTE: For internal use only, has no dependencies
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityPropertiesServiceSpringImpl implements EntityPropertiesService {

    private static final Log log = LogFactory.getLog(EntityPropertiesServiceSpringImpl.class);

    protected Map<String, MessageSource> prefixMap = new ConcurrentHashMap<String, MessageSource>();

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.impl.entityprovider.EntityPropertiesServiceAPI#getLocale()
     */
    public Locale getLocale() {
        // This tries to fire up the CM if it is not already there so we will use the default for now
        //return new ResourceLoader().getLocale();
        return Locale.getDefault();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.impl.entityprovider.EntityPropertiesServiceAPI#loadProperties(java.lang.String, java.lang.String, java.lang.ClassLoader)
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
        EntityMessageSource bundle = new EntityMessageSource();
        bundle.setResourceLoader( new DefaultResourceLoader(classLoader) );
        bundle.setBasename(baseName);
        bundle.setDefaultEncoding("UTF-8");
        /**
      ResourceBundle bundle = PropertyResourceBundle.getBundle(baseName, locale, classLoader);
      ArrayList<String> keys = new ArrayList<String>();
      Enumeration<String> enumKeys = bundle.getKeys();
      while (enumKeys.hasMoreElements()) {
         String key = enumKeys.nextElement();
         keys.add(key);
      }
         **/
        List<String> keys = bundle.getPropertyKeys(locale);
        if (keys.size() > 0) {
            prefixMap.put(prefix, bundle);
            log.info("Added "+keys.size()+" properties for entity prefix (" + prefix + ") and basename ("+baseName+")");
        } else {
            log.warn("No properties to load for entity prefix (" + prefix + ") and basename ("+baseName+")");
        }
    }

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
        MessageSource bundle = prefixMap.get(prefix);
        if (bundle != null) {
            try {
                value = (String) bundle.getMessage(key, null, locale);
            } catch (NoSuchMessageException e) {
                value = null;
            }
        }
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    protected class EntityMessageSource extends ReloadableResourceBundleMessageSource {
        public List<String> getPropertyKeys(Locale locale) {
            ArrayList<String> keys = new ArrayList<String>();
            PropertiesHolder ph = getMergedProperties(locale);
            for (Object o : ph.getProperties().keySet()) {
                keys.add(o.toString());
            }
            return keys;
        }
    }

}
