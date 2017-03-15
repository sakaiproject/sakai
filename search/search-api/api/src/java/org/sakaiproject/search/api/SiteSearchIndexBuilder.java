/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.search.api;


import org.sakaiproject.search.model.SearchBuilderItem;

import java.util.List;

public interface SiteSearchIndexBuilder extends SearchIndexBuilder {

    /**
     * Rebuild the index for the supplied siteId
     * @param currentSiteId
     */
    void rebuildIndex(String currentSiteId);

    /**
     * Refresh the index for the supplied siteId
     * @param currentSiteId
     */
    void refreshIndex(String currentSiteId);

    /**
     * get a list of Master Search Items that control the search operation for the
     * Site (current site)
     * @return
     */
    List<SearchBuilderItem> getSiteMasterSearchItems();

    boolean isOnlyIndexSearchToolSites();

    boolean isExcludeUserSites();

}
