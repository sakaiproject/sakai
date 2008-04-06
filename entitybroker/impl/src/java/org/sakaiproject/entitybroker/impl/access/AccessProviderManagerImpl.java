/**
 * $Id$
 * $URL$
 * AccessProviderManagerImpl.java - entity-broker - Apr 6, 2008 9:03:03 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl.access;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A common generic implementation class for managers of different kinds of access providers.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class AccessProviderManagerImpl<T> {

   private Map<String, WeakReference<T>> prefixMap = new ConcurrentHashMap<String, WeakReference<T>>();

   public void registerProvider(String prefix, T provider) {
      prefixMap.put(prefix, new WeakReference<T>(provider));
   }

   public void unregisterProvider(String prefix, T provider) {
      prefixMap.remove(prefix);
   }

   public T getProvider(String prefix) {
      WeakReference<T> value = prefixMap.get(prefix);
      return value == null ? null : value.get();
   }
}
