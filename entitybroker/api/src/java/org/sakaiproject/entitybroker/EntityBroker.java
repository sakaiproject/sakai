/**
 * EntityBroker.java - created by aaronz on 14 May 2007
 */

package org.sakaiproject.entitybroker;

import java.util.Set;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ParseSpecParseable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider;

/**
 * This service interface defines the capabilities of the entity broker system<br/> It allows sakai
 * system methods, developers, etc. to access Sakai entity information (new and old)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public interface EntityBroker extends PropertiesProvider {

   /**
    * Check if an entity exists by the globally unique reference string, (the global reference
    * string will consist of the entity prefix and any local ID). If no {@link EntityProvider} for
    * the reference is found which implements {@link CoreEntityProvider}, this method will return
    * <code>true</code>
    * 
    * @param reference
    *           a globally unique reference to an entity, consists of the entity prefix and any
    *           local ID.
    * @return true if the entity exists, false otherwise
    */
   public boolean entityExists(String reference);

   /**
    * Retrieve a complete set of all currently registered {@link EntityProvider} prefixes
    * 
    * @return all currently registered entity prefixes
    */
   public Set<String> getRegisteredPrefixes();

   /**
    * Get the full absolute URL to the entity defined by this entity reference, this will fail-safe
    * from a direct URL (if one is defined) all the way down to simply a URL to the sakai server if
    * nothing better can be determined
    * 
    * @param a
    *           globally unique reference to an entity, consists of the entity prefix and optional
    *           segments
    * @return a full URL string
    */
   public String getEntityURL(String reference);

   /**
    * Fire an event to Sakai with the specified name, targetted at the supplied reference, which
    * should be a reference to an existing entity managed by this broker
    * 
    * @param eventName
    *           a string which represents the name of the event (e.g. announcement.create)
    * @param reference
    *           a globally unique reference to an entity, consists of the entity prefix and optional
    *           segments
    */
   public void fireEvent(String eventName, String reference);

   /**
    * Parses an entity reference into a concrete object, of type {@link EntityReference}, or some
    * class derived from it, for example {@link IdEntityReference} or some other class of object
    * which is returned from {@link ParseSpecParseable}.
    * 
    * @param reference
    *           a globally unique reference to an entity, consists of the entity prefix and optional
    *           segments
    * @return an entity reference object which will contain the entity prefix and any optional
    *         segments, or <code>null</code> if the reference was not recognised as a valid entity
    *         handled by the broker
    */
   public EntityReference parseReference(String reference);

   /**
    * Fetches a concrete object representing this entity reference; either one from the
    * {@link Resolvable} capability if implemented by the responsible {@link EntityProvider}, or
    * else from the underlying Sakai entity system.
    * 
    * @param reference
    *           a globally unique reference to an entity, consists of the entity prefix and optional
    *           segments
    * @return an object which represents the entity
    */
   public Object fetchEntity(String reference);

}
