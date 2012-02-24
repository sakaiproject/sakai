/**
 * $Id$
 * $URL$
 * HttpServletAccessProvider.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
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
 * created by antranig on 14 May 2007
 **/

package org.sakaiproject.entitybroker.access;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;

/**
 * Represents a bean which is capable of handling direct HTTP access for an entity.
 * </p>
 * This interface would be typically implemented from a tool (webapp) context, and registered with
 * the {@link HttpServletAccessProviderManager} in a context loader listener.
 * </p>
 * If the implementation throws a {@link SecurityException} during the course of this method, the
 * access will be directed to a login page or authentication method before being redirected to the
 * tool.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 * @deprecated Use {@link EntityViewAccessProvider} instead
 */
public interface HttpServletAccessProvider {

   /**
    * Make and return the data responses for this type of data provider for a specific entity reference,
    * use the request to get any additional sent in information you may need or want
    * and use the response to hold the output you generate
    * 
    * @param ref an entity reference
    * @param req the servlet request (available in case you need to get anything out of it)
    * @param res the servlet response, put the correct data response into the outputstream
    * @throws FormatUnsupportedException if the format requested in the view is not supported
    * @throws EntityException if there is a request processing/handling failure
    * @deprecated Use {@link EntityViewAccessProvider} instead
    */
   public void handleAccess(HttpServletRequest req, HttpServletResponse res, EntityReference ref);

}
