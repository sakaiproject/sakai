/**
 * $Id$
 * $URL$
 * RequestAware.java - entity-broker - Apr 7, 2008 10:12:00 PM - azeckoski
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

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;


/**
 * Indicates that this entity provider needs to be request aware, 
 * this allows the entity provider to get hold of information from the request at any time
 * by directly accessing the request and response objects (if we are inside a request),
 * if there is no current request then this method will fail to return anything<br/>
 * This is primarily intended to provide access to request parameters while operating
 * inside the entity provider
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface RequestAware extends EntityProvider {

   /**
    * Allows the entity provider to access the current request if it is available,
    * sets a getter service which will retrieve the current request/response if there is one<br/>
    * <b>NOTE:</b> this will only be the current request at the instant that the methods
    * on the getter service are called
    */
   public void setRequestGetter(RequestGetter requestGetter);

}
