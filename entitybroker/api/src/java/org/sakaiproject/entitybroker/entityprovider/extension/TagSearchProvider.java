/**
 * $Id$
 * $URL$
 * TagSearchProvider.java - entity-broker - Apr 5, 2008 7:21:20 PM - azeckoski
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

import org.sakaiproject.entitybroker.entityprovider.capabilities.TagProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Taggable;


/**
 * Defines the methods necessary for searching for entities by tags (shared interface)
 * @deprecated use {@link TagProvideable} and {@link TagProvider} instead
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface TagSearchProvider {

   /**
    * Search for all entities with a set of tags (as defined by the {@link Taggable} interface)
    * 
    * @param tags a set of tags defined on these entities in the {@link Taggable} interface
    * @param params (optional) incoming set of parameters which may be used to send data specific to this request, may be null
    * @return a list of globally unique references to entities with these tags
    * @deprecated use {@link TagProvideable} and {@link TagProvider} instead
    */
   public List<String> findEntityRefsByTags(String[] tags);
//   public List<String> findEntityRefsByTags(String[] tags, Map<String, Object> params);

}
