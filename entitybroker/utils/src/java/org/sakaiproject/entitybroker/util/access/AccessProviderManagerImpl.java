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

import java.util.Map;

import org.azeckoski.reflectutils.refmap.ReferenceMap;
import org.azeckoski.reflectutils.refmap.ReferenceType;

/**
 * A common generic implementation class for managers of different kinds of access providers.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class AccessProviderManagerImpl<T> {

   private Map<String, T> prefixMap = new ReferenceMap<String, T>(ReferenceType.STRONG, ReferenceType.WEAK);
   // replaced with a reference map
   //private Map<String, WeakReference<T>> prefixMap = new ConcurrentHashMap<String, WeakReference<T>>();

   public void registerProvider(String prefix, T provider) {
      prefixMap.put(prefix, provider);
   }

   public void unregisterProvider(String prefix, T provider) {
      prefixMap.remove(prefix);
   }

   public void unregisterProvider(String prefix) {
      prefixMap.remove(prefix);
   }

   public T getProvider(String prefix) {
      return prefixMap.get(prefix);
   }
}
