/******************************************************************************
 * $URL: $
 * $Id: $
 ******************************************************************************
 *
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************/

package org.sakaiproject.config.api;

import java.util.Date;
import java.util.List;

/**
 * HibernateConfigItemDao
 * This is the Data Access Object for a HibernateConfigItem
 * <p/>
 * DO NOT USE THIS API to add ConfigItem's to ServerConfigurationService
 * <p/>
 * Use SCS to add, update ConfigItem(s)
 *
 * @author Earle Nietzel
 *         Created on Mar 8, 2013
 */
public interface HibernateConfigItemDao {

    /**
     * Persist a new HibernateConfigItem to the database
     *
     * @param item to persist
     */
    void create(HibernateConfigItem item);

    /**
     * Read a HibernateConfigItem from the database
     *
     * @param id the id of the item to load
     * @return a HibernateConfigItem equal to id
     */
    HibernateConfigItem read(Long id);

    /**
     * Updates a HibernateConfigItem already persisted
     *
     * @param item the updated HibernateConfigItem
     */
    void update(HibernateConfigItem item);

    /**
     * Delete a item from the database
     *
     * @param item HibernateConfigItem to delete
     */
    void delete(HibernateConfigItem item);

    /**
     * Persists or updates all HibernateConfigItem in the List
     *
     * @param props a List of HibernateConfigItem
     */
    void saveOrUpdateAll(List<HibernateConfigItem> props);

    /**
     * Persists or updates a HibernateConfigItem
     *
     * @param item the item to persist or update
     */
    void saveOrUpdate(HibernateConfigItem item);

    /**
     * Number of all properties for a specific node
     *
     * @param node the node to count
     * @return number of properties
     */
    int countByNode(String node);

    /**
     * Number of properties matching name and node
     * there should be only one
     *
     * @param node the node to count
     * @param name name of the item
     * @return number of properties
     */
    int countByNodeAndName(String node, String name);

    /**
     * Get all HibernateConfigItem(s) for a specific node matching the
     * criteria. Passing a null to any of the optional params means
     * that param is not added to the query (i.e. any).
     *
     * @param node       - required, the node this item is for
     * @param name       - optional, an empty string is the same as null
     * @param defaulted  - optional, query for defaulted items
     * @param registered - optional, query for registered items
     * @param dynamic    - optional, query for dynamic items
     * @param secured    - optional, query for secured items
     * @return a List of matching config items
     */
    List<HibernateConfigItem> findAllByCriteriaByNode(String node, String name, Boolean defaulted, Boolean registered, Boolean dynamic, Boolean secured);

    /**
     * Retrieve config items that have a pollOn on or after and before the specified dates for a specific node
     * You can use the dates to specify a time range or if no dates are null then all config items
     * with a pollOn date will be returned.
     *
     * @param node      the node
     * @param onOrAfter items with a pollOn date on or after this Date are selected
     * @param before    items with a pollOn date before this Date are selected
     * @return List
     */
    List<HibernateConfigItem> findPollOnByNode(String node, Date onOrAfter, Date before);

}

