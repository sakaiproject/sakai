/******************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.site.impl;

import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;

/**
 * Makes it possible to swap out legacy and new site caches
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
public interface SiteCache {
    /**
     * @param key the site reference
     * @param payload a Site object OR Boolean (indicates site exists)
     */
    void put(String key, Object payload);

    boolean containsKey(String key);

    /**
     * @param key the site reference
     * @return a Site object OR Boolean (indicates site exists)
     */
    Object get(String key);

    void clear();

    boolean remove(String key);

    /**
     * @param toolId the tool id
     * @return ToolConfiguration or null if not found in the cache
     */
    ToolConfiguration getTool(String toolId);

    /**
     * @param pageId the site page id
     * @return SitePage or null if not in the cache
     */
    SitePage getPage(String pageId);

    /**
     * @param groupId the site group id
     * @return Group or null if not in the cache
     */
    Group getGroup(String groupId);
}
