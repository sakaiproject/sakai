/**
 * ReferenceParsable.java - created by antranig on 01 June 2007
 */

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Indicates an entity provider has the capability of parsing its own reference string. An entity
 * that does not implement this interface is assumed to deal in references of type
 * {@link IdEntityReference}. <br/> This is one of the capability extensions for the
 * {@link EntityProvider} interface<br/>
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface ReferenceParseable extends EntityProvider {

   /**
    * Returns an example instance of the {@link EntityReference} class that this
    * {@link EntityProvider} uses as its reference type. If you do not also implement
    * {@link ParseSpecParseable} a default parse specification will be inferred for you (the entity
    * prefix will always come first).
    * 
    * @return an entity reference class
    */
   public EntityReference getParsedExemplar();

}
