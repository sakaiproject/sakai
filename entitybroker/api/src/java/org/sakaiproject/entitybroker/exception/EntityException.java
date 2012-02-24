/**
 * $Id$
 * $URL$
 * EntityExistsException.java - entity-broker - Apr 6, 2008 8:52:59 AM - azeckoski
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

package org.sakaiproject.entitybroker.exception;

import javax.servlet.http.HttpServletResponse;

/**
 * This exception is used to indicate that a problem occurred attempting to get an entity,
 * the reference to the failed entity should be returned in the exception along with the 
 * response code which is indicative of the failure (if possible)
 * 
 * HTTP Response Codes (http://en.wikipedia.org/wiki/List_of_HTTP_status_codes)<br/>
<b>Client Error</b><br/>
400 Bad Request
    The request could not be understood by the server due to malformed syntax. <br/>
401 Unauthorized
    The request requires user authentication. This will cause a SecurityException to be thrown in the direct servlet. <br/>
404 Not Found
    The requested resource could not be found. Typically indicates the entity as identified cannot be located. <br/>
405 Method Not Allowed
    The method specified in the Request-Line is not allowed for the resource identified by the Request-URI. 
    The response MUST include an Allow header containing a list of valid methods for the requested resource. <br/>
422 Unprocessable Entity
    The server understands the media type of the request entity, but was unable to process the contained instructions. <br/>
501 Not Implemented
    The prefix indicated is not handled by the entity system<br/>
<br/>
<b>Redirection</b><br/>
303 See Other
    The response to the request can be found under a different URI and SHOULD be retrieved using a GET method on that resource. 
    This method exists primarily to allow the output of a POST-activated script to redirect the user agent to a selected resource. <br/>
<br/>
<b>Successful</b><br/>
200 OK
    The request has succeeded.<br/> 
201 Created
    The request has been fulfilled and resulted in a new resource being created. 
    The newly created resource can be referenced by the URI(s) returned in the entity of the response, 
    with the most specific URI for the resource given by a Location header field. <br/>
204 No Content
    The server has fulfilled the request but does not need to return an entity-body, and might want to return updated metainformation. <br/>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityException extends EntityBrokerException {

   /**
    * This is the response code related to the failure that occurred,
    * should match with constants in {@link HttpServletResponse}
    */
   public int responseCode = HttpServletResponse.SC_NOT_FOUND;

   /**
    * Create an exception to indicate that this entity could not found,
    * this will trigger an HTTP NOT FOUND error if not caught before reaching the direct servlet,
    * defaults to not found response
    * 
    * @param message
    * @param entityReference the unique reference to an entity
    */
   public EntityException(String message, String entityReference) {
      super(message, entityReference);
   }

   /**
    * Create an exception to indicate that this entity could not found,
    * this will trigger an HTTP NOT FOUND error if not caught before reaching the direct servlet
    *
    * @param message
    * @param entityReference the unique reference to an entity
    * @param responseCode the response code related to the failure that occurred,
    * should match with the SC constants in {@link HttpServletResponse}
    */
   public EntityException(String message, String entityReference, int responseCode) {
      super(message, entityReference);
      this.responseCode = responseCode;
   }

}
