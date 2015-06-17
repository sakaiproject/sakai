package org.sakaiproject.cmprovider.data.validation;

import java.lang.reflect.Field;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.cmprovider.data.DateUtils;

/**
 * Validates fields in a request data object. If the fields are invalid, an IllegalArgumentException
 * will be returned, resulting in a 400 being sent to the client.
 *
 * @author Christopher Schauer
 */
public class FieldValidator {
  private Object fieldValue;
  private Field field;
  private Object data;

  private static final Log LOG = LogFactory.getLog(FieldValidator.class);

  public FieldValidator(Object data, Field field) {
    this.data = data;
    this.field = field;
    this.fieldValue = getValueFromField(field, data);
  }

  public void validate() {
    if (field.isAnnotationPresent(NotNull.class) && !validateNotNull())
      throw new IllegalArgumentException("Invalid " + data.getClass().getName() + ": " + field.getName() + " cannot be null");

    if (field.isAnnotationPresent(NotEmpty.class) && !validateNotEmpty())
      throw new IllegalArgumentException("Invalid " + data.getClass().getName() + ": " + field.getName() + " cannot be empty");

    if (field.isAnnotationPresent(After.class)) {
      After after = (After)field.getAnnotation(After.class);
      if (!validateAfter(after))
        throw new IllegalArgumentException("Invalid " + data.getClass().getName() + ": " + field.getName() + " must be after " + after.field());
    }
  }

  private boolean validateNotNull() {
    return fieldValue != null;
  }

  private boolean validateNotEmpty() {
    if (!(fieldValue instanceof String)) {
      LOG.warn("Skipping NotEmpty Annotation. NotEmpty should only be used on a field of type String.");
      return true;
    }

    return !StringUtils.isEmpty((String)fieldValue);
  }

  private boolean validateAfter(After after) {
    if (!(fieldValue instanceof String || fieldValue instanceof Date)) {
        LOG.warn("Skipping After Annotation. After should only be used on a field of type Date or String.");
        return true;
    }

    Field otherField = getFieldFromClass(data.getClass(), after.field());
    Date date1 = getDateFromField(field, data);
    Date date2 = getDateFromField(otherField, data);

    if (date1 == null && date2 == null) return true;
    
    return compareDateFields(date1, date2) > 0;
  }

  private int compareDateFields(Date date1, Date date2) {

    if (date1 == null || date2 == null)
      throw new IllegalArgumentException("Invalid " + data.getClass().getName() + ": " + field.getName() + " and " + field.getName() + " must both be null or both be non-null");

    return date1.compareTo(date2);
  }

  private Object getValueFromField(Field field, Object data) {
    if (field == null || data == null)
      throw new IllegalArgumentException("field and data must both be non-null.");

    try {
      return field.get(data);
    } catch (IllegalAccessException ex) {
      throw new RuntimeException("IllegalAccessException for object object: class=" + field.getDeclaringClass().getName() + ", field=" + field.getName());
    }
  }

  private Field getFieldFromClass(Class c, String fieldName) {
    if (c == null || fieldName == null)
      throw new IllegalArgumentException("c and fieldName must both be non-null");

    try {
      return c.getField(fieldName);
    } catch (NoSuchFieldException ex) {
      throw new RuntimeException("NoSuchFieldException for object: class=" + c.getName() + ", field=" + fieldName);
    }
  }

  /**
   * Gets a Date from a field on an object. If the field is of type date then the value
   * is simply returned. If the field is of type String and the proper format it will
   * be converted to a Date and returned.
   */
  private Date getDateFromField(Field field, Object object) {
    if (object == null) return null;
    if (field == null) throw new IllegalArgumentException("field cannot be null");

    Object fieldValue = getValueFromField(field, object);

    if (fieldValue instanceof Date) return (Date)fieldValue;

    if (fieldValue instanceof String) {
      String dateStr = (String)fieldValue;
      if (dateStr.equals("")) return null;
      Date date = DateUtils.stringToDate(dateStr);
      if (date != null) return date;
    }

    throw new IllegalArgumentException("Field " + field.getName() + " must be a Date or String of the form yyyy-MM-dd");
  }
}
