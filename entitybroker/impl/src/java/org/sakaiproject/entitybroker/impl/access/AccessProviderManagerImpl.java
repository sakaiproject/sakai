/*
 * Created on 24 Aug 2007
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
