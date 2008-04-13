/*
 *  Copyright 2007 CodersLog.com 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *   
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *   
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */

/*
 * This code is from: http://www.coderslog.com/Main_Page
 * Imported here since I could not find a jar out in the maven repositories,
 * if anyone else knows where to find a jar for this code I would perfer
 * to simply use it rather than maintaining a copy here -AZ
 */

package org.sakaiproject.entitybroker.impl.util;

import java.beans.PropertyDescriptor;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

/**
 * BeanCloner can be used to fully or partially clone objects, based on
 * patterns.
 * 
 * <pre>
 *   public class Car	{
 *    String name;
 *    Double price;
 *    Wheel[] wheels;
 *    Make make;
 *    ...Getters/Setters...
 *   }
 *   Car car=new Car();
 * </pre>
 * 
 * Invocation Forms
 * 
 * <pre>
 *   Car clone=BeanCloner.clone(car,&quot;&quot;) copy only basic types with 0 depth.
 *   Car clone=BeanCloner.clone(car,&quot;make&quot;) all the basic types plus the reference to Make object
 *   Car clone=BeanCloner.clone(car,&quot;make.*&quot;) with all the references Make object has  
 *   Car clone=BeanCloner.clone(car,&quot;make.**&quot;) with all the collections Make object has
 *   Car clone=BeanCloner.clone(car,&quot;**&quot;) copy all basic fields, reference, and collections 1 level deep
 *   Car clone=BeanCloner.clone(car,&quot;**.**&quot;) 2 levels deep
 * </pre>
 * 
 * In all the cases recursive references are automatically recreated regardless
 * of depth setting.
 */
@SuppressWarnings("unchecked")
public class BeanCloner implements Transformer {
   protected static Set<Class> immutableTypes = new HashSet();
   private String pattern;
   private String path;
   private Map cache;
   static {
      immutableTypes.add(Boolean.class);
      immutableTypes.add(Character.class);
      immutableTypes.add(String.class);
      immutableTypes.add(Integer.class);
      immutableTypes.add(Double.class);
      immutableTypes.add(Float.class);
      immutableTypes.add(Long.class);
      immutableTypes.add(Date.class);
      immutableTypes.add(Timestamp.class);
   }

   public static <T> T clone(T object) {
      return clone(object, null, "");
   }

   public static <T> T clone(T object, String pattern) {
      return clone(object, pattern, "");
   }

   public static <T> T clone(T object, String pattern, String path) {
      return clone(object, pattern, path, new HashMap<Object, Object>());
   }

   public static <T> T clone(T object, String pattern, String path, Map<Object, Object> cache) {
      try {
         if (object != null && (pattern == null || matchPathEnd(path, pattern, object.getClass()))) {
            Object cachedValue;
            if (immutableTypes.contains(object.getClass())) {
               return object;
            } else if ((cachedValue = cache.get(object)) != null) {
               if (pattern != null && pattern.startsWith("NC"))
                  return null;
               else
                  return (T) cachedValue;
            } else if (Collection.class.isAssignableFrom(object.getClass())) {
               return (T) CollectionUtils.collect((Collection) object, new BeanCloner(pattern, path, cache));
            } else {
               T copy = (T) object.getClass().newInstance();
               cache.put(object, copy);
               Map<String, Object> properties = PropertyUtils.describe(object);
               for (Map.Entry<String, Object> entry : properties.entrySet()) {
                  PropertyDescriptor descriptor = PropertyUtils.getPropertyDescriptor(object, entry.getKey());
                  if (descriptor.getWriteMethod() != null)
                     PropertyUtils.setProperty(copy, entry.getKey(), clone(entry.getValue(), pattern, path
                           + (path.length() > 0 ? "." : "") + entry.getKey(), cache));
               }
               return copy;
            }
         } else {
            return null;
         }
      } catch (Exception e) {
         throw new Error(e);
      }
   }

   public static <S, T> T copy(S src, T target) {
      return copy(src, target, "", false);
   }

   public static <S, T> T copy(S src, T target, String pattern, boolean noNulls) {
      try {
         Map<String, Object> properties = PropertyUtils.describe(src);
         for (Map.Entry<String, Object> entry : properties.entrySet()) {
            PropertyDescriptor srcDescriptor = PropertyUtils.getPropertyDescriptor(src, entry.getKey());
            PropertyDescriptor trgDescriptor = PropertyUtils.getPropertyDescriptor(target, entry.getKey());
            if (srcDescriptor != null
                  && trgDescriptor != null
                  && srcDescriptor.getReadMethod() != null
                  && trgDescriptor.getWriteMethod() != null
                  && (entry.getValue() == null || matchPathEnd(entry.getKey(), pattern, entry.getValue()
                        .getClass()))) {
               if (!noNulls || entry.getValue() != null)
                  PropertyUtils.setProperty(target, entry.getKey(), entry.getValue());
            }
         }
      } catch (Exception e) {
         throw new Error(e);
      }
      return target;
   }

   public Object transform(Object object) {
      return clone(object, pattern, path, cache);
   }

   public static boolean matchPathEnd(String path, String pattern, Class type) {
      assert (pattern != null);
      assert (path != null);
      assert (type != null);
      if (pattern.startsWith("NC"))
         pattern = pattern.substring(2);
      if (immutableTypes.contains(type) || path.length() == 0)
         return true;
      String[] patterns = pattern.split(",");
      for (String pat : patterns) {
         String[] pathArray = path.split("\\.");
         String[] patternArray = pat.split("\\.");
         if (patternArray.length >= pathArray.length) {
            String ptrn = patternArray[pathArray.length - 1];
            String pth = pathArray[pathArray.length - 1];
            if (ptrn.equals("**") || ptrn.equals(pth)
                  || (ptrn.equals("*") && !Collection.class.isAssignableFrom(type)))
               return true;
         }
      }
      return false;
   }

   public BeanCloner(String pattern, String path, Map cache) {
      this.pattern = pattern;
      this.path = path;
      this.cache = cache;
   }
}
