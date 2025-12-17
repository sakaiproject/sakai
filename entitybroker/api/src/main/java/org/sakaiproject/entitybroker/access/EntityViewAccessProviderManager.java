/**
 * $Id$
 * $URL$
 * ViewAccessProviderManager.java - entity-broker - Apr 11, 2008 11:41:57 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.entitybroker.access;


/**
 * Manages all the access providers for the entity views in the system<br/>
 * Use this to register yourself as handling the entity views for a set of entites (based on the prefix)
 * 
 * This will be used by the entity broker to determine if anyone is handling entity views for an entity type
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface EntityViewAccessProviderManager {

   /**
    * Register a provider as handling entity view requests and delivering responses 
    * @param prefix the unique entity prefix that defines the entity type handled by this provider
    * @param provider the actual provider bean
    */
   public void registerProvider(String prefix, EntityViewAccessProvider provider);

   /**
    * Removes the provider from the registered set for this prefix
    * @param prefix the unique entity prefix that defines the entity type handled by this provider
    */
   public void unregisterProvider(String prefix);

   /**
    * Get an entity view access provider for a prefix if one exists
    * 
    * @param prefix the unique entity prefix that defines the entity type handled
    * @return the provider related to this prefix or null if no provider can be found
    */
   public EntityViewAccessProvider getProvider(String prefix);

}
