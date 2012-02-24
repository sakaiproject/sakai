/**
 * $Id$
 * $URL$
 * Collectible.java - entity-broker - Apr 8, 2008 11:51:26 AM - azeckoski
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
 * This type of entity supports retrieval of entities in a collection based on a search,
 * this will be invoked when an entity space is accessed (/prefix) rather than accessing an individual
 * entity (/prefix/id)<br/>
 * The data is returned as a list of entity objects ({@link Object}, {@link Map}, whatever POJO, etc.)
 * OR as a list of {@link EntityData} objects (which can contain the entities or just information
 * about them like properties, url, etc.
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * This extends {@link Resolvable} and is part of {@link CRUDable}
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface CollectionResolvable extends EntityProvider, Resolvable {

   /**
    * A search key which indicates the results should be limited by the unique reference for a user
    */
   public static final String SEARCH_USER_REFERENCE = "_userReference";
   /**
    * A search key which indicates the results should be limited by the unique reference for a location (site, group, etc.)
    */
   public static final String SEARCH_LOCATION_REFERENCE = "_locationReference";
   /**
    * A search key which indicates the results should be limited by a tag or an array of tags
    */
   public static final String SEARCH_TAGS = "_tags";

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
    * search filters, and total number of entities returned,<br/>
    * NOTE: There are some predefined search keys which you may optionally support,
    * provider are encourage to support the SEARCH_* search keys listed in this interface
    * @return a list of entity objects (POJOs, {@link Map}, etc.) of the type handled by this provider
    * OR a list of {@link EntityData} objects based on the search OR empty if none found,
    * should not return null
    * @throws SecurityException if the data cannot be accessed by the current user or is not publicly accessible
    * @throws IllegalArgumentException if the reference is invalid or the search is invalid
    * @throws IllegalStateException if any other error occurs
    */
   public List<?> getEntities(EntityReference ref, Search search);

}
