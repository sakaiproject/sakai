/**
 * $Id$
 * $URL$
 * EntityPropertiesService.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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
 **/

package org.sakaiproject.entitybroker.providers;

import java.util.Locale;

/**
 * This provides the entity system with a way to access properties 
 * (and then exposes this to others via the developer helper service)
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface EntityPropertiesService {

    /**
     * @return the current {@link Locale} for the current session or user
     */
    public Locale getLocale();

    /**
     * Register the properties in this {@link ClassLoader} for this entity prefix
     * @param prefix an entity prefix
     * @param baseName (optional) the part before the .properties or _en.properties,
     * example: location/dir/myentity.properties, if null then prefix is used
     * @param classLoader (optional) the ClassLoader to find the properties files in,
     * if null then the default thread ClassLoader is used
     */
    public void loadProperties(String prefix, String baseName, ClassLoader classLoader);

    /**
     * Unregister the properties stored for this prefix
     * @param prefix an entity prefix
     * @return true if unregistered, false if none found to unregister
     */
    public boolean unloadProperties(String prefix);

    /**
     * Get a property for an entity if one is available,
     * uses the default Locale
     * @param prefix an entity prefix
     * @param key the property key
     * @return the property value string OR null if none found
     */
    public String getProperty(String prefix, String key);

    /**
     * Get a property for an entity if one is available
     * @param prefix an entity prefix
     * @param key the property key
     * @param locale the Locale to get the message for
     * @return the property value string OR null if none found
     */
    public String getProperty(String prefix, String key, Locale locale);

    /**
     * Get a property for an entity if one is available
     * @param prefix an entity prefix
     * @param key the property key
     * @param locale the Locale to get the message for
     * @param defaultValue the default value to return if the value is null
     * @return the property value string OR defaultValue if none found
     */
    public String getProperty(String prefix, String key, Locale locale, String defaultValue);

}