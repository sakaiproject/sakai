/**
 * $Id$
 * $URL$
 * RequestGetterImpl.java - entity-broker - Apr 8, 2008 9:03:50 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl.entityprovider.extension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;


/**
 * Service which will retrieve the current request information if it is available,
 * this allows an application scoped bean to get access to request scoped information
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class RequestGetterImpl implements RequestGetter {

   /**
    * Stores the request related to the current thread
    */
   private ThreadLocal<HttpServletRequest> requestTL = new ThreadLocal<HttpServletRequest>();
   /**
    * Stores the response related to the current thread
    */
   private ThreadLocal<HttpServletResponse> responseTL = new ThreadLocal<HttpServletResponse>();

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter#getRequest()
    */
   public HttpServletRequest getRequest() {
      HttpServletRequest req = requestTL.get();
      // TODO try to get this from Sakai?
      return req;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter#getResponse()
    */
   public HttpServletResponse getResponse() {
      HttpServletResponse res = responseTL.get();
      // TODO try to get this from Sakai?
      return res;
   }
   
   /**
    * Sets the request for the current thread, this will be cleared when the thread closes
    * @param req
    */
   public void setRequest(HttpServletRequest req) {
      requestTL.set(req);
   }

   /**
    * Sets the response for the current thread, this will be closed when the thread closes
    * @param res
    */
   public void setResponse(HttpServletResponse res) {
      responseTL.set(res);
   }

}
