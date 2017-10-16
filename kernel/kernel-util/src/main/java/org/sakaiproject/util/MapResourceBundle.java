/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author Earle Nietzel
 *         (earle.nietzel@gmail.com)
 *
 */
public class MapResourceBundle extends ResourceBundle {

    /**
     * The store
     */
    private Map<String, Object> map;

    /**
     * The locale for this bundle.
     */
    private Locale locale;

    /**
     * The base bundle name for this bundle.
     */
    private String name;

    /**
     * Creates a property resource bundle from an {@link java.util.Map}.
     *
     * @param properties a Map<String, Object> that represents a properties
     *        to read from.
     * @param name a <code>String</code> represents the <code>name</code> of
     *        the <code>ResourceBundle</code>.
     * @param locale a <code>Locale</code> represents the locale if the
     *        <code>ResourceBundle</code>.
     * @throws NullPointerException if <code>properties</code>, <code>name</code>,
     *         <code>locale</code> is null.
     */
    public MapResourceBundle(Map<String, Object> properties, String name, Locale locale) 
        throws NullPointerException {

        if (properties == null || name == null || locale == null) {
            throw new NullPointerException("Params cannot be null");
        }

        this.map = new HashMap<String, Object>(properties);
        this.name = name;
        this.locale = locale;
    }

    @Override
    protected Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return map.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        Set<String> allKeysSet = new HashSet<String>(map.keySet());
        Enumeration<String> parentKeys = parent.getKeys();
        if (parentKeys != null) {
            while (parentKeys.hasMoreElements()) {
                String next = parentKeys.nextElement();
                if (!allKeysSet.contains(next)) {
                    allKeysSet.add(next);
                }
            }
        }
        return Collections.enumeration(allKeysSet);
    }

    protected Set<String> handleKeySet() {
        return map.keySet();
    }

    @Override
    public String getBaseBundleName() {
        return name;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }
}
