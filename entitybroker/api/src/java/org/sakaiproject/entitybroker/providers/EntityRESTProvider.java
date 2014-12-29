/**
 * $Id$
 * $URL$
 * EntityRESTProvider.java - entity-broker - Jan 13, 2009 6:15:42 PM - azeckoski
 **********************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 **********************************************************************************/

package org.sakaiproject.entitybroker.providers;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityEncodingException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;
import org.sakaiproject.entitybroker.util.EntityResponse;


/**
 * Handles anything REST based that is not part of the core EB registration piece
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface EntityRESTProvider {

    /**
     * Processes and handles an entity request without requiring servlet processing
     * and a request/response round trip
     * 
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
    public EntityResponse handleEntityRequest(String reference, String viewKey, String format, Map<String, String> params, Object entity);

    /**
     * Format and output an entity or collection included or referred to by this entity ref object
     * into output according to the format string provided,
     * Should take into account the reference when determining what the entities are
     * and how to encode them
     * (This is basically a copy of the code in EntityHandlerImpl with stuff removed)
     * 
     * @param ref a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments
     * @param format a string constant indicating the format (from {@link Formats}) 
     * for output, (example: {@link #XML})
     * @param entities (optional) a list of entities to create formatted output for,
     * if this is null then the entities should be retrieved based on the reference,
     * if this contains only a single item AND the ref refers to a single entity
     * then the entity should be extracted from the list and encoded without the indication
     * that it is a collection, for all other cases the encoding should include an indication that
     * this is a list of entities
     * @param outputStream the output stream to place the formatted data in,
     * should be UTF-8 encoded if there is char data
     * @throws FormatUnsupportedException if you do not handle this format type (passes control to the internal handlers)
     * @throws EntityEncodingException if you cannot encode the received data into an entity
     * @throws IllegalArgumentException if any of the arguments are invalid
     * @throws IllegalStateException for all other failures
     */
    public void formatAndOutputEntity(EntityReference ref, String format, List<EntityData> entities, OutputStream outputStream, Map<String, Object> params);

    /**
     * Translates the input data stream in the supplied format into an entity object for this reference
     * 
     * @param ref a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments
     * @param format a string constant indicating the format (from {@link Formats}) 
     * of the input, (example: {@link #XML})
     * @param input a stream which contains the data to make up this entity,
     * you may assume this is UTF-8 encoded if you don't know anything else about it
     * @return an entity object of the type used for the given reference
     * @throws FormatUnsupportedException if you do not handle this format type (passes control to the internal handlers)
     * @throws EntityEncodingException if you cannot encode the received data into an entity
     * @throws IllegalArgumentException if any of the arguments are invalid
     * @throws IllegalStateException for all other failures
     */
    public Object translateInputToEntity(EntityReference ref, String format, InputStream inputStream, Map<String, Object> params);

    /**
     * This will execute a custom action for an entity or space/collection of entities<br/>
     * This is meant for specialized usage as custom actions are typically meant to be executed by REST calls only
     * 
     * @param actionProvider the action provider which has declared itself as the handler for this action and prefix
     * @param ref a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments
     * @param action key which will be used to trigger the action (e.g. promote, double, photo),
     * can be triggered by a URL like so: /user/aaronz/promote
     * @param actionParams (optional) an optional set of params to pass along with this custom action request,
     * typically used to provide information about the request, may be left null if not needed
     * @param outputStream (optional) an OutputStream to place binary or long text data into,
     * if this is used for binary data then the {@link ActionReturn} should be returned with the correct encoding information
     * and the output variable set to the OutputStream, may be left null if this custom action does not deal with binary streams
     * @param view the entity view corresponding to all data sent in the URL for this action view
     * @param searchParams any search params which were included in the request
     * @return an {@link ActionReturn} which contains entity data or binary/string data OR null if there is no return for this action
     * @throws UnsupportedOperationException if there is no action with this key for this entity
     * @throws IllegalArgumentException if there are required params that are missing or invalid
     * @throws IllegalStateException if the action cannot be performed for some reason
     */
    public ActionReturn handleCustomActionExecution(ActionsExecutable actionProvider, EntityReference ref, String action, 
            Map<String, Object> actionParams, OutputStream outputStream, EntityView view, Map<String, Object> searchParams);

    /**
     * Encode data into a given format, can handle any java object,
     * note that unsupported formats and invalid data will result in an exception
     * 
     * @param data the data to encode
     * @param format the format to use for output (from {@link Formats})
     * @param name (optional) the name to use for the encoded data (e.g. root node for XML)
     * @param properties (optional) extra properties to add into the encoding, ignored if encoded object is not a map or bean
     * @return the encoded string in the requested format
     * @throws UnsupportedOperationException if the data cannot be encoded
     * @throws IllegalArgumentException if the format requested cannot be encoded because there is no encoder
     */
    public String encodeData(Object data, String format, String name, Map<String, Object> properties);

    /**
     * Decode a string of a specified format into a java map of simple objects <br/> 
     * Returned map can be fed into {@link #populate(Object, Map)} if you want to convert it
     * into a known object type <br/> 
     * Types are likely to require conversion as guesses are made about the right formats,
     * use of the {@link #convert(Object, Class)} method is recommended <br/>
     * 
     * @param data encoded data
     * @param format the format of the encoded data (from {@link Formats})
     * @return a map containing all the data derived from the encoded data
     * @throws UnsupportedOperationException if the data cannot be decoded
     * @throws IllegalArgumentException if the data cannot be decoded because there is no decoder for that format
     */
    public Map<String, Object> decodeData(String data, String format);

}
