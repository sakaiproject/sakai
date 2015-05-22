package org.sakaiproject.cmprovider;

import java.lang.reflect.Field;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.cmprovider.api.data.annotations.After;
import org.sakaiproject.cmprovider.api.data.annotations.NotEmpty;
import org.sakaiproject.cmprovider.api.data.annotations.NotNull;
import org.sakaiproject.cmprovider.api.data.util.DateUtils;
import org.sakaiproject.cmprovider.api.data.util.ReflectUtils;

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
    this.fieldValue = ReflectUtils.getValueFromField(field, data);
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

    Field otherField = ReflectUtils.getFieldFromClass(data.getClass(), after.field());
    Date date1 = DateUtils.getDateFromField(field, data);
    Date date2 = DateUtils.getDateFromField(otherField, data);

    if (date1 == null && date2 == null) return true;
    
    return compareDateFields(date1, date2) > 0;
  }

  private int compareDateFields(Date date1, Date date2) {

    if (date1 == null || date2 == null)
      throw new IllegalArgumentException("Invalid " + data.getClass().getName() + ": " + field.getName() + " and " + field.getName() + " must both be null or both be non-null");

    return date1.compareTo(date2);
  }
}
