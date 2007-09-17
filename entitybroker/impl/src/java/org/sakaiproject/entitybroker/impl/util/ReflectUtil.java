/*
 * Created on 24 Aug 2007
 */

package org.sakaiproject.entitybroker.impl.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Reflection utilities. Taken from PonderUtilCore around version 1.2.2
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * 
 */

public class ReflectUtil {

   /**
    * Returns a list of all superclasses and implemented interfaces by the supplied class,
    * recursively to the base, up to but excluding Object.class. These will be listed in order from
    * the supplied class, all concrete superclasses in ascending order, and then finally all
    * interfaces in recursive ascending order.
    */

   public static List<Class<?>> getSuperclasses(Class<?> clazz) {
      List<Class<?>> togo = new ArrayList<Class<?>>();
      while (clazz != Object.class) {
         togo.add(clazz);
         clazz = clazz.getSuperclass();
      }
      int supers = togo.size();
      for (int i = 0; i < supers; ++i) {
         appendSuperclasses(togo.get(i), togo);
      }
      return togo;
   }

   private static void appendSuperclasses(Class<?> clazz, List<Class<?>> accrete) {
      Class<?>[] interfaces = clazz.getInterfaces();
      for (int i = 0; i < interfaces.length; ++i) {
         accrete.add(interfaces[i]);
      }
      for (int i = 0; i < interfaces.length; ++i) {
         appendSuperclasses(interfaces[i], accrete);
      }
   }
}
