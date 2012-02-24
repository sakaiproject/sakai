/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
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
 **/

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;

/**
 * Allows the entities handled by this provider to be accessed directly as objects,
 * this is also the interface for "reading" entities (this is the R in CRUD)<br/>
 * This is also used for resolving the type of entities<br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface Resolvable extends EntityProvider {

   /**
    * Allows this entity to be fetched based on the ref (prefix and local id),
    * also used to determine the class type of these entities by requesting
    * an entity without specifying an id (id = null)<br/> 
    * <b>WARNING:</b> this method should not return null, throw the
    * appropriate exception if the entity data cannot be found
    * <b>Note:</b> The entity class type needs to be able to be resolved from the ClassLoader of the 
    * EntityBrokerManager (currently this means deployed into shared)<br/> 
    * <br/>The entity object does not have to be a model object itself and may simply
    * be something created ({@link Map}, {@link String}, {@link EntityData}, etc.) to give to anyone calling this method.
    * 
    * @param ref the parsed reference object which uniquely represents an entity OR
    * only a prefix (indicating you should return a default constructed object with no properties set)
    * @return an entity object for the prefix and id in the provided ref OR 
    * an empty entity object if only the prefix is supplied (should NOT return null),
    * this return could be an {@link EntityData} object, {@link Map}, your own POJO, etc.
    * @throws IllegalArgumentException if the id is invalid for this type of entity,
    * <b>NOTE:</b> a null id means you should return a default constructed object)
    * @throws SecurityException if access to this entity is not allowed
    * @throws IllegalStateException for all other errors
    */
   public Object getEntity(EntityReference ref);

}
