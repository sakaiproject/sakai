/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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
 **/

package org.sakaiproject.entitybroker.entityprovider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityProviderListener;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetterWrite;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorageWrite;
import org.sakaiproject.entitybroker.providers.EntityPropertiesService;

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
     * @deprecated this method is no longer functional or supported
     */
    public EntityProvider getProviderByReference(String reference);

    /**
     * Get the entity provider by the prefix which uniquely defines all entities of a type, <br/>
     * <b>NOTE:</b> this returns the {@link CoreEntityProvider} that handles the exists check (it
     * may handle many other things as well), the basic {@link EntityProvider} if there is no
     * {@link CoreEntityProvider}, OR null if neither exists
     * 
     * @param prefix the string which represents a type of entity handled by an entity provider
     * @return the {@link EntityProvider} which handles this entity or null if none exists (only if
     *         prefix is not used)
     */
    public EntityProvider getProviderByPrefix(String prefix);

    /**
     * Get the entity provider by the prefix which uniquely defines all entities of a type and also
     * handles a specific capability <br/> <b>NOTE:</b> this returns the provider that handles this
     * capability (it may handle many other things as well)
     * 
     * @param <T> a class which extends {@link EntityProvider}
     * @param prefix
     *           the string which represents a type of entity handled by an entity provider
     * @param capability
     *           any entity provider capability class (these classes extend {@link EntityProvider} or
     *           {@link CoreEntityProvider} or another capability)
     * @return the {@link EntityProvider} which handles this capability for this prefix or null if
     *         none exists or the prefix is not used
     */
    public <T extends EntityProvider> T getProviderByPrefixAndCapability(String prefix, Class<T> capability);

    /**
     * Get all the entity providers which support a specific capability,
     * this useful if you need to get the providers and call the capability methods on them directly
     * 
     * @param <T> a class which extends {@link EntityProvider}
     * @param capability
     *           any entity provider capability class (these classes extend {@link EntityProvider} or
     *           {@link CoreEntityProvider} or another capability)
     * @return the list of providers which implement the given capability, may be an empty list
     */
    public <T extends EntityProvider> List<T> getProvidersByCapability(Class<T> capability);

    /**
     * Gets the prefixes which support a specific capability
     * 
     * @param <T> a class which extends {@link EntityProvider}
     * @param capability
     *           any entity provider capability class (these classes extend {@link EntityProvider} or
     *           {@link CoreEntityProvider} or another capability)
     * @return the list of entity prefixes which support this capability, may be an empty list
     */
    public <T extends EntityProvider> List<String> getPrefixesByCapability(Class<T> capability);

    /**
     * Get all the capabilities for a given entity prefix,
     * <b>WARNING:</b> This is very inefficient so you should normally use {@link #getProviderByPrefixAndCapability(String, Class)}
     * when trying to determine if a provider implements a specific capability
     * 
     * @param prefix
     *           the string which represents a type of entity handled by an entity provider
     * @return a list of the capabilities classes implemented by the entity provider defining this prefix
     */
    public List<Class<? extends EntityProvider>> getPrefixCapabilities(String prefix);

    /**
     * Get all registered prefixes and their capabilities,
     * <b>WARNING:</b> This is very inefficient so you should normally use {@link #getProviderByPrefixAndCapability(String, Class)}
     * when trying to determine if a provider implements a specific capability
     * 
     * @return a map of prefix -> List(capabilities)
     */
    public Map<String, List<Class<? extends EntityProvider>>> getRegisteredEntityCapabilities();

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

    // LISTENER

    /**
     * Registers a listener which is called whenever entity providers are registered depending on the 
     * filters in the {@link EntityProviderListener}<br/>
     * This is particularly useful for capabilities which should/must be executed one time only and should
     * not be called over and over OR will not do anything until they are triggered<br/>
     * 
     * @param notifier the entity provider listener to register
     * @param includeExisting if true then the listener will be called for existing registered providers,
     * otherwise it will only be called for newly registered providers
     * @throws IllegalArgumentException if the params are null
     */
    public <T extends EntityProvider> void registerListener(EntityProviderListener<T> listener, boolean includeExisting);

    /**
     * Unregisters the listener if it is registered or does nothing
     * 
     * @param notifier the entity provider listener to unregister
     * @throws IllegalArgumentException if the params are null
     */
    public <T extends EntityProvider> void unregisterListener(EntityProviderListener<T> listener);

    /**
     * Allows access to the current RequestStorage service
     * @return the current RequestStorageWrite service
     */
    public RequestStorageWrite getRequestStorage();

    /**
     * Allows access to the current RequestGetter service
     * @return the current RequestGetter service
     */
    public RequestGetterWrite getRequestGetter();

    /**
     * Allows access to the current EntityPropertiesService service
     * @return the current EntityPropertiesService service
     */
    public EntityPropertiesService getEntityProperties();

    /**
     * Allows access to the current EntityProviderMethodStore service
     * @return the current EntityProviderMethodStore service
     */
    public EntityProviderMethodStore getEntityProviderMethodStore();

}
