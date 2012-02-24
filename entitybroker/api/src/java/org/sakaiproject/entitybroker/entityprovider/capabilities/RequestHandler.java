/**
 * $Id$
 * $URL$
 * RequestHandler.java - entity-broker - Apr 12, 2008 2:44:44 PM - azeckoski
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

import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Indicates that this entity provider will handle its own entity view requests,
 * this would be very unusual but it allows the entity provider itself to redirect
 * requests to a tool and normally would be used if there is some special circumstance only<br/>
 * <b>WARNING:</b> This will be called before any other request handling and before the access provider
 * is called and will cause all other processing to be skipped (includes REST calls, custom actions, formatting, etc.)<br/>
 * <br/>
 * <b>NOTE:</b> if you want to stop certain requests from coming through then
 * a better option is to use {@link RequestInterceptor} which is triggered
 * just before this would be called
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface RequestHandler extends EntityProvider, EntityViewAccessProvider {

   // this space left blank intentionally

}
