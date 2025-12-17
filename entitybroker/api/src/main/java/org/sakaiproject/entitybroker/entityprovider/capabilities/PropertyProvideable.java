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

import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider;

/**
 * Allows entities handled by the entity provider which implements this interface to have meta properties
 * attached to them, these properties can be accessed via the {@link EntityBroker}, properties will be
 * stored and retrieved via the methods which are implemented in this interface<br/> 
 * For our usage, meta properties are extra or additional properties that are attached to an entity
 * at runtime but are persisted so they can be retrieved later<br/>
 * Allows the entity provider to define and control the way properties are stored on its own entities,
 * if you would prefer to use the internal storage and only have lightweight property storage needs
 * then use {@link Propertyable} instead<br/>
 * <b>NOTE:</b> the validity of references and parameters is checked in the broker before the call
 * goes to the provider <br/> 
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface PropertyProvideable extends Propertyable, PropertiesProvider {

   // this space intentionally left blank

}
