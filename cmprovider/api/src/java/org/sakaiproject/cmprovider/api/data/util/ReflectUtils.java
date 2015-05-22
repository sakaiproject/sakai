package org.sakaiproject.cmprovider.api.data.util;

import java.lang.Class;
import java.lang.IllegalAccessException;
import java.lang.RuntimeException;
import java.lang.reflect.Field;

/**
 * @author Christopher Schauer
 */
public class ReflectUtils {
  public static Object getValueFromField(Field field, Object data) {
    if (field == null || data == null)
      throw new IllegalArgumentException("field and data must both be non-null.");

    try {
      return field.get(data);
    } catch (IllegalAccessException ex) {
      throw new RuntimeException("IllegalAccessException for object object: class=" + field.getDeclaringClass().getName() + ", field=" + field.getName());
    }
  }

  public static Field getFieldFromClass(Class c, String fieldName) {
    if (c == null || fieldName == null)
      throw new IllegalArgumentException("c and fieldName must both be non-null");

    try {
      return c.getField(fieldName);
    } catch (NoSuchFieldException ex) {
      throw new RuntimeException("NoSuchFieldException for object: class=" + c.getName() + ", field=" + fieldName);
    }
  }
}
