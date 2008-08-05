/**
 * $Id$
 * $URL$
 * TagProvider.java - entity-broker - Apr 5, 2008 7:19:14 PM - azeckoski
 **************************************************************************
 * Copyright 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.entityprovider.extension;

import java.util.Set;


/**
 * Defines the methods related to tagging entities (shared between interfaces)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface TagProvider {

   /**
    * Get the set of tags which are associated with this entity
    * 
    * @param reference a globally unique reference to an entity
    * @return a set of the tags which are associated with this entity
    * @throws UnsupportedOperationException if this reference cannot be tagged
    */
   public Set<String> getTags(String reference);

   /**
    * Sets the tags which are associated with this entity,
    * this overwrites any current tags and makes the input
    * tags the only current tags for this entity
    * 
    * @param reference a globally unique reference to an entity
    * @param tags a set of the tags to associate with this entity, 
    * setting this to an empty set will remove all tags from this entity
    * @throws UnsupportedOperationException if this reference cannot be tagged
    */
   public void setTags(String reference, String[] tags);

}
