/**
 * $Id$
 * $URL$
 * MockHttpServletRequest.java - entity-broker - Apr 6, 2008 2:15:08 PM - azeckoski
 **************************************************************************
 * Copyright 2006 Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.mocks;

/**
 * Extends the spring mock HTTP servlet request to make testing easier
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class MockEBHttpServletRequest extends org.springframework.mock.web.MockHttpServletRequest {

   /**
    * @param method GET, POST, PUT, DELETE (PUT and DELETE not supported by browsers),
    * this will be set to POST if null or unset
    * @param pathInfo the part of the URL specifying extra path information that comes 
    * after the servlet path but before the query string in the request URL
    * Example: http://server/servlet/extra/path/info?thing=1, pathInfo = /extra/path/info
    */
   public MockEBHttpServletRequest(String method, String pathInfo) {
      super(method, "");
      if (method == null || "".equals(method)) {
         super.setMethod("POST");
      }
      super.setPathInfo(pathInfo);
   }

   /**
    * @param method GET, POST, PUT, DELETE (PUT and DELETE not supported by browsers),
    * this will be set to POST if null or unset
    * @param params alternating keys and values (starting with keys) to place into the request parameters
    */
   public MockEBHttpServletRequest(String method, String... params) {
      super(method, "");
      if (method == null || "".equals(method)) {
         super.setMethod("POST");
      }
      for (int i = 0; i < params.length; i++) {
         if (params.length < i + 1) {
            break;
         }
         this.addParameter(params[i], params[i+1]);
         i++;
      }
   }

}
