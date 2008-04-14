/**
 * $Id$
 * $URL$
 * MockHttpServletRequest.java - entity-broker - Apr 6, 2008 2:15:08 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.mocks;

/**
 * Extends the spring mock HTTP servlet request to make testing easier
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class MockHttpServletRequest extends org.springframework.mock.web.MockHttpServletRequest {

   /**
    * @param method GET, POST, PUT, DELETE (PUT and DELETE not supported by browsers),
    * this will be set to POST if null or unset
    * @param pathInfo the part of the URL specifying extra path information that comes 
    * after the servlet path but before the query string in the request URL
    * Example: http://server/servlet/extra/path/info?thing=1, pathInfo = /extra/path/info
    */
   public MockHttpServletRequest(String method, String pathInfo) {
      super(method, "");
      if (method == null || method == "") {
         super.setMethod("POST");
      }
      super.setPathInfo(pathInfo);
   }

   /**
    * @param method GET, POST, PUT, DELETE (PUT and DELETE not supported by browsers),
    * this will be set to POST if null or unset
    * @param params alternating keys and values (starting with keys) to place into the request parameters
    */
   public MockHttpServletRequest(String method, String... params) {
      super(method, "");
      if (method == null || method == "") {
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
