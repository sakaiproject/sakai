/**
 * $Id$
 * $URL$
 * Sampleable.java - entity-broker - Apr 8, 2008 11:14:05 AM - azeckoski
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

import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * This entity type can be known and this allows access to a sample object which represents it,
 * the sample object can be of any type including a POJO, a {@link Map}, etc.<br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface Sampleable extends EntityProvider {

   /**
    * Provides a sample entity object which can be populated with data and then passed to 
    * methods like {@link Createable#createEntity(EntityReference, Object, java.util.Map)},
    * this is necessary so that the type of the entity object is known and the right fields can
    * be pre-filled, it also allows us to support the case of different read and write objects
    * <b>Note:</b> The entity class type needs to be able to be resolved from the ClassLoader of the 
    * {@link EntityBroker} (currently this means deployed into shared) <br/> 
    * 
    * @return a sample entity object for entities of the type represented by this provider
    * @throws IllegalStateException if the sample object cannot be obtained for some reason
    */
   public Object getSampleEntity();

}
