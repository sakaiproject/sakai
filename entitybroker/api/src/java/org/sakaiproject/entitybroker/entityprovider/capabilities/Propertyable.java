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

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Allows entities handled by the entity provider which implements this interface to have meta properties
 * attached to them, properties can be accessed via the {@link EntityBroker}, properties will be
 * stored and retrieved using the internal entity property retrieval implementation<br/>
 * For our usage, meta properties are extra or additional properties that are attached to an entity
 * at runtime but are persisted so they can be retrieved later<br/>
 * <b>WARNING:</b> this should be used only for properties of entities which will be accessed very
 * lightly, for production level access OR the ability to control how properties are stored on your
 * own entities, you should use the {@link PropertyProvideable} instead <br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface Propertyable extends EntityProvider {

   // this space intentionally left blank

}
