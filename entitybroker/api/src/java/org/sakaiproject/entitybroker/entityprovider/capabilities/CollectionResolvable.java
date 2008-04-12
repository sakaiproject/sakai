/**
 * $Id$
 * $URL$
 * Collectible.java - entity-broker - Apr 8, 2008 11:51:26 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import java.util.List;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

/**
 * This type of entity supports retrieval of entities in a collection based on a search,
 * this will be invoked when an entity space is accessed (/prefix) rather than accessing an individual
 * entity (/prefix/id)<br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface CollectionResolvable extends EntityProvider {

   /**
    * Allows these entities to be fetched based on search parameters,
    * this should never return null and if there are no entities then the list should be empty<br/>
    * <b>Note:</b> The entity class types in the list need to be able to be 
    * resolved from the ClassLoader of the EntityBrokerManager (currently this means deployed into shared)<br/> 
    * <br/>These do not have to be model objects and may simply
    * be something created (e.g. String, Map, etc.) to give to anyone calling this method
    * 
    * @param ref the parsed reference object which uniquely represents this entity,
    * only the prefix will be used from this reference (since that identifies the space and collection)
    * @param search a search object which can define the order to return entities,
    * search filters, and total number of entities returned
    * @return a list of entity objects of the type handled by this provider based on the search or empty if none found
    */
   public List<?> getEntities(EntityReference ref, Search search);

}
