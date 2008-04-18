/**
 * $Id$
 * $URL$
 * ReflectUtil.java - entity-broker - 24 Aug 2007 6:43:14 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reflection utilities and utilities related to working with classes
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class ReflectUtil {

   /**
    * Returns a list of all superclasses and implemented interfaces by the supplied class,
    * recursively to the base, up to but excluding Object.class. These will be listed in order from
    * the supplied class, all concrete superclasses in ascending order, and then finally all
    * interfaces in recursive ascending order.<br/>
    * This will include duplicates if any superclasses implement the same classes 
    * 
    * Taken from PonderUtilCore around version 1.2.2
    */
   public static List<Class<?>> getSuperclasses(Class<?> clazz) {
      List<Class<?>> accumulate = new ArrayList<Class<?>>();
      while (clazz != Object.class) {
         accumulate.add(clazz);
         clazz = clazz.getSuperclass();
      }
      int supers = accumulate.size();
      for (int i = 0; i < supers; ++i) {
         appendSuperclasses(accumulate.get(i), accumulate);
      }
      return accumulate;
   }

   /**
    * Taken from PonderUtilCore around version 1.2.2
    */
   private static void appendSuperclasses(Class<?> clazz, List<Class<?>> accrete) {
      Class<?>[] interfaces = clazz.getInterfaces();
      for (int i = 0; i < interfaces.length; ++i) {
         accrete.add(interfaces[i]);
      }
      for (int i = 0; i < interfaces.length; ++i) {
         appendSuperclasses(interfaces[i], accrete);
      }
   }


   /**
    * Finds a class type that is in the containing collection,
    * will always return something (failsafe to Object.class)
    * @param collection
    * @return the class type contained in this collecion
    */
   @SuppressWarnings("unchecked")
   public static Class<?> getClassFromCollection(Collection collection) {
      // try to get the type of entities out of this collection
      Class<?> c = Object.class;
      if (collection != null) {
         if (! collection.isEmpty()) {
            c = collection.iterator().next().getClass();
         } else {
            // this always gets Object.class -AZ
            c = collection.toArray().getClass().getComponentType();                     
         }
      }
      return c;
   }

   /**
    * Checks to see if an array contains a value,
    * will return false if a null value is supplied
    * 
    * @param <T>
    * @param array any array of objects
    * @param value the value to check for
    * @return true if the value is found, false otherwise
    */
   public static <T> boolean contains(T[] array, T value) {
      boolean foundValue = false;
      if (value != null) {
         for (int i = 0; i < array.length; i++) {
            if (value.equals(array[i])) {
               foundValue = true;
               break;
            }
         }
      }
      return foundValue;
   }

   /**
    * Get a map of all fieldName -> value and all getterMethodName -> value without the word "get"
    * where the method takes no arguments, in other words, all values available from an object
    * @param object any object
    * @return a map of name -> value
    */
   public Map<String, Object> getObjectValues(Object object) {
      Map<String, Object> values = new HashMap<String, Object>();
      Class<?> elementClass = object.getClass();
      for (Field field : elementClass.getFields()) {
         Object value;
         try {
            value = field.get(object);
            values.put(field.getName(), value);
         } catch (Exception e) {
            // nothing to do here but move on
         }
      }
      for (Method method : elementClass.getMethods()) {
         String name = method.getName();
         if (name.startsWith("get") 
               && ! "getClass".equals(name)) {
            if (method.getParameterTypes().length == 0) {
               try {
                  Object value = method.invoke(object, (Object[])null);
                  values.put(unCapitalize(method.getName().substring(3)), value);
               } catch (Exception e) {
                  // nothing to do here but move on
               }
            }
         }
      }
      return values;
   }

   /**
    * Get the string value of a field or getter method from an object 
    * if the field or getter method exists on the object,
    * if not then return null
    * 
    * @param object any object
    * @param fieldName the name of the field (property) to get the value of or the getter method without the "get" and lowercase first char
    * @param annotationClass if the annotation class is set then we will attempt to get the value from the annotated field or getter method first
    * @return the string value of the field or null if no field or null if the value is null
    */
   public String getFieldValueAsString(Object object, String fieldName, Class<? extends Annotation> annotationClass) {
      String value = null;
      Class<?> elementClass = object.getClass();
      boolean found = false;
      // try to get value from pea
      try {
         Field field;
         try {
            field = getFieldWithAnnotation(elementClass, annotationClass);
         } catch (NoSuchFieldException e) {
            field = elementClass.getDeclaredField(fieldName);
         }
         Object fieldValue = field.get(object);
         found = true;
         if (fieldValue != null) {
            value = fieldValue.toString();
         }
      } catch (Exception e) {
         found = false;
      }
      if (found == false) {
         // try to get value from method
         try {
            Method getMethod;
            try {
               getMethod = getMethodWithAnnotation(elementClass, annotationClass);
            } catch (NoSuchMethodException e) {
               getMethod = elementClass.getMethod("get" + capitalize(fieldName), new Class[] {});
            }
            Object getValue = getMethod.invoke(object, (Object[])null);
            found = true;
            if (getValue != null) {
               value = getValue.toString();
            }
         } catch (Exception e) {
            found = false;
         }
      }
      return value;
   }

   /**
    * Find a method with an annotation on a class if there is one
    * @param c
    * @param annotationClass
    * @return the method
    * @throws NoSuchMethodException if no method can be found
    */
   public Method getMethodWithAnnotation(Class<?> c, Class<? extends Annotation> annotationClass) throws NoSuchMethodException {
      Method m = null;
      if (annotationClass != null) {
         for (Method method : c.getMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
               m = method;
               break;
            }
         }
      }
      if (m == null) {
         throw new NoSuchMethodException("No methods found with annotation: " + annotationClass);
      }
      return m;
   }

   /**
    * Find a field with an annotation on a class if there is one
    * @param c
    * @param annotationClass
    * @return the field
    * @throws NoSuchFieldException if no field can be found
    */
   public Field getFieldWithAnnotation(Class<?> c, Class<? extends Annotation> annotationClass) throws NoSuchFieldException {
      Field f = null;
      if (annotationClass != null) {
         for (Field field : c.getFields()) {
            if (field.isAnnotationPresent(annotationClass)) {
               f = field;
               break;
            }
         }
      }
      if (f == null) {
         throw new NoSuchFieldException("No fields found with annotation: " + annotationClass);
      }
      return f;
   }

   /**
    * Capitalize a string
    * @param input any string
    * @return the string capitalized (e.g. myString -> MyString)
    */
   public static String capitalize(String input) {
      String cap = null;
      if (input.length() == 0) {
         cap = "";
      } else if (input.length() == 1) {
         cap = input.toUpperCase();
      } else {
         cap = input.substring(0, 1).toUpperCase() + input.substring(1);
      }
      return cap;
   }

   /**
    * undo string capitalization
    * @param input any string
    * @return the string uncapitalized (e.g. MyString -> myString)
    */
   public static String unCapitalize(String input) {
      String cap = null;
      if (input.length() == 0) {
         cap = "";
      } else if (input.length() == 1) {
         cap = input.toLowerCase();
      } else {
         cap = input.substring(0, 1).toLowerCase() + input.substring(1);
      }
      return cap;
   }

}
