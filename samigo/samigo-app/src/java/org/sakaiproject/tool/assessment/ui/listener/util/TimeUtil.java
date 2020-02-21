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

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

  private TimeZone m_client_timezone= null;
  private TimeZone m_server_timezone= null;

  public TimeUtil() {
    m_client_timezone= TimeService.getLocalTimeZone();
    m_server_timezone= TimeZone.getDefault();
  }

  /*
   * @deprecated use UserTimeService instead
   * This will return a formatted date/time without adjustment for client time zone.
   * If instructor is located in Michigan and teaches on Sakai based in Chicago, 
   */
  public String getDisplayDateTime(SimpleDateFormat ndf, Date serverDate) {
     //we can't format a null date
    if (serverDate == null) {
      return "";
    }
    
    try {
      return ndf.format(serverDate);
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
   * User could be in a different timezone and modifying dates in the date picker.
   * We need to take the user date and convert back to server time zone for storage in database.
   */
  public Date parseISO8601String(final String dateString) {
	  if (StringUtils.isBlank(dateString)) {
		  return null;
	  }

	  try {
		  // Hidden field from the datepicker will look like: 2015-02-19T02:25:00-06:00
		  // But that timezone offset is the client browser time zone offset (not necessarily their preferred time zone).
		  // So bring in the time as LocalDateTime and then do the zone manipulation later.
		  // 2015-02-19T02:25:00 = 19 characters
		  final String localDateString = StringUtils.left(dateString, 19);
		  LocalDateTime ldt = LocalDateTime.parse(localDateString);
		  log.debug("parseISO8601String: string={}, localdate={}", dateString, ldt.toString());

		  if (ldt != null && m_client_timezone != null && m_server_timezone != null && !m_client_timezone.hasSameRules(m_server_timezone)) {
			  ZonedDateTime zdt = ldt.atZone(m_client_timezone.toZoneId());
			  log.debug("parseISO8601String: original={}, zoned={}", dateString, zdt.toString());
			  return Date.from(zdt.toInstant());
		  }
		  else if (ldt != null && m_server_timezone != null) {
			  ZonedDateTime zdt = ldt.atZone(m_server_timezone.toZoneId());
			  return Date.from(zdt.toInstant());
		  }
		  else if (ldt != null) {
			  return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		  }
	  } catch (Exception e) {
		  log.error("parseISO8601String could not parse: {}", dateString);
	  }

	  return null;
  }
 
}
