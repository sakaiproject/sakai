/**
 * TaggableEntityProviderMock.java - entity-broker - 2007 Aug 8, 2007 5:39:49 PM - AZ
 */

package org.sakaiproject.entitybroker.mocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.entitybroker.entityprovider.capabilities.Taggable;

/**
 * Mock which emulates the taggable abilities, note that by default there are no tags on entities
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TaggableEntityProviderMock extends EntityProviderMock implements Taggable {

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
      Set<String> s = new HashSet<String>();
      for (int i = 0; i < tags.length; i++) {
         s.add(tags[i]);
      }
      setTags(reference, s);
   }

   public Set<String> getTags(String reference) {
      Set<String> tags = entityTags.get(reference);
      if (tags == null) {
         entityTags.put(reference.toString(), new HashSet<String>());
      }
      return entityTags.get(reference.toString());
   }

   public void setTags(String reference, Set<String> tags) {
      entityTags.put(reference, tags);
   }

}
