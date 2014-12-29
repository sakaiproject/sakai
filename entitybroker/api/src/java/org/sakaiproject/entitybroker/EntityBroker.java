/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Browseable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.BrowseEntity;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.LearningTrackingProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.SearchProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.TagProvider;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityEncodingException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;
import org.sakaiproject.entitybroker.util.EntityResponse;

/**
 * This service interface defines the capabilities of the entity broker system<br/> 
 * It allows Sakai system methods, developers, etc. to access Sakai entity information (new and old)
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public interface EntityBroker extends PropertiesProvider, TagProvider, SearchProvider, LearningTrackingProvider {

    /**
     * Check if an entity exists by the globally unique reference string, (the global reference
     * string will consist of the entity prefix and any local ID). If no {@link EntityProvider} for
     * the reference is found which implements {@link CoreEntityProvider}, this method will return
     * <code>true</code> by default, in other words, this cannot determine if a legacy
     * entity exists, only a new entity
     * 
     * @param reference a globally unique reference to an entity (e.g. /myprefix/myid), 
     * consists of the entity prefix and optional segments (normally the id at least)
     * @return true if the entity exists, false otherwise
     * @throws IllegalArgumentException if the reference is invalid
     */
    public boolean entityExists(String reference);

    /**
     * Check if a prefix is currently registered
     * @param prefix the string which represents a type of entity handled by an entity provider
     * @return true if the prefix is registered OR false if not
     */
    public boolean isPrefixRegistered(String prefix);

    /**
     * Retrieve a complete set of all currently registered {@link EntityProvider} prefixes
     * 
     * @return all currently registered entity prefixes
     */
    public Set<String> getRegisteredPrefixes();

    /**
     * Get the full absolute URL to the entity defined by this entity reference, this will fail-safe
     * to a direct URL to an entity space URL if that is all that is available
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments
     * @return a full URL string (e.g. http://server/direct/prefix/id)
     * @throws IllegalArgumentException if the reference or other inputs are invalid
     */
    public String getEntityURL(String reference);

    /**
     * Get the full absolute URL to the entity view defined by these params, this will fail-safe
     * to a direct URL to an entity space URL if that is all that is available,
     * this will use the default entity URL template associated with the viewKey and include
     * an optional extension if specified (these will be inferred if they are missing)
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optionally the local id
     * @param viewKey the specific view type to get the URL for,
     * use the VIEW_* constants from {@link EntityView} (e.g. {@link EntityView#VIEW_LIST}),
     * can be null to determine the key automatically
     * @param extension the optional extension to add to the end 
     * which defines the expected data which is returned,
     * use constants in {@link Formats} (e.g. {@link Formats#XML}),
     * can be null to use no extension,  default is assumed to be html if none is set
     * @return the full URL string to a specific entity or space,
     * (e.g. http://server/direct/prefix/id)
     * @throws IllegalArgumentException if the reference or other inputs are invalid
     */
    public String getEntityURL(String reference, String viewKey, String extension);

    /**
     * Get the {@link EntityView} object which represents a specific view
     * of an entity or entity collection, this is similar to {@link #getEntityURL(String, String, String)}
     * but allows the developer to deal with the {@link EntityView} object if desired
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optionally the local id
     * @param viewKey the specific view type to get the URL for,
     * use the VIEW_* constants from {@link EntityView} (e.g. {@link EntityView#VIEW_LIST}),
     * can be null to determine the key automatically
     * @param extension the optional extension to add to the end 
     * which defines the expected data which is returned,
     * use constants in {@link Formats} (e.g. {@link Formats#XML}),
     * can be null to use no extension,  default is assumed to be html if none is set
     * @return an EntityView object if one can be formed
     * @throws IllegalArgumentException if the params cannot be made into an EntityView 
     */
    public EntityView getEntityView(String reference, String viewKey, String extension);

    /**
     * Fire an event to Sakai with the specified name, targeted at the supplied reference, which
     * should be a reference to an existing entity managed by this broker<br/>
     * <b>NOTE:</b> This will allow events to be fired for references without a broker or invalid references
     * 
     * @param eventName a string which represents the name of the event (e.g. announcement.create),
     * cannot be null or empty
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optionally the local id,
     * cannot be null or empty
     */
    public void fireEvent(String eventName, String reference);

    /**
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optionally the local id,
     * cannot be null or empty
     * @param viewKey specifies what kind of request this is (create, read/show, etc.),
     * must correspond to the VIEW constants in {@link EntityView}, example: {@link EntityView#VIEW_SHOW}
     * @param format (optional) this is the format for this request (from {@link Formats}, e.g. XML),
     * if nothing is specified then the default will be used: {@link Formats#HTML}
     * @param params (optional) any params you want to send along with the request should
     * be included here, they will be placed into the query string or the request body
     * depending on the type of request this is
     * @param entity (optional) leave this null in most cases,
     * if you supply an entity object here it will be encoded based on the supplied format
     * (only if the entity supports output formatting) and then decoded on the other end
     * (only if the entity supports input translation), in most cases it is better to supply
     * the entity values in the params
     * @return the response information encoded in an object,
     * you must check this to see what the results of the request were
     * (getting a response back does not mean the request succeeded)
     * @throws IllegalArgumentException if the inputs are invalid
     * @throws RuntimeException if the http request has an unrecoverable failure or an encoding failure occurs
     */
    public EntityResponse fireEntityRequest(String reference, String viewKey, String format, Map<String, String> params, Object entity);

    /**
     * Parses an entity reference into a concrete object, of type {@link EntityReference}, or some
     * class derived from it, for example {@link IdEntityReference} or some other class of object
     * which is returned from {@link ParseSpecParseable}.
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments
     * @return an entity reference object which will contain the entity prefix and any optional
     *         segments, or <code>null</code> if the reference was not recognized as a valid entity
     *         handled by the broker (will be an {@link IdEntityReference} if there is an id set)
     * @throws IllegalArgumentException if the reference is invalid and cannot be parsed
     */
    public EntityReference parseReference(String reference);

    /**
     * Fetches a concrete object representing this entity reference; either one from the
     * {@link Resolvable} capability if implemented by the responsible {@link EntityProvider}, or
     * else from the underlying legacy Sakai entity system<br/>
     * Note that this may be a {@link String} or {@link Map} and does not have to be a POJO,
     * the type of object should be determined out of band<br/>
     * This will return null if the entity exists but is not {@link Resolvable} or available in the legacy entity system
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and local id
     * @return an object which represents the entity OR null if none can be found or this type does not support fetching
     * @throws SecurityException if the entity cannot be accessed by the current user or is not publicly accessible
     * @throws IllegalArgumentException if the reference is invalid
     * @throws IllegalStateException if any other error occurs
     */
    public Object fetchEntity(String reference);

    /**
     * Allows these entities to be fetched based on search parameters,
     * this should never return null and if there are no entities then the list should be empty<br/>
     * <b>Note:</b> The entity class types in the list need to be able to be 
     * resolved from the ClassLoader of the EntityBrokerManager (currently this means deployed into shared)<br/> 
     * <br/>These do not have to be model objects and may simply
     * be something created (e.g. String, Map, etc.) to give to anyone calling this method
     * 
     * @param prefix the string which represents a type of entity handled by an entity provider,
     * if the prefix does not support fetching collections then no entities will be returned
     * @param search a search object which can define the order to return entities,
     * search filters, and total number of entities returned,<br/>
     * NOTE: There are some predefined search keys which you may optionally use,
     * provider are encourage to support the SEARCH_* search keys listed in this interface
     * @return a list of entity objects (POJOs, {@link Map}, etc.) of the type handled by this provider
     * OR empty if none found, will not return null
     * @throws SecurityException if the data cannot be accessed by the current user or is not publicly accessible
     * @throws IllegalArgumentException if the reference is invalid or the search is invalid
     * @throws IllegalStateException if any other error occurs
     */
    public List<?> fetchEntities(String prefix, Search search, Map<String, Object> params);

    /**
     * Gets the data related to an entity as long as it exists,
     * always includes at least the entity reference information and the URL,
     * may also include a concrete entity object and entity properties
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and local id
     * @return an entity data object which contains data about the entity OR null if the entity does not exist
     * @throws SecurityException if the entity cannot be accessed by the current user or is not publicly accessible
     * @throws IllegalArgumentException if the reference is invalid
     * @throws IllegalStateException if any other error occurs
     */
    public EntityData getEntity(String reference);

    /**
     * Gets entity data (and possibly entities) for a specific entity prefix,
     * entity data contains the reference, URL, display title and optionally the concrete entity and properties<br/>
     * If the entity type indicated by the prefix does not support collections then this will return an empty list
     * 
     * @param prefix the string which represents a type of entity handled by an entity provider,
     * the entity prefix to search for the collection of entities in,
     * if the prefix does not support browsing then no entities will be returned
     * @param search a search object which can define the order to return entities,
     * search filters, and total number of entities returned, may be left empty
     * @param params (optional) incoming set of parameters which may be used to send data specific to this request, may be null
     * @return a list of entity data objects OR an empty list if none found
     * @throws SecurityException if the data cannot be accessed by the current user or is not publicly accessible
     * @throws IllegalArgumentException if the prefix is invalid or the search is invalid
     * @throws IllegalStateException if any other error occurs
     */
    public List<EntityData> getEntities(String prefix, Search search, Map<String, Object> params);

    /**
     * Format and output an entity or collection included or referred to by this entity ref object
     * into output according to the format string provided,
     * Should take into account the reference when determining what the entities are
     * and how to encode them
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments
     * @param format a string constant indicating the format (from {@link Formats}) 
     * for output, (example: {@link #XML})
     * @param entities (optional) a list of entities or {@link EntityData} objects to create formatted output for,
     * if this is null then the entities will be retrieved based on the reference,
     * if this contains only a single item AND the ref refers to a single entity
     * then the entity will be extracted from the list and encoded without the indication
     * that it is a collection, for all other cases the encoding will include an indication that
     * this is a list of entities
     * @param output the output stream to place the formatted data in,
     * should be UTF-8 encoded if there is char data
     * @param params (optional) an optional set of params to pass along with this custom action request,
     * typically used to provide information about the request, may be left null if not needed
     * @throws FormatUnsupportedException if entity cannot handle this format type
     * @throws IllegalArgumentException if any of the arguments are invalid
     * @throws EntityEncodingException is there is failure encoding the output
     * @throws IllegalStateException for all other failures
     */
    public void formatAndOutputEntity(String reference, String format, List<?> entities, OutputStream output, Map<String, Object> params);

    /**
     * Translates the input data stream in the supplied format into an entity object for this reference
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments
     * @param format a string constant indicating the format (from {@link Formats}) 
     * of the input, (example: {@link #XML})
     * @param input a stream which contains the data to make up this entity,
     * you may assume this is UTF-8 encoded if you don't know anything else about it
     * @param params (optional) set of parameters which may be used to control this request, may be left null if not needed
     * @return an entity object of the type used for the given reference
     * @throws FormatUnsupportedException if entity cannot handle this format type
     * @throws IllegalArgumentException if any of the arguments are invalid
     * @throws EntityEncodingException is there is failure translating the input
     * @throws IllegalStateException for all other failures
     */
    public Object translateInputToEntity(String reference, String format, InputStream input, Map<String, Object> params);

    /**
     * This will execute a custom action for an entity or space/collection of entities<br/>
     * This is meant for specialized usage as custom actions are typically meant to be executed by REST calls only
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments
     * @param action key which will be used to trigger the action (e.g. promote, double, photo),
     * can be triggered by a URL like so: /user/aaronz/promote
     * @param params (optional) an optional set of params to pass along with this custom action request,
     * typically used to provide information about the request, may be left null if not needed
     * @param outputStream (optional) an OutputStream to place binary or long text data into,
     * if this is used for binary data then the {@link ActionReturn} should be returned with the correct encoding information
     * and the output variable set to the OutputStream, may be left null if this custom action does not deal with binary streams
     * @return an {@link ActionReturn} which contains entity data or binary/string data OR null if there is no return for this action
     * @throws UnsupportedOperationException if there is no action with this key for this entity
     * @throws IllegalArgumentException if there are required params that are missing or invalid
     * @throws IllegalStateException if the action cannot be performed for some reason
     */
    ActionReturn executeCustomAction(String reference, String action, Map<String, Object> params, OutputStream outputStream);

    /** 
     * @deprecated use {@link TagProvider#getTagsForEntity(String)}
     */
    public Set<String> getTags(String reference);

    /**
     * @deprecated use {@link TagProvider#setTagsForEntity(String, String[])}
     */
    public void setTags(String reference, String[] tags);

    /**
     * Search for all entities which have the given tags,
     * can limit the return using the search object<br/>
     * 
     * @param tags a set of tags associated with entities
     * @param prefixes (optional) a set of unique entity prefixes, 
     * limits the search to only include entities in these prefixes,
     * if this is null then all entities and prefixes are searched<br/>
     * NOTE: It is much more efficient to specify prefixes
     * @param matchAll if true then all tags must exist on the entity for it to be matched,
     * if false then the entity just has to have one or more of the given tags
     * @param search (optional) a search object, used to order or limit the number of returned results,
     * restrictions will be typically ignored
     * @param params (optional) an optional set of params to pass along with this custom action request,
     * typically used to provide information about the request, may be left null if not needed
     * 
     * @return a list of entity search results (contains the ref, url, displayname of the matching entities)
     * @throws IllegalArgumentException if the tags set is empty or null
     */
    public List<EntityData> findEntitesByTags(String[] tags, String[] prefixes, boolean matchAll, Search search, Map<String, Object> params);

    // BROWSE

    /**
     * Returns the list of entity information (and possibly entities) for a user to view while browsing an
     * entity space, this is specially designed to support browsing and picking entities,
     * not all entities support browsing<br/>
     * If the entity type indicated by the prefix does not support browsing then this will return an empty list
     * 
     * @param prefix the string which represents a type of entity handled by an entity provider,
     * the entity prefix to search for browseable entities in,
     * if the prefix does not support browsing then no entities will be returned
     * @param search a search object which can define the order to return entities,
     * search filters, and total number of entities returned, may be left empty
     * @param userReference (optional) the unique entity reference for a user which is browsing the results, 
     * this may be null to indicate that only items which are visible to all users should be shown
     * @param associatedReference (optional) 
     *           a globally unique reference to an entity, this is the entity that the 
     *           returned browseable data must be associated with (e.g. limited by reference to a location or associated entity), 
     *           this may be null to indicate there is no association limit
     * @param parentReference (optional) if not null then only the entities which have the referenced entity as a parent will be searched,
     * if null then the parent/child relationship would be ignored when searching browseable entities
     * @param params (optional) incoming set of parameters which may be used to send data specific to this request, may be null
     * @return a list of entity data objects which contain the reference, URL, display title and optionally other entity data
     * @throws SecurityException if the data cannot be accessed by the current user or is not publicly accessible
     * @throws IllegalArgumentException if the prefix is invalid or the search is invalid
     * @throws IllegalStateException if any other error occurs
     */
    public List<EntityData> browseEntities(String prefix, Search search, String userReference, String associatedReference, String parentReference, Map<String, Object> params);

    /**
     * For authors of entity browsing systems, this provides a list of all the meta data related to the entities
     * in the system which are {@link Browseable}, this provides the root entities when there is no parent prefix provided <br/>
     * 
     * @param parentPrefix (optional) the parent prefix to get the browseable entities for,
     * if this is null then all the root browseable entities are returned
     * @return the list of meta data for all entity prefixes that are {@link Browseable}
     */
    public List<BrowseEntity> getBrowseableEntities(String parentPrefix);

}
