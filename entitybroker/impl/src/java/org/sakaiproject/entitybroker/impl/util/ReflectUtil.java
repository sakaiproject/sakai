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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

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
   protected Map<Class<?>, Map<String, Member>> getterMap = new ReferenceMap<Class<?>, Map<String, Member>>(ReferenceType.WEAK, ReferenceType.SOFT);
   protected Map<Class<?>, Map<String, Member>> setterMap = new ReferenceMap<Class<?>, Map<String, Member>>(ReferenceType.WEAK, ReferenceType.SOFT);

   private PropertyUtilsBean propertyUtils = new PropertyUtilsBean();
   private ConvertUtilsBean convertUtils = new ConvertUtilsBean();
   /**
    * We are using this instead of the static version so we can manage our own caching
    */
   private BeanUtilsBean beanUtils = new BeanUtilsBean(convertUtils, propertyUtils);
   public BeanUtilsBean getBeanUtils() {
      return beanUtils;
   }

   protected void analyzeClass(Class<?> elementClass) {
      if (! getterMap.containsKey(elementClass) 
            || ! setterMap.containsKey(elementClass)) {
         // class was not yet analyzed
         getterMap.put(elementClass, new ConcurrentHashMap<String, Member>());
         setterMap.put(elementClass, new ConcurrentHashMap<String, Member>());

         for (Field field : elementClass.getFields()) {
            try {
               getterMap.get(elementClass).put(field.getName(), field);
               setterMap.get(elementClass).put(field.getName(), field);
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
               } else if ( name.startsWith(PREFIX_GET) 
                     || name.startsWith(PREFIX_IS) ) {
                  Class<?> returnType = method.getReturnType();
                  if (returnType != null) {
                     try {
                        getterMap.get(elementClass).put(makeFieldNameFromMethod(method.getName()), method);
                     } catch (Exception e) {
                        // nothing to do here but move on
                     }  
                  }
               }
            } else if ( name.startsWith(PREFIX_SET) 
                  && paramTypes.length == 1 ) {
               try {
                  setterMap.get(elementClass).put(makeFieldNameFromMethod(method.getName()), method);
               } catch (Exception e) {
                  // nothing to do here but move on
               }                     
            }
         }
      }
   }

   protected Map<String, Member> getGetterMap(Class<?> elementClass) {
      analyzeClass(elementClass);
      return getterMap.get(elementClass);
   }

   protected Map<String, Member> getSetterMap(Class<?> elementClass) {
      analyzeClass(elementClass);
      return setterMap.get(elementClass);
   }

   protected Collection<Member> getGetterMembers(Class<?> elementClass) {
      analyzeClass(elementClass);
      Collection<Member> members = getterMap.get(elementClass).values();
      return members;
   }

   protected Collection<Member> getSetterMembers(Class<?> elementClass) {
      analyzeClass(elementClass);
      Collection<Member> members = setterMap.get(elementClass).values();
      return members;
   }

   private String getAnnotatedFieldNameFromMember(Member member, Class<? extends Annotation> annotationClass) {
      String fieldName = null;
      if (member instanceof Field) {
         Field field = (Field) member;
         try {
            if (field.isAnnotationPresent(annotationClass)) {
               fieldName = field.getName();
            }
         } catch (Exception e) {
            // nothing to do here but move on
         }
      } else if (member instanceof Method) {
         Method method = (Method) member;
         try {
            if (method.isAnnotationPresent(annotationClass)) {
               fieldName = makeFieldNameFromMethod(method.getName());
            }
         } catch (Exception e) {
            // nothing to do here but move on
         }
      }
      return fieldName;
   }


   // PUBLIC methods

   /**
    * Get the value of a field or getter method from an object
    * @param object any object
    * @param fieldName the name of the field (property) to get the value of or the getter method without the "get" and lowercase first char
    * @throws IllegalArgumentException if the fieldName could not be found in this object
    */
   public Object getFieldValue(Object object, String fieldName) {
      Object value = null;
      boolean found = false;
      Class<?> elementClass = object.getClass();
      Member member = getGetterMap(elementClass).get(fieldName);
      if (member != null) {
         if (member instanceof Field) {
            Field field = (Field) member;
            try {
               value = field.get(object);
               found = true;
            } catch (Exception e) {
               // nothing to do here but move on
            }
         } else if (member instanceof Method) {
            Method method = (Method) member;
            try {
               value = method.invoke(object, (Object[])null);
               found = true;
            } catch (Exception e) {
               // nothing to do here but move on
            }
         }
      }
      if (!found) {
         throw new IllegalArgumentException("Could not find a field with name (" + fieldName + ") to get value from");
      }
      return value;
   }

   /**
    * Set the value on the object field or setter method
    * @param object any object
    * @param fieldName the name of the field (property) to set the value of or the setter method without the "set" and lowercase first char
    * @param value the value to set on this field, must match the type in the object (will not attempt to covert)
    * @throws IllegalArgumentException if the fieldName could not be found in this object 
    * OR the value type does not match the field type
    */
   public void setFieldValue(Object object, String fieldName, Object value) {
      boolean found = false;
      Class<?> elementClass = object.getClass();
      Member member = getSetterMap(elementClass).get(fieldName);
      if (member != null) {
         if (member instanceof Field) {
            Field field = (Field) member;
               try {
                  field.set(object, value);
                  found = true;
               } catch (Exception e) {
                  throw new IllegalArgumentException("Could not set fieldName (" + fieldName + ") to value: " + value, e);
               }
               found = true;
         } else if (member instanceof Method) {
            Method method = (Method) member;
            try {
               method.invoke(object, new Object[] {value});
            } catch (Exception e) {
               throw new IllegalArgumentException("Could not invoke setter method (" + method.getName() + ") with value: " + value, e);
            }
         }
      }
      if (!found) {
         throw new IllegalArgumentException("Could not find a field with name (" + fieldName + ") to set value on");
      }
   }

   /**
    * Set the value on the object field or setter method
    * @param object any object
    * @param fieldName the name of the field (property) to set the value of or the setter method without the "set" and lowercase first char
    * @param value the value to set on this field as a string, will attempt to auto-convert from string to the proper type
    * @throws IllegalArgumentException if the fieldName could not be found in this object 
    * OR the value could not be converted from String to the field type
    */
   public void setFieldStringValue(Object object, String fieldName, String value) {
      Class<?> elementClass = object.getClass();
      Class<?> type = getSetType(elementClass, fieldName);
      Object convertedValue;
      try {
         convertedValue = convertUtils.convert(value, type);
      } catch (ConversionException e) {
         throw new IllegalArgumentException("Could not convert value from ("+value+") to an object of type ("+type+")");
      }
      setFieldValue(object, fieldName, convertedValue);
   }

   /**
    * @param <T>
    * @param original the original object to copy from
    * @param destination the object to copy the values to (must have the same fields with the same types)
    * @param fieldNamesToSkip an array of the fieldNames which should NOT be copied from original to destination
    * @throws IllegalArgumentException if the copy cannot be completed because the objects to copy do not have matching fields or types
    */
   public <T> void copyObjectValues(T original, T destination, String[] fieldNamesToSkip) {
      if (original == null || destination == null) {
         throw new IllegalArgumentException("Cannot have null objects involved in the copy");
      }
      Set<String> skip = new HashSet<String>();
      if (fieldNamesToSkip != null) {
         for (int i = 0; i < fieldNamesToSkip.length; i++) {
            if (fieldNamesToSkip[i] != null) {
               skip.add(fieldNamesToSkip[i]);
            }
         }
      }
      try {
         Class<?> elementClass = destination.getClass();
         Map<String, Member> gm = getSetterMap(elementClass);
         for (String fieldName : gm.keySet()) {
            if (skip.isEmpty() || ! skip.contains(fieldName)) {
               Object value = getFieldValue(original, fieldName);
               setFieldValue(destination, fieldName, value);
            }
         }
      } catch (Exception e) {
         throw new IllegalArgumentException("Failed to copy values from original ("+original+") to destination ("+destination+")", e);
      }
   }

   /**
    * Get the return types of the fields and getter methods of a specific class type
    * returns the method names without the "get"/"is" part and camelCased
    * @param elementClass any class
    * @return a map of field name/getter method name -> class type
    */
   public Map<String, Class<?>> getReturnTypes(Class<?> elementClass) {
      Map<String, Class<?>> types = new HashMap<String, Class<?>>();
      Map<String, Member> gm = getGetterMap(elementClass);
      for (String fieldName : gm.keySet()) {
         Class<?> type = getReturnType(elementClass, fieldName);
         types.put(fieldName, type);
      }
      return types;
   }

   /**
    * @param elementClass any class
    * @param fieldName the name of the field (property) or a getter method converted to a fieldname
    * @return the type of object stored in the field or returned by the getter
    */
   public Class<?> getReturnType(Class<?> elementClass, String fieldName) {
      Class<?> type = null;
      Member member = getGetterMap(elementClass).get(fieldName);
      if (member != null) {
         if (member instanceof Field) {
            Field field = (Field) member;
            try {
               type = field.getType();
            } catch (Exception e) {
               // nothing to do here but move on
            }
         } else if (member instanceof Method) {
            Method method = (Method) member;
            try {
               type = method.getReturnType();
            } catch (Exception e) {
               // nothing to do here but move on
            }
         }
      }
      if (type == null) {
         throw new IllegalArgumentException("Could not find a field with name (" + fieldName + ") to get return type from");
      }
      return type;
   }

   /**
    * Get the types of the public fields and setter methods for a specific class,
    * returns the method names without the "set" part and camelCased
    * @param elementClass any class
    * @return a map of field name/setter method name -> class type
    */
   public Map<String, Class<?>> getSetTypes(Class<?> elementClass) {
      Map<String, Class<?>> types = new HashMap<String, Class<?>>();
      Map<String, Member> sm = getSetterMap(elementClass);
      for (String fieldName : sm.keySet()) {
         Class<?> type = getSetType(elementClass, fieldName);
         types.put(fieldName, type);
      }
      return types;
   }

   /**
    * Get the type of the public field or setter method of this name for this class
    * @param elementClass any class
    * @param fieldName the name of the field (property) or a setter method converted to a fieldname
    * @return the type of object stored in the field or expected by the setter
    */
   public Class<?> getSetType(Class<?> elementClass, String fieldName) {
      Class<?> type = null;
      Member member = getSetterMap(elementClass).get(fieldName);
      if (member instanceof Field) {
         Field field = (Field) member;
         try {
            type = field.getType();
         } catch (Exception e) {
            // nothing to do here but move on
         }
      } else if (member instanceof Method) {
         Method method = (Method) member;
         try {
            // expect all setters to have one param
            type = method.getParameterTypes()[0];
         } catch (Exception e) {
            // nothing to do here but move on
         }
      }      
      if (type == null) {
         throw new IllegalArgumentException("Could not find a field with name (" + fieldName + ") to get setter type from");
      }
      return type;
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
      Map<String, Member> gm = getGetterMap(elementClass);
      for (String fieldName : gm.keySet()) {
         Member member = gm.get(fieldName);
         if (member instanceof Field) {
            Field field = (Field) member;
            Object value;
            try {
               value = field.get(object);
               values.put(fieldName, value);
            } catch (Exception e) {
               // nothing to do here but move on
            }
         } else if (member instanceof Method) {
            Method method = (Method) member;
            try {
               Object value = method.invoke(object, (Object[])null);
               values.put(fieldName, value);
            } catch (Exception e) {
               // nothing to do here but move on
            }
         }
      }
      return values;
   }

   /**
    * Find the getter field on a class which has the given annotation
    * @param elementClass any class
    * @param annotationClass the annotation type which is expected to be on the field
    * @return the name of the field or null if no fields are found with the indicated annotation
    */
   public String getFieldNameForGetterWithAnnotation(Class<?> elementClass, Class<? extends Annotation> annotationClass) {
      String fieldName = null;
      if (annotationClass == null) {
         throw new IllegalArgumentException("the annotationClass must not be null");
      }
      for (Member member : getGetterMembers(elementClass)) {
         fieldName = getAnnotatedFieldNameFromMember(member, annotationClass);
         if (fieldName != null) {
            break;
         }
      }
      return fieldName;
   }

   /**
    * Find the setter field on a class which has the given annotation
    * @param elementClass any class
    * @param annotationClass the annotation type which is expected to be on the field
    * @return the name of the field or null if no fields are found with the indicated annotation
    */
   public String getFieldNameForSetterWithAnnotation(Class<?> elementClass, Class<? extends Annotation> annotationClass) {
      String fieldName = null;
      if (annotationClass == null) {
         throw new IllegalArgumentException("the annotationClass must not be null");
      }
      if (annotationClass != null) {
         for (Member member : getSetterMembers(elementClass)) {
            fieldName = getAnnotatedFieldNameFromMember(member, annotationClass);
            if (fieldName != null) {
               break;
            }
         }
      }
      return fieldName;
   }

   /**
    * Get the value of a field or getter method from an object
    * 
    * @param object any object
    * @param fieldName the name of the field (property) to get the value of or the getter method without the "get" and lowercase first char
    * @param annotationClass if the annotation class is set then we will attempt to get the value from the annotated field or getter method first
    * @return the value of the field OR null if the value is null
    * @throws IllegalArgumentException if neither the fieldName or a field with the annotationClass could be found
    */
   public Object getFieldValue(Object object, String fieldName, Class<? extends Annotation> annotationClass) {
      Object value = null;
      Class<?> elementClass = object.getClass();
      if (annotationClass != null) {
         // try to find annotation first
         String annotatedField = getFieldNameForGetterWithAnnotation(elementClass, annotationClass);
         if (annotatedField != null) {
            fieldName = annotatedField;
         }
      }
      value = getFieldValue(object, fieldName);
      return value;
   }

   /**
    * Get the string value of a field or getter method from an object
    * 
    * @param object any object
    * @param fieldName the name of the field (property) to get the value of or the getter method without the "get" and lowercase first char
    * @param annotationClass if the annotation class is set then we will attempt to get the value from the annotated field or getter method first
    * @return the string value of the field OR null if the value is null
    * @throws IllegalArgumentException if neither the fieldName or a field with the annotationClass could be found
    */
   public String getFieldValueAsString(Object object, String fieldName, Class<? extends Annotation> annotationClass) {
      String sValue = null;
      Object value = getFieldValue(object, fieldName, annotationClass);
      if (value != null) {
         sValue = convertUtils.convert(value); //value.toString();
      }
      return sValue;
   }

   /**
    * @param methodName a getter or setter style method name (e.g. getThing, setStuff, isType)
    * @return the fieldName equivalent (thing, stuff, type)
    */
   public String makeFieldNameFromMethod(String methodName) {
      String name = null;
      if (methodName.startsWith(PREFIX_IS)) {
         name = unCapitalize(methodName.substring(2));
      } else if ( methodName.startsWith(PREFIX_GET)
            || methodName.startsWith(PREFIX_SET) ) {
         // set or get
         name = unCapitalize(methodName.substring(3));
      } else {
         name = new String(methodName);
      }
      return name;
   }


   // STATIC methods

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
