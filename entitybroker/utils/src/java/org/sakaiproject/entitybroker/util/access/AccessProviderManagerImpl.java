/**
 * $Id$
 * $URL$
 * AccessProviderManagerImpl.java - entity-broker - Apr 6, 2008 9:03:03 AM - azeckoski
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
 */

package org.sakaiproject.entitybroker.util.access;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A common generic implementation class for managers of different kinds of access providers.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class AccessProviderManagerImpl<T> {

   private final Map<String, WeakReference<T>> prefixMap = new ConcurrentHashMap<String, WeakReference<T>>();

   public void registerProvider(String prefix, T provider) {
      if (prefix == null || provider == null) {
         throw new IllegalArgumentException("prefix and provider must be non-null");
      }
      prefixMap.put(prefix, new WeakReference<T>(provider));
   }

   public void unregisterProvider(String prefix, T provider) {
      if (prefix == null) {
         return;
      }
      WeakReference<T> current = prefixMap.get(prefix);
      if (current != null) {
         T existing = current.get();
         if (existing == null || existing == provider) {
            // Atomically remove only if mapping is unchanged to avoid racing with re-registration
            prefixMap.remove(prefix, current);
         }
      }
   }

   public void unregisterProvider(String prefix) {
      if (prefix != null) {
         prefixMap.remove(prefix);
      }
   }

   public T getProvider(String prefix) {
      if (prefix == null) {
         return null;
      }
      WeakReference<T> reference = prefixMap.get(prefix);
      if (reference == null) {
         return null;
      }
      T provider = reference.get();
      if (provider == null) {
         prefixMap.remove(prefix, reference);
      }
      return provider;
   }
}
