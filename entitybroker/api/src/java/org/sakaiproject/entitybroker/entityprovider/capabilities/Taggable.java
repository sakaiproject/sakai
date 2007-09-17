/**
 * Taggable.java - entity-broker - 2007 Aug 8, 2007 5:24:47 PM - AZ
 */

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import java.util.Set;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Allows an entity to have tags associated with it which can be searched for or simply used as a
 * way to link to this entity <br/> This is one of the capability extensions for the
 * {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface Taggable extends EntityProvider {

   /**
    * Get the set of tags which are associated with this entity
    * 
    * @param reference
    *           a globally unique reference to an entity
    * @return a set of the tags which are associated with this entity
    */
   public Set<String> getTags(String reference);

   /**
    * Set the set of tags which are associated with this entity
    * 
    * @param reference
    *           a globally unique reference to an entity
    * @param tags
    *           a set of the tags to associate with this entity, setting this to an empty set will
    *           remove all tags from this entity
    */
   public void setTags(String reference, Set<String> tags);

}
