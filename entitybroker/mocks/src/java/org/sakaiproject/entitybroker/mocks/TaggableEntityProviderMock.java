/**
 * TaggableEntityProviderMock.java - entity-broker - 2007 Aug 8, 2007 5:39:49 PM - AZ
 */

package org.sakaiproject.entitybroker.mocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.entitybroker.entityprovider.capabilities.TagSearchable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Taggable;

/**
 * Mock which emulates the taggable abilities, note that by default there are no tags on entities
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TaggableEntityProviderMock extends EntityProviderMock implements Taggable, TagSearchable {

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
      setTags(reference, tags);
   }

   public Set<String> getTags(String reference) {
      Set<String> tags = entityTags.get(reference);
      if (tags == null) {
         return new HashSet<String>();
      }
      return entityTags.get(reference);
   }

   public void setTags(String reference, String[] tags) {
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

   public List<String> findEntityRefsByTags(String[] tags) {
      Set<String> refs = new HashSet<String>();
      for (String key : entityTags.keySet()) {
         Set<String> s = entityTags.get(key);
         for (int i = 0; i < tags.length; i++) {
            if (s.contains(tags[i])) {
               refs.add(key);
            }
         }
      }
      return new ArrayList<String>(refs);
   }

}
