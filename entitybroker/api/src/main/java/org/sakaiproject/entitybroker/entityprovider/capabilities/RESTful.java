/**
 * $Id$
 * $URL$
 * RESTful.java - entity-broker - Apr 8, 2008 12:05:12 PM - azeckoski
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

/**
 * Indicates that entities handled by this provider are RESTful as defined by the REST microformat:<br/>
 * <a href="http://microformats.org/wiki/rest/urls">http://microformats.org/wiki/rest/urls</a><br/>
 * Requires all CRUD functionality, Collection handling, HTML handling, and ability to support output formats<br/>
 * This is mostly a convenience interface to make sure that everything needed to support REST has been 
 * implemented, it also includes the marker to indicate that RESTful URLs should work for entities of this type
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface RESTful extends EntityProvider, CRUDable, CollectionResolvable, Outputable, Inputable, 
   Describeable, ActionsExecutable, Redirectable  {

   // this space left blank intentionally

}
