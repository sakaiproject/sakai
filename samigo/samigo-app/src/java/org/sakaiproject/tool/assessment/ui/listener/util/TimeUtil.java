/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.ResourceLoader;
/**
 * <p>Description: Time conversion utility class</p>
 */
@Slf4j
public class TimeUtil 
{
  private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";
  private static DateTimeFormatter dtf = DateTimeFormat.forPattern(ISO_8601_DATE_FORMAT);

  private TimeZone m_client_timezone= null;
  private TimeZone m_server_timezone= null;

  public TimeUtil() {
    m_client_timezone= TimeService.getLocalTimeZone();
    m_server_timezone= TimeZone.getDefault();
  }


  /**
  * Convert a Date representation of date and time in the server TimeZone 
  * to String representation of date and time  in client TimeZone
  * used for display. 
  * tz1 is the client timezone,  tz2 is the server timezone
  */

  private String convertFromServerDateToTimeZone2String(SimpleDateFormat ndf, Date tz2Date, TimeZone tz1) {
    Calendar cal1= new GregorianCalendar(tz1);
    ndf.setCalendar(cal1);
    String clientStr= ndf.format(tz2Date);

    return clientStr;
  }

  /*
   * This will return a formatted date/time with or without adjustment for client time zone.
   * If instructor is located in Michigan and teaches on Sakai based in Chicago, 
   * the date should stay stable in the server timezone when using date/time picker. Previous 
   * behavior meant the date would be constantly manipulated by client timezone because of the
   * convertFromServerDateToTimeZone2String manipulation below.
   */
  public String getDisplayDateTime(SimpleDateFormat ndf, Date serverDate, boolean manipulateTimezoneForClient) {
     //we can't format a null date
    if (serverDate == null) {
      return "";
    }
    
    try {
      if (manipulateTimezoneForClient && m_client_timezone !=null && m_server_timezone!=null && !m_client_timezone.hasSameRules(m_server_timezone)) {
        String sdf = ndf.toPattern();
        // If we are going to manipulate the timezone for client browser, let's be clear and show user the timezone.
        if (StringUtils.containsNone(sdf, "zZ")) {
          ndf = new SimpleDateFormat(sdf + " z");
        }
        return convertFromServerDateToTimeZone2String (ndf, serverDate, m_client_timezone);
      }
      else {
        return ndf.format(serverDate);
      }
    }
    catch (RuntimeException e){
      log.warn("can not format the Date to a string", e);
    }
    return "";
  }

  /*
   * SAM-2323: this is useful for simple sorting by jQuery tablesorter plugin
   * In USA, I expect a date like 2015-02-15 04:00pm
   * In Sweden, I expect a date like 2015-02-15 16:00
   */
  public String getIsoDateWithLocalTime(Date dateToConvert) {
      if (dateToConvert == null) {
          return null;
      }
      DateTime dt = new DateTime(dateToConvert);
      DateTimeFormatter fmt = ISODateTimeFormat.yearMonthDay();
      DateTimeFormatter localFmt = fmt.withLocale(new ResourceLoader().getLocale());
      DateTimeFormatter fmtTime = DateTimeFormat.shortTime();
      DateTimeFormatter localFmtTime = fmtTime.withLocale(new ResourceLoader().getLocale());

      // If the client browser is in a different timezone than server, need to modify date
      if (m_client_timezone !=null && m_server_timezone!=null && !m_client_timezone.hasSameRules(m_server_timezone)) {
        DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(m_client_timezone);
        localFmt = localFmt.withZone(dateTimeZone);
        localFmtTime = localFmtTime.withZone(dateTimeZone);
      }
      return dt.toString(localFmt) + " " + dt.toString(localFmtTime);
  }

  public String getDateTimeWithTimezoneConversion(Date dateToConvert) {
      if (dateToConvert == null) {
          return null;
      }
      DateTime dt = new DateTime(dateToConvert);
      DateTimeFormatter fmt = ISODateTimeFormat.yearMonthDay();
      DateTimeFormatter fmtTime = ISODateTimeFormat.hourMinuteSecond();

      // If the client browser is in a different timezone than server, need to modify date
      if (m_client_timezone !=null && m_server_timezone!=null && !m_client_timezone.hasSameRules(m_server_timezone)) {
        DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(m_client_timezone);
        fmt = fmt.withZone(dateTimeZone);
        fmtTime = fmtTime.withZone(dateTimeZone);
      }
      return dt.toString(fmt) + " " + dt.toString(fmtTime);
  }
  
  /*
   * SAM-2323: the jquery-ui datepicker provides a hidden field with ISO-8601 date/time
   * This method will convert that date string into a Java Date
   */
  public Date parseISO8601String(String dateString) {
    if (StringUtils.isBlank(dateString)) {
      return null;
    }

    try {
      // Hidden field from the datepicker will look like: 2015-02-19T02:25:00-06:00
      DateTime dt = dtf.parseDateTime(dateString);
      return dt.toDate();
    } catch (Exception e) {
      log.error("parseISO8601String could not parse: " + dateString);
    }

    return null;
  }


}
