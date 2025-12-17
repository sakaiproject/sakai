/**
 * $Id$
 * $URL$
 * EntityProviderListener.java - entity-broker - Sep 3, 2008 11:00:01 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.entitybroker.entityprovider.extension;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;


/**
 * This allows a developer to be notified whenever a provider is registered.
 * It will also ensure that it calls the listener method for any providers that are already registered
 * when it is first registered if desired.
 * <p>
 * Usage:
 * <ul>
 * <li>Implement this interface and use {@link EntityProviderManager#registerListener(EntityProviderListener, boolean)} to
 * register it with the system.</li>
 * <li>Whenever an entity provider is registered, the {@link #run(EntityProvider)} method is called.</li>
 * <li>You can limit the calls to your listener using {@link #getPrefixFilter()} and {@link #getCapabilityFilter()}.</li>
 * </ul>
 * <p>
 * Note that you can return a null prefix filter and capability filter, but it means you will get called for
 * every registration of every provider. You will want to use {@link EntityProvider} for the generics type parameter
 * to receive every capability type.
 *
 * @param <T> the type of entity provider this listener handles, must extend {@link EntityProvider}
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface EntityProviderListener<T extends EntityProvider> {

    /**
     * @return the prefix to limit the notifications to OR null for any prefix
     */
    String getPrefixFilter();

    /**
     * @return the capability to limit notifications to OR null for any capability (still will only be called once per provider)
     */
    Class<T> getCapabilityFilter();

    /**
     * This method will be called once per each provider registered with the system
     * depending on the filter methods in this class<br/>
     * WARNING: Do not hold onto (cache) the provider returned to you by this method,
     * if you need to use it again, fetch it using the methods in {@link EntityProviderManager}
     * like {@link EntityProviderManager#getProviderByPrefix(String)} and 
     * {@link EntityProviderManager#getProviderByPrefixAndCapability(String, Class)} among others,
     * you can cache the prefix name and capabilities if you need to
     * 
     * @param provider the currently registered provider that you are being notified about
     */
    void run(T provider);

}
