/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.TagProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.TagSearchProvider;
import org.sakaiproject.entitybroker.util.EntityResponse;

/**
 * This service interface defines the capabilities of the entity broker system<br/> 
 * It allows Sakai system methods, developers, etc. to access Sakai entity information (new and old)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public interface EntityBroker extends PropertiesProvider, TagProvider, TagSearchProvider {

   /**
    * Check if an entity exists by the globally unique reference string, (the global reference
    * string will consist of the entity prefix and any local ID). If no {@link EntityProvider} for
    * the reference is found which implements {@link CoreEntityProvider}, this method will return
    * <code>true</code> by default, in other words, this cannot determine if a legacy
    * entity exists, only a new entity
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments (normally the id at least)
    * @return true if the entity exists, false otherwise
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
    * use constants in {@link Outputable} (e.g. {@link Outputable#XML}),
    * can be null to use no extension,  default is assumed to be html if none is set
    * @return the full URL string to a specific entity or space,
    * (e.g. http://server/direct/prefix/id)
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
    * use constants in {@link Outputable} (e.g. {@link Outputable#XML}),
    * can be null to use no extension,  default is assumed to be html if none is set
    * @return an EntityView object if one can be formed
    * @throws IllegalArgumentException if the params cannot be made into an EntityView 
    */
   public EntityView getEntityView(String reference, String viewKey, String extension);

   /**
    * Fire an event to Sakai with the specified name, targetted at the supplied reference, which
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
    */
   public EntityReference parseReference(String reference);

   /**
    * Fetches a concrete object representing this entity reference; either one from the
    * {@link Resolvable} capability if implemented by the responsible {@link EntityProvider}, or
    * else from the underlying legacy Sakai entity system
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments
    * @return an object which represents the entity or null if none can be found
    */
   public Object fetchEntity(String reference);

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
    * @param entities (optional) a list of entities to create formatted output for,
    * if this is null then the entities should be retrieved based on the reference,
    * if this contains only a single item AND the ref refers to a single entity
    * then the entity should be extracted from the list and encoded without the indication
    * that it is a collection, for all other cases the encoding should include an indication that
    * this is a list of entities
    * @param output the output stream to place the formatted data in,
    * should be UTF-8 encoded if there is char data
    * @param params (optional) set of parameters which may be used to control this request, may be left null if not needed
    * @throws IllegalArgumentException if the entity does not support output formatting or any arguments are invalid
    * @throws EncodingException is there is failure encoding the output
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
    * @throws IllegalArgumentException if the entity does not support input translation or any arguments are invalid
    * @throws EncodingException is there is failure encoding the input
    */
   public Object translateInputToEntity(String reference, String format, InputStream input, Map<String, Object> params);

   /**
    * This will execute a custom action for an entity or space/collection of entities<br/>
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments
    * @param action key which will be used to trigger the action (e.g. promote, double, photo),
    * can be triggered by a URL like so: /user/aaronz/promote
    * @param requestValues (optional) this is an array which contains passed in action params,
    * can be left blank if there are no custom params or this action
    * @param outputStream an OutputStream to place binary or long text data into,
    * if this is used for binary data then the {@link ActionReturn} should be returned with the correct encoding information
    * and the output variable set to the OutputStream
    * @return an {@link ActionReturn} which contains entity data or binary/string data OR null if there is no return for this action
    * @throws UnsupportedOperationException if there is no action with this key for this entity
    * @throws IllegalArgumentException if there are required params that are missing or invalid
    * @throws IllegalStateException if the action cannot be performed for some reason
    */
   ActionReturn executeCustomAction(String reference, String action, Map<String, Object> actionParams, OutputStream outputStream);

}
