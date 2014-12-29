/**
 * $Id$
 * $URL$
 * EntityBrokerManager.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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
 **/

package org.sakaiproject.entitybroker;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderMethodStore;
import org.sakaiproject.entitybroker.entityprovider.capabilities.BrowseSearchable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.BrowseEntity;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetterWrite;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorageWrite;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.providers.EntityPropertiesService;
import org.sakaiproject.entitybroker.providers.EntityRESTProvider;
import org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider;

/**
 * The core of the EB system,
 * this is generally not for use by developers and is mostly for internal use but
 * it should be ok to use most of these methods
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface EntityBrokerManager {

    public static final String POST_METHOD = "_method";

    // Normal interface

    /**
     * Determines if an entity exists based on the reference
     * 
     * @param reference an entity reference object
     * @return true if entity exists, false otherwise
     */
    public boolean entityExists(EntityReference ref);

    /**
     * Creates the full URL to an entity using the sakai {@link ServerConfigurationService}, 
     * (e.g. http://server:8080/direct/entity/123/)<br/>
     * <br/>
     * <b>Note:</b> the webapp name (relative URL path) of the direct servlet, of "/direct" 
     * is hardcoded into this method, and the
     * {@link org.sakaiproject.entitybroker.util.servlet.DirectServlet} must be deployed there on this
     * server.
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optionally the local id
     * @param viewKey the specific view type to get the URL for,
     * can be null to determine the key automatically
     * @param extension the optional extension to add to the end,
     * can be null to use no extension
     * @return the full URL to a specific entity or space
     */
    public String getEntityURL(String reference, String viewKey, String extension);

    /**
     * Reduce code duplication and ensure custom templates are used
     */
    public EntityView makeEntityView(EntityReference ref, String viewKey, String extension);

    /**
     * Parses an entity reference into the appropriate reference form
     * 
     * @param reference a unique entity reference
     * @return the entity reference object or 
     * null if there is no provider found for the prefix parsed out
     * @throws IllegalArgumentException if there is a failure during parsing
     */
    public EntityReference parseReference(String reference);

    /**
     * Parses an entity URL into an entity view object,
     * handles custom parsing templates
     * 
     * @param entityURL an entity URL
     * @return the entity view object representing this URL or 
     * null if there is no provider found for the prefix parsed out
     * @throws IllegalArgumentException if there is a failure during parsing
     */
    public EntityView parseEntityURL(String entityURL);

    /**
     * Make a full entity URL (http://....) from just a path URL (/prefix/id.xml)
     * @param pathURL a path (like pathInfo from a request) (e.g. /prefix/id.xml)
     * @throws IllegalArgumentException is the pathURL is null
     */
    public String makeFullURL(String pathURL);

    /**
     * Get an entity object of some kind for this reference if it has an id,
     * will simply return null if no id is available in this reference
     * 
     * @param ref an entity reference
     * @return the entity object for this reference OR null if none can be retrieved
     */
    public Object fetchEntity(EntityReference ref);

    /**
     * Get the entity data for a reference if possible
     * 
     * @param ref an entity reference
     * @return an {@link EntityData} object for this reference if one can be found OR null if not
     */
    public EntityData getEntityData(EntityReference ref);

    /**
     * Get a list of entities from {@link CollectionResolvable} first if available or {@link BrowseSearchable} if not,
     * returns the entities as actual entities (converts from {@link EntityData} if that was used),
     * correctly handles references to single entities as well
     * 
     * @param ref the reference
     * @param search a search (should not be null)
     * @param params
     * @return a list of entities OR empty list if none found for the given reference
     */
    public List<?> fetchEntities(EntityReference ref, Search search, Map<String, Object> params);

    /**
     * Get a list of entities from {@link CollectionResolvable} first if available or {@link BrowseSearchable} if not,
     * returns the entities wrapped in {@link EntityData},
     * correctly handles references to single entities as well
     * 
     * @param ref the reference
     * @param search a search (should not be null)
     * @param params
     * @return a list of entities OR empty list if none found for the given reference
     */
    public List<EntityData> getEntitiesData(EntityReference ref, Search search,
            Map<String, Object> params);

    /**
     * Fetches the browseable entities
     * @param prefix
     * @param search
     * @param userReference
     * @param associatedReference
     * @param parentEntityRef
     * @param params
     * @return a list of entity data results to browse
     */
    public List<EntityData> browseEntities(String prefix, Search search, String userReference,
            String associatedReference, EntityReference parentEntityRef, Map<String, Object> params);

    /**
     * Get the meta data about browseable entities
     * @param parentPrefix the prefix of the parent type (null for the root types)
     * @return the list of browseable entity meta data
     */
    public List<BrowseEntity> getBrowseableEntities(String parentPrefix);

    /**
     * Convert a list of objects to entity data objects (also populates them),
     * will preserve null (i.e. null in => null out)
     */
    public List<EntityData> convertToEntityData(List<?> entities, EntityReference ref);

    /**
     * Convert a single object to an entity data object (also populates it),
     * will preserve null (i.e. null in => null out)
     */
    public EntityData convertToEntityData(Object entity, EntityReference ref);

    /**
     * Add in the extra meta data (URL, title, etc.) to all entity data objects,
     * handles it as efficiently as possible without remaking an entity view on every call,
     * this is fail safe (i.e. it should throw no exceptions)
     * 
     * @param data a list of entity data
     */
    public void populateEntityData(List<EntityData> data);

    /**
     * Add in the extra meta data (URL, title, etc.) to all entity data objects,
     * handles it as efficiently as possible without remaking an entity view on every call,
     * this is fail safe (i.e. it should throw no exceptions)
     * 
     * @param data a list of entity data
     */
    public void populateEntityData(EntityData[] data);

    /**
     * Safely get the sample entity object which is defined for a prefix,
     * if there is not one then return null
     * @param prefix the entity prefix
     * @param id (optional) will get the actual entity for this id as a sample
     * @return a sample object OR null if none can be found
     */
    public Object getSampleEntityObject(String prefix, String id);

    
    // Special service handling methods
    /**
     * Allows access to the current EntityProviderManager service
     * @return the current EntityProviderManager service
     */
    public EntityProviderManager getEntityProviderManager();

    /**
     * Allows access to the current EntityPropertiesService
     * @return the current EntityPropertiesService
     */
    public EntityPropertiesService getEntityPropertiesService();

    /**
     * Allows access to the current EntityViewAccessProviderManager service
     * @return the current EntityViewAccessProviderManager
     */
    public EntityViewAccessProviderManager getEntityViewAccessProviderManager();

    /**
     * Allows access to the current EntityProviderMethodStore service
     * @return the current EntityProviderMethodStore
     */
    public EntityProviderMethodStore getEntityProviderMethodStore();

    /**
     * Allows access to the current RequestGetter service
     * @return the current RequestGetter
     */
    public RequestGetterWrite getRequestGetter();

    /**
     * Allows access to the current RequestStorageWrite service
     * @return the current RequestStorageWrite
     */
    public RequestStorageWrite getRequestStorage();

    /**
     * Allows access to any registered ExternalIntegrationProvider
     * @return the external integration provider OR null if there is not one
     */
    public ExternalIntegrationProvider getExternalIntegrationProvider();

    /**
     * Allows access to the registered REST provider if there is one
     * @return the REST provider OR null if there is not one
     */
    public EntityRESTProvider getEntityRESTProvider();

    /**
     * Allows developers to setup providers to handle parts of the EB system which cannot
     * really be handled internally, the system will operate without this set
     * @param externalIntegrationProvider the external integration provider to use in the system
     */

    public void setExternalIntegrationProvider(ExternalIntegrationProvider externalIntegrationProvider);

    /**
     * Allows the developer to set a REST provider to add functionality to the {@link EntityBroker}
     * system from a REST handler, the system will operate without this set but some methods will fail
     * @param entityRESTProvider a service to provide REST functionality
     */
    public void setEntityRESTProvider(EntityRESTProvider entityRESTProvider);

    /**
     * Used to get a version info string which can be output to see what version we are working with
     */
    public String getVersionInfo();

    /**
     * FOR INTERNAL USE ONLY (do not mess with this in other words)
     * @param servletContext sets the servlet context being used by the system (defaults to {@link #DIRECT})
     */
    public void setServletContext(String servletContext);
    /**
     * FOR INTERNAL USE ONLY (do not mess with this in other words)
     * This gets the known REST servlet context if there is one, will return a default "/rest" if none is known, will not return "" or null
     */
    public String getServletContext();

	/**
	 * Returns the maximum depth of object graph allowed during transcoding to JSON
	 */
	public int getMaxJSONLevel();

}
