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
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.google.inject.util.ReferenceMap;
import com.google.inject.util.ReferenceType;

/**
 * Reflection utilities and utilities related to working with classes
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ReflectUtil {

   private static final String METHOD_GET_CLASS = "getClass";
   private static final String PREFIX_IS = "is";
   private static final String PREFIX_GET = "get";
   private static final String PREFIX_SET = "set";
   /**
    * Should contain all publicly accessible members (fields OR methods without the "get"/"is")
    */
   protected Map<Class<?>, List<Member>> getterMap = new ReferenceMap<Class<?>, List<Member>>(ReferenceType.WEAK, ReferenceType.SOFT);
   protected Map<Class<?>, List<Member>> setterMap = new ReferenceMap<Class<?>, List<Member>>(ReferenceType.WEAK, ReferenceType.SOFT);

   protected void analyzeClass(Class<?> elementClass) {
      if (! getterMap.containsKey(elementClass) 
            || ! setterMap.containsKey(elementClass)) {
         // class was not yet analyzed
         getterMap.put(elementClass, new Vector<Member>());
         setterMap.put(elementClass, new Vector<Member>());

         for (Field field : elementClass.getFields()) {
            try {
               getterMap.get(elementClass).add(field);
               setterMap.get(elementClass).add(field);
            } catch (Exception e) {
               // nothing to do here but move on
            }
         }
         for (Method method : elementClass.getMethods()) {
            Class<?>[] paramTypes = method.getParameterTypes();
            String name = method.getName();
            if (paramTypes.length == 0) {
               if (METHOD_GET_CLASS.equals(name)) {
                  continue;
               } else if ( name.startsWith(PREFIX_GET) || name.startsWith(PREFIX_IS) ) {
                  Class<?> returnType = method.getReturnType();
                  if (returnType != null) {
                     try {
                        getterMap.get(elementClass).add(method);
                     } catch (Exception e) {
                        // nothing to do here but move on
                     }  
                  }
               }
            } else if ( name.startsWith(PREFIX_SET) 
                  && paramTypes.length == 1 ) {
               try {
                  setterMap.get(elementClass).add(method);
               } catch (Exception e) {
                  // nothing to do here but move on
               }                     
            }
         }
      }
   }

   protected List<Member> getGetterMembers(Class<?> elementClass) {
      analyzeClass(elementClass);
      List<Member> members = getterMap.get(elementClass);
      return members;
   }

   protected List<Member> getSetterMembers(Class<?> elementClass) {
      analyzeClass(elementClass);
      List<Member> members = setterMap.get(elementClass);
      return members;
   }

   protected String getFieldnameFromMethod(String methodName) {
      String name = null;
      if (methodName.startsWith(PREFIX_IS)) {
         name = methodName.substring(2);
      } else {
         // set or get
         name = methodName.substring(3);
      }
      name = unCapitalize(name);
      return name;
   }

   /**
    * Get the return types of the fields and getter methods of a specific class type
    * @param elementClass any class
    * @return a map of field name/getter method name -> class type
    */
   public Map<String, Class<?>> getObjectTypes(Class<?> elementClass) {
      Map<String, Class<?>> types = new HashMap<String, Class<?>>();
      for (Member member : getGetterMembers(elementClass)) {
         if (member instanceof Field) {
            Field field = (Field) member;
            try {
               types.put(field.getName(), field.getType());
            } catch (Exception e) {
               // nothing to do here but move on
            }
         } else if (member instanceof Method) {
            Method method = (Method) member;
            try {
               types.put(getFieldnameFromMethod(method.getName()), method.getReturnType());
            } catch (Exception e) {
               // nothing to do here but move on
            }
         }
      }
      return types;
   }

   /**
    * Get the set types of the fields and setter methods for a specific class
    * @param elementClass any class
    * @return a map of field name/getter method name -> class type
    */
   public Map<String, Class<?>> getSetTypes(Class<?> elementClass) {
      Map<String, Class<?>> types = new HashMap<String, Class<?>>();
      for (Member member : getSetterMembers(elementClass)) {
         if (member instanceof Field) {
            Field field = (Field) member;
            try {
               types.put(field.getName(), field.getType());
            } catch (Exception e) {
               // nothing to do here but move on
            }
         } else if (member instanceof Method) {
            Method method = (Method) member;
            try {
               // expect all setters to have at least one param
               Class<?> type = method.getParameterTypes()[0];
               types.put(getFieldnameFromMethod(method.getName()), type);
            } catch (Exception e) {
               // nothing to do here but move on
            }
         }
      }
      return types;
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
      for (Member member : getGetterMembers(elementClass)) {
         if (member instanceof Field) {
            Field field = (Field) member;
            Object value;
            try {
               value = field.get(object);
               values.put(field.getName(), value);
            } catch (Exception e) {
               // nothing to do here but move on
            }
         } else if (member instanceof Method) {
            Method method = (Method) member;
            try {
               Object value = method.invoke(object, (Object[])null);
               values.put(getFieldnameFromMethod(method.getName()), value);
            } catch (Exception e) {
               // nothing to do here but move on
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
      Object value = null;
      Class<?> elementClass = object.getClass();
      boolean found = false;
      if (annotationClass != null) {
         // try to find annotation first
         for (Member member : getGetterMembers(elementClass)) {
            if (member instanceof Field) {
               Field field = (Field) member;
               try {
                  if (field.isAnnotationPresent(annotationClass)) {
                     value = field.get(object);
                     found = true;
                     break;
                  }
               } catch (Exception e) {
                  // nothing to do here but move on
               }
            } else if (member instanceof Method) {
               Method method = (Method) member;
               try {
                  if (method.isAnnotationPresent(annotationClass)) {
                     value = method.invoke(object, (Object[])null);
                     found = true;
                     break;
                  }
               } catch (Exception e) {
                  // nothing to do here but move on
               }
            }
         }
      }
      if (!found) {
         // no luck with annotation so try to find the field instead
         for (Member member : getGetterMembers(elementClass)) {
            if (member instanceof Field) {
               Field field = (Field) member;
               try {
                  if (fieldName.equals(field.getName())) {
                     found = true;
                     value = field.get(object);
                     break;
                  }
               } catch (Exception e) {
                  // nothing to do here but move on
               }
            } else if (member instanceof Method) {
               Method method = (Method) member;
               String name = unCapitalize(method.getName().substring(3));
               try {
                  if (fieldName.equals(name)) {
                     found = true;
                     value = method.invoke(object, (Object[])null);
                     break;
                  }
               } catch (Exception e) {
                  // nothing to do here but move on
               }
            }
         }
      }
      String sValue = null;
      if (value != null) {
         sValue = value.toString();
      }
      return sValue;
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
      } else {
         char first = Character.toUpperCase(input.charAt(0));
         if (input.length() == 1) {
            cap = first + "";
         } else {
            cap = first + input.substring(1);
         }
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
      } else {
         char first = Character.toLowerCase(input.charAt(0));
         if (input.length() == 1) {
            cap = first + "";
         } else {
            cap = first + input.substring(1);
         }
      }
      return cap;
   }


   /**
    * Returns a list of all superclasses and implemented interfaces by the supplied class,
    * recursively to the base, up to but excluding Object.class. These will be listed in order from
    * the supplied class, all concrete superclasses in ascending order, and then finally all
    * interfaces in recursive ascending order.<br/>
    * This will include duplicates if any superclasses implement the same classes 
    * 
    * Taken from PonderUtilCore around version 1.2.2
    * @author Antranig Basman (antranig@caret.cam.ac.uk)
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
    * @author Antranig Basman (antranig@caret.cam.ac.uk)
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
    * Take an array of anything and turn it into a string
    * 
    * @param array any array
    * @return a string representing that array
    */
   public static String arrayToString(Object[] array) {
      StringBuilder result = new StringBuilder();
      if (array != null && array.length > 0) {
         for (int i = 0; i < array.length; i++) {
            if (i > 0) {
               result.append(",");
            }
            if (array[i] != null) {
               result.append(array[i].toString());
            }
         }
      }
      return result.toString();
   }

}
