/**
 * $Id$
 * $URL$
 * ReflectUtil.java - entity-broker - 24 Aug 2007 6:43:14 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl.util;

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtilsBean2;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.commons.beanutils.expression.Resolver;
import org.sakaiproject.entitybroker.impl.util.exceptions.FieldnameNotFoundException;

/**
 * Reflection utilities and utilities related to working with classes and their fields<br/>
 * <br/>
 * These are built on the <a href="http://commons.apache.org/beanutils/">Apache Commons BeanUtils</a>
 * and the nesting structure works the same, refer to the project docs for details<br/>
 * <br/>
 * Getting and setting fields supports simple, nested, indexed, and mapped values<br/>
 * <b>Simple:</b> Get/set a field in a bean (or map), Example: "title", "id"<br/>
 * <b>Nested:</b> Get/set a field in a bean which is contained in another bean, Example: "someBean.title", "someBean.id"<br/>
 * <b>Indexed:</b> Get/set a list/array item by index in a bean, Example: "myList[1]", "anArray[2]"<br/>
 * <b>Mapped:</b> Get/set a map entry by key in a bean, Example: "myMap(key)", "someMap(thing)"<br/>
 * <br/>
 * Includes support for deep cloning, deep copying, and populating objects using auto-conversion<br/>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ReflectUtil {

   private static final String FIELD_CLASS = "class";
   private static final String PREFIX_IS = "is";
   private static final String PREFIX_GET = "get";
   private static final String PREFIX_SET = "set";

   protected Collection<Member> getMembers(Class<?> elementClass) {
      return getPropertyUtils().getMembers(elementClass);
   }

   /**
    * We are using these instead of the static versions so we can manage our own caching
    */
   private PropertyUtilsAZ propertyUtils = new PropertyUtilsAZ();
   public PropertyUtilsAZ getPropertyUtils() {
      return propertyUtils;
   }

   private ConvertUtilsBean2 convertUtils = new ConvertUtilsBean2();
   public ConvertUtilsBean2 getConvertUtils() {
      return convertUtils;
   }

   /* 
    * Code below derived from BeanCloner
    * http://www.coderslog.com/Main_Page
    * Copyright 2007 CodersLog.com 
    *   Licensed under the Apache License, Version 2.0 (the "License");
    */
   private Set<Class<?>> immutableTypes = null;
   private Set<Class<?>> getImmutableTypes() {
      if (immutableTypes == null 
            || immutableTypes.isEmpty()) {
         makeDefaultImmuatableSet();
      }
      return immutableTypes;
   }

   private void makeDefaultImmuatableSet() {
      immutableTypes = new HashSet<Class<?>>();
      immutableTypes.add(BigDecimal.class);
      immutableTypes.add(BigInteger.class);
      immutableTypes.add(Boolean.class);
      immutableTypes.add(Byte.class);
      immutableTypes.add(Character.class);
      immutableTypes.add(Date.class);
      immutableTypes.add(Double.class);
      immutableTypes.add(Float.class);
      immutableTypes.add(Long.class);
      immutableTypes.add(Integer.class);
      immutableTypes.add(String.class);
      immutableTypes.add(Short.class);
      immutableTypes.add(Timestamp.class);
   }
   /* derived code ends */


   /**
    * @param beanClass
    * @return true if this class is a primitive or other simple class (like String or immutable)
    */
   public boolean isClassSimple(Class<?> beanClass) {
      if (beanClass.isPrimitive() 
            || getImmutableTypes().contains(beanClass)) {
         return true;
      }
      return false;
   }

/*****
   protected void analyzeClass(Class<?> elementClass) {
      if (! getClassMemberMap().containsKey(elementClass) ) {
         // class was not yet analyzed
         getClassMemberMap().put(elementClass, new ConcurrentHashMap<String, Member>());

         for (Field field : elementClass.getFields()) {
            try {
               getClassMemberMap().get(elementClass).put(field.getName(), field);
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
                        getClassMemberMap().get(elementClass).put(makeFieldNameFromMethod(method.getName()), method);
                     } catch (Exception e) {
                        // nothing to do here but move on
                     }  
                  }
               }
//             } else if ( name.startsWith(PREFIX_SET) 
//             && paramTypes.length == 1 ) {
//             try {
//             getClassMemberMap().get(elementClass).put(makeFieldNameFromMethod(method.getName()), method);
//             } catch (Exception e) {
//             // nothing to do here but move on
//             }                     
            }
         }
      }
   }
*****/

   /* 
    * Some code below derived from BeanUtilsBean and PropertyUtilsbean
    * http://commons.apache.org/beanutils/
    *   Licensed under the Apache License, Version 2.0 (the "License");
    */

   /**
    * Construct a class of the given type regardless of whether it has a default constructor
    * @param <T>
    * @param beanClass any object class
    * @return the newly constructed object of the given class type
    */
   @SuppressWarnings("unchecked")
   public <T> T constructClass(Class<T> beanClass) {
      T newC = null;
      try {
         newC = (T) beanClass.newInstance();
      } catch (InstantiationException e) {
         // now we will try to use the various constructors
         Constructor[] c = beanClass.getConstructors();
         for (int i = 0; i < c.length; i++) {
            Object[] params = new Object[ c[i].getParameterTypes().length ];
            try {
               newC = (T) c[i].newInstance(params);
               break;
            } catch (Exception e1) {
               // keep trying
            }
         }
      } catch (IllegalAccessException e) {
         throw new IllegalArgumentException("Cannot construct object for class: " + beanClass, e);
      }
      if (newC == null) {
         throw new IllegalArgumentException("Cannot construct object for class: " + beanClass);
      }
      return newC;
   }

   /**
    * @param <T>
    * @param bean
    * @param maxDepth
    * @param fieldNamesToSkip
    * @param currentDepth
    * @return a deep clone of an object
    */
   @SuppressWarnings("unchecked")
   protected <T> T deepClone(T bean, int maxDepth, Set<String> fieldNamesToSkip, int currentDepth) {
      if ( bean == null ) { return null; }
      Class<T> beanClass = (Class<T>) bean.getClass();
      // always copy the simple types if possible
      if (isClassSimple(beanClass)) {
         return bean;
      }
      T copy = null;
      if (currentDepth <= maxDepth) {
         currentDepth++;
         try {
            // create an instance of the object to clone
            if (DynaBean.class.isAssignableFrom(beanClass)) {
               copy = (T) ((DynaBean) bean).getDynaClass().newInstance();
            } else {
               copy = constructClass(beanClass);
            }
            // now do the cloning based on the thing to clone
            if (beanClass.isArray()) {
               // special case, use array reflection
               for (int i = 0; i < Array.getLength(bean); i++) {
                  Object value = Array.get(bean, i);
                  Array.set(copy, i, 
                        deepClone(value, maxDepth, null, currentDepth));
               }
            } else if (Collection.class.isAssignableFrom(beanClass)) {
               // special case, clone everything in the list
               for (Object element : (Collection) bean) {
                  ((Collection) copy).add( deepClone(element, maxDepth, null, currentDepth) );
               }
            } else if (Map.class.isAssignableFrom(beanClass)) {
               // special case, clone everything in the map except keys
               for (Object key : ((Map) bean).keySet()) {
                  if ( fieldNamesToSkip != null
                        && fieldNamesToSkip.contains(key) ) {
                     continue; // skip to next
                  }
                  Object value = ((Map) bean).get(key);
                  ((Map) copy).put(key, 
                        deepClone(value, maxDepth, null, currentDepth) );                     
               }
            } else if (DynaBean.class.isAssignableFrom(beanClass)) {
               // special handling for dynabeans
               DynaProperty origDescriptors[] =
                  ((DynaBean) bean).getDynaClass().getDynaProperties();
               for (DynaProperty dynaProperty : origDescriptors) {
                  String name = dynaProperty.getName();
                  if ( fieldNamesToSkip != null
                        && fieldNamesToSkip.contains(name) ) {
                     continue; // skip to next
                  }
                  try {
                     Object value = ((DynaBean) bean).get(name);
                     setFieldValue(copy, name, 
                           deepClone(value, maxDepth, null, currentDepth) );
                  } catch (IllegalArgumentException e) {
                     // this is ok, field might not be readable so we will not clone it, continue on
                  }
               }
            } else {
               // regular javabean
               PropertyDescriptor origDescriptors[] =
                  getPropertyUtils().getPropertyDescriptors(bean);
               for (int i = 0; i < origDescriptors.length; i++) {
                  String name = origDescriptors[i].getName();
                  if (FIELD_CLASS.equals(name)) {
                     continue; // No point in trying to set an object's class
                  }
                  if ( fieldNamesToSkip != null
                        && fieldNamesToSkip.contains(name) ) {
                     continue; // skip to next
                  }
                  try {
                     Object value = getFieldValue(bean, name);
                     setFieldValue(copy, name, 
                           deepClone(value, maxDepth, null, currentDepth));
                  } catch (Exception e) {
                     // this is ok, field might not be readable so we will not clone it, continue on
                  }
               }
            }
         } catch (Exception e) {
            throw new RuntimeException("Failure during cloning ("+beanClass+") maxDepth="+maxDepth+", currentDepth="+currentDepth+": " + e.getMessage(), e);
         }
      }
      return copy;
   }

   /**
    * @param orig the original object to copy from
    * @param dest the object to copy the values to (must have the same fields with the same types)
    * @param maxDepth the number of objects to follow when traveling through the object and copying
    * the values from it, 0 means to only copy the simple values in the object, any objects will
    * be ignored and will end up as nulls, 1 means to follow the first objects found and copy all
    * of their simple values as well, and so forth
    * @param fieldNamesToSkip the names of fields to skip while cloning this object,
    * this only has an effect on the bottom level of the object, any fields found
    * on child objects will always be copied (if the maxDepth allows)
    * @param ignoreNulls if true then nulls are ot copied and the destination retains the value it has,
    * if false then nulls are copied and the destination value will become a null if the original value is a null
    * @throws IllegalArgumentException if the copy cannot be completed because the objects to copy do not have matching fields or types
    */
   @SuppressWarnings("unchecked")
   protected void deepCopy(Object orig, Object dest, int maxDepth, Set<String> fieldNamesToSkip, boolean ignoreNulls) {
      if (orig == null || dest == null) {
         throw new IllegalArgumentException("original object and destination object must not be null");
      }

      int currentDepth = 1;
      Class<?> origClass = orig.getClass();
      Class<?> destClass = dest.getClass();
      // copy orig to dest
      try {
         if (getImmutableTypes().contains(origClass)) {
            if (getImmutableTypes().contains(destClass)) {
               dest = orig;
            } else {
               throw new IllegalArgumentException("Cannot copy a simple value to a complex object");
            }
         } else {
            if (origClass.isArray()) {
               // special case, copy and overwrite existing array values
               if (destClass.isArray()) {
                  try {
                     for (int i = 0; i < Array.getLength(orig); i++) {
                        Object value = Array.get(orig, i);
                        if (ignoreNulls && value == null) {
                           // don't copy this null over the existing value
                        } else {
                           Array.set(dest, i, 
                                 deepClone(value, maxDepth, null, currentDepth));
                        }
                     }
                  } catch (ArrayIndexOutOfBoundsException e) {
                     // partial copy is ok, continue on
                  }
               } else {
                  throw new IllegalArgumentException("Cannot copy a simple value to a complex object");                  
               }
            } else if (Collection.class.isAssignableFrom(origClass)) {
               // special case, copy everything from orig and add to dest
               for (Object value : (Collection) orig) {
                  if (ignoreNulls && value == null) {
                     // don't copy this null over the existing value
                  } else {
                     ((Collection) dest).add( 
                           deepClone(value, maxDepth, null, currentDepth) );
                  }
               }
            } else if (Map.class.isAssignableFrom(origClass)) {
               // special case clone everything in the map except keys
               for (Object key : ((Map) orig).keySet()) {
                  if ( fieldNamesToSkip != null
                        && fieldNamesToSkip.contains(key) ) {
                     continue; // skip to next
                  }
                  Object value = ((Map) orig).get(key);
                  if (ignoreNulls && value == null) {
                     // don't copy this null over the existing value
                  } else {
                     ((Map) dest).put(key, 
                           deepClone(value, maxDepth, null, currentDepth) );
                  }
               }
            } else if (DynaBean.class.isAssignableFrom(origClass)) {
               DynaProperty origDescriptors[] =
                  ((DynaBean) orig).getDynaClass().getDynaProperties();
               for (DynaProperty dynaProperty : origDescriptors) {
                  String name = dynaProperty.getName();
                  if ( fieldNamesToSkip != null
                        && fieldNamesToSkip.contains(name) ) {
                     continue; // skip to next
                  }
                  try {
                     Object value = ((DynaBean) orig).get(name);
                     if (ignoreNulls && value == null) {
                        // don't copy this null over the existing value
                     } else {
                        setFieldValue(dest, name, 
                              deepClone(value, maxDepth, null, currentDepth) );
                     }
                  } catch (FieldnameNotFoundException e) {
                     // it is ok for the objects to not be the same
                  } catch (IllegalArgumentException e) {
                     // it is ok for the objects to not be the same
                  }
               }
            } else {
               // regular javabean
               PropertyDescriptor origDescriptors[] =
                  getPropertyUtils().getPropertyDescriptors(orig);
               for (int i = 0; i < origDescriptors.length; i++) {
                  String name = origDescriptors[i].getName();
                  if (FIELD_CLASS.equals(name)) {
                     continue; // No point in trying to set an object's class
                  }
                  if ( fieldNamesToSkip != null
                        && fieldNamesToSkip.contains(name) ) {
                     continue; // skip to next
                  }
                  try {
                     Object value = getFieldValue(orig, name);
                     if (ignoreNulls && value == null) {
                        // don't copy this null over the existing value
                     } else {
                        setFieldValue(dest, name, 
                              deepClone(value, maxDepth, null, currentDepth));
                     }
                  } catch (Exception e) {
                     // this is ok, we will continue the copy
                  }
               }
            }
         }
      } catch (Exception e) {
         throw new IllegalArgumentException("Failure copying " + orig + " ("+origClass+") to " 
               + dest + " ("+destClass+")" + e.getMessage(), e);
      }
   }

   private Set<String> makeSetFromArray(String[] fieldNamesToSkip) {
      Set<String> skip = new HashSet<String>();
      if (fieldNamesToSkip != null) {
         for (int i = 0; i < fieldNamesToSkip.length; i++) {
            if (fieldNamesToSkip[i] != null) {
               skip.add(fieldNamesToSkip[i]);
            }
         }
      }
      return skip;
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
    * Get the value of a field or getter method from an object<br/>
    * Getting fields supports simple, nested, indexed, and mapped values:<br/>
    * <b>Simple:</b> Get/set a field in a bean (or map), Example: "title", "id"<br/>
    * <b>Nested:</b> Get/set a field in a bean which is contained in another bean, Example: "someBean.title", "someBean.id"<br/>
    * <b>Indexed:</b> Get/set a list/array item by index in a bean, Example: "myList[1]", "anArray[2]"<br/>
    * <b>Mapped:</b> Get/set a map entry by key in a bean, Example: "myMap(key)", "someMap(thing)"<br/>
    * 
    * @param object any object
    * @param fieldName the name of the field (property) to get the value of or the getter method without the "get" and lowercase first char
    * @throws FieldnameNotFoundException if this fieldName does not exist on the object
    * @throws IllegalArgumentException if a failure occurs while getting the field value
    */
   public Object getFieldValue(Object object, String fieldName) {
      Object value = null;
      try {
         value = getPropertyUtils().getProperty(object, fieldName);
      } catch (NoSuchMethodException e) {
         Class<?> elementClass = object.getClass();
         Field f = getPropertyUtils().getFieldMap(elementClass).get(fieldName);
         if (f != null) {
            try {
               value = f.get(object);
            } catch (Exception e1) {
               throw new IllegalArgumentException("Could not set field with name (" + fieldName + ") to value ("+value+"): " + e1.getMessage(), e1);
            }
         } else {
            throw new FieldnameNotFoundException(fieldName, e);
         }
      } catch (IllegalAccessException e) {
         throw new IllegalArgumentException("Could not get value from a field with name (" + fieldName + "): " + e.getMessage(), e);
      } catch (InvocationTargetException e) {
         throw new IllegalArgumentException("Could not get value from a field with name (" + fieldName + "): " + e.getMessage(), e);
      }
      return value;
   }

   /**
    * Get the value of a field or getter method from an object allowing an annotation to override<br/>
    * Getting fields supports simple, nested, indexed, and mapped values:<br/>
    * <b>Simple:</b> Get/set a field in a bean (or map), Example: "title", "id"<br/>
    * <b>Nested:</b> Get/set a field in a bean which is contained in another bean, Example: "someBean.title", "someBean.id"<br/>
    * <b>Indexed:</b> Get/set a list/array item by index in a bean, Example: "myList[1]", "anArray[2]"<br/>
    * <b>Mapped:</b> Get/set a map entry by key in a bean, Example: "myMap(key)", "someMap(thing)"<br/>
    * 
    * @param object any object
    * @param fieldName the name of the field (property) to get the value of or the getter method without the "get" and lowercase first char
    * @param annotationClass if the annotation class is set then we will attempt to get the value from the annotated field or getter method first
    * @return the value of the field OR null if the value is null
    * @throws FieldnameNotFoundException if this fieldName does not exist on the object and no annotation was found
    * @throws IllegalArgumentException if a failure occurs while getting the field value
    */
   public Object getFieldValue(Object object, String fieldName, Class<? extends Annotation> annotationClass) {
      Object value = null;
      Class<?> elementClass = object.getClass();
      if (annotationClass != null) {
         // try to find annotation first
         String annotatedField = getFieldNameWithAnnotation(elementClass, annotationClass);
         if (annotatedField != null) {
            fieldName = annotatedField;
         }
      }
      value = getFieldValue(object, fieldName);
      return value;
   }

   /**
    * Get the string value of a field or getter method from an object allowing an annotation to override<br/>
    * Getting fields supports simple, nested, indexed, and mapped values:<br/>
    * <b>Simple:</b> Get/set a field in a bean (or map), Example: "title", "id"<br/>
    * <b>Nested:</b> Get/set a field in a bean which is contained in another bean, Example: "someBean.title", "someBean.id"<br/>
    * <b>Indexed:</b> Get/set a list/array item by index in a bean, Example: "myList[1]", "anArray[2]"<br/>
    * <b>Mapped:</b> Get/set a map entry by key in a bean, Example: "myMap(key)", "someMap(thing)"<br/>
    * 
    * @param object any object
    * @param fieldName the name of the field (property) to get the value of or the getter method without the "get" and lowercase first char
    * @param annotationClass if the annotation class is set then we will attempt to get the value from the annotated field or getter method first
    * @return the string value of the field OR null if the value is null
    * @throws FieldnameNotFoundException if this fieldName does not exist on the object and no annotation was found
    * @throws IllegalArgumentException if a failure occurs while getting the field value
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
    * Set the value on the object field or setter method<br/>
    * Setting fields supports simple, nested, indexed, and mapped values:<br/>
    * <b>Simple:</b> Get/set a field in a bean (or map), Example: "title", "id"<br/>
    * <b>Nested:</b> Get/set a field in a bean which is contained in another bean, Example: "someBean.title", "someBean.id"<br/>
    * <b>Indexed:</b> Get/set a list/array item by index in a bean, Example: "myList[1]", "anArray[2]"<br/>
    * <b>Mapped:</b> Get/set a map entry by key in a bean, Example: "myMap(key)", "someMap(thing)"<br/>
    * 
    * @param object any object
    * @param fieldName the name of the field (property) to set the value of or the setter method without the "set" and lowercase first char
    * @param value the value to set on this field, must match the type in the object (will not attempt to covert)
    * @throws FieldnameNotFoundException if the fieldName could not be found in this object
    * @throws IllegalArgumentException if the value type does not match the field type or other failures occur setting the value
    */
   public void setFieldValue(Object object, String fieldName, Object value) {
      try {
         getPropertyUtils().setProperty(object, fieldName, value);
      } catch (NoSuchMethodException e) {
         Class<?> elementClass = object.getClass();
         Field f = getPropertyUtils().getFieldMap(elementClass).get(fieldName);
         if (f != null) {
            try {
               f.set(object, value);
            } catch (Exception e1) {
               throw new IllegalArgumentException("Could not set field with name (" + fieldName + ") to value ("+value+"): " + e1.getMessage(), e1);
            }
         } else {
            throw new FieldnameNotFoundException(fieldName, e);
         }
      } catch (IllegalAccessException e) {
         throw new IllegalArgumentException("Could not set field with name (" + fieldName + ") to value ("+value+"): " + e.getMessage(), e);
      } catch (InvocationTargetException e) {
         throw new IllegalArgumentException("Could not set field with name (" + fieldName + ") to value ("+value+"): " + e.getMessage(), e);
      }
   }

   /**
    * Set the value on the object field or setter method<br/>
    * Setting fields supports simple, nested, indexed, and mapped values:<br/>
    * <b>Simple:</b> Get/set a field in a bean (or map), Example: "title", "id"<br/>
    * <b>Nested:</b> Get/set a field in a bean which is contained in another bean, Example: "someBean.title", "someBean.id"<br/>
    * <b>Indexed:</b> Get/set a list/array item by index in a bean, Example: "myList[1]", "anArray[2]"<br/>
    * <b>Mapped:</b> Get/set a map entry by key in a bean, Example: "myMap(key)", "someMap(thing)"<br/>
    * 
    * @param object any object
    * @param fieldName the name of the field (property) to set the value of or the setter method without the "set" and lowercase first char
    * @param value the value to set on this field as a string, will attempt to auto-convert from string to the proper type
    * @throws FieldnameNotFoundException if the fieldName could not be found in this object
    * @throws IllegalArgumentException if the value string could not be converted to a type that matches the field
    */
   public void setFieldStringValue(Object object, String fieldName, String value) {
      Class<?> elementClass = object.getClass();
      Class<?> type = getFieldType(elementClass, fieldName);
      Object convertedValue;
      try {
         convertedValue = getConvertUtils().convert(value, type);
      } catch (ConversionException e) {
         throw new IllegalArgumentException("Could not convert value from ("+value+") to an object of type ("+type+")");
      }
      setFieldValue(object, fieldName, convertedValue);
   }

   /**
    * Sets a value on the field of an object and will attempt to convert the property if it does not match<br/>
    * Setting fields supports simple, nested, indexed, and mapped values:<br/>
    * <b>Simple:</b> Get/set a field in a bean (or map), Example: "title", "id"<br/>
    * <b>Nested:</b> Get/set a field in a bean which is contained in another bean, Example: "someBean.title", "someBean.id"<br/>
    * <b>Indexed:</b> Get/set a list/array item by index in a bean, Example: "myList[1]", "anArray[2]"<br/>
    * <b>Mapped:</b> Get/set a map entry by key in a bean, Example: "myMap(key)", "someMap(thing)"<br/>
    * 
    * @param object any object
    * @param fieldName the name of the field (property) to set the value of or the setter method without the "set" and lowercase first char
    * @param value the value to set on this field, does not have to match the type in the object,
    * the type will be determined and then we will attempt to convert this value to the type in the field
    * @throws FieldnameNotFoundException if the fieldName could not be found in this object
    * @throws IllegalArgumentException if the value type could not be converted to the field type
    */
   public void setFieldValueWithConversion(Object object, String fieldName, Object value) {
      // This is basically BeanUtils.setProperty with different exception handling and minor tweaks
      // Resolve any nested expression to get the actual target bean
      Object target = object;
      Resolver resolver = getPropertyUtils().getResolver();
      while (resolver.hasNested(fieldName)) {
         try {
            target = getPropertyUtils().getProperty(target, resolver.next(fieldName));
            fieldName = resolver.remove(fieldName);
         } catch (NoSuchMethodException e) {
            throw new FieldnameNotFoundException("Could not find this fieldName ("+fieldName+") on the target object: " + object, fieldName, e);
         } catch (Exception e) {
            throw new IllegalArgumentException("Failure getting resolver for fieldName ("+fieldName+") on the target object: " + object, e);
         }
      }

      // Declare local variables we will require
      String propName = resolver.getProperty(fieldName); // Simple name of target property
      Class<?> type = null;                              // Java type of target property
      int index  = resolver.getIndex(fieldName);         // Indexed subscript value (if any)
      String key = resolver.getKey(fieldName);           // Mapped key value (if any)

      // Calculate the property type
      if (target instanceof DynaBean) {
         DynaClass dynaClass = ((DynaBean) target).getDynaClass();
         DynaProperty dynaProperty = dynaClass.getDynaProperty(propName);
         if (dynaProperty == null) {
            throw new FieldnameNotFoundException("Could not find this fieldName ("+fieldName+") on the target object: " + object, fieldName, null);
         }
         type = dynaProperty.getType();
      } else {
         // normal object
         PropertyDescriptor descriptor = null;
         try {
            descriptor = getPropertyUtils().getPropertyDescriptor(target, fieldName);
            if (descriptor == null) {
               throw new IllegalArgumentException("Could not find descriptor for this fieldName ("+fieldName+") on the target object: " + object);
            }
         } catch (NoSuchMethodException e) {
            throw new FieldnameNotFoundException("Could not find this fieldName ("+fieldName+") on the target object: " + object, fieldName, e);
         } catch (Exception e) {
            throw new IllegalArgumentException("Failure getting descriptor for fieldName ("+fieldName+") on the target object: " + object, e);
         }
         if (descriptor instanceof MappedPropertyDescriptor) {
            if (((MappedPropertyDescriptor) descriptor).getMappedWriteMethod() == null) {
               throw new IllegalArgumentException("Could not write to this mapped field ("+fieldName+") on the target object: " + object);
            }
            type = ((MappedPropertyDescriptor) descriptor).getMappedPropertyType();
         } else if (index >= 0 && descriptor instanceof IndexedPropertyDescriptor) {
            if (((IndexedPropertyDescriptor) descriptor).getIndexedWriteMethod() == null) {
               throw new IllegalArgumentException("Could not write to this indexed field ("+fieldName+") on the target object: " + object);
            }
            type = ((IndexedPropertyDescriptor) descriptor).getIndexedPropertyType();
         } else if (key != null) {
            if (descriptor.getReadMethod() == null) {
               throw new IllegalArgumentException("Could not read map key for this field ("+fieldName+") on the target object: " + object);
            }
            type = (value == null) ? Object.class : value.getClass();
         } else {
            if (descriptor.getWriteMethod() == null) {
               throw new IllegalArgumentException("Could not write to this field ("+fieldName+") on the target object: " + object);
            }
            type = descriptor.getPropertyType();
         }
      }

      // Convert the specified value to the required type,
      // special handling for strings and string arrays
      Object newValue = null;
      if (type.isArray() && (index < 0)) {
         // Scalar value into array
         if (value == null) {
            String[] values = new String[1];
            values[0] = (String) value;
            newValue = getConvertUtils().convert((String[]) values, type);
         } else if (value instanceof String) {
            newValue = getConvertUtils().convert(value, type);
         } else if (value instanceof String[]) {
            newValue = getConvertUtils().convert((String[]) value, type);
         } else {
            newValue = getConvertUtils().convert(value, type);
         }
      } else if (type.isArray()) {
         // Indexed value into array
         if (value instanceof String || value == null) {
            newValue = getConvertUtils().convert((String) value,
                  type.getComponentType());
         } else if (value instanceof String[]) {
            newValue = getConvertUtils().convert(((String[]) value)[0],
                  type.getComponentType());
         } else {
            newValue = getConvertUtils().convert(value, type.getComponentType());
         }
      } else {
         // Value into scalar
         if ((value instanceof String) || (value == null)) {
            newValue = getConvertUtils().convert((String) value, type);
         } else if (value instanceof String[]) {
            newValue = getConvertUtils().convert(((String[]) value)[0], type);
         } else {
            newValue = getConvertUtils().convert(value, type);
         }
      }

      // Invoke the setter method
      try {
         if (index >= 0) {
            getPropertyUtils().setIndexedProperty(target, propName, index, newValue);
         } else if (key != null) {
            getPropertyUtils().setMappedProperty(target, propName, key, newValue);
         } else {
            getPropertyUtils().setProperty(target, propName, newValue);
         }
      } catch (NoSuchMethodException e) {
         throw new FieldnameNotFoundException("Could not find this fieldName ("+fieldName+") on the target object: " + object, fieldName, e);
      } catch (Exception e) {
         throw new IllegalArgumentException("Cannot set field (" + propName + ") to " +
               "type ["+type.getSimpleName()+"] on target object("+target+"): " + e.getMessage(), e);
      }
   }

   /**
    * Get the types of the fields of a specific class type
    * returns the method names without the "get"/"is" part and camelCased
    * @param elementClass any class
    * @return a map of field name -> class type
    */
   public Map<String, Class<?>> getFieldTypes(Class<?> elementClass) {
      Map<String, Class<?>> types = new HashMap<String, Class<?>>();
      PropertyDescriptor[] pds = getPropertyUtils().getPropertyDescriptors(elementClass);
      for (int i = 0; i < pds.length; i++) {
         PropertyDescriptor pd = pds[i];
         if (FIELD_CLASS.equals(pd.getName())) { continue; } // skip the getClass method
         types.put(pd.getName(), pd.getPropertyType());
      }
      // now add in field types which are not already in the map
      for (Field field : getPropertyUtils().getFields(elementClass)) {
         String fieldName = field.getName();
         if (! types.containsKey(fieldName)) {
            types.put(fieldName, field.getType());
         }
      }
      return types;
   }

   /**
    * Get the type of a field from a class
    * @param elementClass any class
    * @param fieldName the name of the field (property) or a getter method converted to a fieldname
    * @return the type of object stored in the field
    * @throws FieldnameNotFoundException if the fieldName could not be found in this object
    */
   public Class<?> getFieldType(Class<?> elementClass, String fieldName) {
      Class<?> type = null;
      PropertyDescriptor[] pds = getPropertyUtils().getPropertyDescriptors(elementClass);
      for (int i = 0; i < pds.length; i++) {
         PropertyDescriptor pd = pds[i];
         if ( pdNameCompare(pd.getName(), fieldName) ) {
            type = pd.getPropertyType();
            break;
         }
      }
      if (type == null) {
         Field f = getPropertyUtils().getFieldMap(elementClass).get(fieldName);
         if (f != null) {
            type = f.getType();
         }
      }
      if (type == null) {
         throw new FieldnameNotFoundException("Could not find this fieldName ("+fieldName+") in elementClass: " 
               + elementClass.getName(), fieldName, null);
      }
      return type;
   }

   /**
    * Get a map of all fieldName -> value and all getterMethodName -> value without the word "get"
    * where the method takes no arguments, in other words, all values available from an object
    * @param object any object
    * @return a map of name -> value
    * @throws IllegalArgumentException if failures occur
    */
   @SuppressWarnings("unchecked")
   public Map<String, Object> getObjectValues(Object object) {
      Map<String, Object> values;
      try {
         values = getPropertyUtils().describe(object);
      } catch (Exception e) {
         throw new IllegalArgumentException("Could not access the object values for ("+object+"): " + e.getMessage(), e);
      }
      values.remove(FIELD_CLASS); // remove the class property
      return values;
   }

   /**
    * Find the getter field on a class which has the given annotation
    * @param elementClass any class
    * @param annotationClass the annotation type which is expected to be on the field
    * @return the name of the field or null if no fields are found with the indicated annotation
    * @throws IllegalArgumentException if the annotation class is null
    */
   public String getFieldNameWithAnnotation(Class<?> elementClass, Class<? extends Annotation> annotationClass) {
      String fieldName = null;
      if (annotationClass == null) {
         throw new IllegalArgumentException("the annotationClass must not be null");
      }
      Collection<Member> members = getMembers(elementClass);
      for (Member member : members) {
         fieldName = getAnnotatedFieldNameFromMember(member, annotationClass);
         if (fieldName != null) {
            break;
         }
      }
      return fieldName;
   }

   /**
    * Deep clone an object and all the values in it into a brand new object of the same type,
    * this will traverse the bean and will make new objects for all non-null values contained in the object
    * 
    * @param <T>
    * @param object any java object, this can be a list, map, array, or any simple
    * object, it does not have to be a custom object or even a java bean,
    * also works with DynaBeans
    * @param maxDepth the number of objects to follow when traveling through the object and copying
    * the values from it, 0 means to only copy the simple values in the object, any objects will
    * be ignored and will end up as nulls, 1 means to follow the first objects found and copy all
    * of their simple values as well, and so forth
    * @param fieldNamesToSkip the names of fields to skip while cloning this object,
    * this only has an effect on the bottom level of the object, any fields found
    * on child objects will always be copied (if the maxDepth allows)
    * @return the cloned object
    */
   public <T> T clone(T object, int maxDepth, String[] fieldNamesToSkip) {
      Set<String> skip = makeSetFromArray(fieldNamesToSkip);
      return deepClone(object, maxDepth, skip, 0);
   }

   /**
    * Deep copies one object into another, this is primarily for copying between identical types of objects but
    * it can also handle copying between objects which are quite different, 
    * this does not just do a reference copy of the values but actually creates new objects in the current classloader
    * 
    * @param orig the original object to copy from
    * @param dest the object to copy the values to (must have the same fields with the same types)
    * @param maxDepth the number of objects to follow when traveling through the object and copying
    * the values from it, 0 means to only copy the simple values in the object, any objects will
    * be ignored and will end up as nulls, 1 means to follow the first objects found and copy all
    * of their simple values as well, and so forth
    * @param fieldNamesToSkip the names of fields to skip while cloning this object,
    * this only has an effect on the bottom level of the object, any fields found
    * on child objects will always be copied (if the maxDepth allows)
    * @param ignoreNulls if true then nulls are not copied and the destination retains the value it has,
    * if false then nulls are copied and the destination value will become a null if the original value is a null
    * @throws IllegalArgumentException if the copy cannot be completed because the objects to copy do not have matching fields or types
    */
   public void copy(Object orig, Object dest, int maxDepth, String[] fieldNamesToSkip, boolean ignoreNulls) {
      Set<String> skip = makeSetFromArray(fieldNamesToSkip);
      deepCopy(orig, dest, maxDepth, skip, ignoreNulls);
   }

   /**
    * Populates an object with the values in the properties map,
    * this will not fail if the fieldName in the map is not a property on the
    * object or the fieldName cannot be written to with the value in the object.
    * This will attempt to convert the provided object values into the right values
    * to place in the object<br/>
    * <b>NOTE:</b> simple types like numbers and strings can almost always be converted from
    * just about anything though they will probably end up as 0 or ""<br/>
    * Setting fields supports simple, nested, indexed, and mapped values:<br/>
    * <b>Simple:</b> Get/set a field in a bean (or map), Example: "title", "id"<br/>
    * <b>Nested:</b> Get/set a field in a bean which is contained in another bean, Example: "someBean.title", "someBean.id"<br/>
    * <b>Indexed:</b> Get/set a list/array item by index in a bean, Example: "myList[1]", "anArray[2]"<br/>
    * <b>Mapped:</b> Get/set a map entry by key in a bean, Example: "myMap(key)", "someMap(thing)"<br/>
    * 
    * @param object any object
    * @param properties a map of fieldNames -> Object
    * @return the list of fieldNames which were successfully written to the object
    * @throws IllegalArgumentException if the arguments are invalid
    */
   public List<String> populate(Object object, Map<String, Object> properties) {
      if (object == null || properties == null) {
         throw new IllegalArgumentException("object and properties cannot be null");
      }
      List<String> writtenFields = new ArrayList<String>();
      for (String fieldName : properties.keySet()) {
         if (fieldName != null && ! "".equals(fieldName)) {
            Object value = properties.get(fieldName);
            try {
               // this should write the value or fail
               setFieldValueWithConversion(object, fieldName, value);
               writtenFields.add(fieldName); // record that the value was written successfully
            } catch (Exception e) {
               // this is OK, it just means we did not write the value
            }
         }
      }
      return writtenFields;
   }

   /**
    * Populates an object with the String array values in the params map,
    * this will not fail if the fieldName in the map is not a property on the
    * object or the fieldName cannot be written to with the value in the object<br/>
    * Setting fields supports simple, nested, indexed, and mapped values:<br/>
    * <b>Simple:</b> Get/set a field in a bean (or map), Example: "title", "id"<br/>
    * <b>Nested:</b> Get/set a field in a bean which is contained in another bean, Example: "someBean.title", "someBean.id"<br/>
    * <b>Indexed:</b> Get/set a list/array item by index in a bean, Example: "myList[1]", "anArray[2]"<br/>
    * <b>Mapped:</b> Get/set a map entry by key in a bean, Example: "myMap(key)", "someMap(thing)"<br/>
    * 
    * @param object any object
    * @param params a map of fieldNames -> String[] (normally from an http request)
    * @return the list of fieldNames which were successfully written to the object
    * @throws IllegalArgumentException if the arguments are invalid
    */
   public List<String> populateFromParams(Object object, Map<String, String[]> params) {
      if (object == null || params == null) {
         throw new IllegalArgumentException("object and params cannot be null");
      }
      List<String> writtenFields = new ArrayList<String>();
      for (String fieldName : params.keySet()) {
         if (fieldName != null && ! "".equals(fieldName)) {
            String[] value = params.get(fieldName);
            try {
               // this should write the value or fail
               if (value != null && value.length == 1) {
                  setFieldValueWithConversion(object, fieldName, value[0]);                  
               } else {
                  setFieldValueWithConversion(object, fieldName, value);
               }
               writtenFields.add(fieldName); // record that the value was written successfully
            } catch (Exception e) {
               // this is OK, it just means we did not write the value
            }
         }
      }
      return writtenFields;
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
    *  - Antranig Basman (antranig@caret.cam.ac.uk)
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
    *  - Antranig Basman (antranig@caret.cam.ac.uk)
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

   /**
    * @param text string to make MD5 hash from
    * @param maxLength
    * @return an MD5 hash no longer than maxLength
    */
   public static String makeMD5(String text, int maxLength) {

      MessageDigest md;
      try {
         md = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException e) {
         throw new RuntimeException("Stupid java sucks for MD5", e);
      }
      md.update(text.getBytes());

      // convert the binary md5 hash into hex
      String md5 = "";
      byte[] b_arr = md.digest();

      for (int i = 0; i < b_arr.length; i++) {
         // convert the high nibble
         byte b = b_arr[i];
         b >>>= 4;
      b &= 0x0f; // this clears the top half of the byte
      md5 += Integer.toHexString(b);

      // convert the low nibble
      b = b_arr[i];
      b &= 0x0F;
      md5 += Integer.toHexString(b);
      }
      if (maxLength > 0 && md5.length() > maxLength) {
         md5 = md5.substring(0, maxLength);
      }
      return md5;
   }

   /**
    * This is necessary because of the stupid capitalization rules used by Sun
    * @param name a property descriptor name to compare to a fieldname
    * @param fieldName a fieldName
    * @return true if they are almost the same or actually the same
    */
   public static boolean pdNameCompare(String name, String fieldName) {
      // AZ try tweaking the name slightly (uncap the first char) and try again, this fixes the sThing to SThing issue
      boolean match = false;
      if (name == null || fieldName == null) {
         match = false;
      } else if (name.length() == 0 || fieldName.length() == 0) {
         match = false;
      } else if (name.equals(fieldName)) {
         match = true;
      } else if (ReflectUtil.unCapitalize(name).equals(fieldName)) {
         match = true;
      } else if (ReflectUtil.capitalize(name).equals(fieldName)) {
         match = true;
      }
      return match;
   }

}
