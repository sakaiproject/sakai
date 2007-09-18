/**
 * EntityProviderManager.java - created by aaronz on 11 May 2007
 */

package org.sakaiproject.entitybroker.entityprovider;

import java.util.Set;

/**
 * Handles all internal work of managing and working with the entity providers<br/> <br/>
 * Registration of entity brokers happens via spring, see the {@link EntityProvider} interface for
 * details
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public interface EntityProviderManager {

   /**
    * Retrieve a complete list of all currently registered {@link EntityProvider}s
    * 
    * @return all currently registered entity providers
    */
   public Set<String> getRegisteredPrefixes();

   /**
    * Get the entity provider which handles the entity defined by an entity reference, this is a
    * convenience method for {@link #getProviderByPrefix(String)} <br/> <b>NOTE:</b> this returns
    * the {@link CoreEntityProvider} that handles the exists check (it may handle many other things
    * as well) or returns null
    * 
    * @param reference
    *           a globally unique reference to an entity
    * @return the {@link EntityProvider} which handles this entity or null if none exists, fails if
    *         the reference is invalid
    */
   public EntityProvider getProviderByReference(String reference);

   /**
    * Get the entity provider by the prefix which uniquely defines all entities of a type, <br/>
    * <b>NOTE:</b> this returns the {@link CoreEntityProvider} that handles the exists check (it
    * may handle many other things as well), the basic {@link EntityProvider} if there is no
    * {@link CoreEntityProvider}, OR null if neither exists
    * 
    * @param prefix
    *           the string which represents a type of entity handled by an entity provider
    * @return the {@link EntityProvider} which handles this entity or null if none exists (only if
    *         prefix is not used)
    */
   public EntityProvider getProviderByPrefix(String prefix);

   /**
    * Get the entity provider by the prefix which uniquely defines all entities of a type and also
    * handles a specific capability <br/> <b>NOTE:</b> this returns the provider that handles this
    * capability (it may handle many other things as well)
    * 
    * @param prefix
    *           the string which represents a type of entity handled by an entity provider
    * @param capability
    *           any entity provider capability class (these classes extend {@link EntityProvider} or
    *           {@link CoreEntityProvider} or another capability)
    * @return the {@link EntityProvider} which handles this capability for this prefix or null if
    *         none exists or the prefix is not used
    */
   public EntityProvider getProviderByPrefixAndCapability(String prefix,
         Class<? extends EntityProvider> capability);

   /**
    * Registers an entity provider with the manager, this allows registration to happen
    * programatically but the preferred method is to use the {@link AutoRegisterEntityProvider}
    * instead (see the {@link EntityProvider} interface), replaces an existing entity provider which
    * uses the same prefix and handles the same capabilities if one is already registered, does not
    * affect other providers which handle the same prefix but handle other capabilities<br/>
    * <b>NOTE:</b> This allows developers to register providers from all over the code base without
    * requiring all capabilities to live in the same project (i.e. allows for a large reduction in
    * dependencies and conflicts)
    * 
    * @param entityProvider
    *           an entity provider to register with the main entity provider manager
    */
   public void registerEntityProvider(EntityProvider entityProvider);

   /**
    * Unregisters an entity provider with the manager, this will remove a registered entity broker
    * from the manager registration, if the entity provider supplied is not registered then no error
    * is thrown
    * 
    * @param entityProvider
    *           an entity provider to unregister with the main entity provider manager
    */
   public void unregisterEntityProvider(EntityProvider entityProvider);

   /**
    * Unregisters an entity provider with the manager based on a prefix and capability, this will
    * remove a registered entity broker from the manager registration, if the prefix and capability
    * are not registered then no error is thrown<br/> <b>NOTE:</b> Attempting to unregister the
    * base {@link EntityProvider} will cause an exception, if you want to completely unregister a
    * type of entity you must use the {@link #unregisterEntityProviderByPrefix(String)}
    * 
    * @param prefix
    *           the string which represents a type of entity handled by an entity provider
    * @param capability
    *           any entity provider capability class (these classes extend {@link EntityProvider} or
    *           {@link CoreEntityProvider} or another capability)
    */
   public void unregisterCapability(String prefix, Class<? extends EntityProvider> capability);

   /**
    * Unregisters an entity provider with the manager based on the entity prefix it handles, this
    * will remove all registered entity providers from the manager registration by the prefix, if
    * the entity provider prefix provided is not registered then no error is thrown<br/> This
    * effectively purges the entire set of entity providers for a prefix
    * 
    * @param prefix
    *           the string which represents a type of entity handled by entity providers
    */
   public void unregisterEntityProviderByPrefix(String prefix);

}
