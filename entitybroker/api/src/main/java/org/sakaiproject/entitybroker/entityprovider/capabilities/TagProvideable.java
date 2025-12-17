/**
 * $Id$
 * $URL$
 * TagProvideable.java - entity-broker - Aug 4, 2008 9:11:52 PM - azeckoski
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

import java.util.List;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.TagProvider;
import org.sakaiproject.entitybroker.entityprovider.search.Search;


/**
 * Allows an entity to control and provide tag storage and searching,
 * this overrides internal tag storage and any existing tag storage will
 * be ignored in favor of the provided tags<br/>
 * This is the provider interface for {@link Taggable}, inherits all methods from {@link TagProvider}<br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface TagProvideable extends Taggable, TagProvider {

   /**
    * Search for all entities which have the given tags,
    * can limit the return using the search object<br/>
    * NOTE: Don't forget to check the {@link RequestStorable} request params for extra information
    * about the current user and location and other possible params when implementing this<br/>
    * 
    * @param tags a set of tags associated with entities
    * @param matchAll if true then all tags must exist on the entity for it to be matched,
    * if false then the entity just has to have one or more of the given tags
    * @param search (optional) a search object, used to order or limit the number of returned results,
    * restrictions will be typically ignored
    * 
    * @return a list of entity search results (contains the ref, url, displayname of the matching entities)
    * @throws IllegalArgumentException if the tags set is empty or null
    */
   public List<EntityData> findEntitesByTags(String[] tags, boolean matchAll, Search search);

}
