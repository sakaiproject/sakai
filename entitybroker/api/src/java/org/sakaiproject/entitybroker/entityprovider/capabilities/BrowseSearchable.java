/**
 * $Id$
 * $URL$
 * BrowseSearchable.java - entity-broker - Aug 3, 2008 9:24:50 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.EntitySearchResult;
import org.sakaiproject.entitybroker.entityprovider.search.Search;


/**
 * This indicates that this entity will participate in browse functionality for entities,
 * For example, it will provide lists of entities which are visible to users in locations
 * which can be looked through and selected<br/>
 * This provides fine grained control over which entities will appear in a browse list,
 * normally {@link CollectionResolvable} should show all entities, however, for the browse list
 * we will explicitly filter by users and/or locations and may not show all entities,
 * entities which do not implement this or {@link Browseable} will not appear in lists of entities which are being browsed<br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * This extends {@link Browseable}
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface BrowseSearchable extends Browseable {

   /**
    * @param search a search object which can define the order to return entities,
    * search filters, and total number of entities returned, may be empty but cannot be null
    * @param userReference (optional) the unique entity reference for a user, 
    * this may be null to indicate that items which are visible to all users should be shown
    * @param reference (optional) 
    *           a globally unique reference to an entity, this is the entity that the 
    *           returned browseable data must be associated with (e.g. limited by reference to a location or user entity), 
    *           this may be null to indicate there is no association limit
    * @param params (optional) incoming set of parameters which may be used to send data specific to this request, may be null
    * @return a list of search result objects which contain the reference, URL, display title and optionally other entity data
    */
   public List<EntitySearchResult> browseEntities(Search search, String userReference, String reference, Map<String, Object> params);

}
