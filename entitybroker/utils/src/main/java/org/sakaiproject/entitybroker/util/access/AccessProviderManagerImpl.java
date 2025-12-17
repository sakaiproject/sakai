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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A common generic implementation class for managers of different kinds of access providers.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class AccessProviderManagerImpl<T> {

   // Copy-on-write via AtomicReference: reads get a consistent snapshot via get(),
   // writes use a CAS loop to replace the map without blocking readers.
   // WeakReference values prevent cross-webapp classloader leaks.
   private final AtomicReference<Map<String, WeakReference<T>>> providerMapRef = new AtomicReference<>(Collections.emptyMap());

   public void registerProvider(String prefix, T provider) {
      Map<String, WeakReference<T>> current, next;
      do {
         current = providerMapRef.get();
         next = new HashMap<>(current);
         next.put(prefix, new WeakReference<>(provider));
      } while (!providerMapRef.compareAndSet(current, Collections.unmodifiableMap(next)));
   }

   public void unregisterProvider(String prefix) {
      Map<String, WeakReference<T>> current, next;
      do {
         current = providerMapRef.get();
         next = new HashMap<>(current);
         next.remove(prefix);
      } while (!providerMapRef.compareAndSet(current, Collections.unmodifiableMap(next)));
   }

   public T getProvider(String prefix) {
      WeakReference<T> ref = providerMapRef.get().get(prefix);
      return ref != null ? ref.get() : null;
   }
}
