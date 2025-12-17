/**
 * $Id$
 * $URL$
 * HttpResponse.java - entity-broker - Jul 20, 2008 12:19:23 PM - azeckoski
 **************************************************************************
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
 */

package org.sakaiproject.entitybroker.util.http;

import java.util.Map;


/**
 * This is here to contain the information we get back from an http request fired
 * by the methods in {@link HttpRESTUtils}
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class HttpResponse {

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
   public Map<String, String[]> responseHeaders;

   public HttpResponse(int responseCode) {
      this.responseCode = responseCode;
   }

   public HttpResponse(int responseCode, String responseMessage, String responseBody,
         Map<String, String[]> responseHeaders) {
      this.responseCode = responseCode;
      this.responseMessage = responseMessage;
      this.responseBody = responseBody;
      this.responseHeaders = responseHeaders;
   }

   public int getResponseCode() {
      return responseCode;
   }
   
   public void setResponseCode(int responseCode) {
      this.responseCode = responseCode;
   }
   
   public String getResponseMessage() {
      return responseMessage;
   }
   
   public void setResponseMessage(String responseMessage) {
      this.responseMessage = responseMessage;
   }
   
   public String getResponseBody() {
      return responseBody;
   }
   
   public void setResponseBody(String responseBody) {
      this.responseBody = responseBody;
   }
   
   public Map<String, String[]> getResponseHeaders() {
      return responseHeaders;
   }
   
   public void setResponseHeaders(Map<String, String[]> responseHeaders) {
      this.responseHeaders = responseHeaders;
   }

}
