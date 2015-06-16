package org.sakaiproject.cmprovider.data;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Christopher Schauer
 */
public class DateUtils {
  private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * Converts a String with format DateUtils.DATE_FORMAT to a Date.
   * Returns null if the parse fails.
   */
  public static Date stringToDate(String dateStr) {
    if (dateStr == null) return null;

    try {
      return DATE_FORMAT.parse(dateStr);
    } catch (ParseException ex) {
      return null;
    }
  }

  /**
   * Converts a Date to a String with format DateUtils.DATE_FORMAT.
   */
  public static String dateToString(Date date) {
    return date == null ? null : DATE_FORMAT.format(date);
  }

  /**
   * Gets a Date from a field on an object. If the field is of type date then the value
   * is simply returned. If the field is of type String and the proper format it will
   * be converted to a Date and returned.
   */
  public static Date getDateFromField(Field field, Object object) {
    if (object == null) return null;
    if (field == null) throw new IllegalArgumentException("field cannot be null");

    Object fieldValue = ReflectUtils.getValueFromField(field, object);

    if (fieldValue instanceof Date) return (Date)fieldValue;

    if (fieldValue instanceof String) {
      String dateStr = (String)fieldValue;
      if (dateStr.equals("")) return null;
      Date date = stringToDate(dateStr);
      if (date != null) return date;
    }

    throw new IllegalArgumentException("Field " + field.getName() + " must be a Date or String of the form yyyy-MM-dd");
  }
}
