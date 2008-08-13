/**
 * $Id$
 * $URL$
 * EntityViewAccessProviderManagerMock.java - entity-broker - Apr 11, 2008 4:37:13 PM - azeckoski
 **************************************************************************
 * Copyright 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.mocks;

import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;


/**
 * For testing things that use the access provider manager,
 * always return a new {@link EntityViewAccessProviderMock} unless the prefix is invalid,
 * pretends other methods work
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityViewAccessProviderManagerMock implements EntityViewAccessProviderManager {

   public Set<String> invalidPrefixes = new HashSet<String>();

   public EntityViewAccessProvider getProvider(String prefix) {
      if (invalidPrefixes.contains(prefix)) {         
         return null;
      }
      return new EntityViewAccessProviderMock();
   }

   public void registerProvider(String prefix, EntityViewAccessProvider provider) {
      // Okey dokey, do nothing
   }

   public void unregisterProvider(String prefix) {
      // Okey dokey, do nothing
   }

}
