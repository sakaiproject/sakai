/**
 * $Id$
 * $URL$
 * HTMLdefineable.java - entity-broker - Apr 6, 2008 7:44:11 PM - azeckoski
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

import org.sakaiproject.entitybroker.EntityReference;

/**
 * Allows this entity to better control the data that is going to be output by
 * allowing it to intercept the entities and return them as whatever objects
 * which should be serialized for output,
 * if you just want to use the internal methods to handle formatting the output
 * then simply use {@link Outputable}<br/>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface OutputSerializable extends Outputable {

    /**
     * Take the entity and convert it to whatever objects (Map, List, String, etc.) that you want to output,
     * this will be called every time that an entity is about to serialized but only if the type
     * of object matches that of the entities handled by your provider
     * 
     * @param the entity reference for the current entity
     * @param entity an object of the type handled by your provider
     * @return the object you want to be serialized
     */
    public Object makeSerializableObject(EntityReference ref, Object entity);

}
