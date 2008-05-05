/**
 * $Id$
 * $URL$
 * PropertyUtilsAZ.java - genericdao - Apr 27, 2008 11:57:05 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.util.reflect;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.collections.FastHashMap;
import org.sakaiproject.entitybroker.util.refmap.ReferenceMap;
import org.sakaiproject.entitybroker.util.refmap.ReferenceType;

/**
 * Overrides to fix some issues with the apache beanutils and extend functionality
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@SuppressWarnings("unchecked")
public class PropertyUtilsAZ extends PropertyUtilsBean {

   // AZ added so I can get access to it
   private FastHashMap mappedDescriptorsCache = null;
   public FastHashMap getMappedDescriptorsCache() {
      if (mappedDescriptorsCache == null) {
         mappedDescriptorsCache = new FastHashMap();
         mappedDescriptorsCache.setFast(true);
      }
      return mappedDescriptorsCache;
   }

   private Map<Class<?>, Map<String, Field>> classFieldMap;
   protected Map<Class<?>, Map<String, Field>> getClassFieldMap() {
      if (classFieldMap == null) {
         classFieldMap = new ReferenceMap<Class<?>, Map<String, Field>>(ReferenceType.WEAK, ReferenceType.SOFT);
      }
      return classFieldMap;
   }

   /**
    * @param elementClass any class
    * @return always returns a map, empty if there are no fields
    */
   public Map<String, Field> getFieldMap(Class<?> elementClass) {
      analyzeClassFields(elementClass);
      return getClassFieldMap().get(elementClass);
   }

   /**
    * @param elementClass any class
    * @return always returns a collection, even if there no fields in it
    */
   public Collection<Field> getFields(Class<?> elementClass) {
      analyzeClassFields(elementClass);
      return getClassFieldMap().get(elementClass).values();
   }

   /**
    * Find and cache the fields in a class
    */
   protected void analyzeClassFields(Class<?> elementClass) {
      if (! getClassFieldMap().containsKey(elementClass) ) {
         // class was not yet analyzed
         Map<String, Field> fMap = new ConcurrentHashMap<String, Field>();
         try {
            Field[] fields = elementClass.getFields();
            for (Field field : fields) {
               try {
                  if (! field.isAccessible()) {
                     field.setAccessible(true);
                  }
                  fMap.put(field.getName(), field);
               } catch (Exception e) {
                  // if we fail then nothing to do here but move on
               }
            }
         } catch (SecurityException e) {
            // if security manager denies us access then we just move on
         }
         getClassFieldMap().put(elementClass, fMap);
      }
   }

   /**
    * @param elementClass
    * @return a list of all accessible members for a class,
    * methods first (read then write) and fields last
    */
   public List<Member> getMembers(Class<?> elementClass) {
      List<Member> members = new ArrayList<Member>();
      PropertyDescriptor[] pds = getPropertyDescriptors(elementClass);
      for (int i = 0; i < pds.length; i++) {
         PropertyDescriptor pd = pds[i];
         if ("class".equals(pd.getName())) { continue; } // skip the getClass method
         if (pd.getReadMethod() != null) {
            members.add(pd.getReadMethod());
         }
         if (pd.getWriteMethod() != null) {
            members.add(pd.getWriteMethod());
         }
      }
      // now add in field types which are not already in the map
      for (Field field : getFields(elementClass)) {
         members.add(field);
      }
      return members;
   }

   @SuppressWarnings({ "deprecation", "unchecked" })
   @Override
   public PropertyDescriptor getPropertyDescriptor(Object bean, String name)
   throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
      if (bean == null) {
         throw new IllegalArgumentException("No bean specified");
      }
      if (name == null) {
         throw new IllegalArgumentException("No name specified for bean class '" +
               bean.getClass() + "'");
      }

      // Resolve nested references
      while (getResolver().hasNested(name)) {
         String next = getResolver().next(name);
         Object nestedBean = null;
         if (bean instanceof Map) {
            nestedBean = getPropertyOfMapBean((Map)bean, next);
         } else if (getResolver().isMapped(next)) {
            nestedBean = getMappedProperty(bean, next);
         } else if (getResolver().isIndexed(next)) {
            nestedBean = getIndexedProperty(bean, next);
         } else {
            nestedBean = getSimpleProperty(bean, next);
         }
         if (nestedBean == null) {
            throw new NestedNullException("Null property value for '" + name +
                  "' on bean class '" + bean.getClass() + "'");
         }
         bean = nestedBean;
         name = getResolver().remove(name);
      }

      // Remove any subscript from the final name value
      name = getResolver().getProperty(name);

      // Look up and return this property from our cache
      // creating and adding it to the cache if not found.
      if ((bean == null) || (name == null)) {
         return (null);
      }

      PropertyDescriptor[] descriptors = getPropertyDescriptors(bean);
      if (descriptors != null) {
         for (int i = 0; i < descriptors.length; i++) {
            if (name.equals(descriptors[i].getName())) {
               return (descriptors[i]);
            }
         }
         // AZ now try tweaking the name slightly (uncap the first char) and try again, this fixes the sThing to SThing issue
         for (int i = 0; i < descriptors.length; i++) {
            String descriptorName = descriptors[i].getName();
            if ( ReflectUtil.pdNameCompare(name, descriptorName) ) {
               return (descriptors[i]);
            }
         }
      }

      // AZ only get here if getPropertyDescriptors failed (I think this is effectively caching failures to resolve)
      PropertyDescriptor result = null;
      FastHashMap mappedDescriptors = getMappedPropertyDescriptors(bean);
      if (mappedDescriptors == null) {
         mappedDescriptors = new FastHashMap();
         mappedDescriptors.setFast(true);
         getMappedDescriptorsCache().put(bean.getClass(), mappedDescriptors);
      }
      result = (PropertyDescriptor) mappedDescriptors.get(name);
      if (result == null) {
         // not found, try to create it
         try {
            result = new MappedPropertyDescriptor(name, bean.getClass());
         } catch (IntrospectionException ie) {
            /* Swallow IntrospectionException
             * TODO: Why?
             */
         }
         if (result != null) {
            mappedDescriptors.put(name, result);
         }
      }

      return result;
   }

   @Override
   public Map describe(Object bean) throws IllegalAccessException, InvocationTargetException,
         NoSuchMethodException {
      // AZ adding field support
      Map<String, Object> values = new HashMap<String, Object>();
      values.putAll( super.describe(bean) );
      // now add in field types which are not already in the map
      Class<?> elementClass = bean.getClass();
      for (Field field : getFields(elementClass)) {
         String fieldName = field.getName();
         if (! values.containsKey(fieldName)) {
            values.put(fieldName, field.get(bean));
         }
      }
      return values;
   }

   @Override
   public PropertyDescriptor[] getPropertyDescriptors(Class beanClass) {
      PropertyDescriptor[] descriptors = super.getPropertyDescriptors(beanClass);
      // AZ analyze the fields at the same time
      analyzeClassFields(beanClass);
      return (descriptors);
   }

   @Override
   public void clearDescriptors() {
      // AZ adding support for access the mapped cache
      super.clearDescriptors();
      getMappedDescriptorsCache().clear();
   }

   @Override
   public FastHashMap getMappedPropertyDescriptors(Class beanClass) {
      // AZ adding support for access the mapped cache
      if (beanClass == null) {
         return null;
      }
      // Look up any cached descriptors for this bean class
      return (FastHashMap) getMappedDescriptorsCache().get(beanClass);
   }

   @Override
   public FastHashMap getMappedPropertyDescriptors(Object bean) {
      // AZ adding support for access the mapped cache
      if (bean == null) {
         return null;
      }
      return getMappedPropertyDescriptors(bean.getClass());
   }

}
