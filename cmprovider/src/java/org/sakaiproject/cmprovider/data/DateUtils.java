/**
 * Copyright (c) 2015 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
