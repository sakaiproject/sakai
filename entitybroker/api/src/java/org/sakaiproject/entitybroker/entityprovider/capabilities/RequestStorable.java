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
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;

/**
 * Indicates that this entity provider is aware of the requests and can get to
 * the stored values in the request or can store its own, 
 * this allows the entity provider to get hold of information from the request at any time<br/>
 * This is primarily intended to provide access to request data while operating
 * inside the entity provider without depending on servlet knowledge<br/>
 * This provides access to the special indicator values which can be used to see
 * what kind of request is operating and get information about it,
 * see the {@link RequestStorage} object for more info<br/>
 * If you need to get to the servlet data see {@link RequestAware} and {@link RequestInterceptor}
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface RequestStorable extends EntityProvider {

   /**
    * Allows the entity provider to access the current request storage if available,
    * sets a storage service which will retrieve or set the stored data values<br/>
    * <b>NOTE:</b> this will only access data from the current request at
    * the time the call is made, values disappear as soon as the request ends
    */
   public void setRequestStorage(RequestStorage requestStorage);

}
