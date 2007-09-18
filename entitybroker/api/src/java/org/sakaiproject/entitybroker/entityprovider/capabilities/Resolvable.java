/**
 * Resolvable.java - created by aaronz on 11 May 2007
 */

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Allows the entities handled by this provider to be accessed directly as objects <br/> This is one
 * of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public interface Resolvable extends EntityProvider {

   /**
    * Allows Sakai to fetch your entity based on the local id<br/> (the global reference string
    * will consist of the entity prefix and the local id) <br/> <b>Note:</b> This class type needs
    * to be resolveable from the ClassLoader of the EntityBrokerManager (currently this means
    * deployed into shared) <br/> <br/>This does not have to be a model object itself and may simply
    * be something created to give to anyone calling this method.
    * 
    * @param reference
    *           the parsed reference to this entity
    * @return an entity object
    */
   public Object getEntity(EntityReference reference);

}
