/**
 * $Id$
 * $URL$
 * EntityViewAccessProviderManagerMock.java - entity-broker - Apr 11, 2008 4:37:13 PM - azeckoski
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

package org.sakaiproject.entitybroker.mocks;

import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.sakaiproject.entitybroker.mocks.data.TestData;


/**
 * For testing things that use the access provider manager,
 * always return a new {@link EntityViewAccessProviderMock} unless the prefix is invalid,
 * pretends other methods work
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityViewAccessProviderManagerMock implements EntityViewAccessProviderManager {

   public Set<String> validPrefixes = new HashSet<String>();

   public EntityViewAccessProviderManagerMock() {
      validPrefixes.add(TestData.PREFIX8);
      validPrefixes.add(TestData.PREFIXA);
   }

   public EntityViewAccessProvider getProvider(String prefix) {
      if (validPrefixes.contains(prefix)) {         
         return new EntityViewAccessProviderMock(prefix);
      }
      return null;
   }

   public void registerProvider(String prefix, EntityViewAccessProvider provider) {
      // Okey dokey, do nothing
   }

   public void unregisterProvider(String prefix) {
      // Okey dokey, do nothing
   }

}
