/**
 * $Id$
 * $URL$
 * RequestInterceptor.java - entity-broker - Apr 8, 2008 8:42:28 AM - azeckoski
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

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.exception.EntityException;

/**
 * Allows actions to be taken before a direct request is handled or after it has been handled,
 * will only affect requests coming in via the direct servlet
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface RequestInterceptor extends EntityProvider {

   /**
    * Take actions before the request is handled for an entity view,
    * this will be called just before each request is sent to the correct request handler,
    * this might be used to add information to the response before it goes on to be handled
    * or to take some action as a result of information in the request or reference,<br/>
    * if you want to interrupt the handling of this request (stop it) then throw an
    * {@link EntityException} and include the type of response you would like to return in the exception
    * (this can be a success or failure response status)
    * 
    * @param view an entity view, should contain all the information related to the incoming entity URL
    * @param req the servlet request (available in case you need to get anything out of it)
    * @param res the servlet response, put the correct data response into the outputstream
    */
   public void before(EntityView view, HttpServletRequest req, HttpServletResponse res);

   /**
    * Take actions after the request is handled for an entity view,
    * this will be called just before each response is sent back to the requester,
    * normally this would be used to add something to the response as it is getting ready to be
    * sent back to the requester
    * 
    * @param view an entity view, should contain all the information related to the incoming entity URL
    * @param req the servlet request (available in case you need to get anything out of it)
    * @param res the servlet response, put the correct data response into the outputstream
    */
   public void after(EntityView view, HttpServletRequest req, HttpServletResponse res);

}
