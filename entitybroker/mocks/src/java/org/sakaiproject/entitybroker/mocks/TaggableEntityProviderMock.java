/**
 * Copyright (c) 2007-2008 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * TaggableEntityProviderMock.java - entity-broker - 2007 Aug 8, 2007 5:39:49 PM - AZ
 */

package org.sakaiproject.entitybroker.mocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.sakaiproject.entitybroker.entityprovider.capabilities.TagProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Taggable;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

/**
 * Mock which emulates the taggable abilities, note that by default there are no tags on entities
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TaggableEntityProviderMock extends EntityProviderMock implements Taggable, TagProvideable {

   public Map<String, Set<String>> entityTags = new HashMap<String, Set<String>>();

   /**
    * TEST constructor: allows for easy testing
    * 
    * @param prefix
    */
   public TaggableEntityProviderMock(String prefix) {
      super(prefix);
   }

   /**
    * TEST constructor: allows for easy testing by setting up tags for a specific reference
    * 
    * @param prefix
    * @param reference
    *           an entity reference
    * @param tags
    *           an array of tags for this reference
    */
   public TaggableEntityProviderMock(String prefix, String reference, String[] tags) {
      super(prefix);
      setTagsForEntity(reference, tags);
   }

   public void addTagsToEntity(String reference, String[] tags) {
      if (entityTags.containsKey(reference)) {
         for (String tag : tags) {
            // just add it to the set
            entityTags.get(reference).add(tag);
         }
      } else {
         setTagsForEntity(reference, tags);
      }
   }

   public List<String> getTagsForEntity(String reference) {
      if (! entityTags.containsKey(reference)) {
         return new ArrayList<String>();
      }
      ArrayList<String> tags = new ArrayList<String>( entityTags.get(reference) );
      Collections.sort(tags);
      return tags;
   }

   public void removeTagsFromEntity(String reference, String[] tags) {
      if (entityTags.containsKey(reference)) {
         for (String tag : tags) {
            entityTags.get(reference).remove(tag);
         }
      }
   }

   public void setTagsForEntity(String reference, String[] tags) {
      if (tags.length == 0) {
         entityTags.remove(reference);
      } else {
         Set<String> s = new HashSet<String>();
         for (int i = 0; i < tags.length; i++) {
            s.add(tags[i]);
         }
         entityTags.put(reference, s);
      }
   }

   public List<EntityData> findEntitesByTags(String[] tags, boolean matchAll, Search search) {
      Set<String> refs = new HashSet<String>();
      if (matchAll) {
         HashSet<String> allTags = new HashSet<String>();
         for (int i = 0; i < tags.length; i++) {
            allTags.add(tags[i]);
         }
         for (Entry<String, Set<String>> entry : entityTags.entrySet()) {
            if (entry.getValue().containsAll(allTags)) {
               refs.add(entry.getKey());
            }
         }
      } else {
         for (String key : entityTags.keySet()) {
            Set<String> s = entityTags.get(key);
            for (int i = 0; i < tags.length; i++) {
               if (s.contains(tags[i])) {
                  refs.add(key);
               }
            }
         }
      }
      ArrayList<EntityData> results = new ArrayList<EntityData>();
      for (String ref : refs) {
         results.add( new EntityData(ref, (String)null) );
      }
      Collections.sort(results, new EntityData.ReferenceComparator());
      return results;
   }

}
