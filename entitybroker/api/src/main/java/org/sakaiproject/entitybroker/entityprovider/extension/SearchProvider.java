/**
 * $Id: SearchProvider.java 59674 2009-04-03 23:05:58Z arwhyte@umich.edu $
 * $URL:  $
 * SearchProvider.java - entity-broker - Apr 5, 2008 7:19:14 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.entitybroker.entityprovider.extension;

import org.sakaiproject.entitybroker.entityprovider.capabilities.Searchable;

/**
 * Defines the methods related to searching for entities (shared between interfaces),
 * implementing this allows for support for the core EB search functionality
 * 
 * @author Aaron Zeckoski (azeckoski@unicon.net)
 */
public interface SearchProvider {

    /**
     * Do a search for entities and get back the listing of results
     * @param query the search query object (allows paging etc.)
     * @return the search results
     */
    public SearchResults search(QuerySearch query);

    /**
     * Add some content to the search index for a given reference,
     * this will replace existing content (not merge)
     * 
     * @param reference a globally unique reference to an entity (e.g. /myprefix/myid), 
     * consists of the entity prefix and optional segments (normally the id at least)
     * @param content [OPTIONAL] if null the content will be retrieved by called the {@link Searchable}
     * methods, otherwise this will simply index the content it is given into the search system
     * @return true if added a new search index entry, or false if it replaced an existing one
     * @throws UnsupportedOperationException if this reference cannot be searched
     */
    public boolean add(String reference, SearchContent content);

    /**
     * @param reference a globally unique reference to an entity (e.g. /myprefix/myid), 
     * consists of the entity prefix and optional segments (normally the id at least)
     * @return true if the index entry was removed, false otherwise
     */
    public boolean remove(String reference);

    /**
     * WARNING: this should mostly never be run but it tells 
     * the search provider to purge the indexes and to request new search data
     * for the given context
     * 
     * @param context this generally represents either a site or a group in the system
     */
    public void resetSearchIndexes(String context);

}
