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
}
