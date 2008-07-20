/**
 * $Id$
 * $URL$
 * HttpResponse.java - entity-broker - Jul 20, 2008 12:19:23 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.util;

import java.util.Map;

import org.sakaiproject.entitybroker.EntityBroker;


/**
 * This is here to contain the information we get back from an entity (http) request fired
 * by the {@link EntityBroker#fireEntityRequest(String, String, String, Map, Object)} method<br/>
 * This object and the data in it should be considered immutable
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityResponse {

   /**
    * The http response code
    */
   public int responseCode = 200;
   /**
    * The response message
    */
   public String responseMessage = "";
   /**
    * the body of the response
    */
   public String responseBody = "";
   /**
    * The map of the response headers,
    * this may be null
    */
   public Map<String, String> responseHeaders;

   public EntityResponse(int responseCode, String responseMessage, String responseBody,
         Map<String, String> responseHeaders) {
      this.responseCode = responseCode;
      this.responseMessage = responseMessage;
      this.responseBody = responseBody;
      this.responseHeaders = responseHeaders;
   }

   public int getResponseCode() {
      return responseCode;
   }

   public String getResponseMessage() {
      return responseMessage;
   }

   public String getResponseBody() {
      return responseBody;
   }

   public Map<String, String> getResponseHeaders() {
      return responseHeaders;
   }

}
