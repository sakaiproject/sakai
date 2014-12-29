/**
 * $Id$
 * $URL$
 * ViewAccessProvider.java - entity-broker - Apr 11, 2008 11:37:48 AM - azeckoski
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

package org.sakaiproject.entitybroker.access;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;

/**
 * Represents a bean which is capable of handling access for an {@link EntityView},
 * this replaces the {@link HttpServletAccessProvider} as all entity URLs are now being parsed
 * so more information can be provided through the {@link EntityView}<br/>
 * <br/>
 * This interface would be typically implemented from a tool (webapp) context, and registered with
 * the {@link EntityViewAccessProviderManager} in a context loader listener<br/>
 * <br/>
 * If the implementation throws a {@link SecurityException} during the course of this method, the
 * access will be directed to a login page or authentication method before being redirected back to
 * the implementation method<br/>
 * If you want to control the requests which make it through to this by format type you can
 * optionally implement {@link AccessFormats}
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface EntityViewAccessProvider {

   /**
    * Make and return the data responses for this type of data provider for a specific entity view
    * and entity reference (contained within the entity view),
    * use the request to get any additional sent in information you may need or want
    * and use the response to hold the output you generate<br/>
    * <br/>
    * <b>NOTE:</b> If you decide that you cannot handle this access request for any reason 
    * you can either throw an {@link EntityException} to specify why OR throw a general {@link Exception}, 
    * both will kill the request entirely but the general exception will pass through the system
    * while the {@link EntityException} will produce a handled result
    * 
    * @param view an entity view, should contain all the information related to the incoming entity URL
    * @param req the servlet request (available in case you need to get anything out of it)
    * @param res the servlet response, put the correct data response into the outputstream
    * @throws FormatUnsupportedException if the format requested in the view is not supported
    * @throws EntityException if there is a request processing/handling failure
    */
   public void handleAccess(EntityView view, HttpServletRequest req, HttpServletResponse res);

}
