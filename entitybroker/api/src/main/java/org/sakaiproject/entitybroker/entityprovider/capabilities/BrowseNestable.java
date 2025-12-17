/**
 * $Id$
 * $URL$
 * BrowseSearchable.java - entity-broker - Aug 3, 2008 9:24:50 AM - azeckoski
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

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.search.Search;


/**
 * This indicates that this entity will participate in browse functionality for entities
 * and it is nested within another entity: Example: <br/>
 * A blog (parent) contains blog entries (children). When browsing a blog there is a need to be able to select
 * the entries within the blog, the provider for the entry would implement this interface
 * to allow the set of entries to be chosen within the blog <br/>
 * The blog provider would not need to implement anything extra <br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface BrowseNestable extends Browseable {

    /**
     * Defines the parent entity type for this one
     * 
     * @return the prefix of the entity type which is the parent of this entity type
     */
    public String getParentprefix();

    /**
     * Returns the list of entities being browsed which are nested under a parent entity with a given reference <br/>
     * Example: Parent entity is /blog/123, there are 3 entries in this blog, the search limits the return to 2 entities
     * This method should return the first 2 blog entries in the blog with reference /blog/123
     * 
     * @param parentRef the reference object for the parent entity
     * @param search (optional) a search object which can define the order to return entities,
     * search filters, and total number of entities returned, may be empty but will not be null,
     * implementors are encouraged to support ordering and limiting of the number of returned results at least
     * @param userReference (optional) the unique entity reference for a user which is browsing the results, 
     * this may be null to indicate that only items which are visible to all users should be shown
     * @param associatedReference (optional) a globally unique reference to an entity, this is the entity that the 
     *           returned browseable data must be associated with (e.g. limited by reference to a location/site/group or other associated entity), 
     *           this may be null to indicate there is no association limit
     * @param params (optional) incoming set of parameters which may be used to send data specific to this request, may be null
     * @return a list of entity data objects which contain the reference, URL, display title and optionally other entity data
     * @throws SecurityException if the data cannot be accessed by the current user or is not publicly accessible
     * @throws IllegalArgumentException if the reference is invalid or the search is invalid
     * @throws IllegalStateException if any other error occurs
     */
    public List<EntityData> getChildrenEntities(EntityReference parentRef, Search search, String userReference, String associatedReference, Map<String, Object> params);

}
