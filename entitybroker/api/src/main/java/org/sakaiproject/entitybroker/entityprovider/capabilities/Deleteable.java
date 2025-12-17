/**
 * $Id$
 * $URL$
 * Deleteable.java - entity-broker - Apr 8, 2008 11:31:26 AM - azeckoski
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

import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * This entity type can be deleted (this is the D in CRUD),
 * the current user id should be used for permissions checking in most cases<br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface Deleteable extends EntityProvider {

   /**
    * Deletes the entity identified by this entity reference,
    * if the entity cannot be found then nothing happens
    * 
    * @param ref the parsed reference object which uniquely represents this entity
    * @param params (optional) incoming set of parameters which may be used to send data specific to this request, may be null
    * @throws SecurityException if permissions prevented this entity from being created
    * @throws IllegalStateException for all other failures
    */
   public void deleteEntity(EntityReference ref, Map<String, Object> params);

}
