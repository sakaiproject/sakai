/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/branches/SAK-18678/api/src/main/java/org/sakaiproject/site/api/Site.java $
 * $Id: Site.java 81275 2010-08-14 09:24:56Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.messagebundle.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * This interface is used to manage message bundle data in the system.
 *
 */
public interface MessageBundleService {
    /**
     * used to describe an ascending sort order
     */
    public int SORT_ORDER_ASCENDING = 1;
    /**
     * used to describe a descending sort order
     */
    public int SORT_ORDER_DESCENDING = 2;
    /**
     * used to describe sorting by module
     */
    public int SORT_FIELD_MODULE = 1;
    /**
     * used to describe sorting by property
     */
    public int SORT_FIELD_PROPERTY = 2;
    /**
     * used to describe sorting by id
     */
    public int SORT_FIELD_ID = 3;
    /**
     * used to describe sorting by locale
     */
    public int SORT_FIELD_LOCALE = 4;
    /**
     * used to describe sorting by basename
     */
    public int SORT_FIELD_BASENAME = 5;

    /**
     *
     * @param search - text to search for
     * @param module - module name to search within, null means search all modules
     * @param baseName - baseName to search within, null means search all baseNames
     * @param locale - locate to search within, null means search all locales
     * @return list of MessageBundleProperty objects that matches the search inputs
     */
    public List<MessageBundleProperty> search(String search, String module, String baseName, String locale);

    /**
     *
     * @param id - MessageBundleProperty id to retrieve
     * @return the associated MessageBundleProperty, or null if not found
     */
    public MessageBundleProperty getMessageBundleProperty(long id);

    /**
     * updates the existing MessageBundleProperty
     * @param mbp - populated MessageBundleProperty object with values that need to be stored
     */
    public void updateMessageBundleProperty(MessageBundleProperty mbp);

    /**
     *
     * @param sortOrder
     * @param sortField
     * @param startingIndex
     * @param pageSize
     * @return - list of MessageBundleProperty objects in the order prescribed, starting at startingIndex with a length of pageSize
     */
    public List<MessageBundleProperty> getModifiedProperties(int sortOrder, int sortField, int startingIndex, int pageSize);

    /**
     *
     * @return - list of known locales in the persistent store
     */
    public List<String> getLocales();

    /**
     *
     * @return number of modified properties in the persistent store
     */
    public int getModifiedPropertiesCount();
    
    /**
    *
    * @return number of properties in the persistent store
    */
    public int getAllPropertiesCount();

    /**
     *
     * @param locale - locale to match, null means all locales
     * @param module - module to match, null means all modules
     * @return list of MessageBundleProperty for the given locale and module
     */
    public List<MessageBundleProperty> getAllProperties(String locale, String module);

    /**
     * reverts any changed values for properties back to their original values
     *
     * @param locale - which locale to revert, null means all locales
     * @return number of values reverted
     */
    public int revertAll(String locale);

    /**
     * creates or updates properties
     * @param properties to create or update
     * @return number of properties updated
     */
    public int importProperties(List<MessageBundleProperty> properties);

    /**
     *
     * @return list of all known moduleNames as Strings
     */
    public List<String> getAllModuleNames();

    /**
     *
     * @return list of all known baseNames as Strings
     */
    public List<String> getAllBaseNames();

    /**
     *  revert value back to default value in the persistent store
     * @param mbp - object to revert
     */
    public void revert(MessageBundleProperty mbp);

    /**
     *
     * @param searchQuery - text to search for
     * @param module - module name to search within, null means search all modules
     * @param baseName - baseName to search within, null means search all baseNames
     * @param locale - locate to search within, null means search all locales
     * @return number of MessageBundleProperty found matches inputted search criteria
     */
    public int getSearchCount(String searchQuery, String module, String baseName, String locale);

    /**
     * creates or updates the ResourceBundle data for the given baseName, moduleName and locale
     * @param baseName
     * @param moduleName
     * @param newBundle
     * @param loc
     */
    public void saveOrUpdate(String baseName, String moduleName, ResourceBundle newBundle, Locale loc);

    /**
     *
     * @param baseName - retrieve values for this baseName
     * @param moduleName - retrieve values for this moduleName
     * @param loc - retrieve values for this locale
     * @return bundle data as map of name/values pairs
     */
    public Map<String,String> getBundle(String baseName, String moduleName, Locale loc);
}
