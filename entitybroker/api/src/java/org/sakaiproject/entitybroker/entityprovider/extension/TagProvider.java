/**
 * $Id$
 * $URL$
 * TagProvider.java - entity-broker - Apr 5, 2008 7:19:14 PM - azeckoski
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

package org.sakaiproject.entitybroker.entityprovider.extension;

import java.util.List;


/**
 * Defines the methods related to tagging entities (shared between interfaces)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface TagProvider {

   /**
    * Get the set of tags which are associated with this entity
    * 
    * @param reference a globally unique reference to an entity (e.g. /myprefix/myid), 
    * consists of the entity prefix and optional segments (normally the id at least)
    * @return a set of the tags which are associated with this entity
    * @throws UnsupportedOperationException if this reference cannot be tagged
    */
   public List<String> getTagsForEntity(String reference);

   /**
    * Add these tags to the an entity,
    * will have no effect if the entity already has these tags
    * 
    * @param reference a globally unique reference to an entity (e.g. /myprefix/myid), 
    * consists of the entity prefix and optional segments (normally the id at least)
    * @param tags a set of the tags to associate with this entity
    * @throws UnsupportedOperationException if this reference cannot be tagged
    */
   public void addTagsToEntity(String reference, String[] tags);

   /**
    * Sets the tags which are associated with this entity,
    * this overwrites any current tags and makes the input
    * tags the only current tags for this entity
    * 
    * @param reference a globally unique reference to an entity (e.g. /myprefix/myid), 
    * consists of the entity prefix and optional segments (normally the id at least)
    * @param tags a set of the tags to associate with this entity, 
    * setting this to an empty set will remove all tags from this entity
    * @throws UnsupportedOperationException if this reference cannot be tagged
    */
   public void setTagsForEntity(String reference, String[] tags);

   /**
    * Removes these tags from this entity,
    * will have no effect if the tags do not exist on this entity
    * 
    * @param reference a globally unique reference to an entity (e.g. /myprefix/myid), 
    * consists of the entity prefix and optional segments (normally the id at least)
    * @param tags a set of the tags to remove from this entity, 
    * if this is empty then nothing happens, 
    * if this includes tags that do not exist on this entity then they are ignored
    * @throws UnsupportedOperationException if this reference cannot be tagged
    */
   public void removeTagsFromEntity(String reference, String[] tags);

}
